package com.headunit.launcher.ui

import com.headunit.launcher.L

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.composed
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.ForkLeft
import androidx.compose.material.icons.rounded.ForkRight
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.RoundaboutLeft
import androidx.compose.material.icons.rounded.RoundaboutRight
import androidx.compose.material.icons.rounded.Straight
import androidx.compose.material.icons.rounded.TurnLeft
import androidx.compose.material.icons.rounded.TurnRight
import androidx.compose.material.icons.rounded.TurnSlightLeft
import androidx.compose.material.icons.rounded.TurnSlightRight
import androidx.compose.material.icons.rounded.TurnSharpLeft
import androidx.compose.material.icons.rounded.TurnSharpRight
import androidx.compose.material.icons.rounded.UTurnLeft
import androidx.compose.material.icons.rounded.UTurnRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.headunit.launcher.MapBinding
import kotlin.math.cos
import kotlin.math.sin

/** Click with a fluid spring press scale and native Material 3 ripple (supporting optional long-press). */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressClick(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = this.composed {
    val src = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "press"
    )
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    this.graphicsLayer { scaleX = s; scaleY = s }.combinedClickable(
        interactionSource = src,
        indication = androidx.compose.material3.ripple(),
        onClick = {
            try { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
            onClick()
        },
        onLongClick = onLongClick?.let { action ->
            {
                try { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) } catch (_: Exception) {}
                action()
            }
        }
    )
}

fun Modifier.pressClick(onClick: () -> Unit): Modifier = pressClick(null, onClick)

/** Expressive Material 3 elevated card with soft tonal shadow and generous 26dp rounded corners. */
@Composable
fun VCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    val c = LocalVw.current
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.28f)
            )
            .clip(shape)
            .background(c.surface)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = androidx.compose.material3.ripple(),
                    onClick = onClick
                ) else Modifier
            ),
        content = content,
    )
}

/** Flat inset tile for rows that sit on a VCard, using M3 surfaceContainerHighest. */
@Composable
fun Modifier.inset(radius: androidx.compose.ui.unit.Dp = 18.dp, tint: Color? = null): Modifier {
    val c = LocalVw.current
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(if (tint != null) tint.copy(alpha = 0.16f) else c.tile)
}

/** Small quiet section label. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = LocalVw.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text.uppercase(), color = c.sub, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp, modifier = Modifier.weight(1f, fill = trailing != null))
        if (trailing != null) Text(trailing, color = c.sub, fontSize = 12.sp)
    }
}

/** Material 3 Expressive squircle tile holding a coloured glyph. */
@Composable
fun IconBadge(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 42.dp) {
    val c = LocalVw.current
    val wash = if (tint == c.text) c.tile else androidx.compose.ui.graphics.lerp(c.tile, tint, if (c.dark) 0.22f else 0.18f)
    Box(
        modifier.size(size).clip(RoundedCornerShape(14.dp)).background(wash),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.52f)) }
}

/** Page title: big headline with an optional subtitle using M3 typography scale. */
@Composable
fun PageTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val c = LocalVw.current
    Column(modifier) {
        Text(title, color = c.onBg, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp)
        if (subtitle != null) Text(subtitle, color = c.onBgSub, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/** Material 3 Expressive Floating Action Button (squircle with subtle elevation and spring physics). */
@Composable
fun RoundBtn(
    icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier,
    active: Boolean = false, size: Dp = 52.dp, tint: Color? = null,
) {
    val c = LocalVw.current
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .shadow(
                elevation = if (active) 4.dp else 2.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .pressClick(onClick)
            .size(size)
            .clip(shape)
            .background(if (active) c.accent else c.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint ?: if (active) Color.White else c.text, modifier = Modifier.size(size * 0.48f))
    }
}

/** Text that rolls vertically to its new value instead of snapping. */
@Composable
fun RollText(
    text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit, modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
) {
    androidx.compose.animation.AnimatedContent(
        targetState = text,
        modifier = modifier.clipToBounds(),
        transitionSpec = {
            (androidx.compose.animation.slideInVertically { it / 2 } + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(170))) togetherWith
                (androidx.compose.animation.slideOutVertically { -it / 2 } + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)))
        },
        label = "roll",
    ) { t -> Text(t, color = color, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
}

/** Speed colour that warms from white to amber as you near the limit (red once over it); null when there is no reason to tint. */
@Composable
fun speedTint(engine: com.headunit.launcher.NavEngine): Color? {
    if (engine.overLimit) return Bad
    val lim = engine.speedLimit ?: return null
    val sp = engine.fix?.speed ?: return null
    if (engine.limitEstimated || lim <= 0) return null
    val r = (sp / lim).toFloat()
    if (r < 0.85f) return null
    return androidx.compose.ui.graphics.lerp(LocalVw.current.text, Warn, ((r - 0.85f) / 0.12f).coerceIn(0f, 1f))
}

@Composable
fun Pill(
    text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, selectedColor: Color? = null,
) {
    val c = LocalVw.current
    val sel = selectedColor ?: c.accent
    val bg by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) sel else c.tile,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "pill"
    )
    val fg = if (selected) (if (sel.luminance() > 0.6f) Color(0xFF14171C) else Color.White) else c.text
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .shadow(
                elevation = if (selected) 2.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .pressClick(onClick)
            .clip(shape)
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, letterSpacing = 0.1.sp)
    }
}

