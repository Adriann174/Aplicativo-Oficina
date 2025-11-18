package com.example.appoficina

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.jvm.java


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var adapter: EstoqueAdapter
    private var itemsListener: ListenerRegistration? = null
    private lateinit var btnMenu: Button
    private lateinit var imgRemoverTudo: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgRemoverTudo = findViewById<ImageButton>(R.id.imgRemoverTudo)

        viewModel = ViewModelProvider(this).get(CarrinhoViewModel::class.java)

        val listaItens = mutableListOf<Item>()


        adapter = EstoqueAdapter(listaItens, onAddClick = { item ->
            viewModel.adicionarItem(item)
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
        adapter.setHasStableIds(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.adapter = adapter

        // Swipe para direita para excluir item do estoque
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getItemAt(position)
                FirebaseRepository.deletarItem(
                    item.id,
                    onSuccess = {
                        adapter.removeById(item.id)
                        Toast.makeText(
                            this@MainActivity,
                            "Excluído: ${item.nome}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@MainActivity,
                            "Erro ao excluir: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        adapter.notifyDataSetChanged()
                    }
                )
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        // Carregar dados do Firebase
        carregarDadosFirebase()

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

        // Remover todos os itens do estoque
        imgRemoverTudo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Remover tudo")
                .setMessage("Tem certeza que deseja apagar todos os itens?")
                .setPositiveButton("Apagar") { _, _ ->
                    FirebaseRepository.deletarTodosItens(
                        onSuccess = {
                            Toast.makeText(this, "Todos os itens foram removidos", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(this, "Erro ao remover todos: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
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


    override fun onStart() {
        super.onStart()
        // Observa atualizações em tempo real
        itemsListener = FirebaseRepository.observarItens(
            onChange = { items ->
                adapter.updateList(items.toMutableList())
            },
            onError = { e ->
                Toast.makeText(this, "Erro em tempo real: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onStop() {
        super.onStop()
        itemsListener?.remove()
        itemsListener = null
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

    private fun carregarDadosFirebase() {
        FirebaseRepository.buscarTodosItens(
            onSuccess = { items ->
                adapter.updateList(items.toMutableList())
                Toast.makeText(this, "Dados carregados com sucesso!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Erro ao carregar dados: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
    

    companion object {
        private const val REQ_ADD_ITEM = 2001
        private const val REQ_EDIT_ITEM = 2002
    }
}