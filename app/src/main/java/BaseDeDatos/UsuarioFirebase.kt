package proyecto.expotecnica.blooming.BaseDeDatos

data class UsuarioFirebase(
    val uid: String,
    val email: String,
    val username: String,
    val iv: String,
    val profilePicture: String
)
