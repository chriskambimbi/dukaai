package com.example.dukaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.dukaai.ui.navigation.BottomNavigationBar
import com.example.dukaai.ui.navigation.DukaNavGraph
import com.example.dukaai.ui.theme.DukaAITheme

/**
 * Main Activity for Duka.AI
 * Sets up the navigation and theme
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DukaAITheme {
                DukaAIApp()
            }
        }
    }
}

@Composable
fun DukaAIApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        DukaNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
