package com.strela.onebox.api

/**
 * Результат пинга сервера — нейтральный тип прослойки.
 * Ключ поиска — id профиля ([com.strela.onebox.subscription.Server.id]).
 */
sealed interface PingState {
    data object InProgress : PingState
    data class Ok(val millis: Long) : PingState
    data class Failed(val reason: String) : PingState
}
