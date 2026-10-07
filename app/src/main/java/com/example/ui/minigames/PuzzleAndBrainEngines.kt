package com.example.ui.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

// Game #11: Sliding Puzzle 3x3
@Composable
fun SlidingPuzzleEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val tiles = remember {
        mutableStateListOf(1, 2, 3, 4, 5, 6, 7, 0, 8).apply {
            // Do 12 valid random moves so it's always solvable
            var emptyIdx = indexOf(0)
            repeat(14) {
                val r = emptyIdx / 3
                val c = emptyIdx % 3
                val neighbors = mutableListOf<Int>()
                if (r > 0) neighbors.add((r - 1) * 3 + c)
                if (r < 2) neighbors.add((r + 1) * 3 + c)
                if (c > 0) neighbors.add(r * 3 + (c - 1))
                if (c < 2) neighbors.add(r * 3 + (c + 1))
                val pick = neighbors.random()
                this[emptyIdx] = this[pick]
                this[pick] = 0
                emptyIdx = pick
            }
        }
    }
    var score by remember { mutableIntStateOf(0) }
    var moves by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Movimentos: $moves • Ordene de 1 a 8!", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.15f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val number = tiles[idx]
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (number == 0) Color.Transparent else Color(game.accentColorHex),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable(enabled = number != 0) {
                                    val emptyIdx = tiles.indexOf(0)
                                    val r1 = idx / 3
                                    val c1 = idx % 3
                                    val r2 = emptyIdx / 3
                                    val c2 = emptyIdx % 3
                                    if (abs(r1 - r2) + abs(c1 - c2) == 1) {
                                        tiles[emptyIdx] = number
                                        tiles[idx] = 0
                                        moves++
                                        score += 8
                                        onScoreChange(score)
                                        if (tiles.toList() == listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)) {
                                            val finalScore = score + 120
                                            onScoreChange(finalScore)
                                            onGameOver(finalScore)
                                        }
                                    }
                                }
                        ) {
                            if (number != 0) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$number",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = { onGameOver(score + 25) }) {
            Text("Concluir & Coletar Moedas 🪙")
        }
    }
}

