package xray

import (
	"os"
	"testing"
)

// Проверяет, что скачанные в кэш приложения geoip.dat/geosite.dat реально
// загружаются ядром по тем же путям, что и в приложении
// (SetAssetLocation → env xray.location.asset).
//
// Начиная с ленивого скачивания ассеты в репозитории не живут: тест бежит
// только когда датасеты уже качались (аргумент теста — путь кэша или
// любой каталог с geoip.dat/geosite.dat), иначе пропускается.
func TestShippedGeoAssetsLoad(t *testing.T) {
	dir := os.Getenv("ONEBOX_GEO_DIR")
	if dir == "" {
		t.Skip("geoip.dat/geosite.dat качаются при первом подключении; ONEBOX_GEO_DIR не задан")
	}
	SetAssetLocation(dir)
	cfg := `{
	  "log": {"loglevel": "warning"},
	  "inbounds": [{"listen": "127.0.0.1", "port": 0, "protocol": "socks"}],
	  "outbounds": [{"protocol": "freedom"}, {"protocol": "blackhole"}],
	  "routing": {
	    "domainStrategy": "IPIfNonMatch",
	    "rules": [
	      {"type": "field", "ip": ["geoip:ru", "geoip:private"], "outboundTag": "direct"},
	      {"type": "field", "domain": ["geosite:google", "geosite:category-ads-all"], "outboundTag": "block"}
	    ]
	  }
	}`
	if err := StartInstance(cfg); err != nil {
		t.Fatalf("StartInstance с geoip/geosite-правилами: %v", err)
	}
	t.Log("ядро приняло конфиг с geoip:ru/geoip:private/geosite:google/geosite:category-ads-all")
	StopInstance()
}
