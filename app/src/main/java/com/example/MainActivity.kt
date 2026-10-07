package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainTab
import com.example.ui.OvoPetUiState
import com.example.ui.OvoPetViewModel
import com.example.ui.minigames.MiniGameHubScreen
import com.example.ui.screens.HomeRoomScreen
import com.example.ui.screens.KitchenFoodScreen
import com.example.ui.screens.WardrobeAccessoriesScreen
import com.example.ui.theme.OvoPetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OvoPetTheme {
                OvoPetApp()
            }
        }
    }
}

@Composable
fun OvoPetApp(
    viewModel: OvoPetViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.currentTab != MainTab.HOME && uiState.activeMiniGame == null) {
        BackHandler {
            viewModel.selectTab(MainTab.HOME)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            OvoPetTopBar(uiState = uiState)
        },
        bottomBar = {
            OvoPetBottomBar(
                currentTab = uiState.currentTab,
                totalFoodCount = uiState.foodInventory.values.sum(),
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                MainTab.HOME -> HomeRoomScreen(
                    uiState = uiState,
                    onPetTapped = { viewModel.petPou() },
                    onBathClicked = { viewModel.giveBath() },
                    onSleepClicked = { viewModel.toggleSleep() },
                    onHealClicked = { viewModel.giveHealthPotion() },
                    onTrainEvolution = { viewModel.trainEvolutionBond() },
                    onSelectEvolutionForm = { viewModel.selectEvolutionForm(it) },
                    onDismissEvolutionModal = { viewModel.dismissEvolutionDialog() },
                    onQuickFeed = { viewModel.buyAndFeedFoodDirectly(it) },
                    onRenamePet = { viewModel.renamePet(it) },
                    onNavigateTab = { viewModel.selectTab(it) }
                )
                MainTab.KITCHEN -> KitchenFoodScreen(
                    uiState = uiState,
                    onFeedFood = { viewModel.feedPou(it) },
                    onBuyFood = { viewModel.buyFood(it) },
                    onBuyAndFeed = { viewModel.buyAndFeedFoodDirectly(it) }
                )
                MainTab.WARDROBE -> WardrobeAccessoriesScreen(
                    uiState = uiState,
                    onBuyOrEquip = { viewModel.buyOrEquipAccessory(it) }
                )
                MainTab.MINIGAMES -> MiniGameHubScreen(
                    uiState = uiState,
                    onSelectGame = { viewModel.openMiniGame(it) },
                    onCloseGame = { viewModel.closeMiniGame() },
                    onFinishGame = { game, score, cb ->
                        viewModel.finishMiniGame(game, score, cb)
                    }
                )
            }

            // Floating Feedback Toast Banner
            AnimatedVisibility(
                visible = uiState.statusBannerMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Surface(
                    color = Color(0xFF2C221E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = uiState.statusBannerMessage.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OvoPetTopBar(uiState: OvoPetUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🥚", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "OvoPet • ${uiState.pet.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Nível ${uiState.pet.level} • ${uiState.pet.totalMiniGamesPlayed} jogos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Virtual Coins Pill
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("coins_counter_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.pet.coins}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OvoPetBottomBar(
    currentTab: MainTab,
    totalFoodCount: Int,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Casa"
                )
            },
            label = { Text("Casa") },
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.KITCHEN,
            onClick = { onTabSelected(MainTab.KITCHEN) },
            icon = {
                BadgedBox(
                    badge = {
                        if (totalFoodCount > 0) {
                            Badge { Text("$totalFoodCount") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.KITCHEN) Icons.Filled.Restaurant else Icons.Outlined.Restaurant,
                        contentDescription = "Cozinha"
                    )
                }
            },
            label = { Text("Cozinha") },
            modifier = Modifier.testTag("nav_tab_kitchen")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.WARDROBE,
            onClick = { onTabSelected(MainTab.WARDROBE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.WARDROBE) Icons.Filled.Checkroom else Icons.Outlined.Checkroom,
                    contentDescription = "Armário"
                )
            },
            label = { Text("Armário") },
            modifier = Modifier.testTag("nav_tab_wardrobe")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.MINIGAMES,
            onClick = { onTabSelected(MainTab.MINIGAMES) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("30")
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.MINIGAMES) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                        contentDescription = "30 Jogos"
                    )
                }
            },
            label = { Text("30 Jogos") },
            modifier = Modifier.testTag("nav_tab_minigames")
        )
    }
}
