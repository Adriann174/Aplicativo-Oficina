package com.example.appoficina

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.io.File

class DetalhesActivity : AppCompatActivity() {

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var item: Item

    private lateinit var btnVoltar: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        viewModel = ViewModelProvider(this).get(CarrinhoViewModel::class.java)

        item = intent.getSerializableExtra(EXTRA_ITEM) as Item
        setupViews()
    }

    private fun setupViews() {
        val imageView = findViewById<ImageView>(R.id.imageView)
        val txtNome = findViewById<TextView>(R.id.txtNome)
        val txtDescricao = findViewById<TextView>(R.id.txtDescricao)
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)


        // Carregar imagem se existir
        item.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                imageView.setImageURI(android.net.Uri.fromFile(file))
            }
        }

        txtNome.text = item.nome
        txtDescricao.text = item.descricao
        btnAdicionar.setOnClickListener {
            viewModel.adicionarItem(item)
            Toast.makeText(this, "Item adicionado ao carrinho", Toast.LENGTH_SHORT).show()
        }
        btnVoltar.setOnClickListener {
            finish()

        }
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
    }
}
