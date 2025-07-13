package com.example.myapplication.src.Core.Hardware.domain

interface VibratorRepository {
    fun vibrateShort()
    fun hasVibrator(): Boolean
}