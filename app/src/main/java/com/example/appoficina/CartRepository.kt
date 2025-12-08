package com.example.appoficina

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CartRepository {
    private val _items = MutableLiveData<MutableList<Item>>(mutableListOf())
    val items: LiveData<MutableList<Item>> = _items

    fun add(item: Item) {
        val current = _items.value ?: mutableListOf()
        val existingItem = current.find { it.id == item.id }

        val newList = if (existingItem != null) {
            current.map { i ->
                if (i.id == item.id) i.copy(quantidade = i.quantidade + 1) else i
            }.toMutableList()
        } else {
            current.toMutableList().apply { add(item.copy(quantidade = 1)) }
        }

        _items.value = newList
    }

    fun remove(item: Item) {
        val current = _items.value ?: return
        val newList = current.filter { it.id != item.id }.toMutableList()
        _items.value = newList
    }

    fun clear() {
        _items.value = mutableListOf()
    }

    fun incrementarQuantidade(item: Item) {
        val current = _items.value ?: return
        val newList = current.map { i ->
            if (i.id == item.id) i.copy(quantidade = i.quantidade + 1) else i
        }.toMutableList()
        _items.value = newList
    }

    fun decrementarQuantidade(item: Item) {
        val current = _items.value ?: return
        val newList = current.flatMap { i ->
            if (i.id == item.id) {
                if (i.quantidade > 1) listOf(i.copy(quantidade = i.quantidade - 1)) else emptyList()
            } else listOf(i)
        }.toMutableList()
        _items.value = newList
    }

    fun atualizarQuantidade(item: Item, novaQuantidade: Int) {
        val current = _items.value ?: return
        val q = if (novaQuantidade < 1) 1 else novaQuantidade
        val newList = current.map { i ->
            if (i.id == item.id) i.copy(quantidade = q) else i
        }.toMutableList()
        _items.value = newList
    }

    // Atualiza nome, descrição e imagePath para o item já presente no carrinho, preservando quantidade
    fun atualizarDetalhes(itemAtualizado: Item) {
        val current = _items.value ?: return
        val newList = current.map { i ->
            if (i.id == itemAtualizado.id) i.copy(
                nome = itemAtualizado.nome,
                descricao = itemAtualizado.descricao,
                imagePath = itemAtualizado.imagePath
            ) else i
        }.toMutableList()
        _items.value = newList
    }
}


