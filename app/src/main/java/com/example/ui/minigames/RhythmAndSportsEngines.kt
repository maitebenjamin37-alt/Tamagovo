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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

private data class RhythmNote(val id: Long, val lane: Int, val yFrac: Float)

// Games #19 Show de Ritmo, #20 Piano Mágico, #22 Bateria Maluca
@Composable
fun RhythmLanesEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val laneColors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFFB300))
    val notes = remember { mutableStateListOf<RhythmNote>() }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Toque nas pistas no ritmo! 🎶") }
    var timeLeft by remember { mutableIntStateOf(30) }
    var nextId by remember { mutableStateOf(1L) }

    LaunchedEffect(game.id) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        onGameOver(score)
    }

    LaunchedEffect(game.id) {
        var tick = 0
        while (timeLeft > 0) {
            delay(32L)
            tick++
            if (tick % (15 / game.speedFactor).toInt().coerceAtLeast(9) == 0) {
                notes.add(RhythmNote(nextId++, Random.nextInt(4), 0.04f))
            }
            val step = 0.020f * game.speedFactor
            val it = notes.listIterator()
            while (it.hasNext()) {
                val n = it.next()
                val ny = n.yFrac + step
                if (ny > 0.98f) {
                    it.remove()
                    combo = 0
                } else {
                    it.set(n.copy(yFrac = ny))
                }
            }
        }
    }

    fun tapLane(laneIdx: Int) {
        val hit = notes.firstOrNull { it.lane == laneIdx && it.yFrac in 0.62f..0.96f }
        if (hit != null) {
            notes.remove(hit)
            combo++
            val pts = 15 + (combo * 2).coerceAtMost(20)
            score += pts
            onScoreChange(score)
            feedback = "PERFEITO! Combo x$combo 🔥"
        } else {
            score += 4
            onScoreChange(score)
            feedback = "Boa batida! 🎵"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("⏱️ ${timeLeft}s • $feedback", style = MaterialTheme.typography.titleSmall)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1625))
        ) {
            val laneW = maxWidth / 4f
            val h = maxHeight

            // Target Hit Line
            Box(
                modifier = Modifier
                    .offset(y = h * 0.78f)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFFFD54F))
            )

            // Falling Musical Notes
            notes.forEach { n ->
                Box(
                    modifier = Modifier
                        .offset(x = laneW * n.lane + laneW * 0.18f, y = h * n.yFrac)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(laneColors[n.lane]),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = game.targetEmoji, fontSize = 24.sp)
                }
            }
        }

        // 4 Rhythm Pads
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (lane in 0..3) {
                Button(
                    onClick = { tapLane(lane) },
                    colors = ButtonDefaults.buttonColors(containerColor = laneColors[lane]),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .testTag("rhythm_pad_$lane")
                ) {
                    Text(game.targetEmoji, fontSize = 24.sp)
                }
            }
        }
    }
}

