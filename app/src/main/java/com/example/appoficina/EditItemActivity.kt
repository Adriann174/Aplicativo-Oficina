package com.example.appoficina

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditItemActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var editPreco: EditText
    private lateinit var editEstoque: EditText
    private lateinit var btnCamera: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: ImageView

    private var currentPhotoPath: String? = null
    private var itemToEdit: Item? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PERMISSION_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        itemToEdit = intent.getSerializableExtra(EXTRA_ITEM) as? Item
        if (itemToEdit == null) {
            Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        populateFields()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        editNome = findViewById(R.id.editNome)
        editDescricao = findViewById(R.id.editDescricao)
        editPreco = findViewById(R.id.editPreco)
        editEstoque = findViewById(R.id.editEstoque)
        btnCamera = findViewById(R.id.btnCamera)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
    }

    private fun setupClickListeners() {
        btnCamera.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        btnSalvar.setOnClickListener {
            saveItem()
        }
    }

    private fun populateFields() {
        itemToEdit?.let { item ->
            editNome.setText(item.nome)
            editDescricao.setText(item.descricao)
            editPreco.setText(item.preco.toString())
            editEstoque.setText(item.estoque.toString())
            
            // Carregar imagem existente se houver
            item.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    imageView.setImageURI(Uri.fromFile(file))
                    currentPhotoPath = path
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_PERMISSION_CAMERA
        )
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir("Pictures")!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            currentPhotoPath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                imageView.setImageURI(imageUri)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveItem() {
        val nome = editNome.text.toString().trim()
        val descricao = editDescricao.text.toString().trim()
        val precoStr = editPreco.text.toString().trim()
        val estoqueStr = editEstoque.text.toString().trim()

        if (nome.isEmpty() || descricao.isEmpty() || precoStr.isEmpty() || estoqueStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val preco = precoStr.toDoubleOrNull()
        val estoque = estoqueStr.toIntOrNull()

        if (preco == null || estoque == null) {
            Toast.makeText(this, "Preço e estoque devem ser números válidos", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val updatedItem = itemToEdit?.copy(
            nome = nome,
            descricao = descricao,
            preco = preco,
            estoque = estoque,
            imagePath = currentPhotoPath ?: itemToEdit?.imagePath
        )

        val resultIntent = Intent().apply {
            putExtra(EXTRA_ITEM, updatedItem)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
    }
}
