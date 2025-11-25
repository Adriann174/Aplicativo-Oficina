package com.example.appoficina

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import android.widget.EditText
import android.widget.TextView
import android.media.MediaScannerConnection
import android.content.ClipData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.font.TextAttribute
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var bntVoltar: ImageButton
    private lateinit var imgRemoveTudo: ImageButton
    private lateinit var txtQuantidade: TextView
    private lateinit var adapter: CarrinhoAdapter
    private var itemsListener: com.google.firebase.firestore.ListenerRegistration? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrinho)
        bntVoltar = findViewById<ImageButton>(R.id.btnVoltar)

        imgRemoveTudo = findViewById<ImageButton>(R.id.imgRemoveTudo)
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
            },
            onQuantidadeDigitada = { item, quantidade ->
                viewModel.atualizarQuantidade(item, quantidade)
            }
        )
        adapter.setHasStableIds(true)

        bntVoltar.setOnClickListener {
            finish()
        }

        imgRemoveTudo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Remover tudo")
                .setMessage("Tem certeza que deseja limpar o carrinho?")
                .setPositiveButton("Limpar") { _, _ ->
                    viewModel.limparCarrinho()
                    Toast.makeText(this, "Carrinho limpo", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null

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
                                lifecycleScope.launch {
                                    Toast.makeText(this@CarrinhoActivity, "Gerando CSV...", Toast.LENGTH_SHORT).show()
                                    val file = withContext(Dispatchers.IO) { exportCartToCsv(itens) }
                                    MediaScannerConnection.scanFile(
                                        this@CarrinhoActivity,
                                        arrayOf(file.absolutePath),
                                        arrayOf("text/csv")
                                    ) { _, _ -> runOnUiThread { shareFileByEmail(file, "text/csv") } }
                                }
                            }
                            1 -> {
                                lifecycleScope.launch {
                                    Toast.makeText(this@CarrinhoActivity, "Gerando PDF...", Toast.LENGTH_SHORT).show()
                                    val file = withContext(Dispatchers.IO) { exportCartToPdf(itens) }
                                    MediaScannerConnection.scanFile(
                                        this@CarrinhoActivity,
                                        arrayOf(file.absolutePath),
                                        arrayOf("application/pdf")
                                    ) { _, _ -> runOnUiThread { shareFileByEmail(file, "application/pdf") } }
                                }
                            }
                        }
                    }
                    .show()
            }
        }

        // Obter BottomNavigationView do layout (ajuste R.id.bottom se necessário)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNavigationView.selectedItemId = R.id.nav_pedidos
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
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

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        itemsListener?.remove()
        itemsListener = null
    }

    private fun exportCartToCsv(items: List<Item>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: cacheDir
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "pedido_${timestamp}.csv")

        file.bufferedWriter().use { bw ->
            bw.write("Id, Nome,Descricao,Quantidade\n")
            items.forEach { item ->
                val nome = item.nome.replace('"', '\'')
                val descricao = item.descricao.replace('"', '\'')
                val line = "${item.barcode},\"$nome\",\"$descricao\",${item.quantidade}\n"
                bw.write(line)
            }
        }
        return file
    }

    private fun exportCartToPdf(items: List<Item>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: cacheDir
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "pedido_${timestamp}.pdf")

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
        canvas.drawText("Id | Nome | Descrição | Quantidade", 40f, y, paint)
        y += 20f

        items.forEach { item ->
            val line = "" +
                    "${item.barcode} | ${item.nome} | ${item.descricao} | ${item.quantidade}"
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
            putExtra(Intent.EXTRA_SUBJECT, "Orçamento de Produtos - Oficina")
            putExtra(Intent.EXTRA_TEXT, "Olá, venho por meio deste e-mail enviar o orçamento dos produtos que estão em falta no estoque.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("pedido", uri)
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, "Enviar para o setor"))
    }



}