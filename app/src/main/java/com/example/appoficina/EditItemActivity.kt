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
import coil.load
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditItemActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var btnCamera: Button
    private lateinit var btnGaleria: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: ImageView

    private var currentPhotoPath: String? = null
    private var itemToEdit: Item? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2
    private val REQUEST_PERMISSION_CAMERA = 100
    private val REQUEST_PERMISSION_STORAGE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        FirebaseRepository.setDeviceScope(this)

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
        btnCamera = findViewById(R.id.btnCamera)
        btnGaleria = findViewById(R.id.btnGaleria)
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

        this.btnSalvar.setOnClickListener {
            saveItem()
            finish()
        }
    }

    private fun populateFields() {
        itemToEdit?.let { item ->
            editNome.setText(item.nome)
            editDescricao.setText(item.descricao)

            // Carregar imagem existente se houver, suportando file://, content:// e http(s)
            item.imagePath?.let { path ->
                when {
                    path.startsWith("http://") || path.startsWith("https://") -> {
                        imageView.load(path) { placeholder(R.drawable.ic_image); crossfade(true) }
                        currentPhotoPath = path
                    }
                    path.startsWith("content://") || path.startsWith("file://") -> {
                        imageView.load(Uri.parse(path)) { placeholder(R.drawable.ic_image); crossfade(true) }
                        currentPhotoPath = path
                    }
                    else -> {
                        val file = File(path)
                        if (file.exists()) {
                            imageView.load(file) { placeholder(R.drawable.ic_image); crossfade(true) }
                            currentPhotoPath = path
                        }
                    }
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_STORAGE)
    }

    private fun dispatchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,REQUEST_IMAGE_GALLERY)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA
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
                        this, "${packageName}.fileprovider", it
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
                val file = File(path)
                imageView.load(file) { placeholder(R.drawable.ic_image); crossfade(true) }
            }
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val copiedImagePath = copyImageToAppDirectory(uri)
                if (copiedImagePath != null) {
                    currentPhotoPath = copiedImagePath
                    imageView.load(Uri.parse(copiedImagePath)) { placeholder(R.drawable.ic_image); crossfade(true) }
                }
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
        } else if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchGalleryIntent()
            } else {
                Toast.makeText(this, "Permissão de armazenamento negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyImageToAppDirectory(sourceUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir("Pictures"), "JPEG_${timeStamp}.jpg")
            
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao copiar imagem", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun saveItem() {
        val nome = editNome.text.toString().trim()
        val descricao = editDescricao.text.toString().trim()

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedItem = itemToEdit?.copy(
            nome = nome,
            descricao = descricao,
            imagePath = currentPhotoPath ?: itemToEdit?.imagePath
        )

        if (updatedItem != null) {
            FirebaseRepository.atualizarItem(updatedItem,
                onSuccess = {
                    Toast.makeText(this, "Item atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    // Sincronizar o item atualizado com o carrinho (se presente)
                    CartRepository.atualizarDetalhes(updatedItem)
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_ITEM, updatedItem)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Erro ao atualizar item: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
    }
}
