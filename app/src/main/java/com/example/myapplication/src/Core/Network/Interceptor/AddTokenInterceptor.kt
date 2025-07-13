package com.example.myapplication.src.Core.Network.Interceptor

import android.util.Log
import com.example.myapplication.src.Core.DataStore.DataStoreManager
import com.example.myapplication.src.Core.DataStore.PreferenceKeys
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AddTokenInterceptor(
    private val dataStore: DataStoreManager
) : Interceptor {

    companion object {
        private const val TAG = "AddTokenInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()

        Log.d(TAG, "=== ADD TOKEN INTERCEPTOR START ===")
        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request Method: ${request.method}")
        Log.d(TAG, "Request Headers: ${request.headers}")

        val token = runBlocking {
            try {
                Log.d(TAG, "Attempting to get token from DataStore...")
                val storedToken = dataStore.getKey(PreferenceKeys.TOKEN)
                Log.d(TAG, "Token retrieved: ${if (storedToken != null) "EXISTS (${storedToken.length} chars)" else "NULL"}")
                storedToken
            } catch (e: Exception) {
                Log.e(TAG, "Error getting token from DataStore", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                null
            }
        }

        token?.let {
            Log.d(TAG, "Adding Authorization header with token")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        } ?: run {
            Log.w(TAG, "No token available - proceeding without Authorization header")
        }

        val finalRequest = requestBuilder.build()
        Log.d(TAG, "Final request headers: ${finalRequest.headers}")

        return try {
            Log.d(TAG, "Proceeding with request...")
            val response = chain.proceed(finalRequest)
            Log.d(TAG, "Response received - Status: ${response.code}")
            Log.d(TAG, "Response headers: ${response.headers}")
            Log.d(TAG, "=== ADD TOKEN INTERCEPTOR END ===")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error during request execution", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "=== ADD TOKEN INTERCEPTOR ERROR ===")
            throw e
        }
    }
}