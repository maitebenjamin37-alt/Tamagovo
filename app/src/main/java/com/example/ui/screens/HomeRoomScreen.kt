package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CatalogData
import com.example.data.EvolutionStage
import com.example.data.FoodCatalogItem
import com.example.ui.MainTab
import com.example.ui.OvoPetUiState
import com.example.ui.components.PouAvatarCanvas
import com.example.ui.theme.CleanBlue
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.FunPink
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.HungerOrange

@Composable
fun HomeRoomScreen(
    uiState: OvoPetUiState,
    onPetTapped: () -> Unit,
    onBathClicked: () -> Unit,
    onSleepClicked: () -> Unit,
    onHealClicked: () -> Unit,
    onTrainEvolution: () -> Unit,
    onSelectEvolutionForm: (EvolutionStage) -> Unit,
    onDismissEvolutionModal: () -> Unit,
    onQuickFeed: (FoodCatalogItem) -> Unit,
    onRenamePet: (String) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val pet = uiState.pet
    val currentStage = pet.activeEvolutionStage
    val highestStage = EvolutionStage.fromOrdinal(pet.highestEvolutionOrdinal)
    val nextStage = EvolutionStage.entries.getOrNull(highestStage.ordinal + 1)

    var showRenameDialog by remember { mutableStateOf(false) }
    var showEvolutionTreeDialog by remember { mutableStateOf(false) }
    var draftName by remember(pet.name) { mutableStateOf(pet.name) }

    // Celebration Modal when Pet Evolves into a New Creature Form
    val evolvedStage = uiState.newlyEvolvedStage
    if (evolvedStage != null) {
        AlertDialog(
            onDismissRequest = onDismissEvolutionModal,
            title = {
                Text(
                    text = "✨ SEU BICHINHO EVOLUIU! ✨",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PouAvatarCanvas(
                            petState = pet,
                            stageOverride = evolvedStage,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "${evolvedStage.badgeEmoji} Estágio ${evolvedStage.stageNumber}: ${evolvedStage.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = evolvedStage.description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Novos Traços: ${evolvedStage.visualTraitSummary}\nBônus Permanente: +${evolvedStage.coinBonusPercent}% 🪙 e +${evolvedStage.xpBonusPercent}% XP\nPrêmio de Evolução: +100 🪙!",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissEvolutionModal,
                    modifier = Modifier.testTag("confirm_evolution_celebration_btn")
                ) {
                    Text("Incrível! Continuar 🎉")
                }
            }
        )
    }

    // Evolution Tree / 5 Stages Inspector Dialog
    if (showEvolutionTreeDialog) {
        AlertDialog(
            onDismissRequest = { showEvolutionTreeDialog = false },
            title = { Text("🧬 Jornada de Evolução (5 Formas)") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(390.dp)
                ) {
                    items(EvolutionStage.entries) { stage ->
                        val isUnlocked = stage.ordinal <= pet.highestEvolutionOrdinal
                        val isActive = currentStage == stage
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PouAvatarCanvas(
                                        petState = pet,
                                        stageOverride = stage,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${stage.badgeEmoji} ${stage.stageNumber}. ${stage.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stage.visualTraitSummary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (isUnlocked) "Desbloqueado • +${stage.coinBonusPercent}% 🪙"
                                        else "Requer Nível ${stage.minLevel} ou ${stage.minCarePoints} pts de Cuidado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFD84315)
                                    )
                                    if (isUnlocked && !isActive) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                onSelectEvolutionForm(stage)
                                                showEvolutionTreeDialog = false
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Usar Forma", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showEvolutionTreeDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Dar Nome ao seu Pou") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Nome do Bichinho") },
                    singleLine = true,
                    modifier = Modifier.testTag("rename_pet_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenamePet(draftName)
                        showRenameDialog = false
                    },
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 5 Vital Need Bars Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                draftName = pet.name
                                showRenameDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("edit_pet_name_button")
                    ) {
                        Text(
                            text = "${currentStage.badgeEmoji} ${pet.name}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nome do Pou",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Nível ${pet.level} • ${pet.xp}/${pet.level * 80} XP",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Needs Grid (2 rows)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeedMeterItem("🍔 Fome", pet.hunger, HungerOrange, Modifier.weight(1f))
                    NeedMeterItem("⚡ Energia", pet.energy, EnergyYellow, Modifier.weight(1f))
                    NeedMeterItem("🫧 Higiene", pet.cleanliness, CleanBlue, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeedMeterItem("🎉 Diversão", pet.happiness, FunPink, Modifier.weight(1f))
                    NeedMeterItem("💚 Saúde", pet.health, HealthGreen, Modifier.weight(1f))
                }

                // Active Temporary Food Buff Banner
                AnimatedVisibility(visible = uiState.remainingBuffSeconds > 0 && !pet.activeBuffLabel.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ Bônus Ativo: ${pet.activeBuffLabel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "${uiState.remainingBuffSeconds}s",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Interactive Pou Stage with Cozy Room Background
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(275.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_room_living_1791409069872),
                    contentDescription = "Quarto do Pou",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (pet.isSleeping) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99121829))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x44000000))
                                )
                            )
                    )
                }

                // Speech / Mood Bubble
                val moodText = when {
                    uiState.isEating -> "Nhac nhac! Que delícia! 😋"
                    uiState.isBathing -> "Fresquinho e cheiroso! 🫧"
                    pet.isSleeping -> "Zzz... Sonhando com moedas... 😴"
                    pet.hunger < 35 -> "Estou com fominha! Me dê comida 🍕"
                    pet.cleanliness < 45 -> "Preciso de um banho de espuma! 🛁"
                    else -> "${currentStage.badgeEmoji} Forma: ${currentStage.title} (Toque em mim!)"
                }

                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(50),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = moodText,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF2C221E),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                PouAvatarCanvas(
                    petState = pet,
                    isEating = uiState.isEating,
                    isBathing = uiState.isBathing,
                    onPetTapped = onPetTapped,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp, bottom = 8.dp)
                )
            }
        }

        // Creature Evolution Progress & Bonding Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("evolution_progress_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🧬 Evolução: ${currentStage.badgeEmoji} ${currentStage.title} (Estágio ${currentStage.stageNumber}/5)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Pontos de Cuidado & Interação: ${pet.carePoints} pts • Bônus: +${currentStage.coinBonusPercent}% 🪙",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (nextStage != null) {
                    val careProgress = (pet.carePoints.toFloat() / nextStage.minCarePoints.toFloat()).coerceIn(0f, 1f)
                    val levelProgress = (pet.level.toFloat() / nextStage.minLevel.toFloat()).coerceIn(0f, 1f)
                    val bestProgress = maxOf(careProgress, levelProgress)

                    LinearProgressIndicator(
                        progress = { bestProgress },
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Próxima forma: ${nextStage.badgeEmoji} ${nextStage.title} (Meta: ${pet.carePoints}/${nextStage.minCarePoints} Cuidado ou Nível ${pet.level}/${nextStage.minLevel})",
                        style = MaterialTheme.typography.labelSmall
                    )
                } else {
                    Text(
                        text = "🏆 Evolução Máxima Atingida! Todas as 5 formas de criatura desbloqueadas!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTrainEvolution,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_train_evolution")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Interagir (+6 Cuidado)")
                    }

                    OutlinedButton(
                        onClick = { showEvolutionTreeDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_open_evolution_tree")
                    ) {
                        Text("Ver 5 Formas")
                    }
                }
            }
        }

        // 4 Care Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CareActionButton(
                label = "Carinho",
                icon = Icons.Default.Favorite,
                containerColor = Color(0xFFFCE4EC),
                contentColor = Color(0xFFC2185B),
                onClick = onPetTapped,
                testTag = "btn_care_pet",
                modifier = Modifier.weight(1f)
            )
            CareActionButton(
                label = "Banho",
                icon = Icons.Default.Bathtub,
                containerColor = Color(0xFFE1F5FE),
                contentColor = Color(0xFF0277BD),
                onClick = onBathClicked,
                testTag = "btn_care_bath",
                modifier = Modifier.weight(1f)
            )
            CareActionButton(
                label = if (pet.isSleeping) "Acordar" else "Dormir",
                icon = if (pet.isSleeping) Icons.Default.WbSunny else Icons.Default.Bedtime,
                containerColor = Color(0xFFEDE7F6),
                contentColor = Color(0xFF512DA8),
                onClick = onSleepClicked,
                testTag = "btn_care_sleep",
                modifier = Modifier.weight(1f)
            )
            CareActionButton(
                label = "Saúde",
                icon = Icons.Default.MedicalServices,
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                onClick = onHealClicked,
                testTag = "btn_care_heal",
                modifier = Modifier.weight(1f)
            )
        }

        // Quick-Feed Bar from Inventory
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍽️ Lanche Rápido (+4 Cuidado)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(
                        onClick = { onNavigateTab(MainTab.KITCHEN) },
                        modifier = Modifier.testTag("home_go_to_kitchen_button")
                    ) {
                        Text("Ver 14 Comidas →")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(CatalogData.foods, key = { it.id }) { food ->
                        val count = uiState.foodInventory[food.id] ?: 0
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onQuickFeed(food) }
                                .testTag("quick_feed_${food.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = food.emoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = food.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (count > 0) "Estoque: $count (+${food.hungerRestore} 🍔)" else "Comprar: ${food.price} 🪙",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (count > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Call to Action Banner for the 30 Mini-Games & Wardrobe
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavigateTab(MainTab.MINIGAMES) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("cta_play_30_minigames")
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Jogar 30 Games", style = MaterialTheme.typography.titleSmall)
            }

            Button(
                onClick = { onNavigateTab(MainTab.WARDROBE) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("cta_open_wardrobe")
            ) {
                Text("🎩 Acessórios", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun NeedMeterItem(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = "$value%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun CareActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
