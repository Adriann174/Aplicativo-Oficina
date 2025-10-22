package com.example.appoficina

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var adapter: CarrinhoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrinho)

        viewModel = ViewModelProvider(this).get(CarrinhoViewModel::class.java)

        val recycler = findViewById<RecyclerView>(R.id.recyclerCarrinho)
        adapter = CarrinhoAdapter(
            onRemoveClick = { item ->
                viewModel.removerItem(item)
            },
            onIncrementClick = { item ->
                viewModel.incrementarQuantidade(item)
            },
            onDecrementClick = { item ->
                viewModel.decrementarQuantidade(item)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.itensCarrinho.observe(this) { itens ->
            adapter.updateList(itens)
            val total = itens.sumOf { it.preco * it.quantidade }
            findViewById<TextView>(R.id.txtTotal).text = String.format("Total: R$ %.2f", total)
        }

        findViewById<Button>(R.id.btnEnviar).setOnClickListener {
            Toast.makeText(this, "Itens enviados para o setor!", Toast.LENGTH_SHORT).show()
            viewModel.limparCarrinho()
        }

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }
    }
}


