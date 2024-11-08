package mobappdev.example.nback_cimpl.ui.viewmodels

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int
    val eventInterval: Long
    var eventCounter: Int
    val numberOfEvents: Int

    fun setGameType(gameType: GameType)
    fun startGame()
    fun endGame()

    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    override val nBack: Int = 2

    private var job: Job? = null  // coroutine job for the game event
    override val eventInterval: Long = 2000L  // 2000 ms (2s)

    override val numberOfEvents: Int = 10

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    override var eventCounter: Int = 0

    private var tts: TextToSpeech? = null

    fun setTextToSpeech(ttsInstance: TextToSpeech) {
        tts = ttsInstance
    }

    private fun speakLetter(letter: String){
        tts?.speak(letter, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun convertEventToLetter(eventValue: Int): String {
        return when (eventValue){
            1 -> "A"
            2 -> "B"
            3 -> "C"
            4 -> "D"
            5 -> "E"
            6 -> "F"
            7 -> "G"
            8 -> "H"
            9 -> "I"
            else -> "?"
        }
    }

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        eventCounter = 0
        _score.value = 0

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(numberOfEvents, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            if(_score.value >= _highscore.value) _highscore.value = _score.value
        }
    }

    override fun endGame() {
        job?.cancel()
    }

    override fun checkMatch() {
        if(eventCounter-1 < nBack || _gameState.value.eventValue == -1) return

        val isMatch = _gameState.value.eventValue == events[eventCounter-1 - nBack]


        _gameState.value = _gameState.value.copy(
            matchButtonColor = if (isMatch) Color.Green else Color.Red
        )
        if(isMatch){
            _score.value += 1
            //_gameState.value =  _gameState.value.copy(eventValue = events[eventCounter])
        }

        viewModelScope.launch {
            delay(500L)
            _gameState.value = _gameState.value.copy(matchButtonColor = Color.Blue)
        }
    }

    private suspend fun runAudioGame() {
        for (value in events){
            _gameState.value = _gameState.value.copy(eventValue = value)
            val letter = convertEventToLetter(_gameState.value.eventValue)
            speakLetter(letter)
            eventCounter++
            _gameState.value = _gameState.value.copy(eventCounter = eventCounter)
            delay(eventInterval)
        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        // Todo: Replace this code for actual game code
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value)
            eventCounter++
            _gameState.value = _gameState.value.copy(eventCounter = eventCounter)
            delay(eventInterval)
        }

    }

    private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val matchButtonColor: Color = Color.Blue,
    val eventCounter: Int = 0
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2
    override val eventInterval: Long
        get() = 2000L
    override var eventCounter: Int
        get() = 0
        set(value) {}
    override val numberOfEvents: Int
        get() = 10


    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatch() {
    }

    override fun endGame() {

    }
}