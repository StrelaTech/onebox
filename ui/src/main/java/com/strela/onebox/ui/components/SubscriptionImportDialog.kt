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
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.subscription.SubscriptionImportReason
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionUrl
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.design.Button
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.InfiniteProgressIndicator
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.TextField
import com.strela.onebox.ui.design.OneBoxDialog
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.strings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Диалог кнопки «Добавить»: принимает всё — ссылку https:// (отдельная
 * URL-подписка), URI-ссылки серверов или JSON-конфиг (стандартный профиль
 * «Серверы»), base64 (сначала расшифровывается). Тип определяет
 * [onImport] — вся бизнес-логика там (AddSubscriptionUseCase),
 * диалог только управляет состоянием и сообщениями.
 */
@Composable
fun SubscriptionImportDialog(
    onDismiss: () -> Unit,
    onImport: suspend (text: String) -> SubscriptionImportResult,
) {
    val s = strings
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var text by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<StatusMessage?>(null) }

    OneBoxDialog(
        title = s.subscriptionDialogTitle,
        onDismissRequest = { if (!loading) onDismiss() },
    ) {
        Column {
            TextField(
                value = text,
                onValueChange = {
                    text = it
                    message = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = s.subscriptionUrlLabel,
                maxLines = 6,
                enabled = !loading,
                leadingIcon = {
                    Icon(
                        imageVector = OneBoxIcons.Link,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSecondaryContainer,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { pasteFromClipboard(context) { text = it; message = null } }) {
                        Icon(
                            imageVector = OneBoxIcons.Clipboard,
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
                    style = OneBoxTheme.textStyles.footnote2,
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
                    style = OneBoxTheme.textStyles.footnote2,
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
                                onImport(text)
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
                                    message = if (result.subscription.id == Subscription.SERVERS_ID) {
                                        // самодельные серверы легли в стандартный профиль «Серверы»
                                        StatusMessage(
                                            text = s.addedServers(result.serversAdded),
                                            tone = StatusMessage.Tone.SUCCESS,
                                        )
                                    } else if (result.created) {
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
                                    // успешно импортированное — очищаем, неудачный ввод остаётся
                                    if (result.created || result.subscription.id == Subscription.SERVERS_ID) {
                                        text = ""
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
                    enabled = text.isNotBlank() && !loading,
                ) {
                    Text(
                        text = s.importAction,
                        style = OneBoxTheme.textStyles.button,
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
    val clipboardText = clipboard.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(context)
        ?.toString()
        .orEmpty()

    // сначала ссылка (в нормализованном виде), затем любой распознаваемый контент;
    // нераспознанный мусор в поле не попадает
    SubscriptionUrl.validateOrNull(clipboardText)?.let { return onText(it) }
    if (!clipboardText.startsWith("http", ignoreCase = true) &&
        ImportDetector.detect(clipboardText) != null
    ) {
        onText(clipboardText)
    }
}

private fun SubscriptionImportReason.toMessage(s: OneBoxStrings): String = when (this) {
    SubscriptionImportReason.INSECURE_URL -> s.errorInsecureConnection
    SubscriptionImportReason.INVALID_URL -> s.errorInvalidUrl
    SubscriptionImportReason.NETWORK -> s.errorNetwork
    SubscriptionImportReason.EMPTY_CONTENT -> s.errorNoServersInSubscription
    SubscriptionImportReason.UNRECOGNIZED_CONTENT -> s.unrecognizedInput
    SubscriptionImportReason.NO_NEW_SERVERS -> s.noNewServers
}
