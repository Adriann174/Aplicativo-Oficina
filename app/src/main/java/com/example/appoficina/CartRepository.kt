package com.example.appoficina

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CartRepository {
    private val _items = MutableLiveData<MutableList<Item>>(mutableListOf())
    val items: LiveData<MutableList<Item>> = _items

    fun add(item: Item) {
        val current = _items.value ?: mutableListOf()
        
        // Verificar se o item já existe no carrinho (mesmo ID)
        val existingItem = current.find { it.id == item.id }
        
        if (existingItem != null) {
            // Se o item já existe, incrementar a quantidade
            existingItem.quantidade++
        } else {
            // Se é um item novo, adicionar com quantidade 1
            current.add(item.copy(quantidade = 1))
        }
        
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
    
    fun incrementarQuantidade(item: Item) {
        val current = _items.value ?: return
        val existingItem = current.find { it.id == item.id }
        existingItem?.let {
            it.quantidade++
            _items.value = current
        }
    }
    
    fun decrementarQuantidade(item: Item) {
        val current = _items.value ?: return
        val existingItem = current.find { it.id == item.id }
        existingItem?.let {
            if (it.quantidade > 1) {
                it.quantidade--
            } else {
                // Se quantidade for 1, remover o item completamente
                current.remove(it)
            }
            _items.value = current
        }
    }
}


