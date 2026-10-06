package com.grind.quotes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object QuoteRepository {

    fun current(ctx: Context): Quote =
        Prefs.savedQuote(ctx) ?: local(null).also { Prefs.saveQuote(ctx, it) }

    /** Blocking – call from a background thread. Online first, offline fallback. */
    fun next(ctx: Context): Quote {
        val old = Prefs.savedQuote(ctx)
        var q: Quote? = null
        repeat(2) {
            if (q == null) {
                val c = fetchZen() ?: fetchQuotable()
                if (c != null && c.text != old?.text) q = c
            }
        }
        val result = q ?: local(old)
        Prefs.saveQuote(ctx, result)
        return result
    }

    private fun get(url: String): String? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 4000
        conn.readTimeout = 4000
        conn.setRequestProperty("User-Agent", "Grind/1.0")
        if (conn.responseCode == 200) conn.inputStream.bufferedReader().use { it.readText() } else null
    } catch (e: Exception) {
        null
    }

    private fun fetchZen(): Quote? {
        return try {
            val body = get("https://zenquotes.io/api/random") ?: return null
            val o = JSONArray(body).getJSONObject(0)
            val t = o.getString("q").trim()
            val a = o.getString("a").trim()
            if (a.equals("Zen Quotes", true) || t.contains("Too many requests", true)) null
            else Quote(t, a)
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchQuotable(): Quote? {
        return try {
            val body = get("https://api.quotable.io/random?tags=inspirational|success|wisdom")
                ?: return null
            val o = JSONObject(body)
            Quote(o.getString("content").trim(), o.getString("author").trim())
        } catch (e: Exception) {
            null
        }
    }

    private fun local(avoid: Quote?): Quote {
        var q = FALLBACK.random()
        var tries = 0
        while (q.text == avoid?.text && tries++ < 10) q = FALLBACK.random()
        return q
    }

    private val FALLBACK = listOf(
        Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        Quote("It always seems impossible until it's done.", "Nelson Mandela"),
        Quote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln"),
        Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
        Quote("Success is the sum of small efforts repeated day in and day out.", "Robert Collier"),
        Quote("The future depends on what you do today.", "Mahatma Gandhi"),
        Quote("You miss 100% of the shots you don't take.", "Wayne Gretzky"),
        Quote("Hard work beats talent when talent doesn't work hard.", "Tim Notke"),
        Quote("What you get by achieving your goals is not as important as what you become.", "Zig Ziglar"),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        Quote("Whether you think you can or you think you can't, you're right.", "Henry Ford"),
        Quote("Fall seven times, stand up eight.", "Japanese Proverb"),
        Quote("The harder I work, the luckier I get.", "Samuel Goldwyn"),
        Quote("Start where you are. Use what you have. Do what you can.", "Arthur Ashe"),
        Quote("Action is the foundational key to all success.", "Pablo Picasso"),
        Quote("Dream big. Start small. Act now.", "Robin Sharma"),
        Quote("Energy and persistence conquer all things.", "Benjamin Franklin"),
        Quote("Little by little, one travels far.", "J.R.R. Tolkien"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Push yourself, because no one else is going to do it for you.", "Unknown"),
        Quote("Great things never come from comfort zones.", "Unknown"),
        Quote("Do something today that your future self will thank you for.", "Sean Patrick Flanery"),
        Quote("Strength does not come from winning. Your struggles develop your strengths.", "Arnold Schwarzenegger"),
        Quote("Out of difficulties grow miracles.", "Jean de La Bruy\u00E8re"),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Quote("Our greatest glory is not in never falling, but in rising every time we fall.", "Confucius"),
        Quote("Perseverance is not a long race; it is many short races one after the other.", "Walter Elliot"),
        Quote("Motivation gets you going, but discipline keeps you growing.", "John C. Maxwell"),
        Quote("The pain you feel today will be the strength you feel tomorrow.", "Unknown"),
        Quote("Don't stop when you're tired. Stop when you're done.", "Unknown"),
        Quote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar"),
        Quote("Excellence is not an act, but a habit.", "Will Durant"),
        Quote("He who has a why to live can bear almost any how.", "Friedrich Nietzsche"),
        Quote("Obsessed is just a word the lazy use to describe the dedicated.", "Russell Warren"),
        Quote("Wake up with determination. Go to bed with satisfaction.", "Unknown")
    )
}
