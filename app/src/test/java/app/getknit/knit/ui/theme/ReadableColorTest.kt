package app.getknit.knit.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadableColorTest {
    @Test
    fun contrastRatioMatchesWcagEndpoints() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(SurfaceLight, SurfaceLight), 0.001f)
    }

    // The old coral primary made 4.11:1 on an incoming bubble (the FTL accessibility pass's finding, and why the
    // helper exists). The brand's own primary now clears the floor on both fills: pinned so a palette change
    // that loses that is noticed.
    @Test
    fun lightPrimaryClearsTheFloorOnBothBubbleFills() {
        assertEquals(5.01f, contrastRatio(BrandPrimaryLight, SurfaceVariantLight), 0.01f)
        assertEquals(5.51f, contrastRatio(BrandPrimaryLight, BrandPrimaryContainerLight), 0.01f)
    }

    // What a wallpaper scheme can still hand the helper: an accent too light for the fill it is drawn on.
    @Test
    fun anAccentThatFallsShortIsDarkenedJustPastTheFloor() {
        for (bubble in listOf(SurfaceVariantLight, BrandPrimaryContainerLight)) {
            assertTrue(contrastRatio(SHORT_ACCENT, bubble) < MIN_TEXT_CONTRAST)
            val shown = SHORT_ACCENT.readableOn(bubble)
            val ratio = contrastRatio(shown, bubble)
            assertTrue("$ratio on $bubble", ratio >= MIN_TEXT_CONTRAST)
            // Darkened, not swapped for black: barely past the floor, and still green-dominant.
            assertTrue("$ratio on $bubble", ratio < MIN_TEXT_CONTRAST + 0.05f)
            assertTrue(shown.green > shown.red && shown.green > shown.blue)
        }
    }

    @Test
    fun anAccentThatAlreadyPassesIsUntouched() {
        assertEquals(BrandPrimaryLight, BrandPrimaryLight.readableOn(SurfaceLight))
        assertEquals(BrandPrimaryLight, BrandPrimaryLight.readableOn(SurfaceVariantLight))
        assertEquals(BrandPrimaryDark, BrandPrimaryDark.readableOn(SurfaceVariantDark))
    }

    @Test
    fun onADarkFillTheAccentIsLightened() {
        val shown = BrandPrimaryContainerDark.readableOn(SurfaceDark)
        assertTrue(contrastRatio(shown, SurfaceDark) >= MIN_TEXT_CONTRAST)
        assertTrue(shown.green > BrandPrimaryContainerDark.green)
    }

    private companion object {
        /** A mid-tone yellow-green (primary tone 60): about 2.7:1 on the lime bubble, 2.4:1 on the grey one. */
        val SHORT_ACCENT = Color(0xFF7F9D00)
    }
}