// Game #12: Combine 3 Doces (Match-3)
@Composable
fun MatchThreeCandyEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val candies = listOf("🍬", "🍭", "🍩", "🧁", "🍪")
    val grid = remember {
        mutableStateListOf<String>().apply {
            repeat(25) { add(candies.random()) }
        }
    }
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(15) }

    fun checkAndPopMatches() {
        val toReplace = mutableSetOf<Int>()
        // Rows
        for (r in 0..4) {
            for (c in 0..2) {
                val i = r * 5 + c
                if (grid[i] == grid[i + 1] && grid[i] == grid[i + 2]) {
                    toReplace.addAll(listOf(i, i + 1, i + 2))
                }
            }
        }
        // Cols
        for (c in 0..4) {
            for (r in 0..2) {
                val i = r * 5 + c
                if (grid[i] == grid[i + 5] && grid[i] == grid[i + 10]) {
                    toReplace.addAll(listOf(i, i + 5, i + 10))
                }
            }
        }
        if (toReplace.isNotEmpty()) {
            score += toReplace.size * 10
            onScoreChange(score)
            toReplace.forEach { idx ->
                grid[idx] = candies.random()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Jogadas restantes: $movesLeft • Toque em 2 doces vizinhos!", style = MaterialTheme.typography.titleSmall)

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(game.accentColorHex).copy(alpha = 0.14f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 0..4) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in 0..4) {
                        val idx = r * 5 + c
                        val isSelected = selectedIdx == idx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFFFF59D) else Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    val prev = selectedIdx
                                    if (prev == null) {
                                        selectedIdx = idx
                                    } else if (prev == idx) {
                                        selectedIdx = null
                                    } else {
                                        val r1 = prev / 5
                                        val c1 = prev % 5
                                        if (abs(r1 - r) + abs(c1 - c) == 1) {
                                            val tmp = grid[prev]
                                            grid[prev] = grid[idx]
                                            grid[idx] = tmp
                                            selectedIdx = null
                                            score += 10
                                            onScoreChange(score)
                                            checkAndPopMatches()
                                            movesLeft--
                                            if (movesLeft <= 0) {
                                                onGameOver(score)
                                            }
                                        } else {
                                            selectedIdx = idx
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = grid[idx], fontSize = 26.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Game #13: Jogo da Memória
@Composable
fun MemoryPairsEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val icons = remember {
        listOf("🍎", "🍕", "🍦", "👑", "🕶️", "👟", "🍔", "🧁")
            .flatMap { listOf(it, it) }
            .shuffled()
    }
    val matched = remember { mutableStateListOf<Int>() }
    val flipped = remember { mutableStateListOf<Int>() }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(flipped.size) {
        if (flipped.size == 2) {
            val first = flipped[0]
            val second = flipped[1]
            if (icons[first] == icons[second]) {
                matched.add(first)
                matched.add(second)
                flipped.clear()
                score += 25
                onScoreChange(score)
                if (matched.size == icons.size) {
                    delay(400)
                    onGameOver(score + 50)
                }
            } else {
                delay(650)
                flipped.clear()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Pares encontrados: ${matched.size / 2} / 8", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0..3) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0..3) {
                        val idx = r * 4 + c
                        val isVisible = idx in matched || idx in flipped
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isVisible) Color.White else Color(game.accentColorHex)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable(enabled = !isVisible && flipped.size < 2) {
                                    flipped.add(idx)
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isVisible) icons[idx] else "🥚",
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Game #14: 2048 do Ovinho
@Composable
fun Pou2048Engine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    val board = remember {
        mutableStateListOf(
            2, 2, 0, 0,
            0, 4, 0, 0,
            0, 0, 2, 0,
            0, 0, 0, 0
        )
    }
    var score by remember { mutableIntStateOf(0) }

    fun slideLine(line: List<Int>): Pair<List<Int>, Int> {
        val nonZero = line.filter { it != 0 }.toMutableList()
        val result = mutableListOf<Int>()
        var gained = 0
        var i = 0
        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val merged = nonZero[i] * 2
                result.add(merged)
                gained += merged
                i += 2
            } else {
                result.add(nonZero[i])
                i++
            }
        }
        while (result.size < 4) result.add(0)
        return result to gained
    }

    fun move(dir: String) {
        var totalGain = 0
        for (idx in 0..3) {
            val indices = when (dir) {
                "LEFT" -> listOf(idx * 4, idx * 4 + 1, idx * 4 + 2, idx * 4 + 3)
                "RIGHT" -> listOf(idx * 4 + 3, idx * 4 + 2, idx * 4 + 1, idx * 4)
                "UP" -> listOf(idx, idx + 4, idx + 8, idx + 12)
                else -> listOf(idx + 12, idx + 8, idx + 4, idx)
            }
            val currentLine = indices.map { board[it] }
            val (newLine, gain) = slideLine(currentLine)
            totalGain += gain
            indices.forEachIndexed { pos, boardPos -> board[boardPos] = newLine[pos] }
        }
        val empties = board.indices.filter { board[it] == 0 }
        if (empties.isNotEmpty()) {
            board[empties.random()] = if (Random.nextFloat() < 0.8f) 2 else 4
        }
        score += totalGain.coerceAtLeast(4)
        onScoreChange(score)
        if (board.none { it == 0 }) {
            onGameOver(score)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFBCAAA4))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0..3) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0..3) {
                        val v = board[r * 4 + c]
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (v == 0) Color(0xFFD7CCC8) else Color(0xFFFFF8E1),
                            modifier = Modifier.weight(1f).fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (v > 0) {
                                    Text(
                                        text = "$v",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { move("LEFT") }, modifier = Modifier.testTag("btn_2048_left")) { Text("⬅️") }
            Button(onClick = { move("UP") }, modifier = Modifier.testTag("btn_2048_up")) { Text("⬆️") }
            Button(onClick = { move("DOWN") }, modifier = Modifier.testTag("btn_2048_down")) { Text("⬇️") }
            Button(onClick = { move("RIGHT") }, modifier = Modifier.testTag("btn_2048_right")) { Text("➡️") }
        }
    }
}

// Games #16 Color Flood, #17 Mini Sudoku 4x4, #18 Labirinto do Tesouro
@Composable
fun ColorFloodAndMazeAndSudokuEngine(
    game: MiniGameCatalogItem,
    onScoreChange: (Int) -> Unit,
    onGameOver: (Int) -> Unit
) {
    when (game.engineType) {
        com.example.data.MiniGameEngineType.COLOR_FLOOD -> {
            val palette = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835), Color(0xFF8E24AA))
            val cells = remember { mutableStateListOf<Int>().apply { repeat(25) { add(Random.nextInt(palette.size)) } } }
            var score by remember { mutableIntStateOf(0) }

            fun flood(targetColorIdx: Int) {
                val startColor = cells[0]
                if (startColor == targetColorIdx) return
                val visited = mutableSetOf<Int>()
                val queue = ArrayDeque<Int>()
                queue.add(0)
                while (queue.isNotEmpty()) {
                    val curr = queue.removeFirst()
                    if (curr in visited) continue
                    visited.add(curr)
                    cells[curr] = targetColorIdx
                    val r = curr / 5
                    val c = curr % 5
                    listOfNotNull(
                        if (r > 0) (r - 1) * 5 + c else null,
                        if (r < 4) (r + 1) * 5 + c else null,
                        if (c > 0) r * 5 + (c - 1) else null,
                        if (c < 4) r * 5 + (c + 1) else null
                    ).forEach { n ->
                        if (cells[n] == startColor && n !in visited) queue.add(n)
                    }
                }
                score += 15
                onScoreChange(score)
                if (cells.all { it == targetColorIdx }) {
                    onGameOver(score + 60)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Unifique todas as células com a mesma cor!", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0..4) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (c in 0..4) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette[cells[r * 5 + c]])
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palette.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { flood(idx) }
                        )
                    }
                }
            }
        }
        com.example.data.MiniGameEngineType.MINI_SUDOKU -> {
            val symbols = listOf("🍎", "🍕", "🍦", "⭐")
            val solution = listOf(
                0, 1, 2, 3,
                2, 3, 0, 1,
                1, 0, 3, 2,
                3, 2, 1, 0
            )
            val board = remember {
                mutableStateListOf(
                    0, 1, -1, 3,
                    2, -1, 0, 1,
                    1, 0, -1, 2,
                    -1, 2, 1, 0
                )
            }
            var score by remember { mutableIntStateOf(0) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Toque nos espaços '?' para alternar entre 🍎, 🍕, 🍦 e ⭐!", style = MaterialTheme.typography.bodyMedium)
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (r in 0..3) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (c in 0..3) {
                                val idx = r * 4 + c
                                val v = board[idx]
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (v == solution[idx]) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable {
                                            board[idx] = (v + 1) % 4
                                            score += 10
                                            onScoreChange(score)
                                            if (board.toList() == solution) {
                                                onGameOver(score + 80)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = if (v in 0..3) symbols[v] else "❓", fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // Game #18: Maze Escape
            var playerPos by remember { mutableIntStateOf(0) }
            val walls = remember { setOf(1, 7, 11, 13, 17, 18) }
            val coinsSet = remember { mutableStateListOf(2, 6, 12, 19, 22) }
            var score by remember { mutableIntStateOf(0) }

            fun stepMaze(delta: Int) {
                val next = playerPos + delta
                if (next !in 0..24 || next in walls) return
                if (abs((next % 5) - (playerPos % 5)) > 1 && abs(delta) == 1) return
                playerPos = next
                if (next in coinsSet) {
                    coinsSet.remove(next)
                    score += 20
                    onScoreChange(score)
                } else {
                    score += 4
                    onScoreChange(score)
                }
                if (playerPos == 24) {
                    onGameOver(score + 60)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Leve o Pou 🥚 até o Baú 👑 desviando das paredes 🧱!", style = MaterialTheme.typography.bodyMedium)
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0..4) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (c in 0..4) {
                                val idx = r * 5 + c
                                val emoji = when {
                                    idx == playerPos -> "🥚"
                                    idx == 24 -> "👑"
                                    idx in walls -> "🧱"
                                    idx in coinsSet -> "🪙"
                                    else -> "·"
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (idx in walls) Color(0xFF5D4037) else Color.White,
                                    modifier = Modifier.weight(1f).fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { stepMaze(-1) }) { Text("⬅️") }
                    Button(onClick = { stepMaze(-5) }) { Text("⬆️") }
                    Button(onClick = { stepMaze(5) }) { Text("⬇️") }
                    Button(onClick = { stepMaze(1) }) { Text("➡️") }
                }
            }
        }
    }
}
