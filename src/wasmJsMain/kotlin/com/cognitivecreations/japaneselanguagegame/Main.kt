package com.cognitivecreations.japaneselanguagegame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.ComposeViewport
import com.cognitivecreations.japaneselanguagegame.data.createProgressStore
import com.cognitivecreations.japaneselanguagegame.generated.resources.NotoSansJP_Regular
import com.cognitivecreations.japaneselanguagegame.generated.resources.NotoSansJP_SemiBold
import com.cognitivecreations.japaneselanguagegame.generated.resources.Res
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "webApp") {
        WebApp()
    }
}

/**
 * Loads Noto Sans JP before the first frame of tiles so furigana is not drawn
 * with a fallback font that has no kana or kanji.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WebApp() {
    val regular by preloadFont(Res.font.NotoSansJP_Regular, FontWeight.Normal)
    val medium by preloadFont(Res.font.NotoSansJP_Regular, FontWeight.Medium)
    val semi by preloadFont(Res.font.NotoSansJP_SemiBold, FontWeight.SemiBold)
    val bold by preloadFont(Res.font.NotoSansJP_SemiBold, FontWeight.Bold)
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(5_000)
        timedOut = true
    }
    val ready = regular != null && medium != null && semi != null && bold != null
    if (!ready && !timedOut) {
        Box(Modifier.fillMaxSize().background(Color(0xFF14121A)))
        return
    }
    val store = remember { createProgressStore() }
    val initial = remember { store.load() }
    val family = if (ready) {
        FontFamily(regular!!, medium!!, semi!!, bold!!)
    } else {
        null
    }
    JapaneseLanguageGameApp(initial = initial, store = store, fontFamily = family)
}
