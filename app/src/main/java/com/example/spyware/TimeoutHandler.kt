package com.example.spyware

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager

class TimeoutHandler: SensorEventListener {
    private var mediaQuite: MediaPlayer
    private var vibrator: VibratorManager
    private var combinedVibrationEffect: CombinedVibration

    private var context:Context?
    constructor(context:Context?){
        this.context=context
        mediaQuite = MediaPlayer.create(context, R.raw.the_call_pomodoro_tune);
        vibrator = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrationEffect1 =
            VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)
        combinedVibrationEffect=CombinedVibration.createParallel(vibrationEffect1);
    }
    public fun alarm(){
        println("Make noise")
        vibrator.cancel()
        vibrator.vibrate(combinedVibrationEffect)
        startPlayer(mediaQuite);
    }

    private fun startPlayer(mediaPlayer: MediaPlayer){
        val audioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager;
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);
        mediaPlayer.setOnCompletionListener {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,originalVolume,0);
        }
        mediaPlayer.start();
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }
}