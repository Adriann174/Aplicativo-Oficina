package com.example.appoficina

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CartRepository {
    private val _items = MutableLiveData<MutableList<Item>>(mutableListOf())
    val items: LiveData<MutableList<Item>> = _items

    fun add(item: Item) {
        val current = _items.value ?: mutableListOf()
        current.add(item)
        _items.value = current
    }

    fun remove(item: Item) {
        val current = _items.value ?: return
        current.remove(item)
        _items.value = current
    }

    fun clear() {
        _items.value = mutableListOf()
    }
}


