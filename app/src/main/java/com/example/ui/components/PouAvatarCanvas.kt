package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import com.example.data.AccessoryCatalogItem
import com.example.data.CatalogData
import com.example.data.EvolutionStage
import com.example.data.PetStateEntity
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PouAvatarCanvas(
    petState: PetStateEntity,
    isEating: Boolean = false,
    isBathing: Boolean = false,
    stageOverride: EvolutionStage? = null,
    onPetTapped: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stage = stageOverride ?: petState.activeEvolutionStage

    val infiniteTransition = rememberInfiniteTransition(label = "pou_idle")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    var tapPulse by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        targetValue = if (tapPulse) 1.14f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tap_scale"
    )

    LaunchedEffect(tapPulse) {
        if (tapPulse) {
            delay(220)
            tapPulse = false
        }
    }

    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3400)
            isBlinking = true
            delay(160)
            isBlinking = false
        }
    }

    val equippedHat = remember(petState.equippedHatId) {
        CatalogData.accessories.find { it.id == petState.equippedHatId }
    }
    val equippedGlasses = remember(petState.equippedGlassesId) {
        CatalogData.accessories.find { it.id == petState.equippedGlassesId }
    }
    val equippedShirt = remember(petState.equippedShirtId) {
        CatalogData.accessories.find { it.id == petState.equippedShirtId }
    }
    val equippedShoes = remember(petState.equippedShoesId) {
        CatalogData.accessories.find { it.id == petState.equippedShoesId }
    }

    val stageGrowthScale = when (stage) {
        EvolutionStage.EGG -> 0.84f
        EvolutionStage.BABY -> 0.92f
        EvolutionStage.TEEN -> 1.00f
        EvolutionStage.ADULT -> 1.05f
        EvolutionStage.MYTHIC -> 1.08f
    }

    Box(
        modifier = modifier
            .testTag("pou_avatar_canvas")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tapPulse = true
                onPetTapped()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.56f + floatOffset
            val baseRadius = minOf(size.width, size.height) * 0.33f * stageGrowthScale

            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.16f),
                topLeft = Offset(cx - baseRadius * 0.95f, cy + baseRadius * 0.85f - floatOffset),
                size = Size(baseRadius * 1.9f, baseRadius * 0.35f)
            )

            // Aura for Adult and Mythic Stages
            if (stage == EvolutionStage.ADULT || stage == EvolutionStage.MYTHIC) {
                val auraColor = if (stage == EvolutionStage.MYTHIC) Color(0xFFFFD54F) else Color(0xFF80DEEA)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auraColor.copy(alpha = 0.38f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = baseRadius * 1.55f
                    ),
                    radius = baseRadius * 1.55f,
                    center = Offset(cx, cy)
                )
            }

            scale(
                scaleX = (1f / breatheScale) * tapScale,
                scaleY = breatheScale * tapScale,
                pivot = Offset(cx, cy + baseRadius * 0.5f)
            ) {
                val bodyColor = Color(petState.bodyColorHex)
                val darkerBody = Color(
                    red = (bodyColor.red * 0.78f).coerceIn(0f, 1f),
                    green = (bodyColor.green * 0.75f).coerceIn(0f, 1f),
                    blue = (bodyColor.blue * 0.72f).coerceIn(0f, 1f),
                    alpha = 1f
                )
                val lighterBody = Color(
                    red = (bodyColor.red * 1.15f).coerceIn(0f, 1f),
                    green = (bodyColor.green * 1.12f).coerceIn(0f, 1f),
                    blue = (bodyColor.blue * 1.10f).coerceIn(0f, 1f),
                    alpha = 1f
                )

                // Evolution Wings (Adult & Mythic) drawn behind the body
                if (stage == EvolutionStage.ADULT || stage == EvolutionStage.MYTHIC) {
                    drawCreatureWings(
                        cx = cx,
                        cy = cy,
                        r = baseRadius,
                        isMythic = stage == EvolutionStage.MYTHIC,
                        flapFactor = (breatheScale - 0.97f) * 12f
                    )
                }

                // Mythic Tail behind body
                if (stage == EvolutionStage.MYTHIC) {
                    val tailPath = Path().apply {
                        moveTo(cx + baseRadius * 0.65f, cy + baseRadius * 0.65f)
                        quadraticTo(
                            cx + baseRadius * 1.35f,
                            cy + baseRadius * 0.55f,
                            cx + baseRadius * 1.42f,
                            cy + baseRadius * 0.18f
                        )
                        quadraticTo(
                            cx + baseRadius * 1.15f,
                            cy + baseRadius * 0.85f,
                            cx + baseRadius * 0.55f,
                            cy + baseRadius * 0.82f
                        )
                        close()
                    }
                    drawPath(tailPath, color = Color(0xFFFFCA28))
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = baseRadius * 0.11f,
                        center = Offset(cx + baseRadius * 1.42f, cy + baseRadius * 0.18f)
                    )
                }

                // Optional Hero Cape behind body
                if (equippedShirt?.styleCode == "HERO") {
                    val capePath = Path().apply {
                        moveTo(cx - baseRadius * 0.6f, cy - baseRadius * 0.1f)
                        lineTo(cx - baseRadius * 1.25f, cy + baseRadius * 0.88f)
                        lineTo(cx + baseRadius * 1.25f, cy + baseRadius * 0.88f)
                        lineTo(cx + baseRadius * 0.6f, cy - baseRadius * 0.1f)
                        close()
                    }
                    drawPath(capePath, color = Color(equippedShirt.primaryColorHex))
                }

                // Evolution Ears / Horns (Teen, Adult, Mythic)
                if (stage.ordinal >= EvolutionStage.TEEN.ordinal) {
                    drawEvolutionEarsAndHorns(cx, cy, baseRadius, stage, bodyColor, darkerBody)
                }

                // Evolution Side Arms / Flippers (Baby, Teen, Adult, Mythic)
                if (stage.ordinal >= EvolutionStage.BABY.ordinal) {
                    drawEvolutionArms(cx, cy, baseRadius, stage, bodyColor, darkerBody, floatOffset)
                }

                // Classic Pou Egg/Creature Body Path
                val bodyPath = createPouBodyPath(cx, cy, baseRadius, isEggStage = stage == EvolutionStage.EGG)

                // Fill Pou Body with 3D Radial Gradient
                drawPath(
                    path = bodyPath,
                    brush = Brush.radialGradient(
                        colors = listOf(lighterBody, bodyColor, darkerBody),
                        center = Offset(cx - baseRadius * 0.25f, cy - baseRadius * 0.25f),
                        radius = baseRadius * 1.5f
                    )
                )

                // Egg Stage Spots & Hatching Cracks
                if (stage == EvolutionStage.EGG) {
                    clipPath(bodyPath) {
                        drawCircle(lighterBody.copy(alpha = 0.55f), radius = baseRadius * 0.18f, center = Offset(cx - baseRadius * 0.45f, cy + baseRadius * 0.35f))
                        drawCircle(lighterBody.copy(alpha = 0.55f), radius = baseRadius * 0.14f, center = Offset(cx + baseRadius * 0.48f, cy + baseRadius * 0.25f))
                        drawCircle(lighterBody.copy(alpha = 0.45f), radius = baseRadius * 0.12f, center = Offset(cx + baseRadius * 0.28f, cy - baseRadius * 0.55f))
                    }
                    // Hatching Crack Line across upper egg
                    val crackPath = Path().apply {
                        moveTo(cx - baseRadius * 0.42f, cy - baseRadius * 0.52f)
                        lineTo(cx - baseRadius * 0.20f, cy - baseRadius * 0.42f)
                        lineTo(cx - baseRadius * 0.05f, cy - baseRadius * 0.56f)
                        lineTo(cx + baseRadius * 0.15f, cy - baseRadius * 0.44f)
                        lineTo(cx + baseRadius * 0.36f, cy - baseRadius * 0.54f)
                    }
                    drawPath(crackPath, color = darkerBody, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
                }

                // Draw Shirt/Outfit Clipped Inside Pou Body
                if (equippedShirt != null) {
                    clipPath(bodyPath) {
                        drawPouShirt(cx, cy, baseRadius, equippedShirt)
                    }
                }

                // Body Outline
                drawPath(
                    path = bodyPath,
                    color = darkerBody,
                    style = Stroke(width = 6f)
                )

                // Baby Stage Sprout on top & Cracked Eggshell Rim at Bottom
                if (stage == EvolutionStage.BABY && equippedHat == null) {
                    val stemPath = Path().apply {
                        moveTo(cx, cy - baseRadius * 0.90f)
                        quadraticTo(cx + baseRadius * 0.05f, cy - baseRadius * 1.15f, cx + baseRadius * 0.18f, cy - baseRadius * 1.22f)
                    }
                    drawPath(stemPath, color = Color(0xFF43A047), style = Stroke(width = 7f, cap = StrokeCap.Round))
                    drawOval(
                        color = Color(0xFF66BB6A),
                        topLeft = Offset(cx + baseRadius * 0.08f, cy - baseRadius * 1.28f),
                        size = Size(baseRadius * 0.26f, baseRadius * 0.14f)
                    )
                    drawOval(
                        color = Color(0xFF43A047),
                        topLeft = Offset(cx - baseRadius * 0.24f, cy - baseRadius * 1.22f),
                        size = Size(baseRadius * 0.24f, baseRadius * 0.13f)
                    )
                }

                // Mythic Stage Forehead Gem
                if (stage == EvolutionStage.MYTHIC) {
                    val gemY = cy - baseRadius * 0.56f
                    val gemPath = Path().apply {
                        moveTo(cx, gemY - baseRadius * 0.14f)
                        lineTo(cx + baseRadius * 0.11f, gemY)
                        lineTo(cx, gemY + baseRadius * 0.14f)
                        lineTo(cx - baseRadius * 0.11f, gemY)
                        close()
                    }
                    drawPath(gemPath, color = Color(0xFF00E5FF))
                    drawPath(gemPath, color = Color(0xFFFFD54F), style = Stroke(width = 3.5f))
                }

                // Specular Highlight on upper left
                drawOval(
                    color = Color.White.copy(alpha = 0.28f),
                    topLeft = Offset(cx - baseRadius * 0.52f, cy - baseRadius * 0.62f),
                    size = Size(baseRadius * 0.36f, baseRadius * 0.22f)
                )

                // Dirt Smudges if Cleanliness is Low
                if (petState.cleanliness < 55 && !isBathing) {
                    val smudgeColor = Color(0xFF6D4C41).copy(alpha = 0.45f)
                    drawCircle(smudgeColor, radius = baseRadius * 0.13f, center = Offset(cx - baseRadius * 0.48f, cy + baseRadius * 0.25f))
                    drawCircle(smudgeColor, radius = baseRadius * 0.10f, center = Offset(cx + baseRadius * 0.52f, cy + baseRadius * 0.15f))
                    drawCircle(smudgeColor, radius = baseRadius * 0.08f, center = Offset(cx - baseRadius * 0.2f, cy + baseRadius * 0.48f))
                }

                // Rosy Cheeks
                val cheekColor = Color(0xFFFF8A80).copy(alpha = 0.55f)
                drawOval(
                    color = cheekColor,
                    topLeft = Offset(cx - baseRadius * 0.58f, cy - baseRadius * 0.06f),
                    size = Size(baseRadius * 0.24f, baseRadius * 0.14f)
                )
                drawOval(
                    color = cheekColor,
                    topLeft = Offset(cx + baseRadius * 0.34f, cy - baseRadius * 0.06f),
                    size = Size(baseRadius * 0.24f, baseRadius * 0.14f)
                )

                // Eyes
                drawPouEyes(
                    cx = cx,
                    cy = cy,
                    r = baseRadius,
                    isSleeping = petState.isSleeping,
                    isBlinking = isBlinking,
                    isSad = petState.happiness < 35 || petState.hunger < 25
                )

                // Mouth
                drawPouMouth(
                    cx = cx,
                    cy = cy,
                    r = baseRadius,
                    isEating = isEating,
                    isSleeping = petState.isSleeping,
                    happiness = petState.happiness,
                    hunger = petState.hunger
                )

                // Equipped Glasses
                if (equippedGlasses != null) {
                    drawPouGlasses(cx, cy, baseRadius, equippedGlasses)
                }

                // Equipped Hat
                if (equippedHat != null) {
                    drawPouHat(cx, cy, baseRadius, equippedHat)
                }

                // Equipped Shoes
                if (equippedShoes != null) {
                    drawPouShoes(cx, cy, baseRadius, equippedShoes)
                }

                // Bathing Soap Bubbles Effect
                if (isBathing) {
                    val bubbleOffsets = listOf(
                        Offset(-0.6f, -0.4f), Offset(0.55f, -0.35f),
                        Offset(-0.4f, 0.3f), Offset(0.45f, 0.35f),
                        Offset(0.0f, -0.7f), Offset(-0.2f, 0.0f)
                    )
                    bubbleOffsets.forEachIndexed { idx, rel ->
                        val bx = cx + rel.x * baseRadius
                        val by = cy + rel.y * baseRadius
                        val br = baseRadius * (0.14f + (idx % 3) * 0.04f)
                        drawCircle(Color(0xFFE1F5FE).copy(alpha = 0.78f), radius = br, center = Offset(bx, by))
                        drawCircle(Color(0xFF29B6F6), radius = br, center = Offset(bx, by), style = Stroke(width = 3f))
                        drawCircle(Color.White, radius = br * 0.28f, center = Offset(bx - br * 0.3f, by - br * 0.3f))
                    }
                }

                // Orbiting Starlight Particles for Mythic Evolution Stage
                if (stage == EvolutionStage.MYTHIC) {
                    for (i in 0 until 4) {
                        val radAngle = Math.toRadians((orbitAngle + i * 90f).toDouble())
                        val sx = cx + (baseRadius * 1.32f * cos(radAngle)).toFloat()
                        val sy = cy + (baseRadius * 0.85f * sin(radAngle)).toFloat()
                        drawStar(sx, sy, baseRadius * 0.12f, Color(0xFFFFB300), Color(0xFFFFF59D))
                    }
                }
            }
        }
    }
}

