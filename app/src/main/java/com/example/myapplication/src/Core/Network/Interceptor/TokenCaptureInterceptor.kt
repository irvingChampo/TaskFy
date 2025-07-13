package com.example.myapplication.src.Core.Network.Interceptor

import android.util.Log
import com.example.myapplication.src.Core.DataStore.DataStoreManager
import com.example.myapplication.src.Core.DataStore.PreferenceKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class TokenCaptureInterceptor(
    private val dataStore: DataStoreManager
) : Interceptor {

    companion object {
        private const val TAG = "TokenCaptureInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Log.d(TAG, "=== TOKEN CAPTURE INTERCEPTOR START ===")
        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request Method: ${request.method}")

        return try {
            val response = chain.proceed(request)

            Log.d(TAG, "Response received - Status: ${response.code}")
            Log.d(TAG, "Response headers: ${response.headers}")

            // Verificar si hay token en la respuesta
            val authHeader = response.header("Authorization")
            val tokenHeader = response.header("token") // Algunos APIs usan "token" en lugar de "Authorization"
            val bearerToken = response.header("Bearer")

            Log.d(TAG, "Authorization header: $authHeader")
            Log.d(TAG, "Token header: $tokenHeader")
            Log.d(TAG, "Bearer header: $bearerToken")

            val tokenToSave = authHeader ?: tokenHeader ?: bearerToken

            if (!tokenToSave.isNullOrEmpty()) {
                Log.d(TAG, "Token found in response: ${tokenToSave.substring(0, minOf(10, tokenToSave.length))}...")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d(TAG, "Saving token to DataStore...")
                        dataStore.saveKey(PreferenceKeys.TOKEN, tokenToSave)
                        Log.d(TAG, "Token saved successfully to DataStore")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving token to DataStore", e)
                        Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                        Log.e(TAG, "Exception message: ${e.message}")
                    }
                }
            } else {
                Log.w(TAG, "No token found in response headers")
                // Log all headers for debugging
                Log.d(TAG, "All response headers:")
                response.headers.forEach { header ->
                    Log.d(TAG, "  ${header.first}: ${header.second}")
                }
            }

            Log.d(TAG, "=== TOKEN CAPTURE INTERCEPTOR END ===")
            response

        } catch (e: Exception) {
            Log.e(TAG, "Error during token capture", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "=== TOKEN CAPTURE INTERCEPTOR ERROR ===")
            throw e
        }
    }
}