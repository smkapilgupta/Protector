package com.example.spyware
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptedFileIo(private val context: Context) {
    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//    val documentsDir = context.filesDir

    companion object {
        private const val SECRET_PASSWORD = "YourStrongPassword123!"
        private const val SALT = "12345678" // should be securely stored or derived
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 256
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    }

    private fun generateKey(): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            SECRET_PASSWORD.toCharArray(),
            SALT.toByteArray(),
            ITERATIONS,
            KEY_LENGTH
        )
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
    public fun writeEncryptedFile(fileName: String, content: String) {
        if (!documentsDir.exists()) documentsDir.mkdirs()

        val file = File(documentsDir, fileName)

        if (file.exists()) {
            val deleted = file.delete()
            if (!deleted) {
                Toast.makeText(context, "Failed to delete existing file", Toast.LENGTH_LONG).show()
                return
            }
        }

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)

            .build()

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        )
            .build()

        encryptedFile.openFileOutput().use {
            it.write(content.toByteArray())
        }

        Toast.makeText(context, "Encrypted file saved! → ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }

    public fun readEncryptedFile(fileName: String): String {
        if (!documentsDir.exists()) documentsDir.mkdirs()

        val file = File(documentsDir, fileName)
        if (!file.exists()) return ""

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return encryptedFile.openFileInput().bufferedReader().use {
            it.readText()
        }
    }

    public fun appendEncryptedFile(fileName: String, contentToAppend: String) {

        val newContent = readEncryptedFile(fileName) + contentToAppend

        writeEncryptedFile(fileName,newContent)
        Toast.makeText(context, "Appended to encrypted file! "+newContent, Toast.LENGTH_SHORT).show()
    }
}