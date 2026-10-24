package com.strela.onebox

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.settings.ThemeMode
import com.strela.onebox.ui.OneBoxApp
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.xray.XrayController

/**
 * Тонкая Activity: инициализация процесса, VPN-разрешение, системные бары и шаринг логов.
 * Весь интерфейс и навигация находятся в модуле :ui.
 */
class MainActivity : ComponentActivity() {

    private var pendingVpnConnect = false

    private val vpnLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        AppLog.d("диалог согласия: resultCode=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK && pendingVpnConnect) {
            XrayController.connectVpn(this)
        }
        pendingVpnConnect = false
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLog.d("POST_NOTIFICATIONS: granted=$granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppLog.init(applicationContext)
        AppLog.i("MainActivity: onCreate")

        SettingsStore.init(applicationContext)
        SubscriptionStore.init(applicationContext)
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
                OneBoxApp(
                    onStartVpn = {
                        AppLog.d("UI: запрос подключения VPN")
                        prepareAndStartVpn()
                    },
                    onStopVpn = { XrayController.disconnectVpn(this) },
                    onShareLogs = { shareLogs(this) },
                )
            }
        }

        if (savedInstanceState == null) {
            requestNotificationPermission()
            maybeAutoConnect()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Поднимает VPN при запуске, если включено автоподключение и доступ уже выдан. */
    private fun maybeAutoConnect() {
        if (!SettingsStore.autoConnect.value) return
        if (XrayController.isRunning) return
        val subscription = SubscriptionStore.selectedSubscription()
        if ((subscription == null || (!subscription.isJson && subscription.selectedServer == null)) && !XrayController.hasUserConfig) {
            AppLog.d("autoConnect: нет подписки или сохранённого конфига, пропускаю")
            return
        }
        if (VpnService.prepare(this) != null) {
            AppLog.i("autoConnect: разрешение на VPN не выдано, пропускаю")
            return
        }
        AppLog.i("autoConnect: запускаю VPN")
        XrayController.connectVpn(this)
    }

    /** Фон окна до первого кадра — в цвет темы Miuix, чтобы не мигало при старте. */
    private fun applyWindowBackground() {
        val dark = when (SettingsStore.theme.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        window.setBackgroundDrawable(
            (if (dark) 0xFF242424.toInt() else 0xFFFFFFFF.toInt()).toDrawable()
        )
    }

    /** Иконки системных панелей должны контрастировать с фоном Miuix-темы. */
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
            pendingVpnConnect = true
            vpnLauncher.launch(prepare)
        }
    }
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
