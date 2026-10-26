package com.strela.onebox.xray

import com.strela.onebox.subscription.Server
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Сборка xray-конфига — если она сломается, ядро не примет конфиг и не поднимется.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class XrayConfigBuilderTest {

    private val vlessWsTls = Server(
        id = "1",
        name = "test",
        protocol = Server.Protocol.VLESS,
        address = "example.com",
        port = 443,
        credential = "uuid-1",
        network = "ws",
        security = "tls",
        sni = "foo.com",
        path = "/ws",
        host = "foo.com",
    )

    @Test
    fun `vless ws tls попадает в outbound со streamSettings`() {
        val config = JSONObject(XrayConfigBuilder.build(vlessWsTls))

        val outbounds = config.getJSONArray("outbounds")
        assertEquals(3, outbounds.length())
        assertEquals("proxy", outbounds.getJSONObject(0).getString("tag"))

        val proxy = outbounds.getJSONObject(0)
        assertEquals("vless", proxy.getString("protocol"))
        assertEquals("uuid-1", proxy.getJSONObject("settings").getJSONArray("vnext")
            .getJSONObject(0).getJSONArray("users").getJSONObject(0).getString("id"))

        val stream = proxy.getJSONObject("streamSettings")
        assertEquals("ws", stream.getString("network"))
        assertEquals("tls", stream.getString("security"))
        assertEquals("foo.com", stream.getJSONObject("tlsSettings").getString("serverName"))
        assertEquals("/ws", stream.getJSONObject("wsSettings").getString("path"))
        assertEquals("foo.com", stream.getJSONObject("wsSettings").getString("host"))
    }

    @Test
    fun `приватные сети всегда маршрутизируются напрямую`() {
        val config = JSONObject(XrayConfigBuilder.build(vlessWsTls))

        val rules = config.getJSONObject("routing").getJSONArray("rules")
        val direct = (0 until rules.length())
            .map { rules.getJSONObject(it) }
            .first { it.getString("outboundTag") == "direct" }
        // экранируем не через toString(): в org.json слэши могут быть как "\/"
        val ips = direct.getJSONArray("ip")
        val cidrs = (0 until ips.length()).map { ips.getString(it) }
        assertTrue(cidrs.contains("192.168.0.0/16"))
    }

    @Test
    fun `blockAds=false убирает blackhole-правило`() {
        val withAds = JSONObject(XrayConfigBuilder.build(vlessWsTls, blockAds = true))
        val withoutAds = JSONObject(XrayConfigBuilder.build(vlessWsTls, blockAds = false))

        assertTrue(rules(withAds).any { it.getString("outboundTag") == "block" })
        assertFalse(rules(withoutAds).any { it.getString("outboundTag") == "block" })
    }

    @Test
    fun `dns содержит резервные серверы`() {
        val config = JSONObject(XrayConfigBuilder.build(vlessWsTls))

        val servers = config.getJSONObject("dns").getJSONArray("servers")
        assertEquals("1.1.1.1", servers.getString(0))
        assertEquals("8.8.8.8", servers.getString(1))
    }

    private fun rules(config: JSONObject) =
        (0 until config.getJSONObject("routing").getJSONArray("rules").length())
            .map { config.getJSONObject("routing").getJSONArray("rules").getJSONObject(it) }
}
