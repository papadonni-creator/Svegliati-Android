package it.svegliati.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SvegliatiWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val now = LocalDateTime.now()
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ITALY))
        val weekday = now.format(DateTimeFormatter.ofPattern("EEE", Locale.ITALY))
            .replaceFirstChar { it.titlecase(Locale.ITALY) }
        val month = now.format(DateTimeFormatter.ofPattern("MMM", Locale.ITALY))
            .uppercase(Locale.ITALY)
        val day = "$weekday ${now.format(DateTimeFormatter.ofPattern("dd", Locale.ITALY))} $month"

        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.svegliati_widget)
            views.setTextViewText(R.id.widget_time, time)
            views.setTextViewText(R.id.widget_date, day)
            manager.updateAppWidget(id, views)
        }
    }
}
