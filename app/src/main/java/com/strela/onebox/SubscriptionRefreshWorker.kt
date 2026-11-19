package com.strela.onebox

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.UpdateInterval
import java.util.concurrent.TimeUnit

/**
 * Периодическое обновление URL-подписок в фоне. Интервал задаётся в
 * настройках ([UpdateInterval]); при «Никогда» работа отменяется.
 */
class SubscriptionRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppLog.i("SubscriptionRefreshWorker: плановое обновление подписок")
        return try {
            StartupRefresh.refreshNow(applicationContext, "work")
            Result.success()
        } catch (e: Exception) {
            AppLog.e("SubscriptionRefreshWorker: ошибка обновления", e)
            Result.retry()
        }
    }
}

/** Регистрирует/снимает периодическую работу обновления подписок. */
object SubscriptionRefreshScheduler {

    private const val UNIQUE_NAME = "subscription_refresh"

    fun schedule(context: Context, interval: UpdateInterval) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (interval == UpdateInterval.NEVER) {
            workManager.cancelUniqueWork(UNIQUE_NAME)
            AppLog.i("SubscriptionRefreshScheduler: автообновление отключено")
            return
        }

        val request = PeriodicWorkRequestBuilder<SubscriptionRefreshWorker>(
            interval.millis,
            TimeUnit.MILLISECONDS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        AppLog.i("SubscriptionRefreshScheduler: интервал обновления=${interval.key}")
    }
}
