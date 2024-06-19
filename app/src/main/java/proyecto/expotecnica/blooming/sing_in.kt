package proyecto.expotecnica.blooming

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import proyecto.expotecnica.blooming.BaseDeDatos.IngresarUserFirebase
import proyecto.expotecnica.blooming.Encriptado.generateKey
import proyecto.expotecnica.blooming.Encriptado.encrypt
import proyecto.expotecnica.blooming.Encriptado.decrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proyecto.expotecnica.blooming.BaseDeDatos.UsuarioFirebase

class sing_in : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sing_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Generar la clave si no existe
        generateKey()

        val RecuperarContra = findViewById<TextView>(R.id.lbl_ContraOlvidada_Sing_In)
        val Registrarse = findViewById<TextView>(R.id.lbl_Registar_Sing_In)
        val emailEditText = findViewById<EditText>(R.id.txt_Correo_Sing_In)
        val passwordEditText = findViewById<EditText>(R.id.txt_Contra_Sing_In)
        val registerButton = findViewById<Button>(R.id.btn_RegistrseGoogle_Sing_In)
        val loginButton = findViewById<Button>(R.id.btn_Google_Sing_In)

        RecuperarContra.setOnClickListener {
            val intent = Intent(this, Password_recovery1::class.java)
            startActivity(intent)
            finish()
        }

        Registrarse.setOnClickListener {
            val intent = Intent(this, Registrer::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                // El usuario no existe, proceder con el registro
                                firebaseAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { createUserTask ->
                                        if (createUserTask.isSuccessful) {
                                            showUserInputDialog(email)
                                        } else {
                                            Toast.makeText(this@sing_in, "Registration failed: ${createUserTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                // El usuario ya existe
                                Toast.makeText(this, "User already exists with this email", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Failed to check user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            user?.let {
                                val token = it.uid
                                val (iv, encryptedToken) = encrypt(token)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val dbUser = IngresarUserFirebase.getUser(encryptedToken)
                                    withContext(Dispatchers.Main) {
                                        if (dbUser != null) {
                                            val decryptedToken = decrypt(dbUser.iv, dbUser.uid)
                                            if (decryptedToken == token) {
                                                Toast.makeText(this@sing_in, "Login successful", Toast.LENGTH_SHORT).show()
                                                // Navegar a la siguiente actividad o actualizar la UI
                                            } else {
                                                Toast.makeText(this@sing_in, "User does not exist", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(this@sing_in, "User does not exist", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserInputDialog(email: String) {
        val dialogView = layoutInflater.inflate(R.layout.ven_emer_sing_in, null)
        val etUser = dialogView.findViewById<EditText>(R.id.etUser)

        AlertDialog.Builder(this)
            .setTitle("Enter Username")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val username = etUser.text.toString()
                if (username.isNotEmpty()) {
                    checkUserExists(username, email)
                } else {
                    Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun checkUserExists(username: String, email: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userInFirebase = IngresarUserFirebase.getUserFromUserFirebase(username)
            val userInUserTable = IngresarUserFirebase.getUserFromUserTable(username)
            withContext(Dispatchers.Main) {
                if (userInFirebase == null && userInUserTable == null) {
                    // El usuario no existe en ninguna de las dos tablas
                    promptForProfilePicture(username, email)
                } else {
                    // El usuario ya existe
                    Toast.makeText(this@sing_in, "User already exists. Please choose another username.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun promptForProfilePicture(username: String, email: String) {
        AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setMessage("Would you like to upload a profile picture?")
            .setPositiveButton("Upload") { _, _ ->
                // Lógica para subir una imagen
                uploadProfilePicture(username, email)
            }
            .setNegativeButton("Skip") { _, _ ->
                // Usar imagen predeterminada
                saveUserWithDefaultProfilePicture(username, email)
            }
            .create()
            .show()
    }

    private fun uploadProfilePicture(username: String, email: String) {
        // Lógica para subir una imagen
        // Para este ejemplo, simplemente simulamos la subida de la imagen y guardamos el usuario
        // En la vida real, aquí abrirías un intent para seleccionar la imagen y luego la subirías
        saveUserWithProfilePicture(username, email, "path/to/uploaded/picture")
    }

    private fun saveUserWithDefaultProfilePicture(username: String, email: String) {
        // Guardar el usuario con la imagen predeterminada
        saveUserWithProfilePicture(username, email, "path/to/default/picture")
    }

    private fun saveUserWithProfilePicture(username: String, email: String, profilePicturePath: String) {
        val token = firebaseAuth.currentUser?.uid ?: return
        val (iv, encryptedToken) = encrypt(token)

        lifecycleScope.launch(Dispatchers.IO) {
            IngresarUserFirebase.insertUser(encryptedToken, email, username, iv, profilePicturePath)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@sing_in, "User registered successfully", Toast.LENGTH_SHORT).show()
                // Navegar a la siguiente actividad o actualizar la UI
            }
        }
    }
}
