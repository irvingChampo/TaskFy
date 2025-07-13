package com.example.myapplication.src.Core.Hardware.domain

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File

interface CamaraRepository {
    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    fun capturePhoto(outputFile: File, onSuccess: (File) -> Unit, onError: (Exception) -> Unit)
    fun switchCamera()
}