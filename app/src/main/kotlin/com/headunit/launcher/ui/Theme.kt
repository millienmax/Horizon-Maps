package com.headunit.launcher.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.headunit.launcher.Sun

data class Vw(
    val dark: Boolean,
    val bg: Color, val surface: Color, val surface2: Color,
    val text: Color, val sub: Color, val accent: Color, val line: Color,
    val panel: Color, val tile: Color,
    /** Text colours for things drawn straight on the background, so they stay readable when the background colour is overridden. */
    val onBg: Color = text, val onBgSub: Color = sub,
    /** Wash laid over the map so it matches non-default looks. */
    val mapTint: Color = Color.Transparent,
)

val DarkVw = Vw(
    dark = true,
    bg = Color(0xFF101418),
    surface = Color(0xFF1C2024),
    surface2 = Color(0xFF272A2F),
    text = Color(0xFFE1E2E8),
    sub = Color(0xFFC3C7D0),
    accent = Color(0xFF9ECAFF),
    line = Color(0xFF43474E),
    panel = Color(0xFF181C20),
    tile = Color(0xFF32353A)
)

val LightVw = Vw(
    dark = false,
    bg = Color(0xFFF7F9FF),
    surface = Color(0xFFEBEFF5),
    surface2 = Color(0xFFE5E9EF),
    text = Color(0xFF181C20),
    sub = Color(0xFF43474E),
    accent = Color(0xFF0061A4),
    line = Color(0xFFC3C7D0),
    panel = Color(0xFFF1F4FA),
    tile = Color(0xFFDFE3E9)
)

val ExpressiveShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

val Md3DarkExpressive = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBDC7DC),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3E4758),
    onSecondaryContainer = Color(0xFFD9E3F9),
    tertiary = Color(0xFFFFB784),
    onTertiary = Color(0xFF4F2500),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7D0),
    surfaceContainerLowest = Color(0xFF0B0E12),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF272A2F),
    surfaceContainerHighest = Color(0xFF32353A),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E),
)

val Md3LightExpressive = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF8B5000),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F9FF),
    onBackground = Color(0xFF181C20),
    surface = Color(0xFFF7F9FF),
    onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4FA),
    surfaceContainer = Color(0xFFEBEFF5),
    surfaceContainerHigh = Color(0xFFE5E9EF),
    surfaceContainerHighest = Color(0xFFDFE3E9),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7D0),
)

val LocalVw = staticCompositionLocalOf { DarkVw }

val Blue = Color(0xFF2A3647)
val BlueSoft = Color(0xFF364559)
val Good = Color(0xFF34C759)
val Warn = Color(0xFFFFB020)
val Bad = Color(0xFFFF4D4D)

@Composable
fun resolveDark(mode: String, lat: Double?, lon: Double?, nowMs: Long, sysSeen: Boolean = false, system: Boolean = isSystemInDarkTheme(),
                nightFrom: String = "19:00", dayFrom: String = "07:00"): Boolean {
    val sun = if (lat != null && lon != null) !Sun.isDay(lat, lon, nowMs) else null
    return when (mode) {
        "day" -> false
        "night" -> true
        // your own times (for a unit with no GPS fix to work out sunset from, or if you just prefer them)
        "times" -> isNightByClock(nowMs, nightFrom, dayFrom)
        "sun" -> if (lat != null && lon != null) !Sun.isDay(lat, lon, nowMs) else system
        // headlights: head units that switch their own night mode with the lights (the illumination wire) are followed; until this
        // unit has been seen doing that, sunrise and sunset stand in
        "lights" -> if (system) true else if (sysSeen) false else if (lat != null && lon != null) !Sun.isDay(lat, lon, nowMs) else false
        // auto: the head unit's own night mode once it has been seen switching to it; sunrise and sunset until then (many units never
        // switch their system mode, and auto then never went dark)
        else -> if (system) true else if (sysSeen) false else sun ?: false
    }
}

/** Whether [nowMs] falls between [nightFrom] and [dayFrom] ("HH:MM", local time; the night runs over midnight when it starts later). */
fun isNightByClock(nowMs: Long, nightFrom: String, dayFrom: String): Boolean {
    fun mins(t: String, def: Int): Int {
        val p = t.trim().replace('.', ':').split(':')
        val h = p.getOrNull(0)?.toIntOrNull() ?: return def
        val m = p.getOrNull(1)?.toIntOrNull() ?: 0
        return if (h in 0..23 && m in 0..59) h * 60 + m else def
    }
    val c = java.util.Calendar.getInstance().apply { timeInMillis = nowMs }
    val now = c.get(java.util.Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE)
    val n = mins(nightFrom, 19 * 60); val d = mins(dayFrom, 7 * 60)
    return if (n > d) now >= n || now < d else now in n until d
}

/**
 * The head unit's own night mode, read from the system every few seconds as well as when the screen's settings change: some units
 * switch it with the headlights without telling the app it changed, so the map stayed in day mode.
 */
