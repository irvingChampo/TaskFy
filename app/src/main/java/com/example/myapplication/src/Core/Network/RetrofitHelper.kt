package com.example.myapplication.src.Core.Network

import android.util.Log
import com.example.myapplication.src.Core.DI.DataStoreModule
import com.example.myapplication.src.Core.Network.Interceptor.AddTokenInterceptor
import com.example.myapplication.src.Core.Network.Interceptor.LoggingInterceptor
import com.example.myapplication.src.Core.Network.Interceptor.TokenCaptureInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    private const val BASE_URL = "https://todolist.bim2.xyz/api/"
    private const val TIMEOUT = 20L
    private const val TAG = "RetrofitHelper"

    @Volatile
    private var retrofit: Retrofit? = null

    fun init(extraInterceptors: List<Interceptor> = emptyList()) {
        Log.d(TAG, "=== RETROFIT HELPER INITIALIZATION ===")
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    Log.d(TAG, "Creating new Retrofit instance...")
                    retrofit = buildRetrofit(extraInterceptors)
                    Log.d(TAG, "Retrofit instance created successfully")
                } else {
                    Log.d(TAG, "Retrofit instance already exists")
                }
            }
        } else {
            Log.d(TAG, "Retrofit already initialized")
        }
    }

    fun <T> getService(serviceClass: Class<T>): T {
        Log.d(TAG, "Getting service: ${serviceClass.simpleName}")
        requireNotNull(retrofit) {
            "RetrofitHelper no ha sido inicializado. Llama a init() primero."
        }
        return retrofit!!.create(serviceClass)
    }

    private fun buildRetrofit(extraInterceptors: List<Interceptor>): Retrofit {
        Log.d(TAG, "Building Retrofit with base URL: $BASE_URL")
        val client = buildHttpClient(extraInterceptors)
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun buildHttpClient(extraInterceptors: List<Interceptor>): OkHttpClient {
        Log.d(TAG, "Building HTTP client with ${extraInterceptors.size} extra interceptors")
        Log.d(TAG, "Timeout settings: $TIMEOUT seconds")

        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AddTokenInterceptor(DataStoreModule.dataStoreManager))
            .addInterceptor(TokenCaptureInterceptor(DataStoreModule.dataStoreManager))
            .addInterceptor(LoggingInterceptor.provideLoggingInterceptor())
            .apply {
                extraInterceptors.forEach {
                    Log.d(TAG, "Adding extra interceptor: ${it.javaClass.simpleName}")
                    addInterceptor(it)
                }
            }
            .build()
    }
}