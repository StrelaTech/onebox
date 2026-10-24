package com.strela.onebox.subscription

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Определение формата вставленного текста — второй вход в приложение.
 * Ошибка здесь: пользователь импортирует подписку, а получает мусор.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class ImportDetectorTest {

    @Test
    fun `json-конфиг распознаётся как конфиг`() {
        val content = ImportDetector.detect("""{"outbounds":[],"inbounds":[],"log":{"loglevel":"warning"}}""")

        val configs = (content as ImportContent.Config).configs
        assertEquals(1, configs.size)
        assertEquals("""{"outbounds":[],"inbounds":[],"log":{"loglevel":"warning"}}""", configs.single().json)
    }

    @Test
    fun `название конфига берётся из remarks`() {
        val content = ImportDetector.detect("""{"remarks":"Мой сервер","outbounds":[]}""")

        assertEquals("Мой сервер", (content as ImportContent.Config).configs.single().name)
    }

    @Test
    fun `массив конфигов разбивается на отдельные`() {
        val input = """
            [
              {"remarks":"US-1","outbounds":[]},
              {"remarks":"DE-2","outbounds":[]},
              {"outbounds":[]}
            ]
        """.trimIndent()

        val configs = (ImportDetector.detect(input) as ImportContent.Config).configs

        assertEquals(3, configs.size)
        assertEquals("US-1", configs[0].name)
        assertEquals("DE-2", configs[1].name)
        assertNull(configs[2].name)
    }

    @Test
    fun `строка массива с json внутри разбирается как конфиг`() {
        val inner = """{"remarks":"S1","outbounds":[]}"""
        val input = org.json.JSONArray().put(inner).toString()

        val configs = (ImportDetector.detect(input) as ImportContent.Config).configs

        assertEquals(1, configs.size)
        assertEquals("S1", configs.single().name)
    }

    @Test
    fun `массив без конфигов не распознаётся`() {
        assertNull(ImportDetector.detect("""[1,2,3]"""))
        assertNull(ImportDetector.detect("""{"name":"просто объект"}"""))
    }

    @Test
    fun `один объект сервера распознаётся как сервер`() {
        val input = """
            {"id":"s1","name":"Мой","protocol":"VLESS","address":"1.2.3.4","port":443,"credential":"uuid-1"}
        """.trimIndent()

        val servers = (ImportDetector.detect(input) as ImportContent.Servers).servers

        assertEquals(1, servers.size)
        assertEquals("Мой", servers.single().name)
        assertEquals("1.2.3.4", servers.single().address)
        assertEquals(443, servers.single().port)
        assertEquals(Server.Protocol.VLESS, servers.single().protocol)
        assertEquals("uuid-1", servers.single().credential)
    }

    @Test
    fun `массив серверов делится на отдельные серверы`() {
        val input = """
            [
              {"name":"US-1","protocol":"VLESS","address":"1.1.1.1","port":443,"credential":"u1"},
              {"name":"DE-2","protocol":"TROJAN","address":"2.2.2.2","port":8443,"credential":"p2"},
              {"name":"без порта","protocol":"VLESS","address":"3.3.3.3"}
            ]
        """.trimIndent()

        val servers = (ImportDetector.detect(input) as ImportContent.Servers).servers

        assertEquals(2, servers.size)
        assertEquals("US-1", servers[0].name)
        assertEquals(Server.Protocol.VLESS, servers[0].protocol)
        assertEquals("DE-2", servers[1].name)
        assertEquals(Server.Protocol.TROJAN, servers[1].protocol)
    }

    @Test
    fun `массив с конфигами и серверами отдаёт конфиги`() {
        val input = """[{"outbounds":[]},{"name":"S","address":"1.1.1.1","port":443}]"""

        val content = ImportDetector.detect(input)

        assertEquals(1, (content as ImportContent.Config).configs.size)
    }

    @Test
    fun `ссылки распознаются как серверы`() {
        val content = ImportDetector.detect("vless://uuid@example.com:443#a")

        val servers = (content as ImportContent.Servers).servers
        assertEquals(1, servers.size)
        assertEquals("example.com", servers.first().address)
    }

    @Test
    fun `base64-подписка с ссылками разворачивается`() {
        val body = "vless://uuid@example.com:443#a\ntrojan://pass@example.org:443#b"
        val encoded = java.util.Base64.getEncoder().encodeToString(body.toByteArray())

        val content = ImportDetector.detect(encoded)
        val servers = (content as ImportContent.Servers).servers
        assertEquals(2, servers.size)
    }

    @Test
    fun `markdown-обёртка вокруг json убирается`() {
        val fenced = "```json\n{\"outbounds\":[]}\n```"

        val content = ImportDetector.detect(fenced)
        assertEquals("""{"outbounds":[]}""", (content as ImportContent.Config).configs.single().json)
    }

    @Test
    fun `случайный текст не определяется`() {
        assertNull(ImportDetector.detect("привет, как дела?"))
        assertNull(ImportDetector.detect(""))
    }
}
