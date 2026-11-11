package com.example.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.counter.data.CounterStore
import com.example.counter.ui.CounterApp
import com.example.counter.ui.CounterViewModel
import com.example.counter.ui.theme.CounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CounterTheme {
                CounterRoot()
            }
        }
    }
}

@Composable
private fun CounterRoot() {
    val context = LocalContext.current.applicationContext
    val viewModel: CounterViewModel = viewModel { CounterViewModel(CounterStore(context)) }
    CounterApp(viewModel)
}