package com.example.counter.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** One independent tally. Several of these run at the same time on screen. */
data class CounterItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val value: Int = 0,
    val pinned: Boolean = false,
)

/** Tiny SharedPreferences + JSON store: no database needed for a handful of counters. */
class CounterStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): List<CounterItem> = runCatching {
        val raw = prefs.getString(KEY_COUNTERS, null) ?: return emptyList()
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                val label = json.optString(KEY_LABEL).ifBlank { continue }
                add(
                    CounterItem(
                        id = json.optString(KEY_ID).ifBlank { UUID.randomUUID().toString() },
                        label = label,
                        value = json.optInt(KEY_VALUE, 0),
                        pinned = json.optBoolean(KEY_PINNED, false),
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

    fun save(items: List<CounterItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put(KEY_ID, item.id)
                    .put(KEY_LABEL, item.label)
                    .put(KEY_VALUE, item.value)
                    .put(KEY_PINNED, item.pinned)
            )
        }
        prefs.edit().putString(KEY_COUNTERS, array.toString()).apply()
    }

    /** Names the user typed before, newest first. */
    fun loadHistory(): List<String> = runCatching {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val name = array.optString(index).trim()
                if (name.isNotEmpty()) add(name)
            }
        }
    }.getOrElse { emptyList() }

    fun saveHistory(names: List<String>) {
        prefs.edit().putString(KEY_HISTORY, JSONArray(names).toString()).apply()
    }

    /** List presentation style, remembered across launches. */
    fun compactLayout(): Boolean = prefs.getBoolean(KEY_COMPACT, false)

    fun setCompactLayout(compact: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT, compact).apply()
    }

    private companion object {
        const val PREFS = "counter_store"
        const val KEY_COUNTERS = "counters"
        const val KEY_ID = "id"
        const val KEY_LABEL = "label"
        const val KEY_VALUE = "value"
        const val KEY_PINNED = "pinned"
        const val KEY_HISTORY = "history"
        const val KEY_COMPACT = "compact"
    }
}