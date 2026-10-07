package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BuffType
import com.example.data.CatalogData
import com.example.data.FoodCatalogItem
import com.example.data.FoodCategory
import com.example.ui.OvoPetUiState
import com.example.ui.components.PouAvatarCanvas

@Composable
fun KitchenFoodScreen(
    uiState: OvoPetUiState,
    onFeedFood: (FoodCatalogItem) -> Unit,
    onBuyFood: (FoodCatalogItem) -> Unit,
    onBuyAndFeed: (FoodCatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var showOnlyOwned by remember { mutableStateOf(false) }

    val filteredFoods = remember(selectedCategory, showOnlyOwned, uiState.foodInventory) {
        CatalogData.foods.filter { food ->
            val matchesCategory = selectedCategory == null || food.category == selectedCategory
            val matchesStock = !showOnlyOwned || (uiState.foodInventory[food.id] ?: 0) > 0
            matchesCategory && matchesStock
        }
    }

    val totalItemsInFridge = uiState.foodInventory.values.sum()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Kitchen Header with Mini Pou Preview & Hunger Gauge
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_room_kitchen_1791409078935),
                    contentDescription = "Cozinha do Pou",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x881E140C))
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🍽️ Cozinha & 14 Comidas",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Fome atual: ${uiState.pet.hunger}% • Na Geladeira: $totalItemsInFridge itens",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFE0B2)
                        )
                        if (uiState.remainingBuffSeconds > 0 && !uiState.pet.activeBuffLabel.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFFFB300),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${uiState.pet.activeBuffLabel} (${uiState.remainingBuffSeconds}s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF2C221E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Dica: Comidas especiais dão bônus de 2x moedas e XP!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    PouAvatarCanvas(
                        petState = uiState.pet,
                        isEating = uiState.isEating,
                        modifier = Modifier.size(110.dp)
                    )
                }
            }
        }

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null && !showOnlyOwned,
                    onClick = {
                        selectedCategory = null
                        showOnlyOwned = false
                    },
                    label = { Text("Todas (${CatalogData.foods.size})") },
                    modifier = Modifier.testTag("filter_food_all")
                )
            }
            item {
                FilterChip(
                    selected = showOnlyOwned,
                    onClick = { showOnlyOwned = !showOnlyOwned },
                    label = { Text("❄️ Na Geladeira ($totalItemsInFridge)") },
                    modifier = Modifier.testTag("filter_food_fridge")
                )
            }
            items(FoodCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    label = { Text("${category.iconEmoji} ${category.label}") },
                    modifier = Modifier.testTag("filter_food_${category.name}")
                )
            }
        }

        // Food Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredFoods, key = { it.id }) { food ->
                val stock = uiState.foodInventory[food.id] ?: 0
                FoodCardItem(
                    food = food,
                    stock = stock,
                    onFeed = { onFeedFood(food) },
                    onBuy = { onBuyFood(food) },
                    onBuyAndFeed = { onBuyAndFeed(food) }
                )
            }
        }
    }
}

@Composable
private fun FoodCardItem(
    food: FoodCatalogItem,
    stock: Int,
    onFeed: () -> Unit,
    onBuy: () -> Unit,
    onBuyAndFeed: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_card_${food.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(food.accentColorHex).copy(alpha = 0.16f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = food.emoji, fontSize = 30.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Surface(
                            color = if (stock > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Estoque: $stock",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (stock > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = food.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stat Restoration Pills & Temporary Bonus Badge
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatBonusPill("+${food.hungerRestore} 🍔", Color(0xFFFFE0B2), Color(0xFFE65100))
                        if (food.energyRestore > 0) {
                            StatBonusPill("+${food.energyRestore} ⚡", Color(0xFFFFF9C4), Color(0xFFF57F17))
                        }
                        if (food.healthRestore > 0) {
                            StatBonusPill("+${food.healthRestore} 💚", Color(0xFFC8E6C9), Color(0xFF1B5E20))
                        }
                        if (food.buffType != BuffType.NONE) {
                            StatBonusPill("✨ ${food.buffType.badgeText}", Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBuy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("buy_${food.id}")
                ) {
                    Text("Comprar +1 (${food.price} 🪙)")
                }

                Button(
                    onClick = if (stock > 0) onFeed else onBuyAndFeed,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stock > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feed_${food.id}")
                ) {
                    Text(
                        text = if (stock > 0) "Alimentar Agora 😋" else "Comprar & Dar (${food.price} 🪙)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBonusPill(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
