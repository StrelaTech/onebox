package com.strela.onebox.subscription

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Самая критичная логика приложения — разбор ссылок серверов.
 * Ошибка здесь ломает импорт подписок для всех протоколов.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class LinkParserTest {

    @Test
    fun `vless со всеми параметрами разбирается`() {
        val server = LinkParser.parse(
            "vless://b831381d-6324-4d53-ad4f-8cda48b30811@example.com:443" +
                "?type=ws&security=tls&sni=foo.com&path=%2Fws&host=foo.com#Мой%20сервер"
        )

        assertEquals(Server.Protocol.VLESS, server.protocol)
        assertEquals("example.com", server.address)
        assertEquals(443, server.port)
        assertEquals("b831381d-6324-4d53-ad4f-8cda48b30811", server.credential)
        assertEquals("ws", server.network)
        assertEquals("tls", server.security)
        assertEquals("foo.com", server.sni)
        assertEquals("/ws", server.path)
        assertEquals("Мой сервер", server.name)
    }

    @Test
    fun `vmess base64-json разбирается`() {
        val json = """
            {"add":"vm.example.com","port":"8443","id":"a3482e88-6865-4d3b-a5f3-35e3b0e8c3d4",
             "ps":"VMTest","net":"ws","tls":"tls","host":"cdn.example.com","path":"/ray"}
        """.trimIndent().replace("\n", "")
        val server = LinkParser.parse("vmess://" + java.util.Base64.getEncoder().encodeToString(json.toByteArray()))

        assertEquals(Server.Protocol.VMESS, server.protocol)
        assertEquals("vm.example.com", server.address)
        assertEquals(8443, server.port)
        assertEquals("a3482e88-6865-4d3b-a5f3-35e3b0e8c3d4", server.credential)
        assertEquals("VMTest", server.name)
        assertEquals("ws", server.network)
        assertEquals("tls", server.security)
        assertEquals("/ray", server.path)
    }

    @Test
    fun `shadowsocks SIP002 разбирается`() {
        // base64("aes-256-gcm:secret")
        val server = LinkParser.parse("ss://YWVzLTI1Ni1nY206c2VjcmV0@ss.example.com:8388#SS%20Node")

        assertEquals(Server.Protocol.SHADOWSOCKS, server.protocol)
        assertEquals("ss.example.com", server.address)
        assertEquals(8388, server.port)
        assertEquals("aes-256-gcm", server.extra)
        assertEquals("secret", server.credential)
        assertEquals("SS Node", server.name)
    }

    @Test
    fun `socks с авторизацией разбирается`() {
        val server = LinkParser.parse("socks://user:pass@127.0.0.1:1080#Local")

        assertEquals(Server.Protocol.SOCKS, server.protocol)
        assertEquals("127.0.0.1", server.address)
        assertEquals(1080, server.port)
        assertEquals("user", server.credential)
        assertEquals("pass", server.extra)
        assertEquals("Local", server.name)
    }

    @Test
    fun `имя сервера в base64 из фрагмента декодируется`() {
        // base64("Лондон")
        val server = LinkParser.parse(
            "vless://b831381d-6324-4d53-ad4f-8cda48b30811@lon.example.com:443" +
                "?type=ws&security=tls#0JvQvtC90LTQvtC9"
        )
        assertEquals("Лондон", server.name)
    }

    @Test
    fun `имя vmess в url-кодировке декодируется`() {
        val json = """
            {"add":"de.example.com","port":"443","id":"a3482e88-6865-4d3b-a5f3-35e3b0e8c3d4",
             "ps":"%D0%91%D0%B5%D1%80%D0%BB%D0%B8%D0%BD","net":"tcp"}
        """.trimIndent().replace("\n", "")
        val server = LinkParser.parse("vmess://" + java.util.Base64.getEncoder().encodeToString(json.toByteArray()))
        assertEquals("Берлин", server.name)
    }

    @Test
    fun `vless без порта отклоняется`() {
        assertTrue(runCatching { LinkParser.parse("vless://uuid@example.com") }.isFailure)
    }

    @Test
    fun `неизвестная схема отклоняется`() {
        val thrown = runCatching { LinkParser.parse("unknown://example.com:443") }
        assertTrue(thrown.isFailure)
    }

    @Test
    fun `parseMany отбрасывает пустые и битые строки`() {
        val servers = LinkParser.parseMany(
            """
            vless://uuid@example.com:443#ok
            мусор без схемы
            (пустая строка ниже)
            trojan:broken
            """,
        )
        assertEquals(1, servers.size)
        assertEquals(Server.Protocol.VLESS, servers.first().protocol)
    }
}
