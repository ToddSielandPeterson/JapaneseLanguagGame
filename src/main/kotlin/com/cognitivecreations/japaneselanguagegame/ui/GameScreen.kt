package com.cognitivecreations.japaneselanguagegame.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cognitivecreations.japaneselanguagegame.domain.Exercise
import com.cognitivecreations.japaneselanguagegame.domain.GameEvent
import com.cognitivecreations.japaneselanguagegame.domain.GameState
import com.cognitivecreations.japaneselanguagegame.domain.JlptLevel
import com.cognitivecreations.japaneselanguagegame.domain.RoundPhase
import com.cognitivecreations.japaneselanguagegame.domain.Tile
import com.cognitivecreations.japaneselanguagegame.theme.Palette
import kotlin.math.roundToInt

private data class DragGhost(
    val tile: Tile,
    val origin: TileOrigin,
    val barIndex: Int?,
    val position: Offset,
)

@Composable
fun GameScreen(
    state: GameState,
    exercise: Exercise?,
    onEvent: (GameEvent) -> Unit,
    resetDialogOpen: Boolean,
    onResetDialogOpen: (Boolean) -> Unit,
) {
    var barBounds by remember { mutableStateOf<Rect?>(null) }
    var poolBounds by remember { mutableStateOf<Rect?>(null) }
    var ghost by remember { mutableStateOf<DragGhost?>(null) }
    val scroll = rememberScrollState()
    val building = state.phase == RoundPhase.BUILDING
    val roundTiles = remember(exercise, state.tileOrder) {
        val source = exercise
        if (source == null) emptyList() else state.tileOrder.mapNotNull { source.tile(it) }
    }
    val tileById = remember(roundTiles) { roundTiles.associateBy { it.id } }
    val placedTiles = state.placed.mapNotNull { tileById[it] }

    fun drop(session: DragGhost, position: Offset) {
        val onBar = barBounds?.contains(position) == true
        val onPool = poolBounds?.contains(position) == true
        when (session.origin) {
            TileOrigin.POOL -> if (onBar) onEvent(GameEvent.Place(session.tile.id))
            TileOrigin.BAR -> when {
                onPool -> session.barIndex?.let { onEvent(GameEvent.RemoveAt(it)) }
                onBar -> {
                    val index = session.barIndex ?: return
                    onEvent(GameEvent.RemoveAt(index))
                    onEvent(GameEvent.Place(session.tile.id))
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 920.dp).fillMaxWidth()) {
                Header(state)
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.elev)
                        .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Controls(
                        state = state,
                        onEvent = onEvent,
                        onReset = { onResetDialogOpen(true) },
                    )
                    Spacer(Modifier.height(14.dp))
                    MetaRow(state)
                    Spacer(Modifier.height(10.dp))
                    if (exercise == null) {
                        EmptyLevel()
                    } else {
                        Text(
                            text = exercise.english,
                            color = Palette.text,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 34.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (state.hintShown) "Hint: ${exercise.hint}" else " ",
                            color = if (state.hintShown) Palette.accentHot else Palette.muted,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.heightIn(min = 22.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        SentenceBar(
                            phase = state.phase,
                            tiles = placedTiles,
                            showFurigana = state.furigana,
                            enabled = building,
                            onRemove = { index -> onEvent(GameEvent.RemoveAt(index)) },
                            onDragStart = { index, tile, position ->
                                ghost = DragGhost(tile, TileOrigin.BAR, index, position)
                            },
                            onDrag = { position -> ghost = ghost?.copy(position = position) },
                            onDragEnd = { position ->
                                val session = ghost
                                ghost = null
                                if (session != null) drop(session, position)
                            },
                            onBounds = { barBounds = it },
                        )
                        Spacer(Modifier.height(12.dp))
                        Pool(
                            tiles = roundTiles,
                            placed = state.placed.toSet(),
                            showFurigana = state.furigana,
                            enabled = building,
                            onPlace = { onEvent(GameEvent.Place(it)) },
                            onDragStart = { tile, position ->
                                ghost = DragGhost(tile, TileOrigin.POOL, null, position)
                            },
                            onDrag = { position -> ghost = ghost?.copy(position = position) },
                            onDragEnd = { position ->
                                val session = ghost
                                ghost = null
                                if (session != null) drop(session, position)
                            },
                            onBounds = { poolBounds = it },
                        )
                        Spacer(Modifier.height(14.dp))
                        Actions(state = state, onEvent = onEvent)
                        Spacer(Modifier.height(12.dp))
                        Feedback(state = state, exercise = exercise)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Click a tile to place it · click a placed tile to return it · drag onto the sentence bar or back to the pool. " +
                                "Distractors, when on, are extra tiles that must stay unused. " +
                                "Keyboard: Enter checks, or moves on after a result · H hint · N next · Esc clear.",
                            color = Palette.muted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scroll),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 8.dp, horizontal = 2.dp),
        )
        val floating = ghost
        if (floating != null) {
            WordTile(
                tile = floating.tile,
                showFurigana = state.furigana,
                used = false,
                enabled = false,
                onTap = {},
                onDragStart = {},
                onDrag = {},
                onDragEnd = {},
                ghost = true,
                modifier = Modifier
                    .zIndex(2f)
                    .offsetGhost(floating.position),
            )
        }
    }

    if (resetDialogOpen) {
        AlertDialog(
            onDismissRequest = { onResetDialogOpen(false) },
            containerColor = Palette.elev,
            titleContentColor = Palette.text,
            textContentColor = Palette.muted,
            title = { Text("Reset score, streak, and best?") },
            text = { Text("The current sentence stays. Level, furigana, and distractors stay too.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDialogOpen(false)
                        onEvent(GameEvent.ResetStats)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.bad,
                        contentColor = Palette.ink,
                    ),
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { onResetDialogOpen(false) }) {
                    Text("Cancel", color = Palette.muted)
                }
            },
        )
    }
}

