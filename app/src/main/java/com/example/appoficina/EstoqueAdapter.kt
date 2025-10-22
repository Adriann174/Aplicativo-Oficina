package com.example.appoficina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstoqueAdapter(
    private val originalItens: MutableList<Item>,
    private val onAddClick: (Item) -> Unit,
    private val onItemClick: (Item) -> Unit,
    private val onEditClick: (Item) -> Unit
) : RecyclerView.Adapter<EstoqueAdapter.ViewHolder>() {

    private var filteredItens: List<Item> = originalItens

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.txtNomeItem)
        val descricao: TextView = view.findViewById(R.id.txtDescricao)
        val preco: TextView = view.findViewById(R.id.txtPreco)
        val img: ImageView = view.findViewById(R.id.imgThumb)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_estoque, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItens[position]
        holder.nome.text = item.nome
        holder.descricao.text = item.descricao
        holder.preco.text = String.format("R$ %.2f", item.preco)
        
        // Carregar imagem se existir
        item.imagePath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                holder.img.setImageURI(android.net.Uri.fromFile(file))
            }
        }
        
        holder.btnAdd.setOnClickListener { onAddClick(item) }
        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = filteredItens.size

    fun filter(query: String?) {
        val q = query?.trim()?.lowercase() ?: ""
        filteredItens = if (q.isEmpty()) {
            originalItens
        } else {
            originalItens.filter { it.nome.lowercase().contains(q) }
        }
        notifyDataSetChanged()
    }

    fun addItem(item: Item) {
        originalItens.add(item)
        filteredItens = originalItens
        notifyDataSetChanged()
    }

    fun updateItem(updatedItem: Item) {
        val index = originalItens.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            originalItens[index] = updatedItem
            filteredItens = originalItens
            notifyDataSetChanged()
        }
    }
}


