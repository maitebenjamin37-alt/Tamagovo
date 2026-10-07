package com.example.ui.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MiniGameCatalogItem
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private data class FallingEntity(
    val id: Long,
    val lane: Int,
    val yFraction: Float,
    val isHazard: Boolean,
    val emoji: String
)

// Engine for Games #2 Turbo Kart, #3 Sky Fall, #6 Wave Surf, #7 Chuva de Comida, #10 Chuva de Doces
@Composable
fun LaneDodgeAndCatchEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var playerLane by remember { mutableIntStateOf(1) } // 0, 1, 2
    val entities = remember { mutableStateListOf<FallingEntity>() }
    var nextId by remember { mutableStateOf(1L) }

    LaunchedEffect(game.id) {
        var tick = 0
        while (lives > 0) {
            delay(35L)
            tick++
            val step = 0.018f * game.speedFactor + (score * 0.00015f).coerceAtMost(0.018f)

            // Spawn new item/hazard
            if (tick % (18 / game.speedFactor).toInt().coerceAtLeast(10) == 0) {
                val lane = Random.nextInt(3)
                val isHazard = Random.nextFloat() < 0.35f
                val emoji = if (isHazard) game.hazardEmoji else game.targetEmoji
                entities.add(FallingEntity(nextId++, lane, 0.02f, isHazard, emoji))
            }

            // Update positions & check collisions
            val iterator = entities.listIterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val newY = item.yFraction + step
                if (newY in 0.78f..0.92f && item.lane == playerLane) {
                    iterator.remove()
                    if (item.isHazard) {
                        lives -= 1
                        if (lives <= 0) {
                            onGameOver(score)
                        }
                    } else {
                        score += 10
                        onScoreChange(score)
                    }
                } else if (newY > 1.0f) {
                    iterator.remove()
                } else {
                    iterator.set(item.copy(yFraction = newY))
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Vidas: " + "❤️".repeat(lives.coerceAtLeast(0)),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Toque nas 3 faixas para mover!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.14f))
                .border(2.dp, Color(game.accentColorHex).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            val laneWidth = maxWidth / 3f
            val boardHeight = maxHeight

            // 3 Interactive Lanes
            Row(modifier = Modifier.fillMaxSize()) {
                for (laneIdx in 0..2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                            .clickable { playerLane = laneIdx }
                            .testTag("lane_$laneIdx")
                    )
                }
            }

            // Falling Items
            entities.forEach { entity ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = laneWidth * entity.lane + laneWidth * 0.25f,
                            y = boardHeight * entity.yFraction
                        )
                        .size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = entity.emoji, fontSize = 30.sp)
                }
            }

            // Player Avatar at Bottom
            Box(
                modifier = Modifier
                    .offset(
                        x = laneWidth * playerLane + laneWidth * 0.18f,
                        y = boardHeight * 0.80f
                    )
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.playerEmoji, fontSize = 34.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { playerLane = (playerLane - 1).coerceAtLeast(0) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_move_left")
            ) {
                Text("⬅️ Esquerda", style = MaterialTheme.typography.titleSmall)
            }
            Button(
                onClick = { playerLane = (playerLane + 1).coerceAtMost(2) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_move_right")
            ) {
                Text("Direita ➡️", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

// Engine for Games #1 Pou Runner, #4 Pulo nas Nuvens, #5 Jetpack Pou
@Composable
fun RunnerJumpEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var playerY by remember { mutableFloatStateOf(0f) } // 0f = ground, 1f = high jump
    var velocityY by remember { mutableFloatStateOf(0f) }
    var obstacleX by remember { mutableFloatStateOf(1.1f) }
    var coinX by remember { mutableFloatStateOf(0.75f) }
    var lives by remember { mutableIntStateOf(3) }

    LaunchedEffect(game.id) {
        while (lives > 0) {
            delay(30L)
            // Physics update
            playerY = (playerY + velocityY).coerceIn(0f, 1f)
            if (playerY > 0f) {
                velocityY -= 0.009f
            } else {
                velocityY = 0f
            }

            val speed = 0.022f * game.speedFactor
            obstacleX -= speed
            coinX -= speed * 0.9f

            // Check coin pickup
            if (abs(coinX - 0.18f) < 0.08f && playerY > 0.25f) {
                score += 12
                onScoreChange(score)
                coinX = 1.2f + Random.nextFloat() * 0.4f
            } else if (coinX < -0.1f) {
                coinX = 1.1f + Random.nextFloat() * 0.4f
            }

            // Check obstacle collision
            if (abs(obstacleX - 0.18f) < 0.07f && playerY < 0.28f) {
                lives -= 1
                obstacleX = 1.2f
                if (lives <= 0) {
                    onGameOver(score)
                }
            } else if (obstacleX < -0.1f) {
                score += 8
                onScoreChange(score)
                obstacleX = 1.05f + Random.nextFloat() * 0.35f
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Vidas: " + "❤️".repeat(lives.coerceAtLeast(0)), style = MaterialTheme.typography.titleMedium)
            Text("Toque na tela ou no botão para PULAR!", style = MaterialTheme.typography.bodyMedium)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.16f))
                .clickable {
                    if (playerY < 0.55f) velocityY = 0.085f
                }
        ) {
            val w = maxWidth
            val h = maxHeight

            // Ground Track
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(game.accentColorHex).copy(alpha = 0.35f))
            )

            // Floating Bonus Coin
            Box(
                modifier = Modifier
                    .offset(x = w * coinX, y = h * 0.36f)
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.targetEmoji, fontSize = 30.sp)
            }

            // Approaching Hazard
            Box(
                modifier = Modifier
                    .offset(x = w * obstacleX, y = h * 0.72f)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.hazardEmoji, fontSize = 34.sp)
            }

            // Jumping Pou Player
            Box(
                modifier = Modifier
                    .offset(
                        x = w * 0.14f,
                        y = h * (0.70f - playerY * 0.52f)
                    )
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.playerEmoji, fontSize = 34.sp)
            }
        }

        Button(
            onClick = {
                if (playerY < 0.65f) velocityY = 0.09f
            },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("btn_runner_jump")
        ) {
            Text("⬆️ PULAR / IMPULSIONAR!", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Engine for Games #8 Mestre Cuca Burger & #15 Torre de Blocos
@Composable
fun BurgerAndTowerStackEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val ingredients = remember(game.id) {
        if (game.id == 8) listOf("🍞", "🥩", "🧀", "🥬", "🍅", "🥓", "🍔")
        else listOf("🧱", "🟦", "🟧", "🟩", "🟪", "🟨", "🏛️")
    }
    var currentX by remember { mutableFloatStateOf(0.5f) }
    var direction by remember { mutableFloatStateOf(1f) }
    val stackedOffsets = remember { mutableStateListOf(0.5f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }

    LaunchedEffect(game.id, stackedOffsets.size) {
        while (lives > 0) {
            delay(25L)
            val speed = (0.018f + stackedOffsets.size * 0.0012f) * game.speedFactor
            currentX += speed * direction
            if (currentX >= 0.85f) {
                currentX = 0.85f
                direction = -1f
            } else if (currentX <= 0.15f) {
                currentX = 0.15f
                direction = 1f
            }
        }
    }

    fun dropLayer() {
        val lastX = stackedOffsets.lastOrNull() ?: 0.5f
        val diff = abs(currentX - lastX)
        if (diff <= 0.19f) {
            stackedOffsets.add(currentX)
            val bonus = if (diff < 0.06f) 20 else 12
            score += bonus
            onScoreChange(score)
        } else {
            lives -= 1
            if (lives <= 0) {
                onGameOver(score)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Chances: " + "❤️".repeat(lives.coerceAtLeast(0)), style = MaterialTheme.typography.titleMedium)
            Text("Camadas: ${stackedOffsets.size}", style = MaterialTheme.typography.titleMedium)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.14f))
                .clickable { dropLayer() }
        ) {
            val w = maxWidth
            val h = maxHeight

            // Moving Layer at Top
            Box(
                modifier = Modifier
                    .offset(x = w * currentX - 42.dp, y = 24.dp)
                    .width(84.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(game.accentColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ingredients[stackedOffsets.size % ingredients.size],
                    fontSize = 22.sp
                )
            }

            // Stacked Tower Layers at Bottom
            val visibleLayers = stackedOffsets.takeLast(8)
            visibleLayers.forEachIndexed { idx, xPos ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = w * xPos - 42.dp,
                            y = h - 54.dp - (idx * 32).dp
                        )
                        .width(84.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(2.dp, Color(game.accentColorHex), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ingredients[idx % ingredients.size], fontSize = 18.sp)
                }
            }
        }

        Button(
            onClick = { dropLayer() },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("btn_stack_drop")
        ) {
            Text("⬇️ EMPILHAR CAMADA AGORA!", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Engine for Game #9 Ninja das Frutas
private data class FlyingFruit(
    val id: Long,
    val xFrac: Float,
    val yFrac: Float,
    val emoji: String,
    val isBomb: Boolean
)

@Composable
fun FruitTapNinjaEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val fruitEmojis = listOf("🍉", "🍎", "🍌", "🍓", "🍍", "🍊")
    val targets = remember { mutableStateListOf<FlyingFruit>() }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var nextId by remember { mutableStateOf(1L) }

    LaunchedEffect(game.id) {
        while (timeLeft > 0) {
            delay(650L)
            if (targets.size < 6) {
                val isBomb = Random.nextFloat() < 0.22f
                targets.add(
                    FlyingFruit(
                        id = nextId++,
                        xFrac = Random.nextFloat() * 0.72f + 0.08f,
                        yFrac = Random.nextFloat() * 0.68f + 0.08f,
                        emoji = if (isBomb) "💣" else fruitEmojis.random(),
                        isBomb = isBomb
                    )
                )
            }
            if (targets.size > 4) {
                targets.removeAt(0)
            }
        }
    }

    LaunchedEffect(game.id) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        }
        onGameOver(score)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("⏱️ Tempo: ${timeLeft}s", style = MaterialTheme.typography.titleMedium)
            Text("Toque nas frutas 🍉 (Evite 💣!)", style = MaterialTheme.typography.bodyMedium)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.14f))
        ) {
            val w = maxWidth
            val h = maxHeight

            targets.forEach { fruit ->
                Surface(
                    shape = CircleShape,
                    color = if (fruit.isBomb) Color(0xFFFFEBEE) else Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .offset(x = w * fruit.xFrac, y = h * fruit.yFrac)
                        .size(64.dp)
                        .clickable {
                            targets.remove(fruit)
                            if (fruit.isBomb) {
                                score = (score - 15).coerceAtLeast(0)
                                onScoreChange(score)
                            } else {
                                score += 15
                                onScoreChange(score)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = fruit.emoji, fontSize = 34.sp)
                    }
                }
            }
        }
    }
}
