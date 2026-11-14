package app.getknit.knit.ui.theme

import android.os.Build
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection

// `internal` rather than private so ColorSchemeTest can assert over the roles directly.
internal val LightColorScheme =
    lightColorScheme(
        primary = BrandPrimaryLight,
        onPrimary = BrandOnPrimaryLight,
        primaryContainer = BrandPrimaryContainerLight,
        onPrimaryContainer = BrandOnPrimaryContainerLight,
        secondary = BrandSecondaryLight,
        onSecondary = BrandOnSecondaryLight,
        secondaryContainer = BrandSecondaryContainerLight,
        onSecondaryContainer = BrandOnSecondaryContainerLight,
        tertiary = PositiveLight,
        onTertiary = OnPositiveLight,
        tertiaryContainer = BrandTertiaryContainerLight,
        onTertiaryContainer = BrandOnTertiaryContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        error = ErrorLight,
        onError = OnErrorLight,
        // The roles below used to be left to lightColorScheme()'s defaults, which are Material's
        // BASELINE palette — purple-grey neutrals under every Card, AlertDialog, DropdownMenu, sheet and
        // scrolled TopAppBar, next to the brand's own surfaces. Tones follow Material's own role assignment
        // (dynamicLightColorScheme31, material3 1.4.0); several land on constants already declared above.
        inversePrimary = BrandPrimaryDark, // primary 80
        surfaceTint = BrandPrimaryLight, // primary 40
        inverseSurface = NeutralVariant20,
        inverseOnSurface = NeutralVariant95,
        outlineVariant = OnSurfaceVariantDark, // neutral-variant 80, shared with the dark scheme
        scrim = Color.Black,
        surfaceBright = BackgroundLight, // neutral-variant 98
        surfaceDim = NeutralVariant87,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = NeutralVariant96,
        surfaceContainer = NeutralVariant94,
        surfaceContainerHigh = NeutralVariant92,
        surfaceContainerHighest = SurfaceVariantLight, // neutral-variant 90
        // The "fixed" roles are identical in both schemes by definition, and every one of them lands on a
        // tone the brand palette already carries.
        primaryFixed = BrandPrimaryContainerLight, // primary 90
        primaryFixedDim = BrandPrimaryDark, // primary 80
        onPrimaryFixed = BrandOnPrimaryContainerLight, // primary 10
        onPrimaryFixedVariant = BrandPrimaryContainerDark, // primary 30
        secondaryFixed = BrandSecondaryContainerLight, // secondary 90
        secondaryFixedDim = BrandSecondaryDark, // secondary 80
        onSecondaryFixed = BrandOnSecondaryContainerLight, // secondary 10
        onSecondaryFixedVariant = BrandSecondaryContainerDark, // secondary 30
        tertiaryFixed = BrandTertiaryContainerLight, // tertiary 90
        tertiaryFixedDim = PositiveDark, // tertiary 80
        onTertiaryFixed = BrandOnTertiaryContainerLight, // tertiary 10
        onTertiaryFixedVariant = BrandTertiaryContainerDark, // tertiary 30
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = BrandPrimaryDark,
        onPrimary = BrandOnPrimaryDark,
        primaryContainer = BrandPrimaryContainerDark,
        onPrimaryContainer = BrandOnPrimaryContainerDark,
        secondary = BrandSecondaryDark,
        onSecondary = BrandOnSecondaryDark,
        secondaryContainer = BrandSecondaryContainerDark,
        onSecondaryContainer = BrandOnSecondaryContainerDark,
        tertiary = PositiveDark,
        onTertiary = OnPositiveDark,
        tertiaryContainer = BrandTertiaryContainerDark,
        onTertiaryContainer = BrandOnTertiaryContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        error = ErrorDark,
        onError = OnErrorDark,
        // See the note in LightColorScheme; tones from dynamicDarkColorScheme31.
        inversePrimary = BrandPrimaryLight, // primary 40
        surfaceTint = BrandPrimaryDark, // primary 80
        inverseSurface = OnSurfaceDark, // neutral-variant 90
        inverseOnSurface = NeutralVariant20,
        outlineVariant = SurfaceVariantDark, // neutral-variant 30
        scrim = Color.Black,
        surfaceBright = NeutralVariant24,
        surfaceDim = BackgroundDark, // black, as the background is
        surfaceContainerLowest = NeutralVariant4,
        surfaceContainerLow = OnBackgroundLight, // neutral-variant 10, shared with the light scheme
        surfaceContainer = NeutralVariant12,
        surfaceContainerHigh = NeutralVariant17,
        surfaceContainerHighest = NeutralVariant22,
        primaryFixed = BrandPrimaryContainerLight, // primary 90
        primaryFixedDim = BrandPrimaryDark, // primary 80
        onPrimaryFixed = BrandOnPrimaryContainerLight, // primary 10
        onPrimaryFixedVariant = BrandPrimaryContainerDark, // primary 30
        secondaryFixed = BrandSecondaryContainerLight, // secondary 90
        secondaryFixedDim = BrandSecondaryDark, // secondary 80
        onSecondaryFixed = BrandOnSecondaryContainerLight, // secondary 10
        onSecondaryFixedVariant = BrandSecondaryContainerDark, // secondary 30
        tertiaryFixed = BrandTertiaryContainerLight, // tertiary 90
        tertiaryFixedDim = PositiveDark, // tertiary 80
        onTertiaryFixed = BrandOnTertiaryContainerLight, // tertiary 10
        onTertiaryFixedVariant = BrandTertiaryContainerDark, // tertiary 30
    )

