package com.example.appoficina

import java.io.Serializable

data class Item(
    val id: Int,
    val nome: String,
    val descricao: String,
    val imagePath: String?,
    val preco: Double,
    val estoque: Int
) : Serializable


