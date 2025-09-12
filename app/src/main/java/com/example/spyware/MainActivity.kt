package com.example.spyware

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.PowerManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spyware.ui.theme.SpywareTheme
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spyware.common.Constants
import log
import org.json.JSONObject
import android.Manifest.permission.POST_NOTIFICATIONS
import downloadHeatMap
import exportHeatMap
import filterLogsOnDate
import getErrorsFromLogs
import getReducedLogs
import importHeatMap
import java.io.BufferedReader
import java.time.Instant
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    private var mobileDialogOpen=false
    private lateinit var gpsReader: GpsReader;
    private lateinit var alarmManger:AlarmManger
    private val REQUEST_CHECK_SETTINGS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        println("Created MainActivity")
        log(this,JSONObject().put("message","MainActivity started"))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppContent(this)
        }


        requestNotificationPermission()


//        appendShiftedToFile(this.filesDir,"secrets.txt","hello world",4)
//        appendShiftedToFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"23423424.txt","hello world",4)
//        log(this, JSONObject().put("hello","world"))

//        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//        startActivity(intent)
        if(!MainService.isServiceActive && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.startForegroundService(
                Intent(
                    this,
                    MainService::class.java
                )
            );
        }
        alarmManger= AlarmManger(this)
        alarmManger.start(Constants.timerSeconds*1000)
    }

    override fun onResume() {
        println("Resumed MainActivity")
        super.onResume()
        showEnableInternetDialog(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                gpsReader=GpsReader(this)
                gpsReader.grantLocationPermission(this)
                gpsReader.turnOnLocation(this)
                requestBatteryOptimizationPermission()
                log(this,JSONObject().put("info","Notification permission granted"))
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                log(this,JSONObject().put("warning","Notification permission denied"))
            }
        }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
        else{
            gpsReader=GpsReader(this)
            gpsReader.grantLocationPermission(this)
            gpsReader.turnOnLocation(this)
            requestBatteryOptimizationPermission()
        }
    }
        // Step 4: Handle result of user's decision

    private fun requestBatteryOptimizationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
                log(this,JSONObject().put("info","Requested battery optimization permission"))
            }
        }
    }

    fun showEnableInternetDialog(context: Context) {
        if(!isConnectedToInternet(context) && mobileDialogOpen==false) {
            log(context,JSONObject().put("error","Not connected to internet."))
            mobileDialogOpen=true
            var dialog=AlertDialog.Builder(context)
                .setTitle("Enable internet access")
                .setMessage("Make sure you are connected to internet")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    this.startActivity(intent)
                }
                .show()

            dialog.setOnDismissListener {
                mobileDialogOpen=false
//                showEnableMobileDataDialog(context)
            }
        }
    }

    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(context: Context) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Errors", "Heatmap")
    var textState by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(textState.toByteArray())
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val reader = BufferedReader(inputStream.reader())
                textState = reader.readText()
            }
        }
    }

    SpywareTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(text = { Text(title) },
                            selected = tabIndex == index,
                            onClick = { tabIndex = index }
                        )
                    }
                }
                when (tabIndex) {
                    0 -> MainTabContent(
                        context,
                        textValue = textState,
                        onTextChange = { textState = it },
                        onDownloadClick = {
                            downloadHeatMap(context)
                            Toast.makeText(context, "Heatmap downloaded to Documents", Toast.LENGTH_SHORT).show()

                                          },
                        onExportClick = {
                            exportHeatMap(context)
                            Toast.makeText(context, "Heatmap exported to Documents", Toast.LENGTH_SHORT).show()
                        },
                        onImportClick = {
                            importHeatMap(context)
                            Toast.makeText(context, "Heatmap imported from Documents", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> HeatmapTabContent(context)
                }
            }
        }
    }
}

@Composable
fun MainTabContent(
    context:Context,
    textValue: String,
    onTextChange: (String) -> Unit,
    onDownloadClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(getErrorsFromLogs(context))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onDownloadClick, modifier = Modifier.weight(1f)) {
                Text("Download")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onExportClick, modifier = Modifier.weight(1f)) {
                Text("Export")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onImportClick, modifier = Modifier.weight(1f)) {
                Text("Import")
            }
        }
    }
}

@Composable
fun HeatmapTabContent(    context:Context,
) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text(getReducedLogs(context,Instant.now().minus(7, ChronoUnit.DAYS),Instant.now()))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val textToCopy = getReducedLogs(context,null,null)
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboardManager.setPrimaryClip(clip)
            }) {
                Text("Copy")
            }
        }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpywareTheme {
        AppContent(context = androidx.compose.ui.platform.LocalContext.current)
    }
}