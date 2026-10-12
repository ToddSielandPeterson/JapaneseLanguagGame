package com.cognitivecreations.japaneselanguagegame

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.cognitivecreations.japaneselanguagegame.data.ExerciseRepository
import com.cognitivecreations.japaneselanguagegame.data.ProgressStore
import com.cognitivecreations.japaneselanguagegame.domain.ExerciseCatalog
import com.cognitivecreations.japaneselanguagegame.domain.GameEvent
import com.cognitivecreations.japaneselanguagegame.domain.GameReducer
import com.cognitivecreations.japaneselanguagegame.domain.GameState
import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
import com.cognitivecreations.japaneselanguagegame.domain.RoundPhase
import com.cognitivecreations.japaneselanguagegame.theme.GameTheme
import com.cognitivecreations.japaneselanguagegame.theme.Palette
import com.cognitivecreations.japaneselanguagegame.ui.GameScreen
import kotlinx.coroutines.delay
import java.io.IOException

class GameController(
    private val catalog: ExerciseCatalog,
    private val store: ProgressStore,
    initial: PersistedProgress,
) {
    var state by mutableStateOf(GameReducer.start(catalog, initial))
        private set

    private var lastSaved: PersistedProgress = state.toPersisted()

    fun dispatch(event: GameEvent) {
        val next = GameReducer.reduce(state, event, catalog)
        val persisted = next.toPersisted()
        state = next
        if (persisted != lastSaved) {
            try {
                store.save(persisted)
                lastSaved = persisted
            } catch (error: IOException) {
                System.err.println("Could not save progress: ${error.message}")
            }
        }
    }
}

@Composable
fun JapaneseLanguageGameApp(
    initial: PersistedProgress,
    store: ProgressStore,
    catalog: ExerciseCatalog = remember { ExerciseRepository.load() },
) {
    val controller = remember(catalog, store) { GameController(catalog, store, initial) }
    val state = controller.state
    var resetDialogOpen by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(state.feedbackToken, state.phase) {
        if (state.phase != RoundPhase.WRONG_FLASH) return@LaunchedEffect
        val token = state.feedbackToken
        delay(900)
        if (controller.state.feedbackToken == token && controller.state.phase == RoundPhase.WRONG_FLASH) {
            controller.dispatch(GameEvent.DismissWrongFlash)
        }
    }

    LaunchedEffect(Unit) {
        focus.requestFocus()
    }

    GameTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Palette.bgGlow, Palette.bg, Color(0xFF101018)),
                        radius = 1400f,
                    ),
                )
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { event ->
                    handleKey(event, controller.state, resetDialogOpen) { gameEvent ->
                        controller.dispatch(gameEvent)
                    }
                },
        ) {
            GameScreen(
                state = state,
                exercise = state.exerciseId?.let(catalog::byId),
                onEvent = controller::dispatch,
                resetDialogOpen = resetDialogOpen,
                onResetDialogOpen = { resetDialogOpen = it },
            )
        }
    }
}

private fun handleKey(
    event: androidx.compose.ui.input.key.KeyEvent,
    state: GameState,
    resetDialogOpen: Boolean,
    dispatch: (GameEvent) -> Unit,
): Boolean {
    if (resetDialogOpen) return false
    if (event.type != KeyEventType.KeyDown) return false
    if (event.isCtrlPressed || event.isMetaPressed || event.isAltPressed) return false
    return when (event.key) {
        Key.Enter, Key.NumPadEnter -> {
            when (state.phase) {
                RoundPhase.BUILDING -> if (state.placed.isNotEmpty()) dispatch(GameEvent.Check)
                RoundPhase.SOLVED, RoundPhase.REVEALED -> dispatch(GameEvent.Next)
                RoundPhase.WRONG_FLASH -> Unit
            }
            true
        }
        Key.H -> {
            if (state.phase == RoundPhase.BUILDING) dispatch(GameEvent.Hint)
            true
        }
        Key.N -> {
            if (state.phase == RoundPhase.SOLVED || state.phase == RoundPhase.REVEALED) {
                dispatch(GameEvent.Next)
            }
            true
        }
        Key.Escape -> {
            if (state.phase == RoundPhase.BUILDING) dispatch(GameEvent.Clear)
            true
        }
        else -> false
    }
}
