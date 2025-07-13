package com.example.myapplication.src.Features.Login.data.repository

import com.example.myapplication.src.Core.DataStore.DataStoreManager
import com.example.myapplication.src.Core.DataStore.PreferenceKeys
import com.example.myapplication.src.Features.Login.domain.repository.TokenRepository

class TokenRepositoryImpl(
    private val dataStore: DataStoreManager
) : TokenRepository {
    override suspend fun getToken(): String? = dataStore.getKey(PreferenceKeys.TOKEN)
    override suspend fun saveToken(token: String) = dataStore.saveKey(PreferenceKeys.TOKEN, token)
}