package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EvolutionStage(
    val stageNumber: Int,
    val title: String,
    val badgeEmoji: String,
    val minLevel: Int,
    val minCarePoints: Int,
    val coinBonusPercent: Int,
    val xpBonusPercent: Int,
    val visualTraitSummary: String,
    val description: String
) {
    EGG(
        stageNumber = 1,
        title = "Ovinho Curioso",
        badgeEmoji = "🥚",
        minLevel = 1,
        minCarePoints = 0,
        coinBonusPercent = 0,
        xpBonusPercent = 0,
        visualTraitSummary = "Casca protetora com rachaduras de eclosão",
        description = "Seu bichinho ainda está dentro do ovinho esperando carinho, calor e alimento para chocar!"
    ),
    BABY(
        stageNumber = 2,
        title = "Bebê Brotinho",
        badgeEmoji = "🐣",
        minLevel = 2,
        minCarePoints = 10,
        coinBonusPercent = 10,
        xpBonusPercent = 10,
        visualTraitSummary = "Bracinhos fofos, brotinho na cabeça e casca quebrada",
        description = "O ovo chocou! Agora ele tem bracinhos curiosos e um brotinho cheio de energia."
    ),
    TEEN(
        stageNumber = 3,
        title = "Jovem Aventureiro",
        badgeEmoji = "🐾",
        minLevel = 4,
        minCarePoints = 25,
        coinBonusPercent = 20,
        xpBonusPercent = 15,
        visualTraitSummary = "Orelhinhas ágeis, mãos expressivas e porte atlético",
        description = "Cheio de personalidade! Adora correr nos 30 mini-games e experimentar acessórios."
    ),
    ADULT(
        stageNumber = 4,
        title = "Criatura Guardiã",
        badgeEmoji = "🦋",
        minLevel = 6,
        minCarePoints = 45,
        coinBonusPercent = 35,
        xpBonusPercent = 25,
        visualTraitSummary = "Asas majestosas, chifrinhos protetores e aura brilhante",
        description = "Uma forma madura e poderosa! Suas asas batem suavemente enquanto protege o quarto."
    ),
    MYTHIC(
        stageNumber = 5,
        title = "Dragão-Ovo Celestial",
        badgeEmoji = "🐲",
        minLevel = 9,
        minCarePoints = 75,
        coinBonusPercent = 50,
        xpBonusPercent = 50,
        visualTraitSummary = "Asas douradas, cristal místico na testa e estrelas orbitais",
        description = "O ápice da evolução! Uma criatura lendária nascida do cuidado máximo e dedicação."
    );

    companion object {
        fun fromLevelAndCare(level: Int, carePoints: Int): EvolutionStage {
            return entries.lastOrNull {
                level >= it.minLevel || carePoints >= it.minCarePoints
            } ?: EGG
        }

        fun fromOrdinal(ordinal: Int): EvolutionStage {
            return entries.getOrElse(ordinal) { EGG }
        }
    }
}

@Entity(tableName = "pet_state")
data class PetStateEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Pouzinho",
    val coins: Int = 400,
    val level: Int = 1,
    val xp: Int = 0,
    val carePoints: Int = 0,
    val highestEvolutionOrdinal: Int = 0,
    val selectedEvolutionOrdinal: Int? = null,
    val hunger: Int = 75,
    val energy: Int = 85,
    val cleanliness: Int = 80,
    val happiness: Int = 82,
    val health: Int = 90,
    val isSleeping: Boolean = false,
    val equippedHatId: String? = "hat_cap",
    val equippedGlassesId: String? = null,
    val equippedShirtId: String? = null,
    val equippedShoesId: String? = null,
    val bodyColorHex: Long = 0xFFD9A76AL,
    val activeBuffType: String? = null,
    val activeBuffLabel: String? = null,
    val activeBuffExpiresAt: Long = 0L,
    val totalMiniGamesPlayed: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val activeEvolutionStage: EvolutionStage
        get() {
            val chosen = selectedEvolutionOrdinal
            if (chosen != null && chosen <= highestEvolutionOrdinal) {
                return EvolutionStage.fromOrdinal(chosen)
            }
            return EvolutionStage.fromOrdinal(highestEvolutionOrdinal)
        }
}