@Composable
fun MapHost(map: MapBinding, modifier: Modifier = Modifier, cornerDp: Int = 0) {
    val dens by androidx.compose.runtime.rememberUpdatedState(androidx.compose.ui.platform.LocalDensity.current.density)
    val tint = LocalVw.current.mapTint
    Box(modifier) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            FrameLayout(ctx).apply {
                if (cornerDp > 0) {
                                        outlineProvider = object : android.view.ViewOutlineProvider() {
                        override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                            outline.setRoundRect(0, 0, view.width, view.height, cornerDp * dens)
                        }
                    }
                    clipToOutline = true
                }
                (map.mapView.parent as? ViewGroup)?.removeView(map.mapView)
                addView(map.mapView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                // the 3D locator sits just above the map (it's moved to the car's spot on screen as the camera moves)
                (map.roadView.parent as? ViewGroup)?.removeView(map.roadView)
                addView(map.roadView, FrameLayout.LayoutParams(1, 1))
                (map.locatorView.parent as? ViewGroup)?.removeView(map.locatorView)
                addView(map.locatorView, FrameLayout.LayoutParams(1, 1))
                post { map.placeLocator() }
            }
        },
        update = { it.invalidateOutline() },
        onRelease = { it.removeAllViews() },
    )
    // colour wash so the map matches the chosen look; touches pass straight through to the map
    if (tint.alpha > 0f) Box(Modifier.fillMaxSize().clip(RoundedCornerShape(cornerDp.dp)).background(tint))
    }
}

/** How far round a roundabout's exit is from the road you came in on (0 ahead, 90 right, -90 left), or NaN when unknown. */
fun roundaboutTurn(inBearing: Double, outBearing: Double): Float {
    if (inBearing < 0.0 || outBearing < 0.0) return Float.NaN
    var d = (outBearing - inBearing) % 360.0
    if (d > 180.0) d -= 360.0
    if (d <= -180.0) d += 360.0
    return d.toFloat()
}

