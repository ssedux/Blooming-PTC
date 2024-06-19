package proyecto.expotecnica.blooming.BaseDeDatos

import modelo.ClaseConexion
import java.sql.ResultSet
import java.sql.Statement

object IngresarUserFirebase {
    private val conexion = ClaseConexion()

    fun insertUser(encryptedToken: String, email: String, username: String, iv: String, profilePicturePath: String) {
        val connection = conexion.CadenaConexion()
        val statement: Statement?
        try {
            statement = connection?.createStatement()
            val sql = "INSERT INTO UserFirebase (uid, email, username, iv, profilePicture) VALUES ('$encryptedToken', '$email', '$username', '$iv', '$profilePicturePath')"
            statement?.executeUpdate(sql)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
    }

    fun getUser(encryptedToken: String): UsuarioFirebase? {
        val connection = conexion.CadenaConexion()
        val statement: Statement?
        val resultSet: ResultSet?
        return try {
            statement = connection?.createStatement()
            val sql = "SELECT * FROM UserFirebase WHERE uid = '$encryptedToken'"
            resultSet = statement?.executeQuery(sql)
            if (resultSet != null && resultSet.next()) {
                UsuarioFirebase(
                    resultSet.getString("uid"),
                    resultSet.getString("email"),
                    resultSet.getString("username"),
                    resultSet.getString("iv"),
                    resultSet.getString("profilePicture")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.close()
        }
    }

    fun getUserFromUserFirebase(username: String): UsuarioFirebase? {
        val connection = conexion.CadenaConexion()
        val statement: Statement?
        val resultSet: ResultSet?
        return try {
            statement = connection?.createStatement()
            val sql = "SELECT * FROM UserFirebase WHERE username = '$username'"
            resultSet = statement?.executeQuery(sql)
            if (resultSet != null && resultSet.next()) {
                UsuarioFirebase(
                    resultSet.getString("uid"),
                    resultSet.getString("email"),
                    resultSet.getString("username"),
                    resultSet.getString("iv"),
                    resultSet.getString("profilePicture")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.close()
        }
    }

    fun getUserFromUserTable(username: String): UsuarioFirebase? {
        val connection = conexion.CadenaConexion()
        val statement: Statement?
        val resultSet: ResultSet?
        return try {
            statement = connection?.createStatement()
            val sql = "SELECT * FROM User WHERE username = '$username'"
            resultSet = statement?.executeQuery(sql)
            if (resultSet != null && resultSet.next()) {
                UsuarioFirebase(
                    resultSet.getString("uid"),
                    resultSet.getString("email"),
                    resultSet.getString("username"),
                    resultSet.getString("iv"),
                    resultSet.getString("profilePicture")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.close()
        }
    }
}