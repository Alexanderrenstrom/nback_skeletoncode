package mobappdev.example.nback_cimpl

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM
import java.util.Locale

/**
 * This is the MainActivity of the application
 *
 * Your navigation between the two (or more) screens should be handled here
 * For this application you need at least a homescreen (a start is already made for you)
 * and a gamescreen (you will have to make yourself, but you can use the same viewmodel)
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Instantiate the viewmodel
                    val gameViewModel: GameVM = viewModel(
                        factory = GameVM.Factory
                    )

                    tts = TextToSpeech(this) { status ->
                        if (status == TextToSpeech.SUCCESS) {
                            val langResult = tts.setLanguage(Locale.US)
                            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("MainActivity", "Language is not supported or missing data.")
                            }
                        } else {
                            Log.e("MainActivity", "TextToSpeech initialization failed.")
                        }
                    }

                    gameViewModel.setTextToSpeech(tts)

                    AppNavigation(gameViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(vm: GameVM){
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home"){
            HomeScreen(navController = navController, vm = vm)
        }
        composable("game"){
            GameScreen(navController = navController, vm = vm)
        }
    }
}