package com.strela.onebox.subscription

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Разбор заголовков подписки: имя, трафик, срок действия, интервал обновления.
 * Ошибка здесь: у подписки пропадает имя (или показывается мусор вместо лимита).
 * Провайдеры шлют заголовки в разном регистре и часто в base64 — оба случая
 * должны работать.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class SubscriptionHeaderParserTest {

    @Test
    fun `заголовки в разном регистре распознаются`() {
        val meta = SubscriptionHeaderParser.parse(mapOf("PROFILE-TITLE" to "MySub"))
        assertEquals("MySub", meta.title)
    }

    @Test
    fun `заголовок имени в base64 декодируется`() {
        val encoded = Base64.encodeToString("Моя подписка".toByteArray(), Base64.NO_WRAP)
        val meta = SubscriptionHeaderParser.parse(mapOf("profile-title" to encoded))
        assertEquals("Моя подписка", meta.title)
    }

    @Test
    fun `заголовок не-base64 берётся как есть`() {
        val meta = SubscriptionHeaderParser.parse(mapOf("profile-title" to "Simple Name"))
        assertEquals("Simple Name", meta.title)
    }

    @Test
    fun `userinfo разбирается в трафик и срок`() {
        val meta = SubscriptionHeaderParser.parse(
            mapOf("subscription-userinfo" to "upload=100; download=200; total=1000; expire=1700000000"),
        )
        assertEquals(100L, meta.uploadBytes)
        assertEquals(200L, meta.downloadBytes)
        assertEquals(1000L, meta.totalBytes)
        assertEquals(1_700_000_000_000L, meta.expireAtMillis)
        assertEquals(300L, meta.usedBytes)
    }

    @Test
    fun `нулевой лимит трафика считается отсутствием лимита`() {
        val meta = SubscriptionHeaderParser.parse(
            mapOf("subscription-userinfo" to "upload=5; download=5; total=0"),
        )
        assertNull(meta.totalBytes)
    }

    @Test
    fun `интервал обновления принимается только в разумных пределах`() {
        val ok = SubscriptionHeaderParser.parse(mapOf("profile-update-interval" to "24"))
        assertEquals(24, ok.updateIntervalHours)

        val garbage = SubscriptionHeaderParser.parse(mapOf("profile-update-interval" to "9999"))
        assertNull(garbage.updateIntervalHours)

        val negative = SubscriptionHeaderParser.parse(mapOf("profile-update-interval" to "-5"))
        assertNull(negative.updateIntervalHours)
    }

    @Test
    fun `описание из announce в base64 декодируется`() {
        val encoded = Base64.encodeToString("Внимание: плановое обновление".toByteArray(), Base64.NO_WRAP)
        val meta = SubscriptionHeaderParser.parse(mapOf("announce" to encoded))
        assertEquals("Внимание: плановое обновление", meta.announce)
    }

    @Test
    fun `announce не-base64 берётся как есть`() {
        // заголовок в любом регистре, текст как есть
        val meta = SubscriptionHeaderParser.parse(mapOf("ANNOUNCE" to "Простое описание"))
        assertEquals("Простое описание", meta.announce)
    }

    @Test
    fun `отсутствие всех заголовков не ошибка`() {
        val meta = SubscriptionHeaderParser.parse(emptyMap())
        assertNull(meta.title)
        assertNull(meta.announce)
        assertNull(meta.uploadBytes)
        assertNull(meta.expireAtMillis)
        assertNull(meta.updateIntervalHours)
    }

    @Test
    fun `заглушки вместо имени игнорируются`() {
        assertNull(SubscriptionHeaderParser.decodeTitle(""))
        assertNull(SubscriptionHeaderParser.decodeTitle("  "))
    }
}