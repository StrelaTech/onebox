package com.strela.onebox.subscription

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Единый декодер названий (подписки, серверов, remarks): base64 с префиксом
 * `base64:`, голый base64, URL-кодирование (иногда двойное). Консервативен:
 * всё нерасшифровываемое должно возвращаться как есть.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [37])
class NameDecoderTest {

    @Test
    fun `префикс base64 декодируется`() {
        // base64("Hello")
        assertEquals("Hello", NameDecoder.decode("base64:SGVsbG8="))
        assertEquals("Hello", NameDecoder.decode("Base64:SGVsbG8=")) // регистр не важен
    }

    @Test
    fun `голый base64 декодируется`() {
        val encoded = Base64.encodeToString("Моя подписка".toByteArray(), Base64.NO_WRAP)
        val decoded = NameDecoder.decode(encoded)
        assertEquals("Моя подписка", decoded)
    }

    @Test
    fun `url-safe base64 декодируется`() {
        // base64url("Б> Узел"): '-' вместо '+'
        assertEquals("Б> Узел", NameDecoder.decode("0JE-INCj0LfQtdC7"))
    }

    @Test
    fun `url-кодирование снимается`() {
        // «Привет» в percent-encoding
        assertEquals(
            "Привет",
            NameDecoder.decode("%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82"),
        )
    }

    @Test
    fun `двойное url-кодирование снимается`() {
        assertEquals(
            "Привет",
            NameDecoder.decode("%25D0%259F%25D1%2580%25D0%25B8%25D0%25B2%25D0%25B5%25D1%2582"),
        )
    }

    @Test
    fun `url-кодирование после префикса тоже снимается`() {
        // base64("Hello") с percent-кодированным '='
        assertEquals("Hello", NameDecoder.decode("base64:SGVsbG8%3D"))
    }

    @Test
    fun `обычное имя остаётся как есть`() {
        assertEquals("Simple Name", NameDecoder.decode("Simple Name"))
        assertEquals("Мой сервер", NameDecoder.decode("Мой сервер"))
    }

    @Test
    fun `без падения на случайных процентах`() {
        assertEquals("Скидка 50%!", NameDecoder.decode("Скидка 50%!"))
    }

    @Test
    fun `битый префикс убирается хотя бы`() {
        assertEquals("@@@", NameDecoder.decode("base64:@@@"))
    }

    @Test
    fun `голый префикс — заглушка`() {
        assertNull(NameDecoder.decode("base64:"))
        assertNull(NameDecoder.decode("BASE64: "))
    }

    @Test
    fun `пустой ввод - null`() {
        assertNull(NameDecoder.decode(""))
        assertNull(NameDecoder.decode("   "))
    }
}
