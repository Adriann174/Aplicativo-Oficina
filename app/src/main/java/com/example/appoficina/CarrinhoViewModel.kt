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
}