private fun createPouBodyPath(cx: Float, cy: Float, r: Float, isEggStage: Boolean): Path {
    return Path().apply {
        if (isEggStage) {
            // Classic oval egg shape for Stage 1
            moveTo(cx, cy - r * 0.96f)
            cubicTo(
                cx + r * 0.68f, cy - r * 0.96f,
                cx + r * 0.92f, cy + r * 0.12f,
                cx + r * 0.76f, cy + r * 0.72f
            )
            cubicTo(
                cx + r * 0.62f, cy + r * 0.96f,
                cx + r * 0.28f, cy + r * 0.98f,
                cx, cy + r * 0.98f
            )
            cubicTo(
                cx - r * 0.28f, cy + r * 0.98f,
                cx - r * 0.62f, cy + r * 0.96f,
                cx - r * 0.76f, cy + r * 0.72f
            )
            cubicTo(
                cx - r * 0.92f, cy + r * 0.12f,
                cx - r * 0.68f, cy - r * 0.96f,
                cx, cy - r * 0.96f
            )
            close()
        } else {
            // Mature Pou rounded-triangle silhouette
            moveTo(cx, cy - r * 0.92f)
            cubicTo(
                cx + r * 0.58f, cy - r * 0.92f,
                cx + r * 1.08f, cy + r * 0.15f,
                cx + r * 0.92f, cy + r * 0.65f
            )
            cubicTo(
                cx + r * 0.80f, cy + r * 0.94f,
                cx + r * 0.35f, cy + r * 0.96f,
                cx, cy + r * 0.96f
            )
            cubicTo(
                cx - r * 0.35f, cy + r * 0.96f,
                cx - r * 0.80f, cy + r * 0.94f,
                cx - r * 0.92f, cy + r * 0.65f
            )
            cubicTo(
                cx - r * 1.08f, cy + r * 0.15f,
                cx - r * 0.58f, cy - r * 0.92f,
                cx, cy - r * 0.92f
            )
            close()
        }
    }
}

