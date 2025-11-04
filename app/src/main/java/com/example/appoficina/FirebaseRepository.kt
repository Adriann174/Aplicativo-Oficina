package com.example.appoficina

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ListenerRegistration

object FirebaseRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_ITEMS = "items"
    
    // Salvar item no Firebase
    fun salvarItem(item: Item, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemData = hashMapOf(
            "id" to item.id,
            "nome" to item.nome,
            "descricao" to item.descricao,
            "imagePath" to item.imagePath,
            "quantidade" to item.quantidade
        )
        
        db.collection(COLLECTION_ITEMS)
            .document(item.id.toString())
            .set(itemData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    // Buscar todos os itens
    fun buscarTodosItens(onSuccess: (List<Item>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(COLLECTION_ITEMS)
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val item = Item(
                        id = document.getLong("id")?.toInt() ?: 0,
                        nome = document.getString("nome") ?: "",
                        descricao = document.getString("descricao") ?: "",
                        imagePath = document.getString("imagePath"),
                        quantidade = document.getLong("quantidade")?.toInt() ?: 1
                    )
                    items.add(item)
                }
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    // Atualizar item existente
    fun atualizarItem(item: Item, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        salvarItem(item, onSuccess, onFailure)
    }
    
    // Deletar item
    fun deletarItem(itemId: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(COLLECTION_ITEMS)
            .document(itemId.toString())
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Deletar TODOS os itens da coleção
    fun deletarTodosItens(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(COLLECTION_ITEMS)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Observar itens em tempo real
    fun observarItens(onChange: (List<Item>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection(COLLECTION_ITEMS)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Item>()
                snapshot?.documents?.forEach { document ->
                    val item = Item(
                        id = document.getLong("id")?.toInt() ?: 0,
                        nome = document.getString("nome") ?: "",
                        descricao = document.getString("descricao") ?: "",
                        imagePath = document.getString("imagePath"),
                        quantidade = document.getLong("quantidade")?.toInt() ?: 1
                    )
                    items.add(item)
                }
                onChange(items)
            }
    }
}