@Composable
fun rememberSystemNight(useLamps: Boolean = false): Boolean {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val fromConfig = isSystemInDarkTheme()
    fun read() = readSystemNight(ctx) || (useLamps && readLampsNight(ctx) == true)
    var polled by androidx.compose.runtime.remember(useLamps) { androidx.compose.runtime.mutableStateOf(read()) }
    // checked every 1.5 s (it was every 4 s), and straight away when the system's night mode setting itself changes: some units
    // set that first and only change the screen's mode later
    androidx.compose.runtime.LaunchedEffect(useLamps) { while (true) { kotlinx.coroutines.delay(1500L); polled = read() } }
    androidx.compose.runtime.DisposableEffect(useLamps) {
        val cr = ctx.applicationContext.contentResolver
        val obs = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { polled = read() }
        }
        try { cr.registerContentObserver(android.provider.Settings.Secure.getUriFor("ui_night_mode"), false, obs) } catch (e: Throwable) { }
        if (useLamps) try { cr.registerContentObserver(android.provider.Settings.System.getUriFor("car_mode"), false, obs) } catch (e: Throwable) { }
        onDispose { try { cr.unregisterContentObserver(obs) } catch (e: Throwable) { } }
    }
    NightWatch.note(fromConfig, "screen")
    return fromConfig || polled
}

/**
 * The headlight (illumination, "ILL") wire as some head units pass it on without switching Android's night mode: Teyes and FYT-type
 * units keep it in the system setting "car_mode" (0 = lights off). Null when this unit has no such setting. A few units have it the
 * other way round: Settings > Map > "Lights signal is reversed" swaps it.
 */
fun readLampsNight(ctx: android.content.Context): Boolean? = try {
    val v = android.provider.Settings.System.getInt(ctx.applicationContext.contentResolver, "car_mode", Int.MIN_VALUE)
    if (v == Int.MIN_VALUE || v < 0) null else {
        val inv = com.headunit.launcher.Prefs(ctx.applicationContext).b(com.headunit.launcher.PreferenceKeys.LAMPS_INVERT, false)
        val night = (v != 0) != inv
        NightWatch.lamps = v
        NightWatch.note(night, "lights")
        night
    }
} catch (e: Throwable) { null }

fun readSystemNight(ctx: android.content.Context): Boolean = try {
    val cfg = ctx.applicationContext.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    val um = (ctx.getSystemService(android.content.Context.UI_MODE_SERVICE) as? android.app.UiModeManager)?.nightMode
    NightWatch.note(cfg == android.content.res.Configuration.UI_MODE_NIGHT_YES, "config")
    NightWatch.note(um == android.app.UiModeManager.MODE_NIGHT_YES, "setting")
    NightWatch.setting = when (um) { android.app.UiModeManager.MODE_NIGHT_YES -> "night"; android.app.UiModeManager.MODE_NIGHT_NO -> "day"; android.app.UiModeManager.MODE_NIGHT_AUTO -> "auto"; null -> "?"; else -> "custom" }
    cfg == android.content.res.Configuration.UI_MODE_NIGHT_YES || um == android.app.UiModeManager.MODE_NIGHT_YES
} catch (e: Throwable) { false }

/**
 * For the feedback report: when each of the head unit's night signals last changed, so a slow day / night switch shows which
 * signal was late (the screen's mode, the system setting, or both).
 */
object NightWatch {
    @Volatile var setting = "?"
    /** The unit's headlight setting ("car_mode") as last read, or null when it has none. */
    @Volatile var lamps: Int? = null
    private val last = java.util.concurrent.ConcurrentHashMap<String, Pair<Boolean, Long>>()

    fun note(night: Boolean, what: String) {
        val p = last[what]
        if (p == null || p.first != night) last[what] = night to System.currentTimeMillis()
    }

    fun line(): String {
        val fmt = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.UK)
        return "Night signals: system setting $setting" + (lamps?.let { " · car_mode $it" } ?: "") + " · " + listOf("screen", "config", "setting", "lights").filter { it != "lights" || last.containsKey(it) }.joinToString(" · ") { k ->
            val p = last[k]
            if (p == null) "$k ?" else "$k ${if (p.first) "night" else "day"} since ${fmt.format(java.util.Date(p.second))}"
        }
    }
}

@Composable
fun VwTheme(dark: Boolean, content: @Composable () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val supportsDynamic = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    val useDynamic = supportsDynamic && try {
        com.headunit.launcher.Prefs(ctx.applicationContext).b("dynamic_color", true)
    } catch (_: Throwable) {
        true
    }

    val scheme = if (useDynamic) {
        try {
            if (dark) androidx.compose.material3.dynamicDarkColorScheme(ctx)
            else androidx.compose.material3.dynamicLightColorScheme(ctx)
        } catch (_: Throwable) {
            if (dark) Md3DarkExpressive else Md3LightExpressive
        }
    } else {
        if (dark) Md3DarkExpressive else Md3LightExpressive
    }

    val baseVw = if (dark) DarkVw else LightVw
    val v = baseVw.copy(
        dark = dark,
        bg = scheme.background,
        surface = scheme.surfaceContainer,
        surface2 = scheme.surfaceContainerHigh,
        text = scheme.onSurface,
        sub = scheme.onSurfaceVariant,
        accent = scheme.primary,
        line = scheme.outlineVariant,
        panel = scheme.surfaceContainerLow,
        tile = scheme.surfaceContainerHighest,
        onBg = scheme.onBackground,
        onBgSub = scheme.onSurfaceVariant
    )

    CompositionLocalProvider(LocalVw provides v) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}

val NavBlue = Color(0xFF4DA3FF)
