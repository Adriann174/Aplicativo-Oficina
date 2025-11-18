package com.example.appoficina

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddItemActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var btnCamera: Button
    private lateinit var btnGaleria: Button
    private lateinit var btnSalvar: Button
    private lateinit var inputNomeLayout: TextInputLayout
    private lateinit var btnVoltar: ImageView
    private var currentPhotoPath: String? = null
    private var currentBarcode: String? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2
    private val REQUEST_PERMISSION_CAMERA = 100
    private val REQUEST_PERMISSION_STORAGE = 101
    private val REQUEST_SCAN_BARCODE = 301

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        editNome = findViewById(R.id.editNome)
        editDescricao = findViewById(R.id.editDescricao)
        btnCamera = findViewById(R.id.btnCamera)
        btnGaleria = findViewById(R.id.btnGaleria)
        inputNomeLayout = findViewById(R.id.inputNomeLayout)
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

        btnGaleria.setOnClickListener {
            if (checkStoragePermission()) {
                dispatchGalleryIntent()
            } else {
                requestStoragePermission()
            }
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        inputNomeLayout.setEndIconOnClickListener {
            Toast.makeText(this, "Abrindo scanner...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ScanBarcodeActivity::class.java)
            startActivityForResult(intent, REQUEST_SCAN_BARCODE)
            requestCameraPermission()
        }

        btnSalvar.setOnClickListener {
            saveItem()
            finish()
        }
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_STORAGE)
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

    private fun dispatchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
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

    private fun copyImageToAppDirectory(sourceUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File = getExternalFilesDir("Pictures")!!
            val outputFile = File(storageDir, "JPEG_${timeStamp}.jpg")

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            currentPhotoPath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                imageView.setImageURI(imageUri)
            }
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                // Copiar imagem da galeria para o diretório do app
                currentPhotoPath = copyImageToAppDirectory(uri)
                imageView.setImageURI(uri)
            }
        } else if (requestCode == REQUEST_SCAN_BARCODE && resultCode == RESULT_OK) {
            val raw = data?.getStringExtra("barcode_raw")
            currentBarcode = raw
            // Mostrar o código como ajuda abaixo do campo de nome
            inputNomeLayout.helperText = if (!raw.isNullOrEmpty()) "Código: $raw" else null
            val scannedItem = data?.getSerializableExtra("barcode_item") as? Item
            if (scannedItem != null) {
                editNome.setText(scannedItem.nome)
                editDescricao.setText(scannedItem.descricao)
                Toast.makeText(this, "Produto encontrado pelo código", Toast.LENGTH_SHORT).show()
            } else if (!raw.isNullOrEmpty()) {
                // Buscar automaticamente no Firestore pelo código e preencher nome/descrição
                FirebaseRepository.buscarItemPorBarcode(
                    raw,
                    onSuccess = { item ->
                        if (item != null) {
                            editNome.setText(item.nome)
                            editDescricao.setText(item.descricao)
                            Toast.makeText(
                                this,
                                "Produto encontrado pelo código",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Código capturado: $raw", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this,
                            "Erro ao buscar produto: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else {
                Toast.makeText(this, "Nenhum código capturado", Toast.LENGTH_SHORT).show()
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

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }


        val item = Item(
            id = System.currentTimeMillis().toInt(),
            nome = nome,
            descricao = descricao,
            imagePath = currentPhotoPath,
            barcode = currentBarcode,
        )

        // Salvar no Firebase
        FirebaseRepository.salvarItem(
            item = item,
            onSuccess = {
                Toast.makeText(this, "Item salvo com sucesso!", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_ITEM, item)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Erro ao salvar: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
    }
}

