package proyecto.expotecnica.blooming

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.EnvioCorreo
import java.security.SecureRandom

class Password_recovery1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_recovery1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val CampoCorreo = findViewById<EditText>(R.id.txt_Correo_Password_Recovery1)
        val BotonEnviar = findViewById<Button>(R.id.btnIniciarSesion)
        val RecuerdaSuContra = findViewById<TextView>(R.id.lbl_RecuerdaContra_Password_Recovery1)

        BotonEnviar.setOnClickListener {
            val text = CampoCorreo.text.toString()

            when {
                text.isEmpty() -> {
                    Toast.makeText(this, "Error: el campo de texto está vacío", Toast.LENGTH_SHORT)
                        .show()
                }

                !text.contains("@") -> {
                    Toast.makeText(this, "Error: el texto debe contener un '@'", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    val code = generateRandomCode()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            EnvioCorreo.EnvioDeCorreo(text, "Your Verification Code", "Your verification code is: $code")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@Password_recovery1, "Código enviado a $text", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Password_recovery1, Password_recovery2::class.java)
                                intent.putExtra("SENT_CODE", code)
                                intent.putExtra("USER_EMAIL", text)
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@Password_recovery1, "Error al enviar el correo: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }


        RecuerdaSuContra.setOnClickListener {
            val intent = Intent(this, sing_in::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..6)
            .map { chars[SecureRandom().nextInt(chars.length)] }
            .joinToString("")
    }
}
