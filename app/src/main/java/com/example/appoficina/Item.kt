package com.example.appoficina

import java.io.Serializable

data class Item(
    val id: Int,
    val nome: String,
    val descricao: String,
    val imagePath: String?,
    var quantidade: Int = 1,
    val barcode: String? = null
) : Serializable
data class PedidoRequest(
    val produtos: List<Item>
)

data class PedidoResponse(
    val success: Boolean,
    val message: String
)

data class ListaPedidosResponse(
    val produtos: List<Item>
)

data class StatusUpdateRequest(
    val status: String
)
