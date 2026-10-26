package com.strela.onebox.xray

import com.strela.onebox.subscription.XrayConfigParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Контракт между парсером конфигов (:core-parsing) и сборщиком конфига из
 * этого модуля: сервер, разобранный из готового JSON, обязан воспроизводить
 * исходный конфиг как есть — DNS, routing и всё остальное от провайдера.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class XrayConfigSnapshotRoundTripTest {

    @Test
    fun `json-снапшот используется сборщиком конфига как есть`() {
        val server = XrayConfigParser.parse(VLESS_REALITY, "Главный сервер")!!
        assertNotNull(server)

        val built = JSONObject(XrayConfigBuilder.build(server))
        val original = JSONObject(VLESS_REALITY)
        assertEquals(
            original.getJSONArray("outbounds").toString(),
            built.getJSONArray("outbounds").toString(),
        )
        assertEquals(
            original.getJSONObject("routing").toString(),
            built.getJSONObject("routing").toString(),
        )
    }

    private companion object {
        /** Полный провайдерский конфиг — snapshot должен дойти до ядра целиком. */
        val VLESS_REALITY = """
        {
          "log": { "loglevel": "warning" },
          "dns": { "servers": [ "9.9.9.9" ] },
          "inbounds": [ { "listen": "127.0.0.1", "port": 10808, "protocol": "socks" } ],
          "outbounds": [
            {
              "protocol": "vless",
              "settings": {
                "vnext": [ {
                  "address": "main.example.com",
                  "port": 443,
                  "users": [ { "id": "uuid-1", "encryption": "none", "flow": "xtls-rprx-vision" } ]
                } ]
              },
              "streamSettings": {
                "network": "tcp",
                "security": "reality",
                "realitySettings": {
                  "serverName": "www.cloudflare.com",
                  "publicKey": "pbk",
                  "shortId": "abcd1234",
                  "fingerprint": "chrome"
                }
              }
            }
          ],
          "routing": {
            "domainStrategy": "IPIfNonMatch",
            "rules": [ { "type": "field", "domain": ["domain:ads.example.com"], "outboundTag": "block" } ]
          }
        }
        """.trimIndent()
    }
}
