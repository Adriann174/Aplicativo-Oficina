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
import android.app.AlertDialog
import android.net.Uri
import androidx.core.content.FileProvider
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import com.google.firebase.FirebaseApp
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                if (itens.isEmpty()) {
                    Toast.makeText(this, "Carrinho vazio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                AlertDialog.Builder(this)
                    .setTitle("Enviar para o setor")
                    .setItems(arrayOf("Planilha (CSV)", "PDF")) { _, which ->
                        when (which) {
                            0 -> {
                                val file = exportCartToCsv(itens)
                                shareFileByEmail(file, "text/csv")
                            }
                            1 -> {
                                val file = exportCartToPdf(itens)
                                shareFileByEmail(file, "application/pdf")
                            }
                        }
                    }
                    .show()
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
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun exportCartToCsv(items: List<Item>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, "pedido_${timestamp}.csv")
        dir?.mkdirs()

        FileOutputStream(file).use { fos ->
            fos.write("ID,Nome,Descricao,Estoque,Quantidade\n".toByteArray())
            items.forEach { item ->
                val nome = item.nome.replace('"', '\'')
                val descricao = item.descricao.replace('"', '\'')
                val line = "${item.id},\"$nome\",\"$descricao\"${item.quantidade}\n"
                fos.write(line.toByteArray())
            }
        }
        return file
    }

    private fun exportCartToPdf(items: List<Item>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, "pedido_${timestamp}.pdf")
        dir?.mkdirs()

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 approx in points
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        var y = 40f
        canvas.drawText("Pedido - ${timestamp}", 40f, y, paint)
        y += 20f
        canvas.drawText("ID | Nome | Descrição | Quantidade", 40f, y, paint)
        y += 20f

        items.forEach { item ->
            val line = "${item.id} | ${item.nome} | ${item.descricao} | ${item.quantidade}"
            canvas.drawText(line, 40f, y, paint)
            y += 18f
        }

        pdf.finishPage(page)
        FileOutputStream(file).use { fos -> pdf.writeTo(fos) }
        pdf.close()
        return file
    }

    private fun shareFileByEmail(file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_EMAIL, arrayOf("recupadrianalvesbrito5@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Orçamenento de Produtos - Oficina")
            putExtra(Intent.EXTRA_TEXT, "Olá, venho por meio deste e-mail enviar o orçamento dos produtos que estão em falta no estoque.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, "Enviar para o setor"))
    }



}