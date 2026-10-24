package com.strela.onebox.subscription

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Разбор JSON-конфигов xray в серверы: из этих полей собирается конфиг ядра,
 * поэтому адрес/порт/учётные данные/TLS-параметры обязаны совпадать с оригиналом.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class XrayConfigParserTest {

    private val vlessReality = """
        {
          "remarks": "Главный сервер",
          "log": { "loglevel": "warning" },
          "outbounds": [
            { "protocol": "freedom", "tag": "direct" },
            { "protocol": "blackhole", "tag": "block" },
            {
              "protocol": "vless",
              "settings": {
                "vnext": [ {
                  "address": "example.com",
                  "port": 443,
                  "users": [ {
                    "id": "uuid-1",
                    "encryption": "none",
                    "flow": "xtls-rprx-vision"
                  } ]
                } ]
              },
              "streamSettings": {
                "network": "tcp",
                "security": "reality",
                "realitySettings": {
                  "serverName": "www.example.com",
                  "publicKey": "pbk",
                  "shortId": "01ab",
                  "spiderX": "/",
                  "fingerprint": "chrome"
                }
              }
            }
          ],
          "routing": { "rules": [] }
        }
    """.trimIndent()

    @Test
    fun `vless reality config разбирается в сервер`() {
        val server = XrayConfigParser.parse(vlessReality, "Главный сервер")!!

        assertNotNull(server)
        assertEquals("Главный сервер", server.name)
        assertEquals(Server.Protocol.VLESS, server.protocol)
        assertEquals("example.com", server.address)
        assertEquals(443, server.port)
        assertEquals("uuid-1", server.credential)
        assertEquals("xtls-rprx-vision", server.extra)
        assertEquals("tcp", server.network)
        assertEquals("reality", server.security)
        assertEquals("www.example.com", server.sni)
        assertEquals("pbk", server.publicKey)
        assertEquals("01ab", server.shortId)
        assertEquals("/", server.spiderX)
        assertEquals("chrome", server.fingerprint)
        // исходный конфиг сохраняется целиком — без него нельзя обновить подписку
        assertEquals(vlessReality, server.json)
    }

    @Test
    fun `vmess ws tls config разбирается в сервер`() {
        val json = """
            {
              "outbounds": [ {
                "protocol": "vmess",
                "settings": {
                  "vnext": [ {
                    "address": "vm.example.net",
                    "port": 8443,
                    "users": [ { "id": "uid-2", "alterId": 32, "security": "auto" } ]
                  } ]
                },
                "streamSettings": {
                  "network": "ws",
                  "security": "tls",
                  "wsSettings": { "path": "/ws-path", "host": "cdn.example.net" },
                  "tlsSettings": { "serverName": "sni.example.net", "alpn": [ "h2", "http/1.1" ] }
                }
              } ]
            }
        """.trimIndent()

        val server = XrayConfigParser.parse(json)!!

        assertEquals(Server.Protocol.VMESS, server.protocol)
        assertEquals("vm.example.net", server.address)
        assertEquals(8443, server.port)
        assertEquals("uid-2", server.credential)
        assertEquals("32", server.extra)
        assertEquals("ws", server.network)
        assertEquals("tls", server.security)
        assertEquals("sni.example.net", server.sni)
        assertEquals("h2, http/1.1", server.alpn)
        assertEquals("/ws-path", server.path)
        assertEquals("cdn.example.net", server.host)
        // имя — address:port, если remarks нет
        assertEquals("vm.example.net:8443", server.name)
    }

    @Test
    fun `trojan grpc config разбирается в сервер`() {
        val json = """
            {
              "outbounds": [ {
                "protocol": "trojan",
                "settings": {
                  "servers": [ { "address": "t.example.org", "port": 443, "password": "pass-3" } ]
                },
                "streamSettings": {
                  "network": "grpc",
                  "security": "tls",
                  "grpcSettings": { "serviceName": "grpc-svc" }
                }
              } ]
            }
        """.trimIndent()

        val server = XrayConfigParser.parse(json)!!

        assertEquals(Server.Protocol.TROJAN, server.protocol)
        assertEquals("t.example.org", server.address)
        assertEquals(443, server.port)
        assertEquals("pass-3", server.credential)
        assertEquals("grpc", server.network)
        assertEquals("tls", server.security)
        assertEquals("grpc-svc", server.serviceName)
    }

    @Test
    fun `shadowsocks config разбирается в сервер`() {
        val json = """
            {
              "outbounds": [ {
                "protocol": "shadowsocks",
                "settings": {
                  "servers": [ {
                    "address": "ss.example.io",
                    "port": 51820,
                    "method": "chacha20-ietf-poly1305",
                    "password": "ss-pass"
                  } ]
                }
              } ]
            }
        """.trimIndent()

        val server = XrayConfigParser.parse(json)!!

        assertEquals(Server.Protocol.SHADOWSOCKS, server.protocol)
        assertEquals("ss.example.io", server.address)
        assertEquals(51820, server.port)
        assertEquals("ss-pass", server.credential)
        assertEquals("chacha20-ietf-poly1305", server.extra)
        assertEquals("tcp", server.network)
        assertEquals("none", server.security)
    }

    @Test
    fun `socks config разбирается в сервер с логином и паролем`() {
        val json = """
            {
              "outbounds": [ {
                "protocol": "socks",
                "settings": {
                  "servers": [ {
                    "address": "127.0.0.1",
                    "port": 9000,
                    "users": [ { "user": "login", "pass": "secret" } ]
                  } ]
                }
              } ]
            }
        """.trimIndent()

        val server = XrayConfigParser.parse(json)!!

        assertEquals(Server.Protocol.SOCKS, server.protocol)
        assertEquals("login", server.credential)
        assertEquals("secret", server.extra)
    }

    @Test
    fun `мусор и конфиг без проксирующего outbound не разбираются`() {
        assertNull(XrayConfigParser.parse("не json"))
        assertNull(XrayConfigParser.parse("""{ "outbounds": [ { "protocol": "freedom" } ] }"""))
        assertNull(XrayConfigParser.parse("""{ "log": {} }"""))
        // нет адреса
        assertNull(XrayConfigParser.parse(
            """{ "outbounds": [ { "protocol": "vless", "settings": { "vnext": [ { "port": 443, "users": [ { "id": "u" } ] } ] } } ] }"""
        ))
        // порт вне диапазона
        assertNull(XrayConfigParser.parse(
            """{ "outbounds": [ { "protocol": "vless", "settings": { "vnext": [ { "address": "a.b", "port": 70000, "users": [ { "id": "u" } ] } ] } } ] }"""
        ))
    }

    @Test
    fun `json-снапшот используется сборщиком конфига как есть`() {
        val server = XrayConfigParser.parse(vlessReality, "Главный сервер")!!
        assertNotNull(server)

        val built = JSONObject(ConfigBuilder.build(server))
        val original = JSONObject(vlessReality)
        assertEquals(original.getJSONArray("outbounds").toString(), built.getJSONArray("outbounds").toString())
        assertEquals(original.getJSONObject("routing").toString(), built.getJSONObject("routing").toString())
    }
}
