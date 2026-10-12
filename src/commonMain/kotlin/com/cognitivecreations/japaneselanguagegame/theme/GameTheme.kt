package com.cognitivecreations.japaneselanguagegame.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cognitivecreations.japaneselanguagegame.generated.resources.NotoSansJP_Regular
import com.cognitivecreations.japaneselanguagegame.generated.resources.NotoSansJP_SemiBold
import com.cognitivecreations.japaneselanguagegame.generated.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * Dark palette carried over from the HTML game so the desktop port feels like the same desk.
 */
object Palette {
    val bg = Color(0xFF14121A)
    val bgGlow = Color(0xFF2A2038)
    val elev = Color(0xFF1E1B26)
    val soft = Color(0xFF262230)
    val softHover = Color(0xFF302B3C)
    val border = Color(0xFF3A3548)
    val borderStrong = Color(0xFF524C66)
    val text = Color(0xFFF3EFE8)
    val muted = Color(0xFFA89FBC)
    val accent = Color(0xFFE8A87C)
    val accentHot = Color(0xFFF0B892)
    val accentSoft = Color(0x26E8A87C)
    val good = Color(0xFF7DCEA0)
    val goodBg = Color(0x1F7DCEA0)
    val bad = Color(0xFFE88080)
    val badBg = Color(0x1FE88080)
    val badText = Color(0xFFF0A8A8)
    val particle = Color(0xFF9BB7E8)
    val particleBg = Color(0x249BB7E8)
    val particleBorder = Color(0x739BB7E8)
    val slot = Color(0xFF2A2636)
    val tile = Color(0xFF322D40)
    val ink = Color(0xFF1A1410)
    val n5 = Color(0xFF9FD4A8)
    val n4 = Color(0xFF9BB7E8)
    val n3 = Color(0xFFE8B87C)
}

/** Noto Sans JP, so kanji and furigana do not depend on the browser's installed fonts. */
val LocalGameFont = staticCompositionLocalOf<FontFamily> { FontFamily.SansSerif }

@Composable
fun rememberJapaneseFamily(): FontFamily = FontFamily(
    Font(Res.font.NotoSansJP_Regular, FontWeight.Normal),
    Font(Res.font.NotoSansJP_Regular, FontWeight.Medium),
    Font(Res.font.NotoSansJP_SemiBold, FontWeight.SemiBold),
    Font(Res.font.NotoSansJP_SemiBold, FontWeight.Bold),
)

private fun TextStyle.withFont(font: FontFamily) = copy(fontFamily = font)

private fun Typography.withJapanese(font: FontFamily) = copy(
    displayLarge = displayLarge.withFont(font),
    displayMedium = displayMedium.withFont(font),
    displaySmall = displaySmall.withFont(font),
    headlineLarge = headlineLarge.withFont(font),
    headlineMedium = headlineMedium.withFont(font),
    headlineSmall = headlineSmall.withFont(font),
    titleLarge = titleLarge.withFont(font),
    titleMedium = titleMedium.withFont(font),
    titleSmall = titleSmall.withFont(font),
    bodyLarge = bodyLarge.withFont(font),
    bodyMedium = bodyMedium.withFont(font),
    bodySmall = bodySmall.withFont(font),
    labelLarge = labelLarge.withFont(font),
    labelMedium = labelMedium.withFont(font),
    labelSmall = labelSmall.withFont(font),
)

private val Colors = darkColorScheme(
    primary = Palette.accent,
    onPrimary = Palette.ink,
    primaryContainer = Palette.accentSoft,
    onPrimaryContainer = Palette.accentHot,
    secondary = Palette.particle,
    onSecondary = Palette.ink,
    background = Palette.bg,
    onBackground = Palette.text,
    surface = Palette.elev,
    onSurface = Palette.text,
    surfaceVariant = Palette.soft,
    onSurfaceVariant = Palette.muted,
    outline = Palette.border,
    error = Palette.bad,
    onError = Palette.ink,
)

@Composable
fun GameTheme(
    fontFamily: FontFamily? = null,
    content: @Composable () -> Unit,
) {
    val family = fontFamily ?: rememberJapaneseFamily()
    MaterialTheme(
        colorScheme = Colors,
        typography = Typography().withJapanese(family),
    ) {
        CompositionLocalProvider(LocalGameFont provides family, content = content)
    }
}
