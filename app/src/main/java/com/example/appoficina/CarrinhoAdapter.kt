package com.example.appoficina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import coil.load
import androidx.recyclerview.widget.DiffUtil

class CarrinhoAdapter(
    private val onRemoveClick: (Item) -> Unit,
    private val onIncrementClick: (Item) -> Unit,
    private val onDecrementClick: (Item) -> Unit,
    private val onQuantidadeDigitada: (Item, Int) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.ViewHolder>() {

    private var itens = mutableListOf<Item>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.txtNomeCarrinho)
        val descricao: TextView = view.findViewById(R.id.txtDescricaoCarrinho)
        val img: ImageView = view.findViewById(R.id.imgCarrinho)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnIncrementar: ImageButton = view.findViewById(R.id.btnIncrementar)
        val btnDecrementar: ImageButton = view.findViewById(R.id.btnDecrementar)
        val txtQuantidade: android.widget.TextView = view.findViewById(R.id.edtQuantidade)
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

        holder.txtQuantidade.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!holder.txtQuantidade.hasFocus()) return
                val q = s?.toString()?.trim()?.toIntOrNull()
                if (q != null && q > 0 && q != item.quantidade) {
                    onQuantidadeDigitada(item, q)
                }
            }
        })

        item.imagePath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                holder.img.load(file) {
                    placeholder(R.drawable.ic_image)
                    crossfade(true)
                }
            } else {
                holder.img.setImageResource(R.drawable.ic_image)
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

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = itens.size

    override fun getItemId(position: Int): Long = itens[position].id.toLong()

    fun updateList(novaLista: List<Item>) {
        val diff = DiffUtil.calculateDiff(SimpleDiff(itens, novaLista))
        itens = novaLista.toMutableList()
        diff.dispatchUpdatesTo(this)
    }

    private class SimpleDiff(
        private val old: List<Item>,
        private val new: List<Item>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].id == new[newItemPosition].id
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]
    }
}


