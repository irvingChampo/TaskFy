package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.src.Core.AppContext.AppContextHolder
import com.example.myapplication.src.Core.Navigate.AppNavigate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContextHolder.init(this)

        enableEdgeToEdge()
        setContent {
            AppNavigate()
        }
    }
}