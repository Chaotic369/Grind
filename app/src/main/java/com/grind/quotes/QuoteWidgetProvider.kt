package com.grind.quotes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        val q = QuoteRepository.current(ctx)
        ids.forEach { mgr.updateAppWidget(it, build(ctx, q)) }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action == ACTION_NEXT) {
            val pending = goAsync()
            Thread {
                try {
                    QuoteRepository.next(ctx.applicationContext)
                    refreshAll(ctx.applicationContext)
                } finally {
                    pending.finish()
                }
            }.start()
        }
    }

    companion object {
        const val ACTION_NEXT = "com.grind.quotes.ACTION_NEXT_QUOTE"

        fun build(ctx: Context, q: Quote): RemoteViews {
            val views = RemoteViews(ctx.packageName, R.layout.widget_quote)
            views.setTextViewText(R.id.widget_quote, q.display())
            views.setTextViewText(R.id.widget_author, "\u2014 ${q.author}")

            val intent = Intent(ctx, QuoteWidgetProvider::class.java).setAction(ACTION_NEXT)
            val pi = PendingIntent.getBroadcast(
                ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            return views
        }

        fun refreshAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, QuoteWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val q = QuoteRepository.current(ctx)
            ids.forEach { mgr.updateAppWidget(it, build(ctx, q)) }
        }
    }
}
