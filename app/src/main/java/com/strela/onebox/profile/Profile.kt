package com.strela.onebox.profile

/**
 * Профиль — группа серверов (например, один импорт подписки).
 * Хранится сериализованно в SharedPreferences (см. ProfileStore).
 *
 * У профиля есть активный сервер: именно он попадает в конфиг ядра.
 */
data class Profile(
    val id: String,
    val name: String,
    val servers: List<Server> = emptyList(),
    val selectedServerId: String? = null
) {
    /** Активный сервер; если id не задан/устарел — первый в списке. */
    val selectedServer: Server?
        get() = servers.firstOrNull { it.id == selectedServerId } ?: servers.firstOrNull()

    val endpoint: String
        get() = selectedServer?.endpoint.orEmpty()
}
