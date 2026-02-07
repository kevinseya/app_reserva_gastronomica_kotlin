package com.jwmaila.appticketera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jwmaila.appticketera.data.local.UserPreferences
import com.jwmaila.appticketera.navigation.NavigationHost
import com.jwmaila.appticketera.ui.theme.TicketeraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketeraTheme {
                NavigationHost(userPreferences = userPreferences)
            }
        }
    }
}
