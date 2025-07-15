import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

// Shift each character n places backward (encode)
private fun shiftStringBack(str: String, n: Int): String {
    val sb = StringBuilder(str.length)
    for (char in str) {
        sb.append((char.code - n).toChar())
    }
    return sb.toString()
}

// Shift each character n places forward (decode)
private fun shiftStringForward(str: String, n: Int): String {
    val sb = StringBuilder(str.length)
    for (char in str) {
        sb.append((char.code + n).toChar())
    }
    return sb.toString()
}

// Write encoded data to file
fun writeShiftedToFile(location: File, fileName: String, data: String, n: Int) {
    try {
        if (!location.exists()) location.mkdirs()

        val shifted = shiftStringBack(data, n)
        val file = File(location, fileName)
        FileOutputStream(file).use { it.write(shifted.toByteArray()) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Read and decode data from file
fun readShiftedFromFile(location: File, fileName: String, n: Int): String? {
    return try {
        val file = File(location, fileName)
        FileInputStream(file).use {
            val shifted = it.readBytes().toString(Charsets.UTF_8)
            shiftStringForward(shifted, n)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun appendShiftedToFile(location: File, fileName: String,data:String, n: Int) {
    val content:String?=readShiftedFromFile(location,fileName,n)
    val newContent = (content ?: "") + "\n" + data
    println("halo: "+newContent)
    writeShiftedToFile(location,fileName,newContent,n)
}
