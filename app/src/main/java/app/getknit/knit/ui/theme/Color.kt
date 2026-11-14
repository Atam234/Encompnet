package app.getknit.knit.ui.theme

import androidx.compose.ui.graphics.Color

// Encompnet brand palette: black with a yellow-green (lime) accent. Seeds: primary #D1FE17 (HCT hue 122.8 /
// chroma 81.2), secondary the same hue at chroma 12, tertiary #08AD6C (green). The primary family is that
// seed's tonal ramp (10 / 20 / 30 / 40), except that the seed itself stands at tone 90 and is the dark
// scheme's primary, where Material would take the duller tone 80. The green is the app's semantic "good"
// colour rather than a scheme role: it lives in KnitSemanticColors.positive, because Material You would
// otherwise derive tertiary from the wallpaper and the online dot could come out the same hue as an error.
// The dark scheme's background and surface are pure black by design, not a tone of the neutral ramp.

// Light
val BrandPrimaryLight = Color(0xFF516600)
val BrandOnPrimaryLight = Color(0xFFFFFFFF)
val BrandPrimaryContainerLight = Color(0xFFD1FE17)
val BrandOnPrimaryContainerLight = Color(0xFF0C2100)
val BrandSecondaryLight = Color(0xFF5D604C)
val BrandOnSecondaryLight = Color(0xFFFFFFFF)
val BrandSecondaryContainerLight = Color(0xFFE2E5CB)
val BrandOnSecondaryContainerLight = Color(0xFF1A1D0E)
val PositiveLight = Color(0xFF006D43)
val OnPositiveLight = Color(0xFFFFFFFF)
val BrandTertiaryContainerLight = Color(0xFF8FF7BD)
val BrandOnTertiaryContainerLight = Color(0xFF00210F)
val BackgroundLight = Color(0xFFFBF9F1)
val OnBackgroundLight = Color(0xFF1B1C17)
val SurfaceLight = Color(0xFFFBF9F1)
val OnSurfaceLight = Color(0xFF1B1C17)
val SurfaceVariantLight = Color(0xFFE4E3DA)
val OnSurfaceVariantLight = Color(0xFF474741)
val OutlineLight = Color(0xFF787771)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)

// Dark
val BrandPrimaryDark = Color(0xFFD1FE17)
val BrandOnPrimaryDark = Color(0xFF273500)
val BrandPrimaryContainerDark = Color(0xFF3C4D00)
val BrandOnPrimaryContainerDark = Color(0xFFD1FE17)
val BrandSecondaryDark = Color(0xFFC5C9B0)
val BrandOnSecondaryDark = Color(0xFF2E3221)
val BrandSecondaryContainerDark = Color(0xFF454936)
val BrandOnSecondaryContainerDark = Color(0xFFE2E5CB)
val PositiveDark = Color(0xFF72DAA0)
val OnPositiveDark = Color(0xFF00391F)
val BrandTertiaryContainerDark = Color(0xFF005230)
val BrandOnTertiaryContainerDark = Color(0xFF8FF7BD)
val BackgroundDark = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFE4E3DA)
val SurfaceDark = Color(0xFF000000)
val OnSurfaceDark = Color(0xFFE4E3DA)
val SurfaceVariantDark = Color(0xFF474741)
val OnSurfaceVariantDark = Color(0xFFC8C7BF)
val OutlineDark = Color(0xFF92918A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)

// Neutral-variant ramp: hue 122.8 / chroma 4.0, the ramp the surfaceVariant/outline/background constants
// above sit on (the dark background and surface excepted: they are black). Named by tone rather than by
// role because several roles share one tone: light `inverseSurface` and dark `inverseOnSurface` are both
// tone 20. Theme.kt maps role → tone, following Material's own assignment (material3 1.4.0,
// dynamic*ColorScheme31).
//
// These exist because `lightColorScheme()`/`darkColorScheme()` default every unset role to Material's
// BASELINE palette, which is tinted purple — `surfaceContainer` was #F3EDF7 (hue 276°) sitting next to
// the brand's own surfaces, under every Card, AlertDialog, DropdownMenu, ModalBottomSheet and scrolled
// TopAppBar in the app. Regenerate with `python3 scripts/gen-color-scheme.py`.
val NeutralVariant4 = Color(0xFF0E0F0A)
val NeutralVariant12 = Color(0xFF1F201B)
val NeutralVariant17 = Color(0xFF2A2A25)
val NeutralVariant20 = Color(0xFF30312B)
val NeutralVariant22 = Color(0xFF353530)
val NeutralVariant24 = Color(0xFF393A34)
val NeutralVariant87 = Color(0xFFDCDAD2)
val NeutralVariant92 = Color(0xFFEAE8E0)
val NeutralVariant94 = Color(0xFFF0EEE6)
val NeutralVariant95 = Color(0xFFF3F1E9)
val NeutralVariant96 = Color(0xFFF6F4EB)
