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



