package com.example.appoficina

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
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

            findViewById<Button>(R.id.btnEnviar).setOnClickListener {
                Toast.makeText(this, "Itens enviados para o setor!", Toast.LENGTH_SHORT).show()
                viewModel.limparCarrinho()
            }

            findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
                finish()
            }
        }

        // Obter BottomNavigationView do layout (ajuste R.id.bottom se necessário)
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.selectedItemId = R.id.nav_pedidos
        bottom.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_pedidos -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }

                else -> false
            }
        }
    }
}