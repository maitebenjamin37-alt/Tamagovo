package com.example.ui.minigames

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CatalogData
import com.example.data.MiniGameCatalogItem
import com.example.data.MiniGameCategory
import com.example.data.MiniGameEngineType
import com.example.ui.OvoPetUiState

@Composable
fun MiniGameHubScreen(
    uiState: OvoPetUiState,
    onSelectGame: (MiniGameCatalogItem) -> Unit,
    onCloseGame: () -> Unit,
    onFinishGame: (MiniGameCatalogItem, Int, (Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGame = uiState.activeMiniGame
    if (activeGame != null) {
        ActiveMiniGameModal(
            game = activeGame,
            uiState = uiState,
            onBack = onCloseGame,
            onFinishGame = onFinishGame,
            modifier = modifier
        )
        return
    }

    var selectedCategory by remember { mutableStateOf(MiniGameCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredGames = remember(selectedCategory, searchQuery) {
        CatalogData.miniGames.filter { game ->
            val matchesCat = selectedCategory == MiniGameCategory.ALL || game.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                game.title.contains(searchQuery, ignoreCase = true) ||
                game.subtitle.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Arcade Summary Header
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF00897B))
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎮 Central de 30 Mini-Games",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Cada partida recompensa você com moedas virtuais 🪙 e XP!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (uiState.remainingBuffSeconds > 0 && !uiState.pet.activeBuffLabel.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Bônus Ativo: ${uiState.pet.activeBuffLabel} (${uiState.remainingBuffSeconds}s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF2C221E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("30", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            Text("Jogos", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar entre os 30 mini-games...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("minigame_search_input")
        )

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(MiniGameCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.emoji} ${cat.label}") },
                    modifier = Modifier.testTag("minigame_cat_${cat.name}")
                )
            }
        }

        // Grid of 30 Mini-Games
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 158.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredGames, key = { it.id }) { game ->
                val stats = uiState.miniGameScores[game.id]
                MiniGameCard(
                    game = game,
                    highScore = stats?.highScore ?: 0,
                    onPlay = { onSelectGame(game) }
                )
            }
        }
    }
}

@Composable
private fun MiniGameCard(
    game: MiniGameCatalogItem,
    highScore: Int,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("minigame_card_${game.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(game.accentColorHex).copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = game.iconEmoji, fontSize = 24.sp)
                    }
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "+${game.baseCoinReward} 🪙",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = game.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = game.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (highScore > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Recorde: $highScore pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = game.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(game.accentColorHex)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("play_minigame_${game.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Jogar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ActiveMiniGameModal(
    game: MiniGameCatalogItem,
    uiState: OvoPetUiState,
    onBack: () -> Unit,
    onFinishGame: (MiniGameCatalogItem, Int, (Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScore by remember(game.id) { mutableIntStateOf(0) }
    var earnedCoinsResult by remember(game.id) { mutableStateOf<Int?>(null) }

    BackHandler {
        if (earnedCoinsResult == null && currentScore > 0) {
            onFinishGame(game, currentScore) {
                onBack()
            }
        } else {
            onBack()
        }
    }

    if (earnedCoinsResult != null) {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text("🎉 Fim de Jogo! Parabéns!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Jogo: ${game.title}", style = MaterialTheme.typography.titleMedium)
                    Text("Pontuação: $currentScore pontos", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Recompensa: +$earnedCoinsResult Moedas Virtuais 🪙",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onBack() },
                    modifier = Modifier.testTag("claim_minigame_reward_button")
                ) {
                    Text("Coletar Moedas 🪙")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Game HUD Bar
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentScore > 0 && earnedCoinsResult == null) {
                                onFinishGame(game, currentScore) { coins ->
                                    earnedCoinsResult = coins
                                }
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("minigame_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Column {
                        Text(text = "${game.iconEmoji} ${game.title}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = game.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Live Score & Collect Reward Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "⭐ Pontos: $currentScore • Est. +${game.baseCoinReward + currentScore / 4} 🪙",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Button(
                onClick = {
                    onFinishGame(game, currentScore) { coins ->
                        earnedCoinsResult = coins
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("finish_and_collect_coins_button")
            ) {
                Text("Finalizar & Receber 🪙")
            }
        }

        // Engine Dispatcher for all 30 Mini-Games
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val triggerOver: (Int) -> Unit = { finalScore ->
                currentScore = finalScore
                if (earnedCoinsResult == null) {
                    onFinishGame(game, finalScore) { coins ->
                        earnedCoinsResult = coins
                    }
                }
            }

            when (game.engineType) {
                MiniGameEngineType.LANE_DODGE,
                MiniGameEngineType.FOOD_CATCH -> LaneDodgeAndCatchEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.RUNNER_JUMP -> RunnerJumpEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.BURGER_STACK -> BurgerAndTowerStackEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.FRUIT_TAP -> FruitTapNinjaEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.SLIDING_PUZZLE -> SlidingPuzzleEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.MATCH_THREE -> MatchThreeCandyEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.MEMORY_PAIRS -> MemoryPairsEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.GAME_2048 -> Pou2048Engine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.COLOR_FLOOD,
                MiniGameEngineType.MINI_SUDOKU,
                MiniGameEngineType.MAZE_ESCAPE -> ColorFloodAndMazeAndSudokuEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.RHYTHM_LANES -> RhythmLanesEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.SIMON_BEATS -> SimonBeatsEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.GOAL_SHOOTER -> GoalAndHoopShooterEngine(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
                MiniGameEngineType.WHACK_MOLE,
                MiniGameEngineType.PONG_BREAKER,
                MiniGameEngineType.SHELL_CUPS,
                MiniGameEngineType.MATH_RUSH -> ArcadeAndMindEngines(
                    game = game,
                    onScoreChange = { currentScore = it },
                    onGameOver = triggerOver
                )
            }
        }
    }
}
