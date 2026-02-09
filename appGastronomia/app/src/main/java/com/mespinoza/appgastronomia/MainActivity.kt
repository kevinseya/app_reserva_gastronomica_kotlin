package com.mespinoza.appgastronomia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mespinoza.appgastronomia.data.local.UserPreferences
import com.mespinoza.appgastronomia.navigation.NavigationHost
import com.mespinoza.appgastronomia.ui.theme.GastronomiaTheme
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
            GastronomiaTheme {
                NavigationHost(userPreferences = userPreferences)
            }
        }
    }
}
