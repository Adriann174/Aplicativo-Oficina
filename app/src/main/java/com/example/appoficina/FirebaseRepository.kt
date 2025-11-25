package com.example.appoficina

import android.content.Context
import android.provider.Settings
import com.google.firebase.database.*

object FirebaseRepository {

    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("items")

    fun setDeviceScope(context: Context) {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        dbRef = FirebaseDatabase.getInstance().getReference("items").child(androidId ?: "unknown")
    }

    // Classe de handle para remover listener, semelhante ao ListenerRegistration
    class RealtimeListenerHandle(
        private val ref: DatabaseReference,
        private val listener: ValueEventListener
    ) {
        fun remove() {
            ref.removeEventListener(listener)
        }
    }

    // Salvar item (não usado atualmente nas telas, mantido para compatibilidade)
    fun salvarItem(item: Item, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val key = dbRef.push().key
        if (key == null) {
            onFailure(IllegalStateException("Falha ao gerar chave"))
            return
        }
        val dados = mapOf(
            "id" to item.id,
            "nome" to item.nome,
            "descricao" to item.descricao,
            "fotoUrl" to (item.imagePath ?: ""),
            "quantidade" to item.quantidade,
            "codigo" to (item.barcode ?: "")
        )
        dbRef.child(key).setValue(dados)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Buscar todos os itens (Realtime Database)
    fun buscarTodosItens(onSuccess: (List<Item>) -> Unit, onFailure: (Exception) -> Unit) {
        dbRef.get()
            .addOnSuccessListener { snapshot ->
                val items = mutableListOf<Item>()
                snapshot.children.forEach { child ->
                    val id = (child.child("id").getValue(Long::class.java) ?: 0L).toInt()
                    val nome = child.child("nome").getValue(String::class.java) ?: ""
                    val descricao = child.child("descricao").getValue(String::class.java) ?: ""
                    val fotoUrl = child.child("fotoUrl").getValue(String::class.java)
                    val quantidade = (child.child("quantidade").getValue(Long::class.java) ?: 1L).toInt()
                    val codigo = child.child("codigo").getValue(String::class.java)
                    items.add(
                        Item(
                            id = id,
                            nome = nome,
                            descricao = descricao,
                            imagePath = fotoUrl,
                            quantidade = quantidade,
                            barcode = codigo
                        )
                    )
                }
                onSuccess(items)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Atualizar item existente (via consulta por id)
    fun atualizarItem(item: Item, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val query = dbRef.orderByChild("id").equalTo(item.id.toDouble()).limitToFirst(1)
        query.get()
            .addOnSuccessListener { snap ->
                val node = snap.children.firstOrNull()
                if (node != null) {
                    val dados = mapOf(
                        "id" to item.id,
                        "nome" to item.nome,
                        "descricao" to item.descricao,
                        "fotoUrl" to (item.imagePath ?: ""),
                        "quantidade" to item.quantidade,
                        "codigo" to (item.barcode ?: "")
                    )
                    node.ref.setValue(dados)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e) }
                } else {
                    // Fallback sem índice: varre todos os itens e atualiza pelo id
                    dbRef.get()
                        .addOnSuccessListener { allSnap ->
                            val target = allSnap.children.firstOrNull { child ->
                                (child.child("id").getValue(Long::class.java) ?: 0L).toInt() == item.id
                            }
                            if (target != null) {
                                val dados = mapOf(
                                    "id" to item.id,
                                    "nome" to item.nome,
                                    "descricao" to item.descricao,
                                    "fotoUrl" to (item.imagePath ?: ""),
                                    "quantidade" to item.quantidade,
                                    "codigo" to (item.barcode ?: "")
                                )
                                target.ref.setValue(dados)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e -> onFailure(e) }
                            } else {
                                onFailure(IllegalStateException("Item não encontrado"))
                            }
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
            .addOnFailureListener {
                // Em caso de erro (ex.: índice não definido), tenta fallback
                dbRef.get()
                    .addOnSuccessListener { allSnap ->
                        val target = allSnap.children.firstOrNull { child ->
                            (child.child("id").getValue(Long::class.java) ?: 0L).toInt() == item.id
                        }
                        if (target != null) {
                            val dados = mapOf(
                                "id" to item.id,
                                "nome" to item.nome,
                                "descricao" to item.descricao,
                                "fotoUrl" to (item.imagePath ?: ""),
                                "quantidade" to item.quantidade,
                                "codigo" to (item.barcode ?: "")
                            )
                            target.ref.setValue(dados)
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { e -> onFailure(e) }
                        } else {
                            onFailure(IllegalStateException("Item não encontrado"))
                        }
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
    }

    // Deletar item por id
    fun deletarItem(itemId: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val query = dbRef.orderByChild("id").equalTo(itemId.toDouble()).limitToFirst(1)
        query.get()
            .addOnSuccessListener { snap ->
                val node = snap.children.firstOrNull()
                if (node != null) {
                    node.ref.removeValue()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e) }
                } else {
                    // Fallback sem índice
                    dbRef.get()
                        .addOnSuccessListener { allSnap ->
                            val target = allSnap.children.firstOrNull { child ->
                                (child.child("id").getValue(Long::class.java) ?: 0L).toInt() == itemId
                            }
                            if (target != null) {
                                target.ref.removeValue()
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e -> onFailure(e) }
                            } else {
                                onFailure(IllegalStateException("Item não encontrado"))
                            }
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
            .addOnFailureListener {
                // Em caso de erro (ex.: índice não definido), tenta fallback
                dbRef.get()
                    .addOnSuccessListener { allSnap ->
                        val target = allSnap.children.firstOrNull { child ->
                            (child.child("id").getValue(Long::class.java) ?: 0L).toInt() == itemId
                        }
                        if (target != null) {
                            target.ref.removeValue()
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { e -> onFailure(e) }
                        } else {
                            onFailure(IllegalStateException("Item não encontrado"))
                        }
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
    }

    // Deletar TODOS os itens
    fun deletarTodosItens(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        dbRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Observar itens em tempo real
    fun observarItens(onChange: (List<Item>) -> Unit, onError: (Exception) -> Unit): RealtimeListenerHandle {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                snapshot.children.forEach { child ->
                    val id = (child.child("id").getValue(Long::class.java) ?: 0L).toInt()
                    val nome = child.child("nome").getValue(String::class.java) ?: ""
                    val descricao = child.child("descricao").getValue(String::class.java) ?: ""
                    val fotoUrl = child.child("fotoUrl").getValue(String::class.java)
                    val quantidade = (child.child("quantidade").getValue(Long::class.java) ?: 1L).toInt()
                    val codigo = child.child("codigo").getValue(String::class.java)
                    items.add(
                        Item(
                            id = id,
                            nome = nome,
                            descricao = descricao,
                            imagePath = fotoUrl,
                            quantidade = quantidade,
                            barcode = codigo
                        )
                    )
                }
                onChange(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        return RealtimeListenerHandle(dbRef, listener)
    }

    // Buscar item por código de barras
    fun buscarItemPorBarcode(barcode: String, onSuccess: (Item?) -> Unit, onFailure: (Exception) -> Unit) {
        val query = dbRef.orderByChild("codigo").equalTo(barcode).limitToFirst(1)
        query.get()
            .addOnSuccessListener { snap ->
                val child = snap.children.firstOrNull()
                if (child != null) {
                    val id = (child.child("id").getValue(Long::class.java) ?: 0L).toInt()
                    val nome = child.child("nome").getValue(String::class.java) ?: ""
                    val descricao = child.child("descricao").getValue(String::class.java) ?: ""
                    val fotoUrl = child.child("fotoUrl").getValue(String::class.java)
                    val quantidade = (child.child("quantidade").getValue(Long::class.java) ?: 1L).toInt()
                    val codigo = child.child("codigo").getValue(String::class.java)
                    onSuccess(
                        Item(
                            id = id,
                            nome = nome,
                            descricao = descricao,
                            imagePath = fotoUrl,
                            quantidade = quantidade,
                            barcode = codigo
                        )
                    )
                } else {
                    // Fallback sem índice
                    dbRef.get()
                        .addOnSuccessListener { allSnap ->
                            val match = allSnap.children.firstOrNull { c ->
                                c.child("codigo").getValue(String::class.java) == barcode
                            }
                            if (match != null) {
                                val id = (match.child("id").getValue(Long::class.java) ?: 0L).toInt()
                                val nome = match.child("nome").getValue(String::class.java) ?: ""
                                val descricao = match.child("descricao").getValue(String::class.java) ?: ""
                                val fotoUrl = match.child("fotoUrl").getValue(String::class.java)
                                val quantidade = (match.child("quantidade").getValue(Long::class.java) ?: 1L).toInt()
                                val codigo = match.child("codigo").getValue(String::class.java)
                                onSuccess(
                                    Item(
                                        id = id,
                                        nome = nome,
                                        descricao = descricao,
                                        imagePath = fotoUrl,
                                        quantidade = quantidade,
                                        barcode = codigo
                                    )
                                )
                            } else {
                                onSuccess(null)
                            }
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
            .addOnFailureListener {
                // Em caso de erro (ex.: índice não definido), tenta fallback
                dbRef.get()
                    .addOnSuccessListener { allSnap ->
                        val match = allSnap.children.firstOrNull { c ->
                            c.child("codigo").getValue(String::class.java) == barcode
                        }
                        if (match != null) {
                            val id = (match.child("id").getValue(Long::class.java) ?: 0L).toInt()
                            val nome = match.child("nome").getValue(String::class.java) ?: ""
                            val descricao = match.child("descricao").getValue(String::class.java) ?: ""
                            val fotoUrl = match.child("fotoUrl").getValue(String::class.java)
                            val quantidade = (match.child("quantidade").getValue(Long::class.java) ?: 1L).toInt()
                            val codigo = match.child("codigo").getValue(String::class.java)
                            onSuccess(
                                Item(
                                    id = id,
                                    nome = nome,
                                    descricao = descricao,
                                    imagePath = fotoUrl,
                                    quantidade = quantidade,
                                    barcode = codigo
                                )
                            )
                        } else {
                            onSuccess(null)
                        }
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
    }
}
