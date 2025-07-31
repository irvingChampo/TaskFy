package com.example.myapplication.src.Features.Login.di

import com.example.myapplication.src.Core.DI.DataStoreModule
import com.example.myapplication.src.Core.Network.RetrofitHelper
import com.example.myapplication.src.Features.Login.data.datasource.remote.AuthService
import com.example.myapplication.src.Features.Login.data.repository.LoginRepositoryImpl
import com.example.myapplication.src.Features.Login.data.repository.TokenRepositoryImpl
import com.example.myapplication.src.Features.Login.domain.repository.LoginRepository
import com.example.myapplication.src.Features.Login.domain.repository.TokenRepository
import com.example.myapplication.src.Features.Login.domain.usecase.LoginUseCase

object AppModule {

    init {
        RetrofitHelper.init()
    }

    private val authService: AuthService by lazy {
        RetrofitHelper.getService(AuthService::class.java)
    }

    val tokenRepository: TokenRepository by lazy {
        TokenRepositoryImpl(DataStoreModule.dataStoreManager)
    }

    private val repositoryLogin: LoginRepository by lazy {
        LoginRepositoryImpl(authService, tokenRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(repositoryLogin)
    }
}