package Componentes

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class PhoneEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    init {
        // Remover la línea inferior  del campo del número de telefono
        background = null

        // Agregar TextWatcher para formatear el texto
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty() && it.length % 5 == 0) {
                        val lastChar = it.last()
                        if (lastChar != '-') {
                            it.insert(it.length - 1, "-")
                        }
                    }
                }
            }
        })
    }
}