@Entity(tableName = "food_inventory")
data class FoodInventoryEntity(
    @PrimaryKey val foodId: String,
    val quantity: Int
)

@Entity(tableName = "unlocked_accessories")
data class UnlockedAccessoryEntity(
    @PrimaryKey val accessoryId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "minigame_scores")
data class MiniGameScoreEntity(
    @PrimaryKey val gameId: Int,
    val highScore: Int = 0,
    val timesPlayed: Int = 0,
    val totalCoinsEarned: Int = 0
)

enum class AccessoryCategory(val label: String, val iconEmoji: String) {
    HAT("Chapéus", "🎩"),
    GLASSES("Óculos", "🕶️"),
    SHIRT("Camisas", "👕"),
    SHOES("Sapatos", "👟"),
    BODY_COLOR("Cores", "🎨")
}

enum class FoodCategory(val label: String, val iconEmoji: String) {
    FRUTAS("Frutas", "🍎"),
    LANCHES("Lanches", "🍕"),
    DOCES("Doces", "🍦"),
    SAUDAVEIS("Saudáveis", "🥗"),
    ESPECIAIS("Poções & Especiais", "🌶️")
}

enum class BuffType(val code: String, val badgeText: String, val coinMultiplier: Float, val xpMultiplier: Float) {
    NONE("NONE", "", 1.0f, 1.0f),
    COIN_BOOST_25("COIN_25", "+25% Moedas nos Jogos", 1.25f, 1.0f),
    COIN_BOOST_50("COIN_50", "+50% Moedas nos Jogos", 1.50f, 1.1f),
    COIN_DOUBLE("COIN_2X", "2x Moedas nos Jogos!", 2.0f, 1.25f),
    XP_BOOST("XP_BOOST", "+50% XP Rápido", 1.15f, 1.50f),
    ENERGY_SHIELD("ENERGY_SHIELD", "Energia Protegida", 1.15f, 1.15f),
    SUPER_HEALTH("SUPER_HEALTH", "Imunidade & Vitalidade", 1.20f, 1.20f)
}

data class FoodCatalogItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: FoodCategory,
    val price: Int,
    val hungerRestore: Int,
    val energyRestore: Int = 0,
    val happinessRestore: Int = 5,
    val healthRestore: Int = 0,
    val buffType: BuffType = BuffType.NONE,
    val buffDurationSec: Int = 180,
    val description: String,
    val accentColorHex: Long
)

data class AccessoryCatalogItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: AccessoryCategory,
    val price: Int,
    val styleCode: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long = 0xFFFFFFFFL,
    val description: String
)

enum class MiniGameCategory(val label: String, val emoji: String) {
    ALL("Todos (30)", "🎮"),
    CORRIDA("Corrida & Ação", "🏎️"),
    COMIDA("Comida & Coleta", "🍔"),
    QUEBRA_CABECA("Quebra-Cabeça", "🧩"),
    RITMO("Ritmo & Música", "🎵"),
    ESPORTES("Esportes & Arcade", "⚽"),
    MENTE("Mente & Reflexo", "🧠")
}

enum class MiniGameEngineType {
    LANE_DODGE,
    RUNNER_JUMP,
    FOOD_CATCH,
    BURGER_STACK,
    FRUIT_TAP,
    SLIDING_PUZZLE,
    MATCH_THREE,
    MEMORY_PAIRS,
    GAME_2048,
    COLOR_FLOOD,
    MINI_SUDOKU,
    MAZE_ESCAPE,
    RHYTHM_LANES,
    SIMON_BEATS,
    GOAL_SHOOTER,
    WHACK_MOLE,
    PONG_BREAKER,
    SHELL_CUPS,
    MATH_RUSH
}

data class MiniGameCatalogItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val instructions: String,
    val category: MiniGameCategory,
    val engineType: MiniGameEngineType,
    val iconEmoji: String,
    val playerEmoji: String = "🥚",
    val targetEmoji: String = "🪙",
    val hazardEmoji: String = "💣",
    val speedFactor: Float = 1.0f,
    val baseCoinReward: Int = 20,
    val accentColorHex: Long,
    val difficulty: String = "Normal"
)
