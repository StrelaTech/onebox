package com.strela.onebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strela.onebox.api.PingMethod
import com.strela.onebox.api.PingOptions
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.TextField
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.DropdownRow
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.SectionTitle
import com.strela.onebox.ui.strings

/** Пресеты URL-теста: подпись → адрес. */
private fun urlPresets(s: OneBoxStrings) = listOf(
    s.pingUrlPresetGoogle to "https://www.gstatic.com/generate_204",
    s.pingUrlPresetCloudflare to "https://cp.cloudflare.com/generate_204",
    s.pingUrlPresetGithub to "https://github.com/robots.txt",
)

/**
 * Экран «Пинг»: что и как пинговать при проверке серверов — метод
 * (via Proxy GET / via Proxy HEAD / TCP / ICMP), тайм-аут и URL-тест.
 *
 * Настройки хранятся в [SettingsStore] и читаются ядром в момент пинга
 * (ProfileSource.pingOptions), поэтому применяются сразу, перезапуск не нужен.
 * Открывается поверх вкладок — так же, как консоль логов.
 */
@Composable
fun PingSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val s = strings
    val options by SettingsStore.pingOptions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = s.pingScreenTitle,
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = OneBoxIcons.Close,
                        contentDescription = s.close,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            PingSection(title = s.pingMethod, icon = OneBoxIcons.Send) {
                DropdownRow(
                    icon = OneBoxIcons.Send,
                    title = s.pingMethod,
                    subtitle = s.methodHint(options.method),
                    selectedLabel = s.methodLabel(options.method),
                    options = listOf(
                        s.pingMethodGet to {
                            SettingsStore.setPingMethod(context, PingMethod.PROXY_GET)
                        },
                        s.pingMethodHead to {
                            SettingsStore.setPingMethod(context, PingMethod.PROXY_HEAD)
                        },
                        s.pingMethodTcp to {
                            SettingsStore.setPingMethod(context, PingMethod.TCP)
                        },
                        s.pingMethodIcmp to {
                            SettingsStore.setPingMethod(context, PingMethod.ICMP)
                        },
                    ),
                )
            }

            PingSection(title = s.pingTimeout, icon = OneBoxIcons.Clock) {
                DropdownRow(
                    icon = OneBoxIcons.Clock,
                    title = s.pingTimeout,
                    subtitle = s.pingTimeoutHint,
                    selectedLabel = timeoutLabel(options.timeoutMs, s),
                    options = listOf(1000L, 2000L, 3000L, 5000L, 10_000L).map { ms ->
                        s.pingSeconds((ms / 1000).toInt()) to {
                            SettingsStore.setPingTimeout(context, ms)
                        }
                    },
                )
            }

            PingSection(title = s.pingUrlTest, icon = OneBoxIcons.Link) {
                UrlTestRows(options, s)
                Text(
                    text = s.pingUrlHint,
                    style = OneBoxTheme.textStyles.footnote2,
                    color = colors.onSurfaceContainerVariant,
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Метка выбранного метода (технические названия одинаковы на обоих языках). */
private fun OneBoxStrings.methodLabel(method: PingMethod): String = when (method) {
    PingMethod.PROXY_GET -> pingMethodGet
    PingMethod.PROXY_HEAD -> pingMethodHead
    PingMethod.TCP -> pingMethodTcp
    PingMethod.ICMP -> pingMethodIcmp
}

private fun OneBoxStrings.methodHint(method: PingMethod): String = when (method) {
    PingMethod.PROXY_GET -> pingMethodHintGet
    PingMethod.PROXY_HEAD -> pingMethodHintHead
    PingMethod.TCP -> pingMethodHintTcp
    PingMethod.ICMP -> pingMethodHintIcmp
}

/** Метка тайм-аута: пресет («5 с») или произвольное сохранённое значение («2500 мс»). */
private fun timeoutLabel(timeoutMs: Long, s: OneBoxStrings): String {
    val seconds = timeoutMs / 1000
    return if (timeoutMs % 1000 == 0L && seconds in 1..10) {
        s.pingSeconds(seconds.toInt())
    } else {
        s.pingMs(timeoutMs)
    }
}

/** Ряды URL-теста: выпадающий список пресетов (или «Другой URL») + поле ввода. */
@Composable
private fun UrlTestRows(
    value: PingOptions,
    s: OneBoxStrings,
) {
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val presets = urlPresets(s)
    // «Другой URL» активен, когда сохранённое значение не совпадает ни с одним
    // пресетом.
    var editingCustom by rememberSaveable { mutableStateOf(presets.none { it.second == value.testUrl }) }
    // Черновик поля ввода не привязан к testUrl remember-ключом: изменения
    // сохраняются сразу, а ключ ставил бы курсор в конец при каждом вводе.
    var draft by remember { mutableStateOf(value.testUrl) }

    DropdownRow(
        icon = OneBoxIcons.Link,
        title = s.pingUrlTest,
        subtitle = s.pingUrlTestHint,
        selectedLabel = if (editingCustom) {
            s.pingUrlOther
        } else {
            presets.firstOrNull { it.second == value.testUrl }?.first ?: value.testUrl
        },
        options = presets.map { (label, url) ->
            label to {
                editingCustom = false
                draft = url
                SettingsStore.setPingTestUrl(context, url)
            }
        } + listOf(
            s.pingUrlOther to {
                editingCustom = true
                // значение не меняется: пользователь правит его прямо в поле
            },
        ),
    )

    if (editingCustom) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(
                icon = OneBoxIcons.Edit,
                tint = colors.onSurfaceContainerVariant,
                background = colors.surfaceContainerHigh,
                size = 38.dp,
                iconSize = 19.dp,
                corner = 12.dp,
            )
            TextField(
                value = draft,
                onValueChange = {
                    draft = it
                    SettingsStore.setPingTestUrl(context, it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                placeholder = s.pingUrlLabel,
            )
        }
    }
}

/** Секция экрана пинга: подпись сверху и карточка с содержимым (стиль настроек). */
@Composable
private fun PingSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(text = title, icon = icon)
    Spacer(Modifier.height(10.dp))
    Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
        Column(content = content)
    }
    Spacer(Modifier.height(24.dp))
}
