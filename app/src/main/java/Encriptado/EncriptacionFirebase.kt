package proyecto.expotecnica.blooming.Encriptado

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "MyKeyAlias"
private const val AES_MODE = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH = 128
private const val GCM_IV_LENGTH = 12

fun generateKey() {
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()
    keyGenerator.init(keyGenParameterSpec)
    keyGenerator.generateKey()
}

fun getKey(): SecretKey? {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)
    return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
}

fun encrypt(data: String): Pair<String, String> {
    val key = getKey() ?: throw Exception("Key not found")
    val cipher = Cipher.getInstance(AES_MODE)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv
    val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    val ivString = iv.joinToString("") { "%02x".format(it) }
    val encryptedString = encryptedBytes.joinToString("") { "%02x".format(it) }
    return Pair(ivString, encryptedString)
}

fun decrypt(ivString: String, data: String): String {
    val key = getKey() ?: throw Exception("Key not found")
    val cipher = Cipher.getInstance(AES_MODE)
    val iv = ivString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
    val encryptedBytes = data.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes, StandardCharsets.UTF_8)
}