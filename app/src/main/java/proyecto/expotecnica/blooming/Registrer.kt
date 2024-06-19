package proyecto.expotecnica.blooming

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registrer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val CampoDelNombre = findViewById<EditText>(R.id.txt_Nombre_Registrer)
        val InicioSesion = findViewById<TextView>(R.id.lbl_IniciarSesion_Registrer)

        CampoDelNombre.requestFocus()

        InicioSesion.setOnClickListener{
            val intent = Intent(this, sing_in::class.java)
            startActivity(intent)
            finish()
        }
    }

}