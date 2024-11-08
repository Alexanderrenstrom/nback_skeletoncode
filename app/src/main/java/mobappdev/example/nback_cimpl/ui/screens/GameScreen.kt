package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(
    navController: NavController,
    vm : GameViewModel
) {
    val score = vm.score.collectAsState().value
    val gameState by vm.gameState.collectAsState()

    var litBoxIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(gameState.eventCounter){
        if(gameState.eventValue != 0 && gameState.gameType == GameType.Visual){
            litBoxIndex = gameState.eventValue
            delay(500L)
            litBoxIndex = -1
        }
    }

    Scaffold{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(32.dp),
                text = "Score = $score",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                modifier = Modifier.padding(16.dp),
                text = "current event = ${gameState.eventCounter}",
                fontSize = 25.sp
            )

            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(9) { index ->
                        val boxColor = if(index+1 == litBoxIndex) Color.Cyan else Color.Blue
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .background(color = boxColor)
                        )
                    }
                }
            }

            Button(
                onClick = { vm.checkMatch() },
                colors = ButtonDefaults.buttonColors(gameState.matchButtonColor),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.5f)
            ){
                Text("Check Match")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(onClick = {
                    navController.popBackStack()
                    vm.endGame()
                },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = "Back to Home",
                    )
                }

                Button(onClick = {
                    vm.startGame()
                },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = "Restart")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}


@Preview
@Composable
fun GameScreenPreview(){
    Surface(){
        GameScreen(rememberNavController(), FakeVM())
    }
}