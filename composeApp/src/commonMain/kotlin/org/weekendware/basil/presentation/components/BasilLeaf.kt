package org.weekendware.basil.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The Basil leaf logo mark, drawn with [Canvas].
 *
 * Matches the SVG in the design handoff exactly:
 * a teardrop leaf with a central vein and two angled secondary veins.
 *
 * @param size Width of the leaf. Height is proportionally taller (size × 1.15).
 * @param fill Leaf body fill color. Defaults to [MaterialTheme.colorScheme.primary].
 * @param vein Vein line color. Defaults to [Color.White].
 */
@Composable
fun BasilLeaf(
    size: Dp = 28.dp,
    fill: Color = MaterialTheme.colorScheme.primary,
    vein: Color = Color.White
) {
    val height = size * 1.15f
    Canvas(modifier = Modifier.size(size, height).clearAndSetSemantics {}) {
        drawLeaf(fill = fill, vein = vein)
    }
}

private fun DrawScope.drawLeaf(fill: Color, vein: Color) {
    // Normalise to the original 28×32 viewBox
    val sx = size.width  / 28f
    val sy = size.height / 32f

    fun x(v: Float) = v * sx
    fun y(v: Float) = v * sy

    // Leaf body — teardrop path from the design SVG
    val leafPath = Path().apply {
        moveTo(x(14f), y(2f))
        cubicTo(x(9f), y(5.5f), x(3.5f), y(12.5f), x(3.5f), y(20f))
        cubicTo(x(3.5f), y(26.5f), x(8.5f), y(31f), x(14f), y(31f))
        cubicTo(x(19.5f), y(31f), x(24.5f), y(26.5f), x(24.5f), y(20f))
        cubicTo(x(24.5f), y(12.5f), x(19f), y(5.5f), x(14f), y(2f))
        close()
    }
    drawPath(leafPath, color = fill)

    val thinStyle = Stroke(width = 1.1f * sx, cap = StrokeCap.Round)

    // Central vein
    drawLine(
        color = vein,
        start = Offset(x(14f), y(31f)),
        end   = Offset(x(14f), y(14f)),
        strokeWidth = 1.4f * sx,
        cap   = StrokeCap.Round
    )

    // Left secondary vein (lower)
    val leftVein = Path().apply {
        moveTo(x(14f), y(25f))
        cubicTo(x(14f), y(25f), x(9f), y(21.5f), x(7f), y(17f))
    }
    drawPath(leftVein, color = vein.copy(alpha = 0.7f), style = thinStyle)

    // Right secondary vein (upper)
    val rightVein = Path().apply {
        moveTo(x(14f), y(20f))
        cubicTo(x(14f), y(20f), x(19.5f), y(16f), x(21.5f), y(11f))
    }
    drawPath(rightVein, color = vein.copy(alpha = 0.7f), style = thinStyle)
}