/** Turn arrow drawn using official Google Material Navigation Icons. */
@Composable
fun ManeuverArrow(
    maneuver: String,
    angle: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    roundaboutExit: Int = 0,
    roundaboutClockwise: Boolean = true,
    /** The real exit direction at a roundabout (degrees, 0 ahead, 90 right), when the route data has it. */
    exitAngle: Float = Float.NaN,
) {
    val u = maneuver.uppercase()
    val (icon, rot) = androidx.compose.runtime.remember(u, angle) {
        when {
            u.startsWith("ARRIVE") -> Icons.Rounded.Flag to 0f
            u == "MAKE_UTURN" || u == "ROUNDABOUT_BACK" || u == "LEFT_U_TURN" -> Icons.Rounded.UTurnLeft to 0f
            u == "RIGHT_U_TURN" -> Icons.Rounded.UTurnRight to 0f
            u.startsWith("ROUNDABOUT") && (u.contains("LEFT") || angle < -20f) -> Icons.Rounded.RoundaboutLeft to 0f
            u.startsWith("ROUNDABOUT") -> Icons.Rounded.RoundaboutRight to 0f
            u.contains("MERGE") -> Icons.Rounded.Merge to 0f
            u.contains("FORK") && u.contains("LEFT") -> Icons.Rounded.ForkLeft to 0f
            u.contains("FORK") && u.contains("RIGHT") -> Icons.Rounded.ForkRight to 0f
            u == "SHARP_LEFT" || u.contains("SHARP_LEFT") -> Icons.Rounded.TurnSharpLeft to 0f
            u == "SHARP_RIGHT" || u.contains("SHARP_RIGHT") -> Icons.Rounded.TurnSharpRight to 0f
            u == "BEAR_LEFT" || u == "KEEP_LEFT" || u == "SLIGHT_LEFT" || u.contains("SLIGHT_LEFT") -> Icons.Rounded.TurnSlightLeft to 0f
            u == "BEAR_RIGHT" || u == "KEEP_RIGHT" || u == "SLIGHT_RIGHT" || u.contains("SLIGHT_RIGHT") -> Icons.Rounded.TurnSlightRight to 0f
            u == "TURN_LEFT" || u == "LEFT" -> Icons.Rounded.TurnLeft to 0f
            u == "TURN_RIGHT" || u == "RIGHT" -> Icons.Rounded.TurnRight to 0f
            u == "STRAIGHT" || u == "DEPART" || u == "FOLLOW" -> Icons.Rounded.Straight to 0f
            u.contains("LEFT") -> Icons.Rounded.TurnLeft to 0f
            u.contains("RIGHT") -> Icons.Rounded.TurnRight to 0f
            else -> Icons.Rounded.Straight to 0f
        }
    }
    Icon(
        imageVector = icon,
        contentDescription = maneuver,
        tint = color,
        modifier = if (rot != 0f) modifier.graphicsLayer { rotationZ = rot } else modifier
    )
}

/** Returns a function that opens the system speech recogniser and passes back the first result. */
@Composable
fun rememberVoiceInput(prompt: String, onResult: (String) -> Unit): () -> Unit {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val latest = androidx.compose.runtime.rememberUpdatedState(onResult)
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) { r ->
        val text = r.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (r.resultCode == android.app.Activity.RESULT_OK && !text.isNullOrBlank()) latest.value(text)
    }
    return {
        try {
            launcher.launch(
                android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    // (listens in the app's language, so a Dutch, German, French or Spanish command is heard right)
                    .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, L.locale.toLanguageTag())
                    .putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, prompt),
            )
        } catch (e: Exception) {
            android.widget.Toast.makeText(ctx, L.t("No voice recognition app found on this device"), android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

/** Material 3 Expressive floating card: clean tonal container with soft drop shadow. */
@Composable
fun Modifier.floating(radius: Dp = 26.dp): Modifier {
    val c = LocalVw.current
    val shape = RoundedCornerShape(radius)
    return this
        .shadow(
            elevation = 5.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.20f),
            spotColor = Color.Black.copy(alpha = 0.28f)
        )
        .clip(shape)
        .background(c.surface)
}

/**
 * A looping 0..1 value that only exists while [active]. Running infinite animations keep the screen redrawing 60 times a second,
 * so they are switched on only when something is actually pulsing.
 */
@Composable
fun rememberPulse(active: Boolean, ms: Int, from: Float = 0f, to: Float = 1f): androidx.compose.runtime.State<Float> {
    if (!active) return androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(from) }
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    return t.animateFloat(
        from, to,
        androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(ms, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "pulseV",
    )
}

/** Themed on/off switch: accent track with checkmark thumb when on, MD3 outlined track when off. */
@Composable
fun Switch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalVw.current
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onChange,
        thumbContent = if (checked) {
            {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = if (c.accent.luminance() > 0.6f) Color(0xFF10131C) else Color.White,
            checkedTrackColor = c.accent,
            checkedBorderColor = c.accent,
            checkedIconColor = c.accent,
            uncheckedThumbColor = c.sub,
            uncheckedTrackColor = c.tile.copy(alpha = 0.4f),
            uncheckedBorderColor = c.sub.copy(alpha = 0.6f),
        ),
    )
}
