package com.diegodeabreu.ifoodprocesso

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.diegodeabreu.ifoodprocesso.di.createAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppContextHolder.init(applicationContext)
        AppContextHolder.setActivity(this)

        try {
            startKoin {
                androidContext(applicationContext)
                modules(createAppModule(BuildConfig.SPOONACULAR_API_KEY))
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Koin initialization notice (likely already started)", e)
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
