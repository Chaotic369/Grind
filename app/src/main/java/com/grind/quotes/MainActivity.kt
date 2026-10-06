package com.grind.quotes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var quoteView: TextView
    private lateinit var authorView: TextView
    private lateinit var newBtn: Button
    private lateinit var switch: MaterialSwitch
    private lateinit var chips: ChipGroup
    private var pendingTest = false

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (pendingTest) Notifier.show(this, QuoteRepository.current(this))
                else enableNotifications()
            } else {
                if (!pendingTest) switch.isChecked = false
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
            pendingTest = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quoteView = findViewById(R.id.quote_text)
        authorView = findViewById(R.id.quote_author)
        newBtn = findViewById(R.id.btn_new)
        switch = findViewById(R.id.switch_notif)
        chips = findViewById(R.id.chip_group)

        showQuote(QuoteRepository.current(this))

        newBtn.setOnClickListener { loadNext() }

        findViewById<Button>(R.id.btn_share).setOnClickListener {
            val q = QuoteRepository.current(this)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, q.shareText())
            }
            startActivity(Intent.createChooser(send, "Share quote"))
        }

        // restore settings
        val hours = Prefs.intervalHours(this)
        chips.check(
            when (hours) {
                1L -> R.id.chip1
                3L -> R.id.chip3
                12L -> R.id.chip12
                24L -> R.id.chip24
                else -> R.id.chip6
            }
        )
        switch.isChecked = Prefs.notificationsEnabled(this)

        switch.setOnCheckedChangeListener { _, on ->
            if (on) {
                if (Notifier.hasPermission(this)) enableNotifications()
                else if (Build.VERSION.SDK_INT >= 33) {
                    pendingTest = false
                    permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                Prefs.setNotificationsEnabled(this, false)
                QuoteWorker.cancel(this)
            }
        }

        chips.setOnCheckedStateChangeListener { group, ids ->
            val id = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val h = group.findViewById<Chip>(id).tag.toString().toLong()
            Prefs.setIntervalHours(this, h)
            if (Prefs.notificationsEnabled(this)) QuoteWorker.schedule(this, h)
        }

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            if (Notifier.hasPermission(this)) {
                Notifier.show(this, QuoteRepository.current(this))
            } else if (Build.VERSION.SDK_INT >= 33) {
                pendingTest = true
                permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showQuote(QuoteRepository.current(this))
    }

    private fun enableNotifications() {
        Prefs.setNotificationsEnabled(this, true)
        QuoteWorker.schedule(this, Prefs.intervalHours(this))
        Toast.makeText(this, "Motivation notifications on \uD83D\uDD25", Toast.LENGTH_SHORT).show()
    }

    private fun loadNext() {
        newBtn.isEnabled = false
        newBtn.text = "Loading\u2026"
        Thread {
            val q = QuoteRepository.next(applicationContext)
            QuoteWidgetProvider.refreshAll(applicationContext)
            runOnUiThread {
                showQuote(q)
                newBtn.isEnabled = true
                newBtn.text = getString(R.string.new_quote)
            }
        }.start()
    }

    private fun showQuote(q: Quote) {
        quoteView.text = q.display()
        authorView.text = "\u2014 ${q.author}"
    }
}
