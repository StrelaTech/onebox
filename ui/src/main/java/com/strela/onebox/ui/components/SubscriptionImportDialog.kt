package com.strela.onebox.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.subscription.ImportDetector
import com.strela.onebox.subscription.SubscriptionImportReason
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionUrl
import com.strela.onebox.ui.strings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.extra.SuperDialog

/**
 * Диалог импорта подписки: принимает ссылку https:// (список серверов или
 * JSON-конфиг(ы) ответа), вставленный JSON-конфиг или список ссылок.
 * Вся бизнес-логика — в коллбэке [onImport] (UseCase / SubscriptionStore),
 * диалог только управляет состоянием и сообщениями.
 */
@Composable
fun SubscriptionImportDialog(
    onDismiss: () -> Unit,
    onImport: suspend (text: String, title: String) -> SubscriptionImportResult,
) {
    val s = strings
    val colors = MiuixTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var url by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<StatusMessage?>(null) }

    val show = remember { mutableStateOf(true) }

    SuperDialog(
        show = show,
        title = s.subscriptionDialogTitle,
        onDismissRequest = { if (!loading) onDismiss() },
    ) {
        Column {
            // Название подписки: важно для JSON-конфигов и списков ссылок —
            // URL-подписка берёт заголовок из метаданных ответа
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = s.subscriptionTitleOptional,
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !loading,
                leadingIcon = {
                    Icon(
                        imageVector = com.strela.onebox.ui.design.icons.OneBoxIcons.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSecondaryContainer,
                    )
                },
            )

            Spacer(Modifier.height(14.dp))

            TextField(
                value = url,
                onValueChange = {
                    url = it
                    message = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = s.subscriptionUrlLabel,
                useLabelAsPlaceholder = true,
                maxLines = 6,
                enabled = !loading,
                leadingIcon = {
                    Icon(
                        imageVector = com.strela.onebox.ui.design.icons.OneBoxIcons.Link,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSecondaryContainer,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { pasteFromClipboard(context) { url = it; message = null } }) {
                        Icon(
                            imageVector = com.strela.onebox.ui.design.icons.OneBoxIcons.Clipboard,
                            contentDescription = s.pasteFromClipboard,
                            modifier = Modifier.size(20.dp),
                            tint = colors.onSecondaryContainer,
                        )
                    }
                },
            )

            message?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = msg.text,
                    style = MiuixTheme.textStyles.footnote2,
                    color = when (msg.tone) {
                        StatusMessage.Tone.SUCCESS -> colors.primary
                        StatusMessage.Tone.ERROR -> colors.error
                    },
                )
            }

            if (loading) {
                Spacer(Modifier.height(14.dp))
                InfiniteProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = s.importingSubscription,
                    style = MiuixTheme.textStyles.footnote2,
                    color = colors.onSurfaceContainerVariant,
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            message = null
                            val result = try {
                                onImport(url, title)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                // последний рубеж: диалог не падает ни на какой ошибке
                                message = StatusMessage(s.errorNetwork, StatusMessage.Tone.ERROR)
                                loading = false
                                return@launch
                            }
                            loading = false
                            when (result) {
                                is SubscriptionImportResult.Success -> {
                                    message = if (result.created) {
                                        StatusMessage(
                                            text = s.subscriptionAdded(
                                                result.subscription.title,
                                                result.serversCount,
                                            ),
                                            tone = StatusMessage.Tone.SUCCESS,
                                        )
                                    } else {
                                        StatusMessage(
                                            text = s.subscriptionUpdated(
                                                result.subscription.title,
                                                result.serversCount,
                                            ),
                                            tone = StatusMessage.Tone.SUCCESS,
                                        )
                                    }
                                    // успешно импортированное — очищаем, неудачный ввод остаётся на месте
                                    if (result.created) {
                                        url = ""
                                        title = ""
                                    }
                                }

                                is SubscriptionImportResult.Failure -> message = StatusMessage(
                                    text = result.reason.toMessage(s),
                                    tone = StatusMessage.Tone.ERROR,
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = url.isNotBlank() && !loading,
                ) {
                    Text(
                        text = s.importAction,
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
        }
    }
}

/** Сообщение под полем ввода. */
private data class StatusMessage(val text: String, val tone: Tone) {
    enum class Tone { SUCCESS, ERROR }
}

/**
 * Читает текст из буфера обмена: https-ссылка, JSON-конфиг или base64-подписка.
 * Ничего не вставляет, если буфер пуст или содержит нераспознанный мусор.
 */
private fun pasteFromClipboard(context: Context, onText: (String) -> Unit) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    val text = clipboard.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(context)
        ?.toString()
        .orEmpty()

    // сначала ссылка (в нормализованном виде), затем любой распознаваемый контент
    SubscriptionUrl.validateOrNull(text)?.let { return onText(it) }
    if (!text.startsWith("http", ignoreCase = true) && ImportDetector.detect(text) != null) {
        onText(text)
    }
}

private fun SubscriptionImportReason.toMessage(s: OneBoxStrings): String = when (this) {
    SubscriptionImportReason.INSECURE_URL -> s.errorInsecureConnection
    SubscriptionImportReason.INVALID_URL -> s.errorInvalidUrl
    SubscriptionImportReason.NETWORK -> s.errorNetwork
    SubscriptionImportReason.EMPTY_CONTENT -> s.errorNoServersInSubscription
}
