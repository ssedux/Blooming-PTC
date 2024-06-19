package proyecto.expotecnica.blooming

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Password_recovery3 : AppCompatActivity() {
    private lateinit var CampoNuevaContra: EditText
    private lateinit var CampoConfirmarContra: EditText
    private lateinit var ImgOjoNuevaContra: ImageView
    private lateinit var ImgOjoConfirmarContra: ImageView
    private var isNuevaContraVisible = false
    private var isConfirmarContraVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_recovery3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CampoNuevaContra = findViewById(R.id.txt_ContraNueva_Password_Recovery3)
        CampoConfirmarContra = findViewById(R.id.txt_ConfirmacionContra_Password_Recovery3)
        ImgOjoNuevaContra = findViewById(R.id.Img_NuevaContra_Password_Recovery3)
        ImgOjoConfirmarContra = findViewById(R.id.Img_ConfirmarContra_Password_Recovery3)

        CampoNuevaContra.requestFocus()

        CampoNuevaContra.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        CampoConfirmarContra.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        ImgOjoNuevaContra.setOnClickListener {
            if (isNuevaContraVisible) {
                CampoNuevaContra.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ImgOjoNuevaContra.setImageResource(R.drawable.hide_pasword)
            } else {
                CampoNuevaContra.inputType = InputType.TYPE_CLASS_TEXT
                ImgOjoNuevaContra.setImageResource(R.drawable.show_password)
            }
            isNuevaContraVisible = !isNuevaContraVisible
            CampoNuevaContra.setSelection(CampoNuevaContra.text.length)
        }

        ImgOjoConfirmarContra.setOnClickListener {
            if (isConfirmarContraVisible) {
                CampoConfirmarContra.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ImgOjoConfirmarContra.setImageResource(R.drawable.hide_pasword)
            } else {
                CampoConfirmarContra.inputType = InputType.TYPE_CLASS_TEXT
                ImgOjoConfirmarContra.setImageResource(R.drawable.show_password)
            }
            isConfirmarContraVisible = !isConfirmarContraVisible
            CampoConfirmarContra.setSelection(CampoConfirmarContra.text.length)
        }

    }

}