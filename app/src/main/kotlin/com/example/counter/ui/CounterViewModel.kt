package com.example.counter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.counter.data.CounterItem
import com.example.counter.data.CounterStore

class CounterViewModel(private val store: CounterStore) : ViewModel() {

    private val items = mutableStateListOf<CounterItem>()

    /** Pinned first, otherwise insertion order. Both sorts are stable. */
    val counters: List<CounterItem> get() = items.sortedByDescending { it.pinned }

    val history = mutableStateListOf<String>().also { it.addAll(store.loadHistory()) }

    var compactLayout by mutableStateOf(store.compactLayout())
        private set

    init {
        items.addAll(store.load())
    }

    fun toggleLayout() {
        compactLayout = !compactLayout
        store.setCompactLayout(compactLayout)
    }

    fun add(label: String) {
        val clean = label.trim().ifEmpty { "计数器" }
        items.add(CounterItem(label = clean))
        rememberName(clean)
        persist()
    }

    fun step(id: String, delta: Int) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = items[index]
        items[index] = current.copy(value = current.value + delta)
        persist()
    }

    fun reset(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items[index] = items[index].copy(value = 0)
        persist()
    }

    fun resetAll() {
        if (items.isEmpty()) return
        for (index in items.indices) {
            items[index] = items[index].copy(value = 0)
        }
        persist()
    }

    fun togglePin(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items[index] = items[index].copy(pinned = !items[index].pinned)
        persist()
    }

    fun rename(id: String, label: String) {
        val index = items.indexOfFirst { it.id == id }
        val clean = label.trim()
        if (index < 0 || clean.isEmpty()) return
        items[index] = items[index].copy(label = clean)
        rememberName(clean)
        persist()
    }

    fun remove(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items.removeAt(index)
        persist()
    }

    fun removeAll() {
        if (items.isEmpty()) return
        items.clear()
        persist()
    }

    fun pinnedSum(): Int = items.filter { it.pinned }.sumOf { it.value }

    fun totalSum(): Int = items.sumOf { it.value }

    /** Drops one entry from the suggestion history. */
    fun forgetName(name: String) {
        if (history.remove(name)) {
            store.saveHistory(history.toList())
        }
    }

    private fun rememberName(name: String) {
        history.remove(name)
        history.add(0, name)
        while (history.size > HISTORY_LIMIT) {
            history.removeAt(history.lastIndex)
        }
        store.saveHistory(history.toList())
    }

    private fun persist() = store.save(items.toList())

    private companion object {
        const val HISTORY_LIMIT = 12
    }
}