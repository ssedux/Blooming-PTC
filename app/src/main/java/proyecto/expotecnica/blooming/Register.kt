package proyecto.expotecnica.blooming

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.ImageDecoder
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import modelo.ClaseConexion
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.coroutines.resumeWithException

class Register : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100
    private val GALLERY_PERMISSION_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private val GALLERY_REQUEST_CODE = 103

    private var selectedImageUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var CampoCorreo: EditText
    private var Direccion_Descarga: String = "El usuario eligio la imagen por defecto" // Variable inicializada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Inicializar Firebase
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val CampoNombres = findViewById<EditText>(R.id.txt_Nombre_Registrer)
        val CampoApellidos = findViewById<EditText>(R.id.txt_Apellido_Registrer)
        val CampoUsuario = findViewById<EditText>(R.id.txt_Usuario_Registrer)
        val CampoTelefono = findViewById<EditText>(R.id.txt_Telefono_Registrer)
        CampoCorreo = findViewById(R.id.txt_Correo_Registrer)
        val CampoContrasena = findViewById<EditText>(R.id.txt_Contrasena_Registrer)
        val CampoConfirmarContra = findViewById<EditText>(R.id.txt_ConfirmarContra_Registrer)
        val Btn_Foto = findViewById<Button>(R.id.btn_foto_perfil_register)

        CampoNombres.requestFocus()

        Btn_Foto.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.imagen_register, null)

            dialogBuilder.setView(dialogView)
            val alertDialog = dialogBuilder.create()

            val btn_Seleccion_img = dialogView.findViewById<Button>(R.id.btn_subir_imagen_reg)
            val btn_Crear_Cuenta = dialogView.findViewById<Button>(R.id.btn_CrearCuenta_reg)
            val btn_Cancelar_reg = dialogView.findViewById<Button>(R.id.btn_cancelar_reg)
            imageView = dialogView.findViewById<ImageView>(R.id.ImgPerfil_reg)

            btn_Seleccion_img.setOnClickListener {
                val campoCorreoText = CampoCorreo.text.toString()
                if (campoCorreoText.isNotBlank()) {
                    val options = arrayOf<CharSequence>("Cámara", "Galería")
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Seleccionar Imagen")
                    builder.setItems(options) { dialog, which ->
                        when (which) {
                            0 -> openCamera()
                            1 -> openGallery()
                        }
                    }
                    builder.show()
                } else {
                    Toast.makeText(this, "Ingrese un correo válido antes de subir una imagen.", Toast.LENGTH_SHORT).show()
                }
            }

            btn_Crear_Cuenta.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    if (validarCamposRegistro(
                            this@Register,
                            CampoNombres,
                            CampoApellidos,
                            CampoUsuario,
                            CampoTelefono,
                            CampoCorreo,
                            CampoContrasena,
                            CampoConfirmarContra,
                            Btn_Foto
                        )
                    ) {
                        if (selectedImageUri != null) {
                            val bitmap = getBitmapFromUri(selectedImageUri!!)
                            if (bitmap != null) {
                                try {
                                    val url = withContext(Dispatchers.IO) {
                                        uploadImageToFirebase(bitmap, CampoCorreo.text.toString())
                                    }
                                    Direccion_Descarga = url
                                } catch (e: Exception) {
                                    Toast.makeText(this@Register, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        insertarDatosEnBaseDeDatos(
                            CampoNombres.text.toString(),
                            CampoApellidos.text.toString(),
                            CampoUsuario.text.toString(),
                            CampoTelefono.text.toString(),
                            CampoCorreo.text.toString(),
                            CampoContrasena.text.toString()
                        )

                        alertDialog.dismiss()
                    }
                }
            }

            btn_Cancelar_reg.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }

    private fun cleanEmail(email: String): String {
        return email.replace(".", "_").replace("@", "_")
    }

    private suspend fun uploadImageToFirebase(imageBitmap: Bitmap, userEmail: String): String {
        return suspendCancellableCoroutine { continuation ->
            val cleanedEmail = cleanEmail(userEmail)
            val fileName = "$cleanedEmail.jpg"
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val storageRef = FirebaseStorage.getInstance().reference.child("Clientes/$fileName")
            val uploadTask = storageRef.putBytes(data)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resume(uri.toString(), null)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    private fun checkCameraPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            false
        } else {
            true
        }
    }

    private fun checkGalleryPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), GALLERY_PERMISSION_CODE)
            false
        } else {
            true
        }
    }

    private fun openCamera() {
        if (checkCameraPermissions()) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        if (checkGalleryPermissions()) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        imageView.setImageBitmap(imageBitmap)
                        setRoundedImage(imageBitmap)
                        selectedImageUri = null // Reset Uri to prevent re-upload
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        imageView.setImageURI(uri)
                        val bitmap = getBitmapFromUri(uri)
                        if (bitmap != null) {
                            setRoundedImage(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setRoundedImage(bitmap: Bitmap?) {
        bitmap?.let {
            val roundedBitmapDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
            roundedBitmapDrawable.isCircular = true
            imageView.setImageDrawable(roundedBitmapDrawable)
        }
    }

    private fun compressImage(imageUri: Uri?, context: Context): Bitmap? {
        return if (imageUri != null) {
            try {
                val bitmap = getBitmapFromUri(imageUri)
                val outputStream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    private fun mostrarMensajeError(context: Context, mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    private suspend fun usuarioExiste(usuario: String): Boolean {
        val sql = """
        SELECT COUNT(*) AS usuario_existe
        FROM TbUsers
        WHERE Nombre_de_Usuario = ?
        """.trimIndent()

        val claseConexion = ClaseConexion()
        val conexion = claseConexion.CadenaConexion()

        var usuarioExiste = false

        if (conexion != null) {
            try {
                val statement = withContext(Dispatchers.IO) { conexion.prepareStatement(sql) }
                statement.setString(1, usuario)

                val resultado = withContext(Dispatchers.IO) { statement.executeQuery() }

                if (resultado.next()) {
                    val count = resultado.getInt("usuario_existe")
                    usuarioExiste = count > 0
                }
            } catch (e: Exception) {
                println("Error al ejecutar la consulta SQL: $e")
            } finally {
                try {
                    withContext(Dispatchers.IO) { conexion.close() }
                } catch (e: Exception) {
                    println("Error al cerrar la conexión: $e")
                }
            }
        } else {
            println("No se pudo establecer la conexión a la base de datos.")
        }

        return usuarioExiste
    }

    private suspend fun correoExiste(correo: String): Boolean {
        val sql = """
        SELECT COUNT(*) AS correo_existe
        FROM TbUsers
        WHERE Email_User = ?
        """.trimIndent()

        val claseConexion = ClaseConexion()
        val conexion = claseConexion.CadenaConexion()

        var correoExiste = false

        if (conexion != null) {
            try {
                val statement = withContext(Dispatchers.IO) { conexion.prepareStatement(sql) }
                statement.setString(1, correo)

                val resultado = withContext(Dispatchers.IO) { statement.executeQuery() }

                if (resultado.next()) {
                    val count = resultado.getInt("correo_existe")
                    correoExiste = count > 0
                }
            } catch (e: Exception) {
                println("Error al ejecutar la consulta SQL: $e")
            } finally {
                try {
                    withContext(Dispatchers.IO) { conexion.close() }
                } catch (e: Exception) {
                    println("Error al cerrar la conexión: $e")
                }
            }
        } else {
            println("No se pudo establecer la conexión a la base de datos.")
        }

        return correoExiste
    }

    private suspend fun validarCamposRegistro(
        context: Context,
        CampoNombres: EditText,
        CampoApellidos: EditText,
        CampoUsuario: EditText,
        CampoTelefono: EditText,
        CampoCorreo: EditText,
        CampoContrasena: EditText,
        CampoConfirmarContra: EditText,
        Btn_Foto: Button
    ): Boolean {
        var camposValidos = true

        fun agregarTextWatcher(editText: EditText, maxLength: Int) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null && s.length > maxLength) {
                        editText.error = "Máximo de caracteres alcanzado"
                    } else {
                        editText.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s != null && s.length > maxLength) {
                        editText.setText(s.substring(0, maxLength))
                        editText.setSelection(maxLength)
                    }
                }
            })
        }

        agregarTextWatcher(CampoNombres, 13)
        agregarTextWatcher(CampoApellidos, 13)
        agregarTextWatcher(CampoUsuario, 18)
        agregarTextWatcher(CampoTelefono, 9)
        agregarTextWatcher(CampoCorreo, 28)
        agregarTextWatcher(CampoContrasena, 28)
        agregarTextWatcher(CampoConfirmarContra, 28)

        if (CampoNombres.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese los nombres.")
            camposValidos = false
        } else if (CampoApellidos.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese los apellidos.")
            camposValidos = false
        } else if (CampoUsuario.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese el nombre de usuario.")
            camposValidos = false
        } else if (CampoTelefono.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese el número de teléfono.")
            camposValidos = false
        } else if (CampoCorreo.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese el correo electrónico.")
            camposValidos = false
        } else if (CampoContrasena.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, ingrese la contraseña.")
            camposValidos = false
        } else if (CampoConfirmarContra.text.toString().isEmpty()) {
            mostrarMensajeError(context, "Por favor, confirme la contraseña.")
            camposValidos = false
        }

        if (camposValidos) {
            if (withContext(Dispatchers.IO) { usuarioExiste(CampoUsuario.text.toString()) }) {
                mostrarMensajeError(context, "El nombre de usuario ya está en uso.")
                camposValidos = false
            }
        }

        if (camposValidos) {
            if (withContext(Dispatchers.IO) { correoExiste(CampoCorreo.text.toString()) }) {
                mostrarMensajeError(context, "Ya existe una cuenta registrada con ese correo.")
                camposValidos = false
            }
        }

        if (camposValidos) {
            val contrasena = CampoContrasena.text.toString()
            val confirmarContrasena = CampoConfirmarContra.text.toString()

            if (contrasena != confirmarContrasena) {
                mostrarMensajeError(context, "La contraseña no coincide con la confirmación.")
                camposValidos = false
            }
        }

        return camposValidos
    }

    private suspend fun insertarDatosEnBaseDeDatos(
        nombres: String,
        apellidos: String,
        usuario: String,
        telefono: String,
        correo: String,
        contrasena: String
    ) {
        val ObjConexion = ClaseConexion().CadenaConexion()

        if (ObjConexion != null) {
            withContext(Dispatchers.IO) {
                val CrearCuenta =
                    ObjConexion.prepareStatement("INSERT INTO TbUsers (ID_User, UUID_User, Nombres_User, Apellido_User, Nombre_de_Usuario, Num_Telefono_User, Email_User, Contra_User, Img_User) VALUES (SEQ_Users.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)")

                CrearCuenta.setString(1, UUID.randomUUID().toString())
                CrearCuenta.setString(2, nombres)
                CrearCuenta.setString(3, apellidos)
                CrearCuenta.setString(4, usuario)
                CrearCuenta.setString(5, telefono)
                CrearCuenta.setString(6, correo)
                CrearCuenta.setString(7, contrasena)
                CrearCuenta.setString(8, Direccion_Descarga)
                CrearCuenta.executeUpdate()
            }
        } else {
            println("No se pudo establecer la conexión a la base de datos.")
        }
    }
}
