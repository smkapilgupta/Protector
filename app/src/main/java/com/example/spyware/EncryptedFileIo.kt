import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.spyware.common.Constants.Companion.logShift
import com.example.spyware.common.Constants.Companion.outputFileName
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

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
        writeShiftedToFile(location,UUID.randomUUID().toString()+fileName,data,n)
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
    println("Full Content: "+newContent)
    writeShiftedToFile(location,fileName,newContent,n)
}

fun log(context:Context, jsonObject: JSONObject){
    val currentUtcTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    jsonObject.put("timestamp", currentUtcTime)
    println(jsonObject.toString())
    appendShiftedToFile(context.filesDir,outputFileName, jsonObject.toString(),logShift)
}

fun downloadHeatMap(context:Context){
    val content:String?=readShiftedFromFile(context.filesDir,outputFileName,logShift)
    writeShiftedToFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),outputFileName,(content?:""),0)
}

fun exportHeatMap(context:Context){
    val content:String?=readShiftedFromFile(context.filesDir,outputFileName,logShift)
    writeShiftedToFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),outputFileName,(content?:""), logShift)
}

fun importHeatMap(context:Context){
    val content:String?=readShiftedFromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),outputFileName,logShift)
    writeShiftedToFile(context.filesDir,outputFileName,(content?:""), logShift)
}

