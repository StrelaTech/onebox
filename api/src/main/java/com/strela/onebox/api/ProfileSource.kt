package com.strela.onebox.api

import com.strela.onebox.subscription.Server

/**
 * Выбранный профиль подключения: ядро получает его через [ProfileSource]
 * и собирает из него свой собственный конфиг.
 */
data class ProfileSelection(
    /**
     * Готовый JSON-конфиг подписки (JSON-режим). Приоритетнее [server]:
     * конфиг провайдера авторитетнее пересборки.
     */
    val configJson: String? = null,
    /** Выбранный сервер — из него ядро собирает конфиг своим конфигбилдером. */
    val server: Server? = null,
)

/**
 * Источник активного профиля для ядра: подписки и настройки приложения.
 * Реализация живёт в :data ([StoreProfileSource]) — ядра не зависят от
 * хранилищ и читают профиль только через этот интерфейс.
 */
interface ProfileSource {

    /** Активный профиль или null, если не выбрана ни подписка, ни сервер. */
    fun activeProfile(): ProfileSelection?

    /** Блокировка рекламы (настройка) — учитывается при сборке конфига ядра. */
    fun blockAds(): Boolean

    /** Настройки пинга серверов (метод, тайм-аут, URL-тест). */
    fun pingOptions(): PingOptions

    /** Пользовательский «сырой» конфиг (JSON-режим); null — не сохранён. */
    fun userConfig(): String?

    /** Сохраняет пользовательский «сырой» конфиг (JSON-режим). */
    fun saveUserConfig(json: String)

    /** Удаляет пользовательский «сырой» конфиг (сброс настроек). */
    fun clearUserConfig()
}