// Game #21: DJ Pou Mix (Simon Musical)
@Composable
fun SimonBeatsEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val padColors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFFB300))
    val padEmojis = listOf("🥁", "🎹", "🎸", "🎺")
    val sequence = remember { mutableStateListOf(Random.nextInt(4), Random.nextInt(4)) }
    var userStep by remember { mutableIntStateOf(0) }
    var litPad by remember { mutableStateOf<Int?>(null) }
    var isShowingSeq by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(sequence.size) {
        isShowingSeq = true
        delay(350)
        for (pad in sequence) {
            litPad = pad
            delay(420)
            litPad = null
            delay(180)
        }
        userStep = 0
        isShowingSeq = false
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isShowingSeq) "👀 Observe a sequência musical..." else "🎧 Sua vez! Repita os ${sequence.size} sons!",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.88f).aspectRatio(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (r in 0..1) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (c in 0..1) {
                        val idx = r * 2 + c
                        val isLit = litPad == idx
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isLit) Color.White else padColors[idx],
                            shadowElevation = if (isLit) 10.dp else 3.dp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable(enabled = !isShowingSeq) {
                                    if (sequence[userStep] == idx) {
                                        userStep++
                                        score += 12
                                        onScoreChange(score)
                                        if (userStep >= sequence.size) {
                                            sequence.add(Random.nextInt(4))
                                        }
                                    } else {
                                        onGameOver(score)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = padEmojis[idx], fontSize = 44.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Games #23 Pênalti Campeão, #24 Basquete Pou, #26 Arremesso de Argolas
@Composable
fun GoalAndHoopShooterEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var goalieX by remember { mutableFloatStateOf(0.3f) }
    var aimX by remember { mutableFloatStateOf(0.5f) }
    var aimDir by remember { mutableFloatStateOf(1f) }
    var shotsLeft by remember { mutableIntStateOf(8) }
    var score by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Mire e chute! ⚽") }

    LaunchedEffect(game.id) {
        var tick = 0f
        while (shotsLeft > 0) {
            delay(28L)
            tick += 0.06f * game.speedFactor
            aimX += 0.024f * aimDir * game.speedFactor
            if (aimX >= 0.86f) { aimX = 0.86f; aimDir = -1f }
            if (aimX <= 0.14f) { aimX = 0.14f; aimDir = 1f }
            goalieX = 0.5f + kotlin.math.sin(tick) * 0.32f
        }
    }

    fun shoot() {
        val hitKeeper = abs(aimX - goalieX) < 0.15f
        if (!hitKeeper) {
            score += 22
            onScoreChange(score)
            feedback = "GOLAÇO / CESTA PERFEITA! 🎉 +22 pts"
        } else {
            score += 5
            onScoreChange(score)
            feedback = "Quase! Defendido! 🧤"
        }
        shotsLeft--
        if (shotsLeft <= 0) {
            onGameOver(score)
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
            Text("Arremessos: $shotsLeft", style = MaterialTheme.typography.titleMedium)
            Text(feedback, style = MaterialTheme.typography.titleSmall)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.16f))
                .clickable { shoot() }
        ) {
            val w = maxWidth
            val h = maxHeight

            // Goal Frame at Top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .fillMaxWidth(0.8f)
                    .height(74.dp)
                    .border(4.dp, Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.targetEmoji, fontSize = 36.sp)
            }

            // Moving Goalkeeper / Blocker
            Box(
                modifier = Modifier
                    .offset(x = w * goalieX - 28.dp, y = 48.dp)
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.hazardEmoji, fontSize = 38.sp)
            }

            // Moving Aim Reticle
            Box(
                modifier = Modifier
                    .offset(x = w * aimX - 24.dp, y = h * 0.45f)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", fontSize = 26.sp)
            }

            // Ball at Bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = game.playerEmoji, fontSize = 44.sp)
            }
        }

        Button(
            onClick = { shoot() },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("btn_goal_shoot")
        ) {
            Text("🔥 CHUTAR / LANÇAR AGORA!", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Games #25 Whack-a-Pou, #27 Pong, #28 Quebra-Tijolos, #29 Onde Está o Pou, #30 Matemática Relâmpago
@Composable
fun ArcadeAndMindEngines(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    when (game.engineType) {
        com.example.data.MiniGameEngineType.WHACK_MOLE -> {
            var activeHole by remember { mutableIntStateOf(Random.nextInt(9)) }
            var isBomb by remember { mutableStateOf(false) }
            var score by remember { mutableIntStateOf(0) }
            var timeLeft by remember { mutableIntStateOf(25) }

            LaunchedEffect(game.id) {
                while (timeLeft > 0) {
                    delay(650L)
                    activeHole = Random.nextInt(9)
                    isBomb = Random.nextFloat() < 0.2f
                }
            }
            LaunchedEffect(game.id) {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
                onGameOver(score)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("⏱️ ${timeLeft}s • Toque no Pou 🐹 nas tocas!", style = MaterialTheme.typography.titleMedium)
                Column(
                    modifier = Modifier.fillMaxWidth(0.88f).aspectRatio(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (r in 0..2) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (c in 0..2) {
                                val idx = r * 3 + c
                                val hasTarget = activeHole == idx
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasTarget) Color(0xFFFFF8E1) else Color(0xFF4E342E),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable(enabled = hasTarget) {
                                            if (isBomb) {
                                                score = (score - 10).coerceAtLeast(0)
                                            } else {
                                                score += 15
                                            }
                                            onScoreChange(score)
                                            activeHole = Random.nextInt(9)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (hasTarget) (if (isBomb) "💣" else "🥚") else "🕳️",
                                            fontSize = 34.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        com.example.data.MiniGameEngineType.PONG_BREAKER -> {
            var paddleX by remember { mutableFloatStateOf(0.5f) }
            var ballX by remember { mutableFloatStateOf(0.5f) }
            var ballY by remember { mutableFloatStateOf(0.4f) }
            var vx by remember { mutableFloatStateOf(0.018f) }
            var vy by remember { mutableFloatStateOf(0.022f) }
            var score by remember { mutableIntStateOf(0) }
            var lives by remember { mutableIntStateOf(3) }

            LaunchedEffect(game.id) {
                while (lives > 0) {
                    delay(28L)
                    ballX += vx * game.speedFactor
                    ballY += vy * game.speedFactor
                    if (ballX <= 0.06f || ballX >= 0.94f) vx = -vx
                    if (ballY <= 0.08f) {
                        vy = -vy
                        score += 10
                        onScoreChange(score)
                    }
                    if (ballY in 0.78f..0.88f && abs(ballX - paddleX) < 0.22f && vy > 0) {
                        vy = -vy
                        score += 12
                        onScoreChange(score)
                    } else if (ballY > 0.96f) {
                        lives--
                        ballX = 0.5f
                        ballY = 0.35f
                        if (lives <= 0) onGameOver(score)
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Vidas: " + "❤️".repeat(lives.coerceAtLeast(0)), style = MaterialTheme.typography.titleMedium)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A237E))
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    // Bricks / Top Goal Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) {
                            Text(text = game.iconEmoji, fontSize = 26.sp)
                        }
                    }

                    // Ball
                    Box(
                        modifier = Modifier
                            .offset(x = w * ballX - 16.dp, y = h * ballY - 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.targetEmoji, fontSize = 20.sp)
                    }

                    // Player Paddle
                    Box(
                        modifier = Modifier
                            .offset(x = w * paddleX - 52.dp, y = h * 0.82f)
                            .width(104.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFFD54F))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { paddleX = (paddleX - 0.18f).coerceAtLeast(0.18f) },
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("⬅️ Esquerda") }
                    Button(
                        onClick = { paddleX = (paddleX + 0.18f).coerceAtMost(0.82f) },
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Direita ➡️") }
                }
            }
        }
        com.example.data.MiniGameEngineType.SHELL_CUPS -> {
            var correctCup by remember { mutableIntStateOf(Random.nextInt(3)) }
            var revealed by remember { mutableStateOf(false) }
            var round by remember { mutableIntStateOf(1) }
            var score by remember { mutableIntStateOf(0) }
            var msg by remember { mutableStateOf("Em qual copo o Pou 🥚 se escondeu?") }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Rodada $round / 6 • $msg", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (cup in 0..2) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                            modifier = Modifier
                                .size(100.dp, 130.dp)
                                .clickable {
                                    if (cup == correctCup) {
                                        score += 25
                                        onScoreChange(score)
                                        msg = "ACERTOU! O Pou estava no copo ${cup + 1}! 🎉"
                                    } else {
                                        score += 5
                                        onScoreChange(score)
                                        msg = "Quase! Ele estava no copo ${correctCup + 1}!"
                                    }
                                    if (round >= 6) {
                                        onGameOver(score)
                                    } else {
                                        round++
                                        correctCup = Random.nextInt(3)
                                    }
                                }
                                .testTag("shell_cup_$cup")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = if (revealed && cup == correctCup) "🥚" else "🥤", fontSize = 48.sp)
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // Game #30: Matemática Relâmpago
            var a by remember { mutableIntStateOf(Random.nextInt(3, 15)) }
            var b by remember { mutableIntStateOf(Random.nextInt(2, 12)) }
            var round by remember { mutableIntStateOf(1) }
            var score by remember { mutableIntStateOf(0) }

            val correct = a + b
            val options = remember(a, b, round) {
                listOf(correct, correct + 2, (correct - 3).coerceAtLeast(1), correct + 5).shuffled()
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Pergunta $round de 8", style = MaterialTheme.typography.titleMedium)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Box(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$a + $b = ?",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEachIndexed { idx, ans ->
                        Button(
                            onClick = {
                                if (ans == correct) {
                                    score += 20
                                } else {
                                    score += 5
                                }
                                onScoreChange(score)
                                if (round >= 8) {
                                    onGameOver(score)
                                } else {
                                    round++
                                    a = Random.nextInt(4, 20)
                                    b = Random.nextInt(3, 16)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("math_option_$idx")
                        ) {
                            Text("$ans", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
