package com.example.myapplication.src.Core.AppContext

import android.content.Context

object AppContextHolder {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun get(): Context {
        check(::context.isInitialized) { "AppContextHolder no ha sido inicializado" }
        return context
    }
}