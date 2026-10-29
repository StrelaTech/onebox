// Package xray — обёртка над xray-core для встраивания в приложение (gomobile bind).
package xray

import (
	"errors"
	"io"
	stdlog "log"
	"os"
	"strconv"
	"sync"

	applog "github.com/xtls/xray-core/app/log"
	xlog "github.com/xtls/xray-core/common/log"
	"github.com/xtls/xray-core/core"
	_ "github.com/xtls/xray-core/main/distro/all" // все протоколы/транспорты + JSON-лоадер
	_ "github.com/xtls/xray-core/proxy/tun"       // tun-инбаунд для VpnService (не входит в distro/all)
)

var (
	mu       sync.Mutex
	instance *core.Instance
)

// quietHandler — заглушка, поглощает все сообщения ядра.
type quietHandler struct{}

func (quietHandler) Handle(xlog.Message) {}

// SetQuiet отключает вывод ядра.
//
// Одного RegisterHandler мало: при старте ядро пересоздаёт handler'ы из конфига
// (app/log.New → createHandler), а для console-вывода по умолчанию пишет в stdout.
// gomobile подменяет реальные fd 1/2 на пайп в logcat (syscall.Dup3), поэтому
// любые записи в os.Stdout всё равно попадают в logcat. Решение — переопределить
// создатель console-хендлера на заглушку ДО старта ядра.
func SetQuiet() {
	applog.RegisterHandlerCreator(applog.LogType_Console, func(
		applog.LogType, applog.HandlerCreatorOptions,
	) (xlog.Handler, error) {
		return quietHandler{}, nil
	})

	xlog.RegisterHandler(quietHandler{})
	stdlog.SetOutput(io.Discard)
}

// Version возвращает версию встроенного xray-core.
func Version() string {
	return core.Version()
}

// SetTunFd передаёт fd tun-интерфейса из Android VpnService ядру.
// tun-инбаунд читает его из переменной окружения "xray.tun.fd" при старте.
// Вызывать ДО StartInstance.
func SetTunFd(fd int) {
	os.Setenv("xray.tun.fd", strconv.Itoa(fd))
}

// SetAssetLocation задаёт каталог с geoip.dat/geosite.dat.
//
// Без этого ядро ищет их в каталоге исполняемого файла, которым на Android
// оказывается /system/bin — отсюда ошибка «open /system/bin/geoip.dat:
// no such file or directory» при конфигах с geoip:/geosite:-правилами.
// Вызывать ДО StartInstance; dir — каталог, куда приложение распаковало
// файлы из APK.
func SetAssetLocation(dir string) {
	os.Setenv("xray.location.asset", dir)
}

// StartInstance запускает ядро xray с переданным JSON-конфигом.
// Возвращает ошибку, если ядро уже запущено или конфиг некорректен.
func StartInstance(config string) error {
	mu.Lock()
	defer mu.Unlock()
	if instance != nil {
		return errors.New("xray: already running")
	}
	inst, err := core.StartInstance("json", []byte(config))
	if err != nil {
		return err
	}
	instance = inst
	return nil
}

// StopInstance останавливает запущенное ядро. Если ядро не запущено — no-op.
func StopInstance() error {
	mu.Lock()
	defer mu.Unlock()
	if instance == nil {
		return nil
	}
	err := instance.Close()
	instance = nil
	return err
}

// IsRunning сообщает, запущено ли ядро.
func IsRunning() bool {
	mu.Lock()
	defer mu.Unlock()
	return instance != nil
}
