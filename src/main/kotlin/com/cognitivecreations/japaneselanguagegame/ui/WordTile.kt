package com.cognitivecreations.japaneselanguagegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitivecreations.japaneselanguagegame.domain.Tile
import com.cognitivecreations.japaneselanguagegame.theme.Palette

enum class TileOrigin { POOL, BAR }

@Composable
fun WordTile(
    tile: Tile,
    showFurigana: Boolean,
    used: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    ghost: Boolean = false,
) {
    val shape = RoundedCornerShape(10.dp)
    val particle = tile.role.particleStyled
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val label = when {
        tile.showsReading && tile.reading != null -> "${tile.surface}, ${tile.reading}"
        else -> tile.surface
    }
    val borderColor = when {
        ghost -> Palette.accent
        hovered && enabled && !used -> Palette.accent
        particle -> Palette.particleBorder
        else -> Palette.borderStrong
    }
    val background = if (particle) Palette.particleBg else Palette.tile
    val textColor = if (particle) Palette.particle else Palette.text

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates = it }
            .alpha(if (used && !ghost) 0.28f else 1f)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .hoverable(interaction, enabled = enabled && !used && !ghost)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .then(
                if (!enabled || used || ghost) {
                    Modifier
                } else {
                    Modifier.pointerInput(tile.id) {
                        val slop = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            val origin = coordinates?.localToRoot(down.position) ?: down.position
                            var last = origin
                            var dragged = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    if (dragged) onDragEnd(last) else onTap()
                                    break
                                }
                                val root = coordinates?.localToRoot(change.position) ?: change.position
                                if (!dragged && (root - origin).getDistance() > slop) {
                                    dragged = true
                                    onDragStart(root)
                                }
                                if (dragged) {
                                    change.consume()
                                    last = root
                                    onDrag(root)
                                }
                            }
                        }
                    }
                },
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        FuriganaLabel(
            tile = tile,
            showFurigana = showFurigana,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = if (particle) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}
