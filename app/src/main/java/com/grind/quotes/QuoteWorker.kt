package com.grind.quotes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class QuoteWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val q = QuoteRepository.next(applicationContext)
        QuoteWidgetProvider.refreshAll(applicationContext)
        Notifier.show(applicationContext, q)
        return Result.success()
    }

    companion object {
        private const val NAME = "grind_quotes_work"

        fun schedule(ctx: Context, hours: Long) {
            val req = PeriodicWorkRequestBuilder<QuoteWorker>(hours, TimeUnit.HOURS)
                .setInitialDelay(hours, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        fun cancel(ctx: Context) {
            WorkManager.getInstance(ctx).cancelUniqueWork(NAME)
        }
    }
}
