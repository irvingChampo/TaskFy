package com.example.myapplication.src.Core.Network.Interceptor

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

object LoggingInterceptor {

    private const val TAG = "NetworkLogger"

    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}