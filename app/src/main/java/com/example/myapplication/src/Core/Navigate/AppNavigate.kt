package com.example.myapplication.src.Core.Navigate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.src.Features.Login.presentation.view.LoginScreen
import com.example.myapplication.src.Features.Login.presentation.view.RegisterScreen
import com.example.myapplication.src.Features.Home.presentation.view.HomeScreen
import com.example.myapplication.src.Features.Task.presentation.view.CreateTaskScreen
import com.example.myapplication.src.Features.Task.presentation.view.UpdateTaskScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_TASK = "create_task"
    const val UPDATE_TASK = "update_task"
}

@Composable
fun AppNavigate(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCreateTask = {
                    navController.navigate(Routes.CREATE_TASK)
                },
                onNavigateToUpdateTask = { taskId ->
                    navController.navigate("${Routes.UPDATE_TASK}/$taskId")
                }
            )
        }

        composable(Routes.CREATE_TASK) {
            CreateTaskScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveTask = { _, _, _, _ ->
                    navController.popBackStack()
                },
                onTakePhoto = {
                },
                onSelectPhoto = {
                }
            )
        }

        composable("${Routes.UPDATE_TASK}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0

            UpdateTaskScreen(
                taskId = taskId,
                initialTitle = "Estudiar",
                initialDescription = "Estudiar para aprobar la materia",
                initialDate = "30/05/2025",
                initialPhotoUri = null,
                onBackClick = {
                    navController.popBackStack()
                },
                onUpdateTask = { id, title, description, date, photo ->
                    navController.popBackStack()
                },
                onTakePhoto = {
                },
                onSelectPhoto = {
                }
            )
        }
    }
}