private fun Modifier.offsetGhost(position: Offset): Modifier = offset {
    IntOffset((position.x - 28).roundToInt(), (position.y - 22).roundToInt())
}

@Composable
private fun Header(state: GameState) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.padding(end = 16.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("Sentence Builder — ")
                    withStyle(SpanStyle(color = Palette.accent)) {
                        append("文を作ろう")
                    }
                },
                color = Palette.text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Build Japanese sentences tile by tile. Practice particles and word order.",
                color = Palette.muted,
                fontSize = 15.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill("Score", state.score)
            StatPill("Streak", state.streak)
            StatPill("Best", state.best)
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Palette.elev)
            .border(1.dp, Palette.border, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Palette.muted, fontSize = 14.sp)
        Text(
            text = " $value",
            color = Palette.accentHot,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Controls(
    state: GameState,
    onEvent: (GameEvent) -> Unit,
    onReset: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LevelSelector(selected = state.jlpt, onSelect = { onEvent(GameEvent.SelectLevel(it)) })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(
                label = if (state.distractorsEnabled) "Distractors ON" else "Distractors OFF",
                pressed = state.distractorsEnabled,
                onClick = { onEvent(GameEvent.ToggleDistractors) },
            )
            ToggleChip(
                label = if (state.furigana) "ふりがな ON" else "ふりがな OFF",
                pressed = state.furigana,
                onClick = { onEvent(GameEvent.ToggleFurigana) },
            )
            ToggleChip(
                label = "Reset",
                pressed = false,
                danger = true,
                onClick = onReset,
            )
        }
    }
}

