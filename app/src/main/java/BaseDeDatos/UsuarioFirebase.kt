package proyecto.expotecnica.blooming.BaseDeDatos

data class UsuarioFirebase(
    val uuid: String,
    val email: String,
    val username: String,
    val iv: String,
    val profilePicture: String
)
