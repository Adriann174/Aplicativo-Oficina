package com.example.appoficina

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class CarrinhoViewModel : ViewModel() {
    val itensCarrinho: LiveData<MutableList<Item>> = CartRepository.items

    fun adicionarItem(item: Item) {
        CartRepository.add(item)
    }

    fun removerItem(item: Item) {
        CartRepository.remove(item)
    }

    fun limparCarrinho() {
        CartRepository.clear()
    }

    fun incrementarQuantidade(item: Item) {
        CartRepository.incrementarQuantidade(item)
    }

    fun decrementarQuantidade(item: Item) {
        CartRepository.decrementarQuantidade(item)
    }

    fun atualizarQuantidade(item: Item, quantidade: Int) {
        CartRepository.atualizarQuantidade(item, quantidade)
    }
}