@Composable
private fun LevelSelector(selected: JlptLevel, onSelect: (JlptLevel) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Palette.soft)
            .border(1.dp, Palette.border, RoundedCornerShape(999.dp))
            .padding(3.dp)
            .semantics { contentDescription = "JLPT level" },
    ) {
        JlptLevel.entries.forEach { level ->
            val on = level == selected
            val enabled = level.playable
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (on) Palette.accent else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable(enabled = enabled, onClick = { onSelect(level) })
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .semantics {
                        contentDescription = if (enabled) level.name else "${level.name}, coming soon"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = level.name,
                    color = when {
                        on -> Palette.ink
                        !enabled -> Palette.muted.copy(alpha = 0.45f)
                        else -> Palette.muted
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun ToggleChip(
    label: String,
    pressed: Boolean,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    val background = when {
        pressed -> Palette.accent
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val content = when {
        pressed -> Palette.ink
        danger -> Palette.bad
        else -> Palette.text
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(
                1.dp,
                if (danger) Palette.bad.copy(alpha = 0.55f) else Palette.border,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = content, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun MetaRow(state: GameState) {
    val total = state.queue.size
    val progress = if (total == 0) "0 / 0" else "${state.index + 1} / $total"
    val badgeColor = when (state.jlpt) {
        JlptLevel.N5 -> Palette.n5
        JlptLevel.N4 -> Palette.n4
        JlptLevel.N3 -> Palette.n3
        JlptLevel.N2 -> Palette.muted
    }
    val badgeBg = when (state.jlpt) {
        JlptLevel.N5 -> Palette.goodBg
        JlptLevel.N4 -> Palette.particleBg
        JlptLevel.N3 -> Palette.accentSoft
        JlptLevel.N2 -> Palette.soft
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = state.jlpt.name,
            color = badgeColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
        Text(progress, color = Palette.muted, fontSize = 14.sp)
    }
}

@Composable
private fun EmptyLevel() {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Text(
            "No exercises for this level.",
            color = Palette.muted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SentenceBar(
    phase: RoundPhase,
    tiles: List<Tile>,
    showFurigana: Boolean,
    enabled: Boolean,
    onRemove: (Int) -> Unit,
    onDragStart: (Int, Tile, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onBounds: (Rect) -> Unit,
) {
    val fill = when (phase) {
        RoundPhase.SOLVED -> Palette.goodBg
        RoundPhase.WRONG_FLASH, RoundPhase.REVEALED -> Palette.badBg
        RoundPhase.BUILDING -> Palette.slot
    }
    val stroke = when (phase) {
        RoundPhase.SOLVED -> Palette.good
        RoundPhase.WRONG_FLASH, RoundPhase.REVEALED -> Palette.bad
        RoundPhase.BUILDING -> Palette.borderStrong
    }
    val dashed = phase == RoundPhase.BUILDING
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fill)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val style = if (dashed) {
                    Stroke(strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
                } else {
                    Stroke(strokeWidth)
                }
                drawRoundRect(
                    color = stroke,
                    style = style,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                )
            }
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .padding(10.dp)
            .semantics { contentDescription = "Your sentence" },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (tiles.isEmpty()) {
            Text(
                "Tap tiles below to build the sentence",
                color = Palette.muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tiles.forEachIndexed { index, tile ->
                    WordTile(
                        tile = tile,
                        showFurigana = showFurigana,
                        used = false,
                        enabled = enabled,
                        onTap = { onRemove(index) },
                        onDragStart = { position -> onDragStart(index, tile, position) },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                    )
                }
            }
        }
    }
}

@Composable
private fun Pool(
    tiles: List<Tile>,
    placed: Set<String>,
    showFurigana: Boolean,
    enabled: Boolean,
    onPlace: (String) -> Unit,
    onDragStart: (Tile, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onBounds: (Rect) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .semantics { contentDescription = "Word tiles" },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { tile ->
            val used = tile.id in placed
            WordTile(
                tile = tile,
                showFurigana = showFurigana,
                used = used,
                enabled = enabled,
                onTap = { onPlace(tile.id) },
                onDragStart = { position -> onDragStart(tile, position) },
                onDrag = onDrag,
                onDragEnd = onDragEnd,
            )
        }
    }
}

@Composable
private fun Actions(state: GameState, onEvent: (GameEvent) -> Unit) {
    val building = state.phase == RoundPhase.BUILDING
    val canAdvance = state.phase == RoundPhase.SOLVED || state.phase == RoundPhase.REVEALED
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton("Check", primary = true, enabled = building && state.placed.isNotEmpty()) {
            onEvent(GameEvent.Check)
        }
        ActionButton("Hint", enabled = building) { onEvent(GameEvent.Hint) }
        ActionButton("Reveal", enabled = building) { onEvent(GameEvent.Reveal) }
        ActionButton("Clear", enabled = building && state.placed.isNotEmpty()) { onEvent(GameEvent.Clear) }
        ActionButton("Next →", enabled = canAdvance) { onEvent(GameEvent.Next) }
    }
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = if (primary) {
            ButtonDefaults.buttonColors(
                containerColor = Palette.accent,
                contentColor = Palette.ink,
                disabledContainerColor = Palette.soft,
                disabledContentColor = Palette.muted.copy(alpha = 0.7f),
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Palette.soft,
                contentColor = Palette.text,
                disabledContainerColor = Palette.soft.copy(alpha = 0.5f),
                disabledContentColor = Palette.muted.copy(alpha = 0.55f),
            )
        },
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Feedback(state: GameState, exercise: Exercise) {
    val shape = RoundedCornerShape(10.dp)
    val (background, border, title, titleColor) = when (state.phase) {
        RoundPhase.SOLVED -> FeedbackStyle(
            Palette.goodBg,
            Palette.good.copy(alpha = 0.45f),
            "Correct.",
            Palette.good,
        )
        RoundPhase.REVEALED -> FeedbackStyle(
            Palette.badBg,
            Palette.bad.copy(alpha = 0.45f),
            "Answer revealed. This counts as a miss.",
            Palette.badText,
        )
        RoundPhase.WRONG_FLASH -> FeedbackStyle(
            Palette.badBg,
            Palette.bad.copy(alpha = 0.45f),
            "Not quite. Try again, use Hint, or Reveal.",
            Palette.badText,
        )
        RoundPhase.BUILDING -> FeedbackStyle(
            androidx.compose.ui.graphics.Color.Transparent,
            androidx.compose.ui.graphics.Color.Transparent,
            "",
            Palette.muted,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (title.isNotEmpty()) {
            Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        if (state.phase == RoundPhase.WRONG_FLASH) {
            Text(
                "Check the particle and the verb-last order. A distractor anywhere in the sentence fails the check.",
                color = Palette.muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (state.phase == RoundPhase.REVEALED) {
            Spacer(Modifier.height(8.dp))
            AnswerReadout(exercise.answerTiles(), state.furigana)
        }
        if ((state.phase == RoundPhase.SOLVED || state.phase == RoundPhase.REVEALED) && exercise.notes.isNotBlank()) {
            Text(
                exercise.notes,
                color = Palette.muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private data class FeedbackStyle(
    val background: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val title: String,
    val titleColor: androidx.compose.ui.graphics.Color,
)
