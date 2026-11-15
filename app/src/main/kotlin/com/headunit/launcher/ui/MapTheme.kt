package com.headunit.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.headunit.launcher.NavEngine
import com.headunit.launcher.PreferenceKeys

/** Deep teal of the turn card (and a darker shade for its lane and "then" rows). */
val GmGreen = Color(0xFF127A6C)
val GmGreenDeep = Color(0xFF0C5C52)

/** True while the Modern map colours are selected. */
val NavEngine.immersive: Boolean get() = prefs.s(PreferenceKeys.MAP_THEME, "immersive") == "immersive"

/** Surface of floating pills and chips: near-black at night, white by day. */
@Composable
fun immSurface(): Color = if (LocalVw.current.dark) Color(0xFF1C2030) else Color(0xFFFFFFFF)

/**
 * The palette shared by the map screen and the settings: adapts to Material 3 / dynamic color.
 */
fun navVw(c: Vw): Vw = c

/**
 * Glass panels: the cards turn see-through and pick up the colours of the map under them, with a slightly brighter edge.
 * Real blur is too slow for a head unit, so this is a tinted see-through look; text keeps full contrast.
 */
fun glassVw(c: Vw): Vw =
    if (c.dark) c.copy(surface = c.surface.copy(alpha = 0.75f), tile = c.tile.copy(alpha = 0.65f), line = Color(0x33FFFFFF))
    else c.copy(surface = c.surface.copy(alpha = 0.82f), tile = c.tile.copy(alpha = 0.72f), line = Color(0x26000000))

/**
 * A floating card: clipped to its shape and filled. Solid cards get a soft drop shadow; see-through (glass) cards don't,
 * because the shadow would show through the fill as a faint ring and corner lines.
 */
fun Modifier.panel(elev: Dp, shape: Shape, fill: Color): Modifier =
    (if (fill.alpha >= 0.99f) this.shadow(
        elevation = (elev * 0.75f).coerceAtLeast(3.dp),
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.16f),
        spotColor = Color.Black.copy(alpha = 0.22f)
    ) else this).clip(shape).background(fill)
