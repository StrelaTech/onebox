// Файл ping.go — пинг серверов всеми режимами: HTTP-проба через ядро
// (GET/HEAD), TCP-хендшейк и ICMP-эхо. Ключи режимов совпадают с
// com.strela.onebox.api.PingMethod в приложении.
package xray

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"time"

	xnet "github.com/xtls/xray-core/common/net"
	"github.com/xtls/xray-core/core"
	"golang.org/x/net/icmp"
	"golang.org/x/net/ipv4"
)

// Режимы пинга (строки передаются из Android через gomobile).
const (
	pingModeGet  = "get"
	pingModeHead = "head"
	pingModeTCP  = "tcp"
	pingModeICMP = "icmp"
)

// Ping измеряет задержку до сервера выбранным способом и возвращает её в мс.
//
// GET/HEAD — HTTP-проба через запущенное ядро (тот же путь, что и реальный
// трафик: маршрутизация → outbound прокси; ядро должно быть запущено
// StartInstance — так измеряется реальная задержка сервера, включая установку
// соединения и прокси-хендшейк, как это делает xray в observatory).
// TCP — чистый TCP-хендшейк до target:port, ядро не запускается.
// ICMP — эхо-запрос на адрес target: сначала непривилегированный ICMP-сокет
// (SOCK_DGRAM, как ping в Linux), при отказе прав — утилита /system/bin/ping.
//
// timeoutMs — лимит ожидания в мс (<= 0 → 5000). probeUrl — цель HTTP-проб
// (пусто → generate_204 на gstatic); если он не похож на URL, запрос идёт на
// https://target:port/. HTTP-проба засчитывает ЛЮБОЙ ответ (204/3xx — уже
// живое соединение).
func Ping(mode string, target string, port int, timeoutMs int, probeUrl string) (int64, error) {
	if timeoutMs <= 0 {
		timeoutMs = 5000
	}
	timeout := time.Duration(timeoutMs) * time.Millisecond

	switch mode {
	case pingModeTCP:
		return tcpPing(target, port, timeout)
	case pingModeICMP:
		return icmpPing(target, timeout)
	case pingModeGet, pingModeHead, "":
		return httpPingThroughCore(mode, target, port, timeoutMs, probeUrl)
	default:
		return 0, fmt.Errorf("xray: unknown ping mode %q", mode)
	}
}

// tcpPing меряет время установки TCP-соединения с target:port.
func tcpPing(target string, port int, timeout time.Duration) (int64, error) {
	if strings.TrimSpace(target) == "" {
		return 0, errors.New("xray: empty target")
	}
	if port <= 0 {
		port = 443
	}
	// DialTimeout возвращает уже подключённый коннект или ошибку —
	// то есть меряется именно хендшейк, байт не отправляется.
	start := time.Now()
	conn, err := net.DialTimeout("tcp", net.JoinHostPort(target, strconv.Itoa(port)), timeout)
	elapsed := time.Since(start).Milliseconds()
	if conn != nil {
		_ = conn.Close()
	}
	if err != nil {
		return 0, err
	}
	return elapsed, nil
}

// icmpPing меряет RTT эхо-запроса: сначала ICMP-datagram сокет (без прав root),
// если система его не даёт — системная утилита ping.
func icmpPing(target string, timeout time.Duration) (int64, error) {
	if strings.TrimSpace(target) == "" {
		return 0, errors.New("xray: empty target")
	}
	addr, err := net.ResolveIPAddr("ip4", target)
	if err != nil {
		return 0, err
	}
	ms, dgramErr := icmpDatagramPing(addr.IP, timeout)
	if dgramErr == nil {
		return ms, nil
	}
	ms, execErr := icmpExecPing(target, timeout)
	if execErr == nil {
		return ms, nil
	}
	return 0, fmt.Errorf("xray: icmp unavailable: %v / %v", dgramErr, execErr)
}

// icmpDatagramPing — непривилегированный ICMP через SOCK_DGRAM-сокет
// (ядро Linux само ставит echo-id и мультиплексирует ответы по сокету).
func icmpDatagramPing(ip net.IP, timeout time.Duration) (int64, error) {
	conn, err := icmp.ListenPacket("udp4", "0.0.0.0")
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	// protocol code 1 = ICMP (IANA); константы x/net/internal/iana наружу
	// не экспортируются.
	const protoICMP = 1
	msg := icmp.Message{
		Type: ipv4.ICMPTypeEcho,
		Code: 0,
		Body: &icmp.Echo{ID: 0, Seq: 1, Data: []byte("onebox-ping")},
	}
	request, err := msg.Marshal(nil)
	if err != nil {
		return 0, err
	}
	start := time.Now()
	if _, err := conn.WriteTo(request, &net.IPAddr{IP: ip}); err != nil {
		return 0, err
	}
	_ = conn.SetReadDeadline(start.Add(timeout))

	reply := make([]byte, 1500)
	n, _, err := conn.ReadFrom(reply)
	if err != nil {
		return 0, err // дидлайн → "i/o timeout"
	}
	response, err := icmp.ParseMessage(protoICMP, reply[:n])
	if err != nil {
		return 0, err
	}
	if response.Type != ipv4.ICMPTypeEchoReply {
		return 0, fmt.Errorf("xray: unexpected icmp type %v", response.Type)
	}
	return time.Since(start).Milliseconds(), nil
}

