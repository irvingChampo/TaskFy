package com.example.myapplication.src.Core.Hardware.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.example.myapplication.src.Core.Hardware.domain.VibratorRepository

class VibrationManager(private val context: Context) : VibratorRepository {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
            vibratorManager?.defaultVibrator ?: context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun vibrateShort() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }


    override fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }
}