@Composable
fun KnitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default so the yellow-green brand identity shows instead of the wallpaper palette.
    // Callers can opt into Material You on Android 12+.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    // LocalReduceMotion is provided here rather than at each animation: KnitMotion reads it, so honoring the
    // platform's "Remove animations" setting is a property of being inside the theme, not something a call
    // site can forget. MaterialTheme's motionScheme is left at its default (standard, not expressive) — see
    // KnitMotion for why.
    // LocalKnitColors is keyed on darkTheme, and on dynamicColor only for the avatar palette: the semantic
    // green is fixed in every scheme (see KnitSemanticColors), and pairing it with onPositive by mode keeps
    // the two legible together in all four combinations. The avatar tints are static under the brand scheme
    // they were drawn against, and under Material You are harmonized toward the wallpaper's primary — a
    // bounded hue nudge (AvatarTint.harmonizedToward), so an identity keeps its colour family while the
    // palette stops fighting the wallpaper. Remembered on the primary rather than recomputed per read: it
    // is twelve pairs of colour-space round trips, and it changes only when the theme does.
    val dynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val knitColors =
        remember(darkTheme, dynamic, colorScheme.primary) {
            val base = if (darkTheme) DarkSemanticColors else LightSemanticColors
            if (dynamic) {
                base.copy(avatarTints = base.avatarTints.map { it.harmonizedToward(colorScheme.primary) })
            } else {
                base
            }
        }
    // LocalOverscrollFactory is provided for the same reason: the stretch/glow that every list draws when
    // it runs out of content is the last colour in the app that came from neither KnitTheme nor the XML
    // theme. Compose Foundation hardcodes it — AndroidOverscroll.android.kt's DefaultGlowColor is
    // Color(0xFF666666) — and hands it to EdgeEffect.setColor. From API 31 the effect is the stretch, which
    // ignores setColor entirely, so this is only ever visible on 29-30; those are the two releases minSdk
    // still carries, and a flat grey glow under a brand-coloured list is exactly the fallthrough the colour-role
    // sweep was about. primary is the role the platform's own themes pointed colorEdgeEffect at.
    // Deliberately NOT gated on LocalReduceMotion, unlike everything in KnitMotion: overscroll tracks the
    // finger 1:1, and the platform's own RecyclerView keeps stretching at animator scale 0.
    // Read here, at the theme's root, so the type scale's default alignment is the layout's start (knitTypography).
    val direction = LocalLayoutDirection.current
    CompositionLocalProvider(
        LocalReduceMotion provides rememberReduceMotion(),
        LocalKnitColors provides knitColors,
        LocalOverscrollFactory provides rememberPlatformOverscrollFactory(glowColor = colorScheme.primary),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = remember(direction) { knitTypography(direction) },
            content = content,
        )
    }
}
