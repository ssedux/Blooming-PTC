package modelo

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class AuFirebase(private val context: Context) {
    fun autenticar(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // El usuario está autenticado, llama a la función de éxito
                onSuccess()
            } else {
                // Llama a la función de fallo con la excepción
                onFailure(task.exception ?: Exception("Error desconocido"))
                // Manejar el error de autenticación
                Toast.makeText(context, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}