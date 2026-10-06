package com.grind.quotes

import android.content.Context

object Prefs {
    private const val NAME = "grind_prefs"
    private fun p(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun notificationsEnabled(c: Context) = p(c).getBoolean("notif", false)
    fun setNotificationsEnabled(c: Context, v: Boolean) = p(c).edit().putBoolean("notif", v).apply()

    fun intervalHours(c: Context) = p(c).getLong("hours", 6L)
    fun setIntervalHours(c: Context, v: Long) = p(c).edit().putLong("hours", v).apply()

    fun savedQuote(c: Context): Quote? {
        val t = p(c).getString("q_text", null) ?: return null
        return Quote(t, p(c).getString("q_author", "Unknown") ?: "Unknown")
    }

    fun saveQuote(c: Context, q: Quote) =
        p(c).edit().putString("q_text", q.text).putString("q_author", q.author).apply()
}
