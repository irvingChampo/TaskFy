package com.example.myapplication.src.Core.Hardware.data

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.src.Core.Hardware.domain.CamaraRepository
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CamaraManager(private val context: Context) : CamaraRepository {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var preview: Preview? = null

    private var isBackCamera = true
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCamera(previewView, lifecycleOwner)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        try {
            cameraProvider?.unbindAll()

            val cameraSelector = if (isBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            preview = Preview.Builder().build().also { preview ->
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        } catch (exc: Exception) {
        }
    }

    override fun capturePhoto(
        outputFile: File,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(Exception("Cámara no inicializada"))
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    override fun switchCamera() {
        isBackCamera = !isBackCamera
    }
}