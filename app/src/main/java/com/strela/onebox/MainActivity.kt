package com.strela.onebox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.strela.onebox.i18n.strings
import com.strela.onebox.log.AppLog
import com.strela.onebox.profile.Profile
import com.strela.onebox.profile.ProfileStore
import com.strela.onebox.profile.Server
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.settings.ThemeMode
import com.strela.onebox.ui.ServerRow
import com.strela.onebox.ui.ServersScreen
import com.strela.onebox.ui.SettingsScreen
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.IconTile
import com.strela.onebox.ui.design.LocalOneBoxTypography
import com.strela.onebox.ui.design.OneBoxTheme
import com.strela.onebox.ui.design.OutlineButton
import com.strela.onebox.ui.design.Palette
import com.strela.onebox.ui.design.Panel
import com.strela.onebox.ui.design.SectionLabel
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.xray.XrayController

class MainActivity : ComponentActivity() {

    private lateinit var vpnLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppLog.init(applicationContext)
        AppLog.i("MainActivity: onCreate")

        vpnLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                AppLog.d("диалог согласия: resultCode=${result.resultCode}")
                if (result.resultCode == Activity.RESULT_OK) XrayController.connectVpn(this)
            }

        SettingsStore.init(applicationContext)
        ProfileStore.init(applicationContext)
        XrayController.init(applicationContext)
        applyWindowBackground()
        enableEdgeToEdge()
        setContent {
            val language by SettingsStore.language.collectAsState()
            val themeMode by SettingsStore.theme.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ApplySystemBars(darkTheme = darkTheme)

            OneBoxTheme(darkTheme = darkTheme, language = language) {
                AppRoot(
                    onStartVpn = {
                        AppLog.d("UI: нажат «Старт (VPN)», запрашиваю разрешение")
                        prepareAndStartVpn()
                    },
                    onStopVpn = { XrayController.disconnectVpn(this) }
                )
            }
        }

        // Автоподключение — только при обычном старте, а не при повороте экрана.
        if (savedInstanceState == null) maybeAutoConnect()
    }

    /** Поднимает VPN при запуске, если пользователь включил автоподключение и доступ уже выдан. */
    private fun maybeAutoConnect() {
        if (!SettingsStore.autoConnect.value) return
        if (XrayController.isRunning) return
        if (ProfileStore.selectedServer() == null && !XrayController.hasUserConfig) {
            AppLog.d("autoConnect: нет профиля или сохранённого конфига, пропускаю")
            return
        }
        if (VpnService.prepare(this) != null) {
            AppLog.i("autoConnect: разрешение на VPN не выдано, пропускаю")
            return
        }
        AppLog.i("autoConnect: запускаю VPN")
        XrayController.connectVpn(this)
    }

    /** Фон окна до первого кадра — в цвет выбранной темы, чтобы не мигало при старте. */
    private fun applyWindowBackground() {
        val dark = when (SettingsStore.theme.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        window.setBackgroundDrawable(
            ColorDrawable(if (dark) 0xFF0E1116.toInt() else 0xFFF2F5F8.toInt())
        )
    }

    /** Иконки системных панелей должны контрастировать с фоном выбранной темы. */
    @Composable
    private fun ApplySystemBars(darkTheme: Boolean) {
        val view = LocalView.current
        val context = LocalContext.current
        SideEffect {
            val window = (context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    private fun prepareAndStartVpn() {
        val prepare = VpnService.prepare(this)
        if (prepare == null) {
            AppLog.d("Vpn.prepare() == null → сразу стартуем сервис")
            XrayController.connectVpn(this)
        } else {
            AppLog.d("Vpn.prepare() → показываю диалог согласия")
            vpnLauncher.launch(prepare)
        }
    }
}

private enum class Tab(val icon: ImageVector) {
    MAIN(OneBoxIcons.Home),
    SERVER(OneBoxIcons.Globe),
    SETTINGS(OneBoxIcons.Settings)
}

@Composable
private fun AppRoot(onStartVpn: () -> Unit, onStopVpn: () -> Unit) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(Tab.MAIN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Header(title = tab.title())

        Spacer(Modifier.height(18.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                Tab.MAIN -> ConnectScreen(
                    onStartVpn = onStartVpn,
                    onStopVpn = onStopVpn,
                    onOpenProfiles = { tab = Tab.SERVER },
                    modifier = Modifier.fillMaxSize()
                )

                Tab.SERVER -> ServersTab(
                    context = context,
                    modifier = Modifier.fillMaxSize()
                )

                Tab.SETTINGS -> SettingsTab(
                    context = context,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        BottomBar(current = tab, onSelect = { tab = it })
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun Tab.title(): String = when (this) {
    Tab.MAIN -> strings.tabMain
    Tab.SERVER -> strings.tabServer
    Tab.SETTINGS -> strings.tabSettings
}

@Composable
private fun Header(title: String) {
    val typo = LocalOneBoxTypography.current
    val version by XrayController.version.collectAsState()
    val status by XrayController.status.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = OneBoxIcons.StrelaLogo,
            size = 46.dp,
            iconSize = 27.dp,
            tint = Palette.AccentPrimary,
            background = Palette.AccentSoft,
            corner = 14.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            BasicText(
                text = title,
                style = typo.title.copy(color = Palette.TextPrimary)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    icon = OneBoxIcons.Terminal,
                    modifier = Modifier.size(11.dp),
                    tint = Palette.TextMuted
                )
                BasicText(
                    text = "xray-core $version",
                    style = typo.caption.copy(color = Palette.TextMuted),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        StatusChip(status = status)
    }
}

@Composable
private fun StatusChip(status: XrayController.Status) {
    val typo = LocalOneBoxTypography.current
    val color = statusColor(status)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.35f)), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        BasicText(
            text = statusLabel(status),
            style = typo.caption.copy(color = color),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    val typo = LocalOneBoxTypography.current
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.SurfaceElevated)
            .border(BorderStroke(1.dp, Palette.Border), shape)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Tab.entries.forEach { entry ->
            val active = entry == current
            val itemShape = RoundedCornerShape(12.dp)
            val label = when (entry) {
                Tab.MAIN -> strings.tabMain
                Tab.SERVER -> strings.tabServer
                Tab.SETTINGS -> strings.tabSettings
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .background(if (active) Palette.Surface else Color.Transparent)
                    .then(
                        if (active) Modifier.border(
                            BorderStroke(1.dp, Palette.AccentPrimary.copy(alpha = 0.45f)),
                            itemShape
                        )
                        else Modifier
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon = entry.icon,
                    modifier = Modifier.size(17.dp),
                    tint = if (active) Palette.AccentPrimary else Palette.TextMuted
                )
                BasicText(
                    text = label,
                    style = typo.body.copy(
                        color = if (active) Palette.TextPrimary else Palette.TextSecondary
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(start = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectScreen(
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
    onOpenProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by XrayController.status.collectAsState()
    val profiles by ProfileStore.profiles.collectAsState()
    val selectedProfileId by ProfileStore.selectedProfileId.collectAsState()
    val pings by XrayController.pings.collectAsState()
    val selected = remember(profiles, selectedProfileId) {
        profiles.firstOrNull { it.id == selectedProfileId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ConnectHero(
            status = status,
            onStartVpn = onStartVpn,
            onStopVpn = onStopVpn
        )

        Spacer(Modifier.height(20.dp))

        ProfileServersSection(
            profile = selected,
            hasProfiles = profiles.isNotEmpty(),
            pings = pings,
            onOpenProfiles = onOpenProfiles,
            onSelectServer = { server ->
                selected?.let {
                    ProfileStore.selectServer(context, it.id, server.id)
                    XrayController.refreshFromProfile()
                }
            },
            onPing = { server -> XrayController.pingServer(server) }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectHero(
    status: XrayController.Status,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit
) {
    val typo = LocalOneBoxTypography.current
    val visual = statusVisual(status)
    val running = status is XrayController.Status.Running

    Panel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (running) visual.color.copy(alpha = 0.45f) else Palette.Border,
        padding = PaddingValues(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(visual.color)
                )
                BasicText(
                    text = visual.label,
                    style = typo.label.copy(color = visual.color),
                    modifier = Modifier.padding(start = 7.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            BigConnectButton(
                status = status,
                onStartVpn = onStartVpn,
                onStopVpn = onStopVpn
            )

            Spacer(Modifier.height(16.dp))

            BasicText(
                text = visual.description,
                style = typo.caption.copy(
                    color = Palette.TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            if (status is XrayController.Status.Error) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.ErrorSoft)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        icon = OneBoxIcons.Alert,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(14.dp),
                        tint = Palette.Error
                    )
                    BasicText(
                        text = status.message,
                        style = typo.mono.copy(color = Palette.Error),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/** Главная кнопка: круг с иконкой и подписью, цвет зависит от состояния VPN. */
@Composable
private fun BigConnectButton(
    status: XrayController.Status,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit
) {
    val typo = LocalOneBoxTypography.current
    val s = strings
    val visual = statusVisual(status)
    val starting = status is XrayController.Status.Starting

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !starting) 0.965f else 1f,
        label = "connectButtonScale"
    )

    val fill = when (status) {
        is XrayController.Status.Running -> Palette.Running
        is XrayController.Status.Starting -> Palette.Starting
        is XrayController.Status.Error -> Palette.Error
        is XrayController.Status.Stopped -> Palette.AccentPrimary
    }
    val label = when (status) {
        is XrayController.Status.Running -> s.disconnect
        is XrayController.Status.Starting -> s.starting
        is XrayController.Status.Error -> s.retry
        is XrayController.Status.Stopped -> s.connect
    }
    val icon = when (status) {
        is XrayController.Status.Running -> OneBoxIcons.Pause
        is XrayController.Status.Starting -> OneBoxIcons.Refresh
        is XrayController.Status.Error -> OneBoxIcons.Refresh
        is XrayController.Status.Stopped -> OneBoxIcons.Play
    }
    // На ярких статусных заливках (зелёный/жёлтый/красный) белый текст плохо читается
    // в тёмной теме — используем цвет фона как контрастный.
    val contentColor = when (status) {
        is XrayController.Status.Stopped -> Palette.OnAccent
        else -> Palette.Background
    }

    Box(
        modifier = Modifier
            .size(206.dp)
            .clip(CircleShape)
            .background(visual.softColor)
            .border(BorderStroke(1.dp, visual.color.copy(alpha = 0.35f)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(164.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(fill)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = !starting,
                    onClick = {
                        if (status is XrayController.Status.Running) onStopVpn() else onStartVpn()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (starting) {
                    SpinningIcon(
                        icon = icon,
                        tint = contentColor,
                        size = 32.dp
                    )
                } else {
                    Icon(
                        icon = icon,
                        modifier = Modifier.size(32.dp),
                        tint = contentColor
                    )
                }
                BasicText(
                    text = label,
                    style = typo.subtitle.copy(color = contentColor),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

/** Список серверов активного профиля — сразу под большой кнопкой подключения. */
@Composable
private fun ProfileServersSection(
    profile: Profile?,
    hasProfiles: Boolean,
    pings: Map<String, XrayController.Ping>,
    onOpenProfiles: () -> Unit,
    onSelectServer: (Server) -> Unit,
    onPing: (Server) -> Unit
) {
    val typo = LocalOneBoxTypography.current
    val s = strings

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel(
            text = s.profileServers,
            icon = OneBoxIcons.Globe,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            icon = OneBoxIcons.Folder,
            contentDescription = s.chooseProfile,
            onClick = onOpenProfiles,
            tint = Palette.AccentPrimary,
            background = Palette.AccentSoft,
            size = 34.dp,
            iconSize = 17.dp
        )
    }

    Spacer(Modifier.height(10.dp))

    if (profile == null) {
        Panel(
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(horizontal = 18.dp, vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconTile(
                    icon = if (hasProfiles) OneBoxIcons.Code else OneBoxIcons.Folder,
                    size = 48.dp,
                    iconSize = 22.dp,
                    tint = if (hasProfiles) Palette.AccentPrimary else Palette.TextSecondary,
                    background = if (hasProfiles) Palette.AccentSoft else Palette.SurfaceElevated,
                    corner = 14.dp
                )
                BasicText(
                    text = if (hasProfiles) s.configMode else s.noProfiles,
                    style = typo.subtitle.copy(color = Palette.TextPrimary),
                    modifier = Modifier.padding(top = 12.dp)
                )
                BasicText(
                    text = if (hasProfiles) s.configModeHint else s.noProfilesHint,
                    style = typo.caption.copy(
                        color = Palette.TextMuted,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(14.dp))
                OutlineButton(
                    text = if (hasProfiles) s.chooseProfile else s.importAction,
                    icon = if (hasProfiles) OneBoxIcons.Folder else OneBoxIcons.Plus,
                    onClick = onOpenProfiles,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Surface)
            .border(BorderStroke(1.dp, Palette.Border), RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenProfiles)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = OneBoxIcons.Folder,
            size = 34.dp,
            iconSize = 17.dp,
            tint = Palette.AccentPrimary,
            background = Palette.AccentSoft,
            corner = 10.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp)
        ) {
            BasicText(
                text = profile.name,
                style = typo.heading.copy(color = Palette.TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            BasicText(
                text = s.serversCount(profile.servers.size),
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            icon = OneBoxIcons.ArrowRight,
            modifier = Modifier.size(16.dp),
            tint = Palette.TextMuted
        )
    }

    Spacer(Modifier.height(10.dp))

    if (profile.servers.isEmpty()) {
        Panel(modifier = Modifier.fillMaxWidth()) {
            BasicText(
                text = s.noServersInProfile,
                style = typo.caption.copy(color = Palette.TextMuted)
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            profile.servers.forEach { server ->
                ServerRow(
                    server = server,
                    selected = server.id == profile.selectedServer?.id,
                    ping = pings[server.id],
                    onSelect = { onSelectServer(server) },
                    onPing = { onPing(server) }
                )
            }
        }
    }
}

/** Вращающаяся иконка — индикатор запуска подключения. */
@Composable
private fun SpinningIcon(icon: ImageVector, tint: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "connect")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "connectAngle"
    )
    Icon(
        icon = icon,
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = angle },
        tint = tint
    )
}

@Composable
private fun ServersTab(context: Context, modifier: Modifier = Modifier) {
    val profiles by ProfileStore.profiles.collectAsState()
    val selectedProfileId by ProfileStore.selectedProfileId.collectAsState()
    val pings by XrayController.pings.collectAsState()

    ServersScreen(
        profiles = profiles,
        selectedProfileId = selectedProfileId,
        pings = pings,
        onImportServers = { servers, profileName ->
            AppLog.i("ServersTab: распознаны ссылки, серверов=${servers.size}")
            val added = ProfileStore.import(context, servers, profileName)?.servers?.size ?: 0
            // импортированный профиль сразу становится активным
            if (added > 0) XrayController.refreshFromProfile()
            added
        },
        onImportConfig = { json ->
            AppLog.i("ServersTab: распознан JSON-конфиг, символов=${json.length}")
            // JSON используется, только когда профиль не выбран
            ProfileStore.clearSelection(context)
            XrayController.saveConfig(context, json)
        },
        onSelectProfile = { id ->
            ProfileStore.selectProfile(context, id)
            XrayController.refreshFromProfile()
        },
        onDeleteProfile = { id ->
            ProfileStore.removeProfile(context, id)
            XrayController.refreshFromProfile()
        },
        onSelectServer = { profileId, serverId ->
            ProfileStore.selectServer(context, profileId, serverId)
            XrayController.refreshFromProfile()
        },
        onDeleteServer = { profileId, serverId ->
            ProfileStore.removeServer(context, profileId, serverId)
            XrayController.refreshFromProfile()
        },
        onPing = { server -> XrayController.pingServer(server) },
        modifier = modifier
    )
}

@Composable
private fun SettingsTab(context: Context, modifier: Modifier = Modifier) {
    val theme by SettingsStore.theme.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val blockAds by SettingsStore.blockAds.collectAsState()
    val autoConnect by SettingsStore.autoConnect.collectAsState()
    val coreVersion by XrayController.version.collectAsState()
    val profiles by ProfileStore.profiles.collectAsState()
    val selectedProfileId by ProfileStore.selectedProfileId.collectAsState()
    val activeProfile = remember(profiles, selectedProfileId) {
        profiles.firstOrNull { it.id == selectedProfileId }?.name
    }

    SettingsScreen(
        theme = theme,
        language = language,
        blockAds = blockAds,
        autoConnect = autoConnect,
        coreVersion = coreVersion,
        activeProfile = activeProfile,
        onThemeChange = { SettingsStore.setTheme(context, it) },
        onLanguageChange = { SettingsStore.setLanguage(context, it) },
        onBlockAdsChange = { SettingsStore.setBlockAds(context, it) },
        onAutoConnectChange = { SettingsStore.setAutoConnect(context, it) },
        onShareLogs = { shareLogs(context) },
        onClearLogs = { AppLog.clear() },
        onResetSettings = { SettingsStore.reset(context) },
        modifier = modifier
    )
}

private data class StatusVisual(
    val color: Color,
    val softColor: Color,
    val icon: ImageVector,
    val label: String,
    val description: String
)

@Composable
private fun statusVisual(status: XrayController.Status): StatusVisual = when (status) {
    is XrayController.Status.Stopped -> StatusVisual(
        color = Palette.Stopped,
        softColor = Palette.StoppedSoft,
        icon = OneBoxIcons.Minus,
        label = strings.statusStoppedTitle,
        description = strings.statusStoppedDescription
    )

    is XrayController.Status.Starting -> StatusVisual(
        color = Palette.Starting,
        softColor = Palette.StartingSoft,
        icon = OneBoxIcons.Clock,
        label = strings.statusStartingTitle,
        description = strings.statusStartingDescription
    )

    is XrayController.Status.Running -> StatusVisual(
        color = Palette.Running,
        softColor = Palette.RunningSoft,
        icon = OneBoxIcons.Check,
        label = strings.statusRunningTitle,
        description = strings.statusRunningDescription
    )

    is XrayController.Status.Error -> StatusVisual(
        color = Palette.Error,
        softColor = Palette.ErrorSoft,
        icon = OneBoxIcons.Alert,
        label = strings.statusErrorTitle,
        description = strings.statusErrorDescription
    )
}

@Composable
private fun statusColor(status: XrayController.Status): Color = statusVisual(status).color

@Composable
private fun statusLabel(status: XrayController.Status): String = when (status) {
    is XrayController.Status.Stopped -> strings.statusStoppedShort
    is XrayController.Status.Starting -> strings.statusStartingShort
    is XrayController.Status.Running -> strings.statusRunningShort
    is XrayController.Status.Error -> strings.statusErrorShort
}

/** Отправляет logs.log через системный Share (файл из filesDir по другому не достать). */
private fun shareLogs(context: Context) {
    val file = AppLog.file
    if (file == null || !file.exists()) {
        AppLog.w("shareLogs: файл логов ещё не создан")
        return
    }
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, SettingsStore.strings().logsChooser))
}
