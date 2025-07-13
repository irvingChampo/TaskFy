package com.example.myapplication.src.Core.DI

import com.example.myapplication.src.Core.AppContext.AppContextHolder
import com.example.myapplication.src.Core.Hardware.data.CamaraManager
import com.example.myapplication.src.Core.Hardware.data.VibrationManager
import com.example.myapplication.src.Core.Hardware.domain.CamaraRepository
import com.example.myapplication.src.Core.Hardware.domain.VibratorRepository

object HardwareModule {
    val cameraRepository: CamaraRepository by lazy {
        CamaraManager(AppContextHolder.get())
    }

    val vibratorRepository: VibratorRepository by lazy {
        VibrationManager(AppContextHolder.get())
    }
}