private fun DrawScope.drawEvolutionArms(
    cx: Float,
    cy: Float,
    r: Float,
    stage: EvolutionStage,
    bodyColor: Color,
    darkerBody: Color,
    floatOffset: Float
) {
    val armLen = if (stage == EvolutionStage.BABY) r * 0.26f else r * 0.38f
    val armH = if (stage == EvolutionStage.BABY) r * 0.18f else r * 0.22f
    val armY = cy + r * 0.12f + floatOffset * 0.4f

    // Left Arm
    drawOval(
        color = bodyColor,
        topLeft = Offset(cx - r * 0.92f - armLen * 0.65f, armY - armH / 2),
        size = Size(armLen, armH)
    )
    drawOval(
        color = darkerBody,
        topLeft = Offset(cx - r * 0.92f - armLen * 0.65f, armY - armH / 2),
        size = Size(armLen, armH),
        style = Stroke(width = 4.5f)
    )

    // Right Arm
    drawOval(
        color = bodyColor,
        topLeft = Offset(cx + r * 0.92f - armLen * 0.35f, armY - armH / 2),
        size = Size(armLen, armH)
    )
    drawOval(
        color = darkerBody,
        topLeft = Offset(cx + r * 0.92f - armLen * 0.35f, armY - armH / 2),
        size = Size(armLen, armH),
        style = Stroke(width = 4.5f)
    )
}

