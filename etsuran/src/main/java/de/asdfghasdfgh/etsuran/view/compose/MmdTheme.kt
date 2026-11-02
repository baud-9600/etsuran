@file:Suppress("DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")

package de.asdfghasdfgh.etsuran.view.compose

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Typography
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.asdfghasdfgh.etsuran.R

/**
 * Lato, the family Mudita Mindful Design draws every screen in.
 *
 * MMD asks for Medium in its text styles, but its own family has no Medium face — what
 * it draws is Lato Regular. The four faces shipped here are the ones the browser UI
 * actually uses (regular, bold, and their italics); the thin/light/black faces would
 * only add weight to the APK.
 */
val lato = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

/**
 * MMD's type scale, mapped onto Material 2's roles.
 *
 * MMD is built on Material 3: headlineLarge 28, titleLarge 24, titleMedium 20,
 * titleSmall 16, bodyLarge 20, bodyMedium 18, bodySmall 15, labelLarge 18,
 * labelMedium 15, labelSmall 14. Material 2 names the same sizes differently, so the
 * mapping below keeps every role at the MMD size that matches its job:
 *
 * - bars and screen titles: h2 / h3 (24 / 20)
 * - section headings: h6 (20)
 * - list rows: subtitle1 / body1 (20 / 18)
 * - supporting text: body2 / caption (15 / 14)
 * - buttons: button (18)
 */
val mmdTypography = Typography(
    h1 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 28.sp),
    h2 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    h3 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    h4 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    h5 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    h6 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    subtitle1 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    subtitle2 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    body1 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    body2 = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    button = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    caption = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    overline = TextStyle(fontFamily = lato, fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

/**
 * No ripple. MMD turns the ripple off globally: on e-ink every animated frame is a
 * full panel refresh, and a ripple repaints twice per tap for no information.
 */
object MmdNoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationNode()

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = System.identityHashCode(this)
}

private class NoIndicationNode : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() = drawContent()
}

/**
 * The other half of "no ripple": Material 2's Button, IconButton and friends ask
 * [RippleTheme] directly instead of reading LocalIndication, so the theme has to be
 * transparent too. Both are provided by [MyTheme].
 */
object MmdNoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = Color.Transparent

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
        draggedAlpha = 0f,
        focusedAlpha = 0f,
        hoveredAlpha = 0f,
        pressedAlpha = 0f,
    )
}

/**
 * Provides every MMD style override in one place: the type scale as the default text
 * style, and no indication from either ripple implementation.
 */
@Composable
fun MmdStyleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalIndication provides MmdNoIndication,
        // Kept alongside the new Indication API: Material 2's Button and IconButton in
        // this app still ask RippleTheme directly, and this is the only hook that reaches
        // them. Remove when the app moves to Material 3's ripple.
        LocalRippleTheme provides MmdNoRippleTheme,
        LocalTextStyle provides mmdTypography.body1,
        content = content,
    )
}
