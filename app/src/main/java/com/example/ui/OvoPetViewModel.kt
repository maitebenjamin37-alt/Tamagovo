package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccessoryCatalogItem
import com.example.data.BuffType
import com.example.data.EvolutionStage
import com.example.data.FoodCatalogItem
import com.example.data.FoodInventoryEntity
import com.example.data.MiniGameCatalogItem
import com.example.data.MiniGameScoreEntity
import com.example.data.OvoPetDatabase
import com.example.data.OvoPetRepository
import com.example.data.PetStateEntity
import com.example.data.UnlockedAccessoryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    HOME("Casa"),
    KITCHEN("Cozinha"),
    WARDROBE("Armário"),
    MINIGAMES("30 Jogos")
}

data class OvoPetUiState(
    val pet: PetStateEntity = PetStateEntity(),
    val foodInventory: Map<String, Int> = emptyMap(),
    val unlockedAccessoryIds: Set<String> = setOf("hat_cap", "color_classic"),
    val miniGameScores: Map<Int, MiniGameScoreEntity> = emptyMap(),
    val currentTab: MainTab = MainTab.HOME,
    val activeMiniGame: MiniGameCatalogItem? = null,
    val isEating: Boolean = false,
    val isBathing: Boolean = false,
    val statusBannerMessage: String? = null,
    val remainingBuffSeconds: Int = 0,
    val newlyEvolvedStage: EvolutionStage? = null
)

class OvoPetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OvoPetRepository = OvoPetRepository(
        OvoPetDatabase.getInstance(application).ovoPetDao()
    )

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    private val _activeMiniGame = MutableStateFlow<MiniGameCatalogItem?>(null)
    private val _isEating = MutableStateFlow(false)
    private val _isBathing = MutableStateFlow(false)
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val _newlyEvolvedStage = MutableStateFlow<EvolutionStage?>(null)

    val uiState: StateFlow<OvoPetUiState> = combine(
        repository.petStateFlow,
        repository.foodInventoryFlow,
        repository.unlockedAccessoriesFlow,
        repository.miniGameScoresFlow,
        combine(_currentTab, _activeMiniGame, _isEating, _isBathing, _statusBanner) { tab, game, eating, bathing, banner ->
            Tuple5(tab, game, eating, bathing, banner)
        },
        _nowMillis,
        _newlyEvolvedStage
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val pet = (flows[0] as? PetStateEntity) ?: PetStateEntity()
        @Suppress("UNCHECKED_CAST")
        val foods = (flows[1] as? List<FoodInventoryEntity>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val unlocked = (flows[2] as? List<UnlockedAccessoryEntity>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val scores = (flows[3] as? List<MiniGameScoreEntity>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val transient = flows[4] as Tuple5<MainTab, MiniGameCatalogItem?, Boolean, Boolean, String?>
        val now = flows[5] as Long
        val evolved = flows[6] as? EvolutionStage

        val remSec = ((pet.activeBuffExpiresAt - now) / 1000L).toInt().coerceAtLeast(0)

        OvoPetUiState(
            pet = pet,
            foodInventory = foods.associate { it.foodId to it.quantity },
            unlockedAccessoryIds = unlocked.map { it.accessoryId }.toSet() + setOf("hat_cap", "color_classic"),
            miniGameScores = scores.associateBy { it.gameId },
            currentTab = transient.a,
            activeMiniGame = transient.b,
            isEating = transient.c,
            isBathing = transient.d,
            statusBannerMessage = transient.e,
            remainingBuffSeconds = remSec,
            newlyEvolvedStage = evolved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OvoPetUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        viewModelScope.launch {
            var tickCounter = 0
            while (true) {
                delay(1000L)
                _nowMillis.value = System.currentTimeMillis()
                tickCounter++
                if (tickCounter % 25 == 0) {
                    repository.updatePetState { pet ->
                        val hasSuperHealth = pet.activeBuffExpiresAt > System.currentTimeMillis() &&
                            pet.activeBuffType == BuffType.SUPER_HEALTH.code
                        if (pet.isSleeping) {
                            pet.copy(
                                energy = (pet.energy + 6).coerceIn(0, 100),
                                hunger = (pet.hunger - 1).coerceIn(0, 100),
                                health = if (hasSuperHealth) (pet.health + 3).coerceIn(0, 100) else pet.health
                            )
                        } else {
                            pet.copy(
                                hunger = (pet.hunger - 1).coerceIn(0, 100),
                                cleanliness = (pet.cleanliness - 1).coerceIn(0, 100),
                                health = if (hasSuperHealth) (pet.health + 2).coerceIn(0, 100) else pet.health
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _activeMiniGame.value = null
        _currentTab.value = tab
    }

    fun openMiniGame(game: MiniGameCatalogItem) {
        _activeMiniGame.value = game
    }

    fun closeMiniGame() {
        _activeMiniGame.value = null
    }

    fun dismissEvolutionDialog() {
        _newlyEvolvedStage.value = null
    }

    fun selectEvolutionForm(stage: EvolutionStage) {
        viewModelScope.launch {
            repository.selectEvolutionStage(stage)
            showTransientBanner("✨ Forma alterada para ${stage.badgeEmoji} ${stage.title}!")
        }
    }

    fun showTransientBanner(message: String) {
        viewModelScope.launch {
            _statusBanner.value = message
            delay(2600)
            if (_statusBanner.value == message) {
                _statusBanner.value = null
            }
        }
    }

    fun petPou() {
        viewModelScope.launch {
            val evolved = repository.performCareInteraction(
                happinessDelta = 8,
                xpGain = 12,
                carePointsGain = 2,
                coinReward = 4
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            }
            showTransientBanner("💕 ${uiState.value.pet.name} adorou o carinho! (+2 Cuidado, +12 XP, +4 🪙)")
        }
    }

    fun giveBath() {
        viewModelScope.launch {
            _isBathing.value = true
            val evolved = repository.performCareInteraction(
                happinessDelta = 10,
                cleanlinessTarget = 100,
                healthDelta = 6,
                xpGain = 20,
                carePointsGain = 4,
                coinReward = 8
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            }
            showTransientBanner("🫧 Banho tomado! Higiene 100% (+4 Cuidado, +8 🪙)")
            delay(1600)
            _isBathing.value = false
        }
    }

    fun toggleSleep() {
        viewModelScope.launch {
            val nowSleeping = !uiState.value.pet.isSleeping
            repository.updatePetState { pet ->
                pet.copy(isSleeping = nowSleeping)
            }
            val evolved = repository.performCareInteraction(
                energyDelta = if (nowSleeping) 20 else 5,
                xpGain = 10,
                carePointsGain = 2,
                coinReward = 2
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            }
            if (nowSleeping) {
                showTransientBanner("😴 ${uiState.value.pet.name} está dormindo e recuperando energia (+2 Cuidado)...")
            } else {
                showTransientBanner("☀️ Bom dia! ${uiState.value.pet.name} acordou cheio de disposição!")
            }
        }
    }

    fun giveHealthPotion() {
        viewModelScope.launch {
            val pet = uiState.value.pet
            val cost = if (pet.coins >= 15 && pet.health < 95) 15 else 0
            val evolved = repository.performCareInteraction(
                healthDelta = 100,
                energyDelta = 20,
                xpGain = 18,
                carePointsGain = 3,
                coinReward = -cost
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            }
            showTransientBanner("🧪 Elixir de Saúde usado! Saúde 100%, +20 Energia e +3 Cuidado!")
        }
    }

    fun trainEvolutionBond() {
        viewModelScope.launch {
            val evolved = repository.performCareInteraction(
                happinessDelta = 12,
                energyDelta = -4,
                xpGain = 35,
                carePointsGain = 6,
                coinReward = 10
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            } else {
                showTransientBanner("🌟 Treino de Evolução concluído! (+6 Cuidado, +35 XP, +10 🪙)")
            }
        }
    }

    fun renamePet(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updatePetState { it.copy(name = trimmed.take(18)) }
            showTransientBanner("✨ Nome atualizado para ${trimmed.take(18)}!")
        }
    }

    fun buyFood(food: FoodCatalogItem) {
        viewModelScope.launch {
            val ok = repository.buyFood(food)
            if (ok) {
                showTransientBanner("🛒 Comprou 1x ${food.emoji} ${food.name} para o inventário!")
            } else {
                showTransientBanner("⚠️ Moedas insuficientes! Jogue os 30 minigames para ganhar mais 🪙.")
            }
        }
    }

    fun feedPou(food: FoodCatalogItem) {
        viewModelScope.launch {
            val (ok, evolved) = repository.consumeFood(food)
            if (ok) {
                _isEating.value = true
                if (evolved != null) {
                    _newlyEvolvedStage.value = evolved
                }
                val buffText = if (food.buffType != BuffType.NONE) " • Bônus: ${food.buffType.badgeText}" else ""
                showTransientBanner("😋 Comeu ${food.emoji} ${food.name} (+${food.hungerRestore} Fome, +4 Cuidado$buffText)")
                delay(1200)
                _isEating.value = false
            } else {
                showTransientBanner("🍽️ Você não tem ${food.name} no estoque! Compre na aba Cozinha.")
            }
        }
    }

    fun buyAndFeedFoodDirectly(food: FoodCatalogItem) {
        viewModelScope.launch {
            val count = uiState.value.foodInventory[food.id] ?: 0
            if (count > 0) {
                feedPou(food)
            } else {
                val bought = repository.buyFood(food)
                if (bought) {
                    feedPou(food)
                } else {
                    showTransientBanner("⚠️ Moedas insuficientes para comprar ${food.name}!")
                }
            }
        }
    }

    fun buyOrEquipAccessory(item: AccessoryCatalogItem) {
        viewModelScope.launch {
            val wasUnlocked = uiState.value.unlockedAccessoryIds.contains(item.id)
            val ok = repository.buyAndEquipAccessory(item)
            if (ok) {
                if (wasUnlocked) {
                    showTransientBanner("✨ Visual atualizado com ${item.emoji} ${item.name}!")
                } else {
                    showTransientBanner("🎉 Comprou e equipou ${item.emoji} ${item.name} (+3 Cuidado)!")
                }
            } else {
                showTransientBanner("⚠️ Você precisa de ${item.price} 🪙 para comprar ${item.name}!")
            }
        }
    }

    fun finishMiniGame(game: MiniGameCatalogItem, rawScore: Int, onRewardCalculated: (Int) -> Unit) {
        viewModelScope.launch {
            val (coinsEarned, evolved) = repository.recordMiniGameResult(
                gameId = game.id,
                rawScore = rawScore,
                baseCoins = game.baseCoinReward
            )
            if (evolved != null) {
                _newlyEvolvedStage.value = evolved
            }
            onRewardCalculated(coinsEarned)
            showTransientBanner("🏆 Ganhou +$coinsEarned 🪙 e +5 Cuidado jogando ${game.title}!")
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
