package it.svegliati.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var date: TextView

    private val tick = object : Runnable {
        override fun run() {
            val now = LocalDateTime.now()
            clock.text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ITALY))
            val weekday = now.format(DateTimeFormatter.ofPattern("EEE", Locale.ITALY))
                .replaceFirstChar { it.titlecase(Locale.ITALY) }
            val month = now.format(DateTimeFormatter.ofPattern("MMM", Locale.ITALY))
                .uppercase(Locale.ITALY)
            date.text = "$weekday ${now.format(DateTimeFormatter.ofPattern("dd", Locale.ITALY))} $month"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clock = findViewById(R.id.clock)
        date = findViewById(R.id.date)
        handler.post(tick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }
}