// icmpExecPing — запасной путь: системная утилита ping (есть на любом Android,
// запуск без root). RTT берём из вывода "time=12.3 ms" — точнее таймера.
func icmpExecPing(target string, timeout time.Duration) (int64, error) {
	bin := "/system/bin/ping"
	if _, err := os.Stat(bin); err != nil {
		found, lookErr := exec.LookPath("ping")
		if lookErr != nil {
			return 0, errors.New("xray: ping utility not found")
		}
		bin = found
	}

	// -W у ping принимает целые секунды; на сам процесс добавляем запас.
	seconds := int((timeout + time.Second - 1) / time.Second)
	if seconds < 1 {
		seconds = 1
	}
	ctx, cancel := context.WithTimeout(context.Background(), timeout+2*time.Second)
	defer cancel()

	start := time.Now()
	out, err := exec.CommandContext(ctx, bin, "-n", "-c", "1", "-W",
		strconv.Itoa(seconds), target).CombinedOutput()
	wall := time.Since(start).Milliseconds()
	if err != nil {
		return 0, fmt.Errorf("xray: ping exit: %s", trimOutput(out))
	}
	if ms, parseErr := parsePingOutput(out); parseErr == nil {
		return ms, nil
	}
	return wall, nil
}

// parsePingOutput извлекает из вывода ping первый RTT "time=...".
func parsePingOutput(out []byte) (int64, error) {
	for _, line := range strings.Split(string(out), "\n") {
		i := strings.Index(line, "time=")
		if i < 0 {
			continue
		}
		fields := strings.Fields(line[i+len("time="):])
		if len(fields) == 0 {
			continue
		}
		value, err := strconv.ParseFloat(fields[0], 64)
		if err != nil {
			continue
		}
		return int64(value + 0.5), nil
	}
	return 0, errors.New("xray: no time= in ping output")
}

// trimOutput укорачивает вывод утилиты до разумного размера для ошибки.
func trimOutput(out []byte) string {
	const limit = 200
	text := strings.TrimSpace(string(out))
	if len(text) > limit {
		text = text[:limit] + "…"
	}
	return text
}

// httpPingThroughCore — HTTP GET/HEAD через запущенное ядро.
func httpPingThroughCore(method string, target string, port int, timeoutMs int, probeUrl string) (int64, error) {
	mu.Lock()
	inst := instance
	mu.Unlock()

	if inst == nil {
		return 0, errors.New("xray: not running")
	}
	if probeUrl == "" {
		probeUrl = "https://www.gstatic.com/generate_204"
	}

	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeoutMs)*time.Millisecond)
	defer cancel()

	dial := func(ctx context.Context, network, addr string) (net.Conn, error) {
		dest, err := xnet.ParseDestination(network + ":" + addr)
		if err != nil {
			return nil, err
		}
		return core.Dial(ctx, inst, dest)
	}

	transport := &http.Transport{
		ForceAttemptHTTP2: true,
		DialContext:       dial,
		// не переиспользуем соединения — каждый пинг должен быть честным
		DisableKeepAlives: true,
	}
	client := &http.Client{
		Transport: transport,
		Timeout:   time.Duration(timeoutMs) * time.Millisecond,
		// не идём по редиректам — 204/3xx уже означают живое соединение
		CheckRedirect: func(*http.Request, []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}
	if method == "" {
		method = http.MethodGet
	}

	req, err := http.NewRequestWithContext(ctx, method, probeUrl, nil)
	if err != nil {
		// if probeUrl isn't a URL, treat as host:port
		probeUrl = "https://" + net.JoinHostPort(target, strconv.Itoa(port)) + "/"
		req, err = http.NewRequestWithContext(ctx, method, probeUrl, nil)
		if err != nil {
			return 0, err
		}
	}

	start := time.Now()
	resp, err := client.Do(req)
	elapsed := time.Since(start).Milliseconds()
	if err != nil {
		return 0, err
	}
	// Для GET/HEAD контракт один: факт ответа = живое соединение; тело
	// просто дочитывается (у HEAD его нет, io.Copy отработает в ноль).
	if resp.Body != nil {
		_, _ = io.Copy(io.Discard, resp.Body)
		resp.Body.Close()
	}
	return elapsed, nil
}
