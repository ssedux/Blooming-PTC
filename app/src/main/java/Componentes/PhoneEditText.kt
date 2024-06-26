package Componetes

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
class PhoneEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    init {
        // Remover la línea inferior
        background = null

        // Agregar TextWatcher para formatear el texto
        addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    removeTextChangedListener(this)

                    val userInput = s.toString().replace(Regex("[^\\d]"), "")
                    val formatted = StringBuilder()

                    for (i in userInput.indices) {
                        formatted.append(userInput[i])
                        if ((i + 1) % 4 == 0 && i + 1 != userInput.length) {
                            formatted.append(" ")
                        }
                    }

                    current = formatted.toString()
                    setText(current)
                    setSelection(current.length)

                    addTextChangedListener(this)
                }
            }
        })
    }
}