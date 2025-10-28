package com.example.appoficina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

class CarrinhoAdapter(
    private val onRemoveClick: (Item) -> Unit,
    private val onIncrementClick: (Item) -> Unit,
    private val onDecrementClick: (Item) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.ViewHolder>() {

    private var itens = mutableListOf<Item>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.txtNomeCarrinho)
        val descricao: TextView = view.findViewById(R.id.txtDescricaoCarrinho)
        val img: ImageView = view.findViewById(R.id.imgCarrinho)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnIncrementar: ImageButton = view.findViewById(R.id.btnIncrementar)
        val btnDecrementar: ImageButton = view.findViewById(R.id.btnDecrementar)
        val txtQuantidade: TextView = view.findViewById(R.id.txtQuantidade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrinho, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.nome.text = item.nome
        holder.descricao.text = item.descricao
        holder.txtQuantidade.text = item.quantidade.toString()

        // carregar as imagens
        item.imagePath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                holder.img.setImageURI(android.net.Uri.fromFile(file))
            }
        }
        
        holder.btnDelete.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    onRemoveClick(item)
                }.start()
        }
        
        holder.btnIncrementar.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    onIncrementClick(item)
                }.start()
        }
        
        holder.btnDecrementar.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    onDecrementClick(item)
                }.start()
        }
    }

    override fun getItemCount() = itens.size

    fun updateList(novaLista: List<Item>) {
        itens = novaLista.toMutableList()
        notifyDataSetChanged()
    }
}


