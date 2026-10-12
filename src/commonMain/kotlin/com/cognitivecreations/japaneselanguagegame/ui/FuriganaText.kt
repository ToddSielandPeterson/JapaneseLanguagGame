package com.cognitivecreations.japaneselanguagegame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitivecreations.japaneselanguagegame.domain.Tile
import com.cognitivecreations.japaneselanguagegame.theme.LocalGameFont
import com.cognitivecreations.japaneselanguagegame.theme.Palette

/**
 * Kanji with a reading stacked above it. Kana-only tiles stay a single line,
 * matching the HTML rule that ruby is omitted when the surface has no kanji.
 */
@Composable
fun FuriganaLabel(
    tile: Tile,
    showFurigana: Boolean,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Medium,
) {
    val font = LocalGameFont.current
    val reading = tile.reading
    if (showFurigana && tile.showsReading && reading != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = reading,
                color = Palette.muted,
                fontSize = fontSize * 0.5f,
                lineHeight = fontSize * 0.7f,
                fontWeight = FontWeight.Normal,
                fontFamily = font,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Visible,
            )
            Text(
                text = tile.surface,
                color = color,
                fontSize = fontSize,
                lineHeight = fontSize * 1.2f,
                fontWeight = fontWeight,
                fontFamily = font,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Visible,
            )
        }
    } else {
        Text(
            text = tile.surface,
            color = color,
            fontSize = fontSize,
            lineHeight = fontSize * 1.2f,
            fontWeight = fontWeight,
            fontFamily = font,
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
fun AnswerReadout(tiles: List<Tile>, showFurigana: Boolean) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tiles.forEachIndexed { index, tile ->
            if (index > 0) {
                Text(
                    text = "·",
                    color = Palette.muted.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                )
            }
            val color = if (tile.role.particleStyled) Palette.particle else Palette.text
            FuriganaLabel(
                tile = tile,
                showFurigana = showFurigana,
                color = color,
                fontSize = 18.sp,
                fontWeight = if (tile.role.particleStyled) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}