private fun DrawScope.drawEvolutionEarsAndHorns(
    cx: Float,
    cy: Float,
    r: Float,
    stage: EvolutionStage,
    bodyColor: Color,
    darkerBody: Color
) {
    val hornColor = when (stage) {
        EvolutionStage.MYTHIC -> Color(0xFFFFD54F)
        EvolutionStage.ADULT -> Color(0xFFFFCC80)
        else -> bodyColor
    }
    val innerColor = Color(0xFFFF8A80).copy(alpha = 0.6f)
    val topY = cy - r * 0.72f

    // Left Ear/Horn
    val leftEar = Path().apply {
        moveTo(cx - r * 0.48f, topY + r * 0.18f)
        quadraticTo(cx - r * 0.78f, topY - r * 0.38f, cx - r * 0.38f, topY - r * 0.32f)
        lineTo(cx - r * 0.24f, topY + r * 0.04f)
        close()
    }
    drawPath(leftEar, hornColor)
    drawPath(leftEar, darkerBody, style = Stroke(width = 4.5f))
    drawCircle(innerColor, radius = r * 0.07f, center = Offset(cx - r * 0.44f, topY - r * 0.06f))

    // Right Ear/Horn
    val rightEar = Path().apply {
        moveTo(cx + r * 0.48f, topY + r * 0.18f)
        quadraticTo(cx + r * 0.78f, topY - r * 0.38f, cx + r * 0.38f, topY - r * 0.32f)
        lineTo(cx + r * 0.24f, topY + r * 0.04f)
        close()
    }
    drawPath(rightEar, hornColor)
    drawPath(rightEar, darkerBody, style = Stroke(width = 4.5f))
    drawCircle(innerColor, radius = r * 0.07f, center = Offset(cx + r * 0.44f, topY - r * 0.06f))
}

