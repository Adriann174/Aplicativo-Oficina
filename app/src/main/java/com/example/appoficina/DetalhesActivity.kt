package com.example.appoficina

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import java.io.File
import coil.load
import android.net.Uri

class DetalhesActivity : AppCompatActivity() {

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var item: Item
    private lateinit var chipBarcode: Chip
    private lateinit var barcodeActivity: ScanBarcodeActivity

    private lateinit var btnVoltar: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        viewModel = ViewModelProvider(this).get(CarrinhoViewModel::class.java)

        item = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_ITEM, Item::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_ITEM) as Item
        }
        setupViews()
    }

    private fun setupViews() {
        val imageView = findViewById<ImageView>(R.id.imageView)
        val txtNome = findViewById<TextView>(R.id.txtNome)
        val txtCodigo = findViewById<TextView>(R.id.chipBarcode)
        val txtDescricao = findViewById<TextView>(R.id.txtDescricao)
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)


        // Carregar imagem com suporte a file://, content:// e http(s)
        item.imagePath?.let { path ->
            when {
                path.startsWith("http://") || path.startsWith("https://") -> {
                    imageView.load(path) { placeholder(R.drawable.ic_image); crossfade(true) }
                }
                path.startsWith("content://") || path.startsWith("file://") -> {
                    imageView.load(Uri.parse(path)) { placeholder(R.drawable.ic_image); crossfade(true) }
                }
                else -> {
                    val file = File(path)
                    if (file.exists()) {
                        imageView.load(file) { placeholder(R.drawable.ic_image); crossfade(true) }
                    } else {
                        imageView.setImageResource(R.drawable.ic_image)
                    }
                }
            }
        }

        txtNome.text = item.nome
        txtDescricao.text = item.descricao
        txtCodigo.text = item.barcode
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
