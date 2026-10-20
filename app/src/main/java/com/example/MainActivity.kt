package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.PizzeriaApp
import com.example.ui.theme.PizzeriaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate, savedInstanceState=${savedInstanceState != null}")

        enableEdgeToEdge()
        setContent {
            PizzeriaTheme {
                PizzeriaApp()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy, isChangingConfigurations=$isChangingConfigurations")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "PizzeriaLifecycle"
    }
}