private fun DrawScope.drawCreatureWings(
    cx: Float,
    cy: Float,
    r: Float,
    isMythic: Boolean,
    flapFactor: Float
) {
    val wingPrimary = if (isMythic) Color(0xFFFFD54F) else Color(0xFF80DEEA)
    val wingSecondary = if (isMythic) Color(0xFFFF8F00) else Color(0xFF26C6DA)
    val tipOffsetY = -r * 0.42f - flapFactor * r * 0.15f

    // Left Wing
    val leftWing = Path().apply {
        moveTo(cx - r * 0.65f, cy - r * 0.05f)
        cubicTo(
            cx - r * 1.45f, cy + tipOffsetY,
            cx - r * 1.65f, cy + r * 0.15f,
            cx - r * 1.25f, cy + r * 0.45f
        )
        quadraticTo(cx - r * 0.95f, cy + r * 0.35f, cx - r * 0.68f, cy + r * 0.28f)
        close()
    }
    drawPath(leftWing, wingPrimary)
    drawPath(leftWing, wingSecondary, style = Stroke(width = 5f))

    // Right Wing
    val rightWing = Path().apply {
        moveTo(cx + r * 0.65f, cy - r * 0.05f)
        cubicTo(
            cx + r * 1.45f, cy + tipOffsetY,
            cx + r * 1.65f, cy + r * 0.15f,
            cx + r * 1.25f, cy + r * 0.45f
        )
        quadraticTo(cx + r * 0.95f, cy + r * 0.35f, cx + r * 0.68f, cy + r * 0.28f)
        close()
    }
    drawPath(rightWing, wingPrimary)
    drawPath(rightWing, wingSecondary, style = Stroke(width = 5f))
}

