package com.example.appoficina

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.badge.BadgeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var adapter: EstoqueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)


        viewModel = ViewModelProvider(this).get(CarrinhoViewModel::class.java)

        val listaItens = mutableListOf<Item>()


        adapter = EstoqueAdapter(listaItens, onAddClick = { item ->
            viewModel.adicionarItem(item)
            Toast.makeText(this, "${item.nome} adicionado ao carrinho", Toast.LENGTH_SHORT).show()
        }, onItemClick = { item ->
            val intent = Intent(this, DetalhesActivity::class.java)
            intent.putExtra(DetalhesActivity.EXTRA_ITEM, item)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, onEditClick = { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra(EditItemActivity.EXTRA_ITEM, item)
            startActivityForResult(intent, REQ_EDIT_ITEM)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        })

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerEstoque)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        val cartBadge: BadgeDrawable = bottom.getOrCreateBadge(R.id.nav_pedidos)
        cartBadge.isVisible = false
        viewModel.itensCarrinho.observe(this) { itens ->
            val count = itens.size
            if (count > 0) {
                cartBadge.number = count
                cartBadge.isVisible = true
            } else {
                cartBadge.clearNumber()
                cartBadge.isVisible = false
            }
        }

        val search = findViewById<EditText>(R.id.searchBar)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<Button>(R.id.btnAdicionar).setOnClickListener {
            startActivityForResult(Intent(this, AddItemActivity::class.java), REQ_ADD_ITEM)
        }

        bottom.selectedItemId = R.id.nav_home
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, CarrinhoActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }

                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ADD_ITEM && resultCode == RESULT_OK && data != null) {
            val item = data.getSerializableExtra(AddItemActivity.EXTRA_ITEM) as Item
            adapter.addItem(item)
            Toast.makeText(this, "Item adicionado ao estoque", Toast.LENGTH_SHORT).show()
        } else if (requestCode == REQ_EDIT_ITEM && resultCode == RESULT_OK && data != null) {
            val item = data.getSerializableExtra(EditItemActivity.EXTRA_ITEM) as Item
            adapter.updateItem(item)
            Toast.makeText(this, "Item atualizado com sucesso", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQ_ADD_ITEM = 2001
        private const val REQ_EDIT_ITEM = 2002
    }
}