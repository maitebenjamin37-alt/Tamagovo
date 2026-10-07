package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccessoryCatalogItem
import com.example.data.AccessoryCategory
import com.example.data.CatalogData
import com.example.ui.OvoPetUiState
import com.example.ui.components.PouAvatarCanvas

@Composable
fun WardrobeAccessoriesScreen(
    uiState: OvoPetUiState,
    onBuyOrEquip: (AccessoryCatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(AccessoryCategory.HAT) }

    val itemsForCategory = remember(selectedCategory) {
        CatalogData.accessories.filter { it.category == selectedCategory }
    }

    val pet = uiState.pet

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Live Pou Fitting Room Preview Card
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4A148C),
                                Color(0xFF7B1FA2),
                                Color(0xFFAB47BC)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🎩 Provador do Pou",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Personalize seu Pou com chapéus, óculos, camisas, sapatos e cores!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f)
                        )

                        // Currently Equipped Summary
                        val equippedNames = remember(pet) {
                            listOfNotNull(
                                CatalogData.accessories.find { it.id == pet.equippedHatId }?.let { "${it.emoji} ${it.name}" },
                                CatalogData.accessories.find { it.id == pet.equippedGlassesId }?.let { "${it.emoji} ${it.name}" },
                                CatalogData.accessories.find { it.id == pet.equippedShirtId }?.let { "${it.emoji} ${it.name}" },
                                CatalogData.accessories.find { it.id == pet.equippedShoesId }?.let { "${it.emoji} ${it.name}" }
                            )
                        }
                        Text(
                            text = if (equippedNames.isEmpty()) "Nenhum acessório equipado" else "Usando: ${equippedNames.joinToString(" • ")}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFE082),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    PouAvatarCanvas(
                        petState = pet,
                        modifier = Modifier.size(145.dp)
                    )
                }
            }
        }

        // Category Tabs: Chapéus, Óculos, Camisas, Sapatos, Cores
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(AccessoryCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = "${category.iconEmoji} ${category.label}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.testTag("wardrobe_tab_${category.name}")
                )
            }
        }

        // Grid of Accessories in Selected Category
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 155.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(itemsForCategory, key = { it.id }) { item ->
                val isUnlocked = uiState.unlockedAccessoryIds.contains(item.id) || item.price == 0
                val isEquipped = when (item.category) {
                    AccessoryCategory.HAT -> pet.equippedHatId == item.id
                    AccessoryCategory.GLASSES -> pet.equippedGlassesId == item.id
                    AccessoryCategory.SHIRT -> pet.equippedShirtId == item.id
                    AccessoryCategory.SHOES -> pet.equippedShoesId == item.id
                    AccessoryCategory.BODY_COLOR -> pet.bodyColorHex == item.primaryColorHex
                }

                AccessoryGridCard(
                    item = item,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    onAction = { onBuyOrEquip(item) }
                )
            }
        }
    }
}

@Composable
private fun AccessoryGridCard(
    item: AccessoryCatalogItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onAction: () -> Unit
) {
    val borderMod = if (isEquipped) {
        Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
    } else {
        Modifier
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = borderMod
            .fillMaxWidth()
            .testTag("accessory_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color(item.primaryColorHex).copy(alpha = 0.22f),
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = item.emoji, fontSize = 30.sp)
                }
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isEquipped -> MaterialTheme.colorScheme.secondary
                        isUnlocked -> MaterialTheme.colorScheme.primary
                        else -> Color(0xFFFF8F00)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessory_action_${item.id}")
            ) {
                val btnText = when {
                    isEquipped && item.category != AccessoryCategory.BODY_COLOR -> "Equipado (Tirar)"
                    isEquipped -> "Cor Equipada ✓"
                    isUnlocked -> "Equipar"
                    else -> "Comprar ${item.price} 🪙"
                }
                Text(
                    text = btnText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
