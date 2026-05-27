package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.RoutineRepository
import com.example.ui.RoutineDashboardScreen
import com.example.ui.RoutineViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RoutineRepository(database.dailyRoutineDao())

        setContent {
            MyApplicationTheme {
                // Initialize ViewModel using a simple Constructor Factory
                val routineViewModel: RoutineViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
                                return RoutineViewModel(repository) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class")
                        }
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.SlateBlack
                ) {
                    RoutineDashboardScreen(viewModel = routineViewModel)
                }
            }
        }
    }
}
