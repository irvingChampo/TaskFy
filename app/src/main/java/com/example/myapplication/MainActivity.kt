package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.Navigation
import com.example.myapplication.src.Core.Navigate.AppNavigate
import com.example.myapplication.src.Features.Home.presentation.view.HomeScreen
import com.example.myapplication.src.Features.Task.presentation.view.CreateTaskScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigate()
        }
    }
}