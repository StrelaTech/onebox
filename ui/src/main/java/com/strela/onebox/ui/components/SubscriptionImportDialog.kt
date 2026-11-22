package com.strela.onebox.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.ImportDetector
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.subscription.SubscriptionImportReason
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionUrl
import com.strela.sui.design.icons.SuiIcons
import com.strela.sui.design.Button
import com.strela.sui.design.Icon
import com.strela.sui.design.IconButton
import com.strela.sui.design.InfiniteProgressIndicator
import com.strela.sui.design.Surface
import com.strela.sui.design.Text
import com.strela.sui.design.TextField
import com.strela.sui.design.SuiDialog
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.strings
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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

    /** Общая логика импорта для кнопки, вставки и QR-кода. */
    val submit: (String) -> Unit = { raw ->
        val source = raw.trim()
        if (source.isEmpty()) {
            loading = false
        } else {
            AppLog.i("Подписка: старт импорта (${source.length} символов)")
            scope.launch {
                loading = true
                message = null
                val result = try {
                    onImport(source)
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
        }
    }

    // QR-код: содержимое распознаётся тем же импортом, что и вставленный текст.
    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents?.trim().orEmpty()
        if (contents.isEmpty()) {
            AppLog.i("Подписка: QR-код не распознан")
        } else {
            AppLog.i("Подписка: QR-код распознан")
            text = contents
            message = null
            submit(contents)
        }
    }

    SuiDialog(
        title = s.subscriptionDialogTitle,
        onDismissRequest = {
            if (!loading) {
                AppLog.i("Подписка: диалог импорта закрыт")
                onDismiss()
            }
        },
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
                        imageVector = SuiIcons.Link,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSecondaryContainer,
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            AppLog.i("Подписка: вставка из буфера обмена")
                            val pasted = pasteFromClipboard(context) { text = it; message = null }
                            if (!pasted) {
                                message = StatusMessage(
                                    text = s.clipboardNoHttpsUrl,
                                    tone = StatusMessage.Tone.ERROR,
                                )
                            }
                        },
                    ) {
                        Icon(
                            imageVector = SuiIcons.Clipboard,
                            contentDescription = s.pasteFromClipboard,
                            modifier = Modifier.size(20.dp),
                            tint = colors.onSecondaryContainer,
                        )
                    }
                },
            )

            // Быстрые способы: сканировать QR-код или сразу вставить из буфера.
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DialogAction(
                    icon = SuiIcons.Camera,
                    label = s.scanQrAction,
                    enabled = !loading,
                    onClick = {
                        AppLog.i("Подписка: запуск сканера QR-кодов")
                        qrLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt(s.scanQrPrompt)
                                setBeepEnabled(false)
                                setOrientationLocked(false)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
                DialogAction(
                    icon = SuiIcons.Clipboard,
                    label = s.pasteAction,
                    enabled = !loading,
                    onClick = {
                        AppLog.i("Подписка: вставка из буфера обмена (кнопка)")
                        val pasted = pasteFromClipboard(context) { text = it; message = null }
                        if (!pasted) {
                            message = StatusMessage(
                                text = s.clipboardNoHttpsUrl,
                                tone = StatusMessage.Tone.ERROR,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

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
                        AppLog.i("Подписка: нажата кнопка импорта")
                        submit(text)
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

/** Вспомогательная кнопка диалога: иконка + подпись на светлой поверхности. */
@Composable
private fun DialogAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (enabled) colors.primary else colors.onSurfaceContainerVariant,
            )
            Text(
                text = label,
                style = OneBoxTheme.textStyles.footnote1,
                color = if (enabled) colors.primary else colors.onSurfaceContainerVariant,
                maxLines = 1,
            )
        }
    }
}

/** Сообщение под полем ввода. */
private data class StatusMessage(val text: String, val tone: Tone) {
    enum class Tone { SUCCESS, ERROR }
}

/**
 * Читает текст из буфера обмена: https-ссылка, JSON-конфиг или base64-подписка.
 * Возвращает false, если буфер пуст или содержит нераспознанный мусор —
 * тогда вызывающий показывает подсказку.
 */
private fun pasteFromClipboard(context: Context, onText: (String) -> Unit): Boolean {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
    val clipboardText = clipboard.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(context)
        ?.toString()
        .orEmpty()
        .trim()

    if (clipboardText.isEmpty()) return false

    // сначала ссылка (в нормализованном виде), затем любой распознаваемый контент;
    // нераспознанный мусор в поле не попадает
    SubscriptionUrl.validateOrNull(clipboardText)?.let { onText(it); return true }
    if (!clipboardText.startsWith("http", ignoreCase = true) &&
        ImportDetector.detect(clipboardText) != null
    ) {
        onText(clipboardText)
        return true
    }
    return false
}

private fun SubscriptionImportReason.toMessage(s: OneBoxStrings): String = when (this) {
    SubscriptionImportReason.INSECURE_URL -> s.errorInsecureConnection
    SubscriptionImportReason.INVALID_URL -> s.errorInvalidUrl
    SubscriptionImportReason.NETWORK -> s.errorNetwork
    SubscriptionImportReason.EMPTY_CONTENT -> s.errorNoServersInSubscription
    SubscriptionImportReason.UNRECOGNIZED_CONTENT -> s.unrecognizedInput
    SubscriptionImportReason.NO_NEW_SERVERS -> s.noNewServers
}