private fun DrawScope.drawPouEyes(
    cx: Float,
    cy: Float,
    r: Float,
    isSleeping: Boolean,
    isBlinking: Boolean,
    isSad: Boolean
) {
    val eyeY = cy - r * 0.24f
    val leftX = cx - r * 0.26f
    val rightX = cx + r * 0.26f
    val eyeW = r * 0.22f
    val eyeH = r * 0.28f

    if (isSleeping || isBlinking) {
        drawLine(
            color = Color(0xFF2C221E),
            start = Offset(leftX - eyeW * 0.5f, eyeY),
            end = Offset(leftX + eyeW * 0.5f, eyeY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF2C221E),
            start = Offset(rightX - eyeW * 0.5f, eyeY),
            end = Offset(rightX + eyeW * 0.5f, eyeY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
    } else {
        drawOval(
            color = Color.White,
            topLeft = Offset(leftX - eyeW / 2, eyeY - eyeH / 2),
            size = Size(eyeW, eyeH)
        )
        drawOval(
            color = Color.White,
            topLeft = Offset(rightX - eyeW / 2, eyeY - eyeH / 2),
            size = Size(eyeW, eyeH)
        )
        drawOval(
            color = Color(0xFF2C221E),
            topLeft = Offset(leftX - eyeW / 2, eyeY - eyeH / 2),
            size = Size(eyeW, eyeH),
            style = Stroke(width = 4f)
        )
        drawOval(
            color = Color(0xFF2C221E),
            topLeft = Offset(rightX - eyeW / 2, eyeY - eyeH / 2),
            size = Size(eyeW, eyeH),
            style = Stroke(width = 4f)
        )

        val pupilR = eyeW * 0.30f
        val pupilOffsetY = if (isSad) eyeH * 0.08f else 0f
        drawCircle(
            color = Color(0xFF1E1916),
            radius = pupilR,
            center = Offset(leftX + eyeW * 0.04f, eyeY + pupilOffsetY)
        )
        drawCircle(
            color = Color(0xFF1E1916),
            radius = pupilR,
            center = Offset(rightX - eyeW * 0.04f, eyeY + pupilOffsetY)
        )
        drawCircle(
            color = Color.White,
            radius = pupilR * 0.38f,
            center = Offset(leftX - pupilR * 0.25f, eyeY - pupilR * 0.35f)
        )
        drawCircle(
            color = Color.White,
            radius = pupilR * 0.38f,
            center = Offset(rightX - pupilR * 0.35f, eyeY - pupilR * 0.35f)
        )
    }
}

private fun DrawScope.drawPouMouth(
    cx: Float,
    cy: Float,
    r: Float,
    isEating: Boolean,
    isSleeping: Boolean,
    happiness: Int,
    hunger: Int
) {
    val mouthY = cy + r * 0.05f
    if (isEating) {
        drawOval(
            color = Color(0xFF6D1B1B),
            topLeft = Offset(cx - r * 0.22f, mouthY - r * 0.08f),
            size = Size(r * 0.44f, r * 0.32f)
        )
        drawOval(
            color = Color(0xFFFF8A80),
            topLeft = Offset(cx - r * 0.13f, mouthY + r * 0.08f),
            size = Size(r * 0.26f, r * 0.14f)
        )
    } else if (isSleeping) {
        drawCircle(
            color = Color(0xFF3E2723),
            radius = r * 0.06f,
            center = Offset(cx, mouthY + r * 0.04f)
        )
    } else if (happiness < 35 || hunger < 25) {
        val frownPath = Path().apply {
            moveTo(cx - r * 0.22f, mouthY + r * 0.12f)
            quadraticTo(cx, mouthY - r * 0.06f, cx + r * 0.22f, mouthY + r * 0.12f)
        }
        drawPath(frownPath, color = Color(0xFF2C221E), style = Stroke(width = 7f, cap = StrokeCap.Round))
    } else {
        val smilePath = Path().apply {
            moveTo(cx - r * 0.28f, mouthY - r * 0.02f)
            quadraticTo(cx, mouthY + r * 0.24f, cx + r * 0.28f, mouthY - r * 0.02f)
        }
        drawPath(smilePath, color = Color(0xFF2C221E), style = Stroke(width = 7f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawPouShirt(
    cx: Float,
    cy: Float,
    r: Float,
    shirt: AccessoryCatalogItem
) {
    val topY = cy + r * 0.26f
    val bottomY = cy + r * 1.0f
    val primary = Color(shirt.primaryColorHex)
    val secondary = Color(shirt.secondaryColorHex)

    drawRect(
        color = primary,
        topLeft = Offset(cx - r * 1.1f, topY),
        size = Size(r * 2.2f, bottomY - topY)
    )

    when (shirt.styleCode) {
        "STRIPES" -> {
            val stripeH = (bottomY - topY) / 5f
            for (i in 0..4 step 2) {
                drawRect(
                    color = secondary,
                    topLeft = Offset(cx - r * 1.1f, topY + i * stripeH),
                    size = Size(r * 2.2f, stripeH)
                )
            }
        }
        "TUXEDO" -> {
            val vPath = Path().apply {
                moveTo(cx - r * 0.32f, topY)
                lineTo(cx + r * 0.32f, topY)
                lineTo(cx, topY + r * 0.42f)
                close()
            }
            drawPath(vPath, Color.White)
            drawCircle(secondary, radius = r * 0.07f, center = Offset(cx, topY + r * 0.06f))
            drawCircle(secondary, radius = r * 0.09f, center = Offset(cx - r * 0.1f, topY + r * 0.06f))
            drawCircle(secondary, radius = r * 0.09f, center = Offset(cx + r * 0.1f, topY + r * 0.06f))
        }
        "HOODIE" -> {
            drawRoundRect(
                color = secondary,
                topLeft = Offset(cx - r * 0.38f, topY + r * 0.22f),
                size = Size(r * 0.76f, r * 0.28f),
                cornerRadius = CornerRadius(14f, 14f)
            )
            drawLine(Color.White, Offset(cx - r * 0.14f, topY), Offset(cx - r * 0.14f, topY + r * 0.18f), strokeWidth = 5f)
            drawLine(Color.White, Offset(cx + r * 0.14f, topY), Offset(cx + r * 0.14f, topY + r * 0.18f), strokeWidth = 5f)
        }
        "HERO" -> {
            drawRect(secondary, topLeft = Offset(cx - r * 1.1f, topY + r * 0.40f), size = Size(r * 2.2f, r * 0.12f))
            drawCircle(secondary, radius = r * 0.16f, center = Offset(cx, topY + r * 0.18f))
        }
        "OVERALLS" -> {
            drawRect(primary, topLeft = Offset(cx - r * 0.45f, topY - r * 0.15f), size = Size(r * 0.16f, r * 0.20f))
            drawRect(primary, topLeft = Offset(cx + r * 0.29f, topY - r * 0.15f), size = Size(r * 0.16f, r * 0.20f))
            drawCircle(secondary, radius = r * 0.06f, center = Offset(cx - r * 0.37f, topY + r * 0.06f))
            drawCircle(secondary, radius = r * 0.06f, center = Offset(cx + r * 0.37f, topY + r * 0.06f))
        }
        "NINJA" -> {
            drawLine(secondary, Offset(cx - r * 0.4f, topY), Offset(cx + r * 0.15f, topY + r * 0.35f), strokeWidth = 10f)
            drawRect(secondary, topLeft = Offset(cx - r * 1.1f, topY + r * 0.36f), size = Size(r * 2.2f, r * 0.13f))
        }
        "ARMOR" -> {
            drawCircle(secondary, radius = r * 0.16f, center = Offset(cx, topY + r * 0.22f))
            drawCircle(Color.White, radius = r * 0.07f, center = Offset(cx - r * 0.04f, topY + r * 0.18f))
        }
    }
}

private fun DrawScope.drawPouGlasses(
    cx: Float,
    cy: Float,
    r: Float,
    glasses: AccessoryCatalogItem
) {
    val eyeY = cy - r * 0.24f
    val leftX = cx - r * 0.27f
    val rightX = cx + r * 0.27f
    val primary = Color(glasses.primaryColorHex)
    val secondary = Color(glasses.secondaryColorHex)

    when (glasses.styleCode) {
        "SUNGLASSES" -> {
            drawRoundRect(primary, Offset(leftX - r * 0.22f, eyeY - r * 0.14f), Size(r * 0.42f, r * 0.28f), CornerRadius(10f, 10f))
            drawRoundRect(primary, Offset(rightX - r * 0.20f, eyeY - r * 0.14f), Size(r * 0.42f, r * 0.28f), CornerRadius(10f, 10f))
            drawLine(primary, Offset(cx - r * 0.1f, eyeY - r * 0.05f), Offset(cx + r * 0.1f, eyeY - r * 0.05f), strokeWidth = 8f)
            drawLine(secondary, Offset(leftX - r * 0.12f, eyeY - r * 0.08f), Offset(leftX + r * 0.05f, eyeY + r * 0.08f), strokeWidth = 5f)
            drawLine(secondary, Offset(rightX - r * 0.10f, eyeY - r * 0.08f), Offset(rightX + r * 0.07f, eyeY + r * 0.08f), strokeWidth = 5f)
        }
        "NERD" -> {
            drawCircle(secondary.copy(alpha = 0.35f), radius = r * 0.20f, center = Offset(leftX, eyeY))
            drawCircle(secondary.copy(alpha = 0.35f), radius = r * 0.20f, center = Offset(rightX, eyeY))
            drawCircle(primary, radius = r * 0.20f, center = Offset(leftX, eyeY), style = Stroke(width = 8f))
            drawCircle(primary, radius = r * 0.20f, center = Offset(rightX, eyeY), style = Stroke(width = 8f))
            drawLine(Color.White, Offset(cx - r * 0.08f, eyeY), Offset(cx + r * 0.08f, eyeY), strokeWidth = 9f)
        }
        "STAR" -> {
            drawStar(leftX, eyeY, r * 0.22f, primary, secondary)
            drawStar(rightX, eyeY, r * 0.22f, primary, secondary)
            drawLine(primary, Offset(cx - r * 0.1f, eyeY), Offset(cx + r * 0.1f, eyeY), strokeWidth = 6f)
        }
        "HEART" -> {
            drawCircle(primary, radius = r * 0.18f, center = Offset(leftX, eyeY))
            drawCircle(primary, radius = r * 0.18f, center = Offset(rightX, eyeY))
            drawCircle(secondary.copy(alpha = 0.6f), radius = r * 0.13f, center = Offset(leftX, eyeY))
            drawCircle(secondary.copy(alpha = 0.6f), radius = r * 0.13f, center = Offset(rightX, eyeY))
            drawLine(primary, Offset(cx - r * 0.1f, eyeY), Offset(cx + r * 0.1f, eyeY), strokeWidth = 6f)
        }
        "CYBER_VR" -> {
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.56f, eyeY - r * 0.16f),
                size = Size(r * 1.12f, r * 0.32f),
                cornerRadius = CornerRadius(22f, 22f)
            )
            drawRoundRect(
                color = secondary,
                topLeft = Offset(cx - r * 0.48f, eyeY - r * 0.10f),
                size = Size(r * 0.96f, r * 0.20f),
                cornerRadius = CornerRadius(16f, 16f)
            )
        }
        "MONOCLE" -> {
            drawCircle(secondary.copy(alpha = 0.35f), radius = r * 0.21f, center = Offset(rightX, eyeY))
            drawCircle(primary, radius = r * 0.21f, center = Offset(rightX, eyeY), style = Stroke(width = 7f))
            drawLine(primary, Offset(rightX + r * 0.18f, eyeY + r * 0.1f), Offset(rightX + r * 0.26f, eyeY + r * 0.55f), strokeWidth = 4f)
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, strokeColor: Color, fillColor: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.toRadians((i * 36 - 90).toDouble())
        val rad = if (i % 2 == 0) radius else radius * 0.48f
        val x = cx + (rad * cos(angle)).toFloat()
        val y = cy + (rad * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, fillColor.copy(alpha = 0.7f))
    drawPath(path, strokeColor, style = Stroke(width = 4f))
}

private fun DrawScope.drawPouHat(
    cx: Float,
    cy: Float,
    r: Float,
    hat: AccessoryCatalogItem
) {
    val topY = cy - r * 0.88f
    val primary = Color(hat.primaryColorHex)
    val secondary = Color(hat.secondaryColorHex)

    when (hat.styleCode) {
        "CAP" -> {
            drawArc(
                color = primary,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - r * 0.46f, topY - r * 0.28f),
                size = Size(r * 0.92f, r * 0.56f)
            )
            drawCircle(secondary, radius = r * 0.09f, center = Offset(cx, topY - r * 0.12f))
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.48f, topY - r * 0.04f),
                size = Size(r * 1.12f, r * 0.12f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
        "CROWN" -> {
            val crownPath = Path().apply {
                moveTo(cx - r * 0.42f, topY + r * 0.05f)
                lineTo(cx - r * 0.46f, topY - r * 0.34f)
                lineTo(cx - r * 0.22f, topY - r * 0.14f)
                lineTo(cx, topY - r * 0.42f)
                lineTo(cx + r * 0.22f, topY - r * 0.14f)
                lineTo(cx + r * 0.46f, topY - r * 0.34f)
                lineTo(cx + r * 0.42f, topY + r * 0.05f)
                close()
            }
            drawPath(crownPath, primary)
            drawPath(crownPath, Color(0xFFB26A00), style = Stroke(width = 4f))
            drawCircle(secondary, radius = r * 0.055f, center = Offset(cx, topY - r * 0.12f))
            drawCircle(Color(0xFF29B6F6), radius = r * 0.045f, center = Offset(cx - r * 0.24f, topY - r * 0.06f))
            drawCircle(Color(0xFF29B6F6), radius = r * 0.045f, center = Offset(cx + r * 0.24f, topY - r * 0.06f))
        }
        "TOPHAT" -> {
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.36f, topY - r * 0.56f),
                size = Size(r * 0.72f, r * 0.58f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRect(
                color = secondary,
                topLeft = Offset(cx - r * 0.36f, topY - r * 0.12f),
                size = Size(r * 0.72f, r * 0.10f)
            )
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.58f, topY - r * 0.02f),
                size = Size(r * 1.16f, r * 0.11f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
        "CHEF" -> {
            drawCircle(primary, radius = r * 0.24f, center = Offset(cx - r * 0.22f, topY - r * 0.30f))
            drawCircle(primary, radius = r * 0.28f, center = Offset(cx, topY - r * 0.36f))
            drawCircle(primary, radius = r * 0.24f, center = Offset(cx + r * 0.22f, topY - r * 0.30f))
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.38f, topY - r * 0.22f),
                size = Size(r * 0.76f, r * 0.28f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRect(secondary, Offset(cx - r * 0.38f, topY - r * 0.02f), Size(r * 0.76f, r * 0.06f))
        }
        "WIZARD" -> {
            val cone = Path().apply {
                moveTo(cx - r * 0.58f, topY + r * 0.04f)
                lineTo(cx + r * 0.12f, topY - r * 0.72f)
                lineTo(cx + r * 0.58f, topY + r * 0.04f)
                close()
            }
            drawPath(cone, primary)
            drawRoundRect(primary, Offset(cx - r * 0.66f, topY - r * 0.02f), Size(r * 1.32f, r * 0.11f), CornerRadius(14f, 14f))
            drawCircle(secondary, radius = r * 0.06f, center = Offset(cx, topY - r * 0.26f))
            drawCircle(secondary, radius = r * 0.04f, center = Offset(cx - r * 0.15f, topY - r * 0.12f))
        }
        "CATEARS" -> {
            val leftEar = Path().apply {
                moveTo(cx - r * 0.52f, topY + r * 0.14f)
                lineTo(cx - r * 0.42f, topY - r * 0.32f)
                lineTo(cx - r * 0.15f, topY + r * 0.04f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(cx + r * 0.52f, topY + r * 0.14f)
                lineTo(cx + r * 0.42f, topY - r * 0.32f)
                lineTo(cx + r * 0.15f, topY + r * 0.04f)
                close()
            }
            drawPath(leftEar, primary)
            drawPath(rightEar, primary)
            drawCircle(secondary, radius = r * 0.08f, center = Offset(cx - r * 0.34f, topY - r * 0.08f))
            drawCircle(secondary, radius = r * 0.08f, center = Offset(cx + r * 0.34f, topY - r * 0.08f))
        }
        "HALO" -> {
            drawOval(
                color = primary,
                topLeft = Offset(cx - r * 0.44f, topY - r * 0.36f),
                size = Size(r * 0.88f, r * 0.22f),
                style = Stroke(width = 12f)
            )
            drawOval(
                color = secondary,
                topLeft = Offset(cx - r * 0.44f, topY - r * 0.36f),
                size = Size(r * 0.88f, r * 0.22f),
                style = Stroke(width = 5f)
            )
        }
    }
}

private fun DrawScope.drawPouShoes(
    cx: Float,
    cy: Float,
    r: Float,
    shoes: AccessoryCatalogItem
) {
    val footY = cy + r * 0.88f
    val leftFootX = cx - r * 0.42f
    val rightFootX = cx + r * 0.42f
    val primary = Color(shoes.primaryColorHex)
    val secondary = Color(shoes.secondaryColorHex)

    listOf(leftFootX, rightFootX).forEach { fx ->
        drawRoundRect(
            color = primary,
            topLeft = Offset(fx - r * 0.24f, footY - r * 0.10f),
            size = Size(r * 0.48f, r * 0.24f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = secondary,
            topLeft = Offset(fx - r * 0.24f, footY + r * 0.08f),
            size = Size(r * 0.48f, r * 0.06f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        if (shoes.styleCode == "SKATES") {
            drawCircle(secondary, radius = r * 0.055f, center = Offset(fx - r * 0.12f, footY + r * 0.18f))
            drawCircle(secondary, radius = r * 0.055f, center = Offset(fx + r * 0.12f, footY + r * 0.18f))
        } else if (shoes.styleCode == "BUNNY") {
            drawOval(secondary, Offset(fx - r * 0.12f, footY - r * 0.22f), Size(r * 0.09f, r * 0.16f))
            drawOval(secondary, Offset(fx + r * 0.03f, footY - r * 0.22f), Size(r * 0.09f, r * 0.16f))
        }
    }
}
