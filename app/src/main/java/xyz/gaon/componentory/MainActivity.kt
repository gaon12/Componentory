package xyz.gaon.componentory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import xyz.gaon.componentory.lab.LabScreen
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ComponentoryTheme(dynamicColor = false) { LabScreen() } }
    }
}
