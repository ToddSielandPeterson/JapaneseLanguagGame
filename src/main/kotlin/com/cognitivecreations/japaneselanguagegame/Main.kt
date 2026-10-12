package com.cognitivecreations.japaneselanguagegame

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cognitivecreations.japaneselanguagegame.data.ProgressStore
import java.awt.Dimension

fun main() = application {
    val store = ProgressStore()
    val saved = store.load()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sentence Builder — 文を作ろう",
        state = rememberWindowState(width = 1040.dp, height = 900.dp),
    ) {
        window.minimumSize = Dimension(780, 640)
        JapaneseLanguageGameApp(initial = saved, store = store)
    }
}
