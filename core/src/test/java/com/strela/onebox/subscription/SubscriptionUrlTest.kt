package com.strela.onebox.subscription

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Валидация ссылки подписки — ворота безопасности: http:// не должен пройти ни при
 * каких условиях, битый URL — тоже. Ошибка здесь: подписка уезжает по http и
 * учётные данные серверов едут открытым текстом.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class SubscriptionUrlTest {

    @Test
    fun `корректная https ссылка проходит валидацию`() {
        val url = "https://sub.example.com/path?token=abc"
        assertEquals(url, SubscriptionUrl.validate(url))
    }

    @Test
    fun `пробелы по краям обрезаются`() {
        assertEquals("https://sub.example.com", SubscriptionUrl.validate("  https://sub.example.com \n"))
    }

    @Test
    fun `http отклоняется как небезопасное соединение`() {
        val e = assertThrows(SubscriptionUrl.InsecureUrlException::class.java) {
            SubscriptionUrl.validate("http://sub.example.com")
        }
        assertNull(SubscriptionUrl.validateOrNull("http://sub.example.com"))
        assert(e.message!!.contains("https"))
    }

    @Test
    fun `ссылка без схемы отклоняется`() {
        assertThrows(SubscriptionUrl.InvalidUrlException::class.java) {
            SubscriptionUrl.validate("sub.example.com")
        }
    }

    @Test
    fun `мусор вместо ссылки отклоняется`() {
        assertThrows(SubscriptionUrl.InvalidUrlException::class.java) {
            SubscriptionUrl.validate("не ссылка вообще")
        }
        assertNull(SubscriptionUrl.validateOrNull("ftp://example.com"))
    }

    @Test
    fun `пустая ссылка отклоняется`() {
        assertThrows(SubscriptionUrl.InvalidUrlException::class.java) {
            SubscriptionUrl.validate("   ")
        }
    }

    @Test
    fun `домен извлекается для имени группы`() {
        assertEquals("sub.example.com", SubscriptionUrl.hostOf("https://sub.example.com/path?x=1"))
        assertEquals("example.com", SubscriptionUrl.hostOf("https://example.com"))
        assertNull(SubscriptionUrl.hostOf("не url"))
    }
}
