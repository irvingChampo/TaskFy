package com.example.myapplication.src.Core.DI

import com.example.myapplication.src.Core.AppContext.AppContextHolder
import com.example.myapplication.src.Core.DataStore.DataStoreManager

object DataStoreModule {
    val dataStoreManager: DataStoreManager by lazy {
        DataStoreManager(AppContextHolder.get())
    }
}