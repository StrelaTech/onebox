package xray

import (
	"testing"
)

// Проверяет, что поставленные в APK geoip.dat/geosite.dat реально
// загружаются ядром по тем же путям, что и в приложении
// (SetAssetLocation → env xray.location.asset).
func TestShippedGeoAssetsLoad(t *testing.T) {
	SetAssetLocation("../app/src/main/assets/xray")
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
