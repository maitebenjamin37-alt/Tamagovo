package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Dao
interface OvoPetDao {
    @Query("SELECT * FROM pet_state WHERE id = 1")
    fun observePetState(): Flow<PetStateEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1")
    suspend fun getPetStateOnce(): PetStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePetState(state: PetStateEntity)

    @Query("SELECT * FROM food_inventory")
    fun observeFoodInventory(): Flow<List<FoodInventoryEntity>>

    @Query("SELECT * FROM food_inventory WHERE foodId = :foodId")
    suspend fun getFoodItemOnce(foodId: String): FoodInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFoodItem(item: FoodInventoryEntity)

    @Query("SELECT * FROM unlocked_accessories")
    fun observeUnlockedAccessories(): Flow<List<UnlockedAccessoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockAccessory(entity: UnlockedAccessoryEntity)

    @Query("SELECT * FROM minigame_scores")
    fun observeMiniGameScores(): Flow<List<MiniGameScoreEntity>>

    @Query("SELECT * FROM minigame_scores WHERE gameId = :gameId")
    suspend fun getMiniGameScoreOnce(gameId: Int): MiniGameScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMiniGameScore(score: MiniGameScoreEntity)
}

@Database(
    entities = [
        PetStateEntity::class,
        FoodInventoryEntity::class,
        UnlockedAccessoryEntity::class,
        MiniGameScoreEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OvoPetDatabase : RoomDatabase() {
    abstract fun ovoPetDao(): OvoPetDao

    companion object {
        @Volatile
        private var INSTANCE: OvoPetDatabase? = null

        fun getInstance(context: Context): OvoPetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OvoPetDatabase::class.java,
                    "ovopet_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class OvoPetRepository(private val dao: OvoPetDao) {
    val petStateFlow: Flow<PetStateEntity?> = dao.observePetState()
    val foodInventoryFlow: Flow<List<FoodInventoryEntity>> = dao.observeFoodInventory()
    val unlockedAccessoriesFlow: Flow<List<UnlockedAccessoryEntity>> = dao.observeUnlockedAccessories()
    val miniGameScoresFlow: Flow<List<MiniGameScoreEntity>> = dao.observeMiniGameScores()

    suspend fun ensureInitialized() {
        val current = dao.getPetStateOnce()
        if (current == null) {
            dao.savePetState(PetStateEntity())
            dao.unlockAccessory(UnlockedAccessoryEntity("hat_cap"))
            dao.unlockAccessory(UnlockedAccessoryEntity("color_classic"))
            dao.saveFoodItem(FoodInventoryEntity("food_apple", 4))
            dao.saveFoodItem(FoodInventoryEntity("food_pizza", 2))
            dao.saveFoodItem(FoodInventoryEntity("food_icecream", 2))
            dao.saveFoodItem(FoodInventoryEntity("food_salad", 2))
            dao.saveFoodItem(FoodInventoryEntity("food_banana", 3))
        }
    }

    private fun applyXpAndEvolution(
        pet: PetStateEntity,
        addedXp: Int,
        addedCarePoints: Int
    ): Pair<PetStateEntity, EvolutionStage?> {
        val stageBonusMult = 1f + (pet.activeEvolutionStage.xpBonusPercent / 100f)
        val totalXpGain = (addedXp * stageBonusMult).toInt()
        var newXp = pet.xp + totalXpGain
        var newLevel = pet.level
        var bonusCoins = 0

        while (newXp >= newLevel * 80) {
            newXp -= newLevel * 80
            newLevel++
            bonusCoins += 40
        }

        val newCarePoints = pet.carePoints + addedCarePoints
        val computedStage = EvolutionStage.fromLevelAndCare(newLevel, newCarePoints)
        val evolvedNewStage = if (computedStage.ordinal > pet.highestEvolutionOrdinal) {
            bonusCoins += 100
            computedStage
        } else {
            null
        }
        val finalHighestOrdinal = maxOf(pet.highestEvolutionOrdinal, computedStage.ordinal)
        val finalSelectedOrdinal = if (evolvedNewStage != null) {
            evolvedNewStage.ordinal
        } else {
            pet.selectedEvolutionOrdinal
        }

        val updated = pet.copy(
            level = newLevel,
            xp = newXp,
            carePoints = newCarePoints,
            highestEvolutionOrdinal = finalHighestOrdinal,
            selectedEvolutionOrdinal = finalSelectedOrdinal,
            coins = pet.coins + bonusCoins,
            lastUpdated = System.currentTimeMillis()
        )
        return updated to evolvedNewStage
    }

    suspend fun updatePetState(transform: (PetStateEntity) -> PetStateEntity) {
        val current = dao.getPetStateOnce() ?: PetStateEntity()
        val updated = transform(current).copy(lastUpdated = System.currentTimeMillis())
        dao.savePetState(updated)
    }

    suspend fun performCareInteraction(
        happinessDelta: Int = 0,
        cleanlinessTarget: Int? = null,
        healthDelta: Int = 0,
        energyDelta: Int = 0,
        xpGain: Int = 15,
        carePointsGain: Int = 3,
        coinReward: Int = 5
    ): EvolutionStage? {
        val current = dao.getPetStateOnce() ?: PetStateEntity()
        val baseUpdated = current.copy(
            happiness = (current.happiness + happinessDelta).coerceIn(0, 100),
            cleanliness = cleanlinessTarget ?: current.cleanliness,
            health = (current.health + healthDelta).coerceIn(0, 100),
            energy = (current.energy + energyDelta).coerceIn(0, 100),
            coins = current.coins + coinReward
        )
        val (finalState, evolvedStage) = applyXpAndEvolution(baseUpdated, xpGain, carePointsGain)
        dao.savePetState(finalState)
        return evolvedStage
    }

    suspend fun selectEvolutionStage(stage: EvolutionStage) {
        val current = dao.getPetStateOnce() ?: PetStateEntity()
        if (stage.ordinal <= current.highestEvolutionOrdinal) {
            dao.savePetState(current.copy(selectedEvolutionOrdinal = stage.ordinal))
        }
    }

    suspend fun buyFood(food: FoodCatalogItem): Boolean {
        val pet = dao.getPetStateOnce() ?: PetStateEntity()
        if (pet.coins < food.price) return false
        dao.savePetState(pet.copy(coins = pet.coins - food.price))
        val existing = dao.getFoodItemOnce(food.id)?.quantity ?: 0
        dao.saveFoodItem(FoodInventoryEntity(food.id, existing + 1))
        return true
    }

    suspend fun consumeFood(food: FoodCatalogItem): Pair<Boolean, EvolutionStage?> {
        val existing = dao.getFoodItemOnce(food.id)?.quantity ?: 0
        if (existing <= 0) return false to null
        dao.saveFoodItem(FoodInventoryEntity(food.id, existing - 1))

        val pet = dao.getPetStateOnce() ?: PetStateEntity()
        val now = System.currentTimeMillis()

        val fedPet = pet.copy(
            hunger = (pet.hunger + food.hungerRestore).coerceIn(0, 100),
            energy = (pet.energy + food.energyRestore).coerceIn(0, 100),
            happiness = (pet.happiness + food.happinessRestore).coerceIn(0, 100),
            health = (pet.health + food.healthRestore).coerceIn(0, 100),
            activeBuffType = if (food.buffType != BuffType.NONE) food.buffType.code else pet.activeBuffType,
            activeBuffLabel = if (food.buffType != BuffType.NONE) "${food.emoji} ${food.buffType.badgeText}" else pet.activeBuffLabel,
            activeBuffExpiresAt = if (food.buffType != BuffType.NONE) now + food.buffDurationSec * 1000L else pet.activeBuffExpiresAt
        )
        val (finalPet, evolved) = applyXpAndEvolution(fedPet, addedXp = 22, addedCarePoints = 4)
        dao.savePetState(finalPet)
        return true to evolved
    }

    suspend fun buyAndEquipAccessory(item: AccessoryCatalogItem): Boolean {
        val pet = dao.getPetStateOnce() ?: PetStateEntity()
        val unlockedList = dao.observeUnlockedAccessories().firstOrNull().orEmpty()
        val isUnlocked = unlockedList.any { it.accessoryId == item.id } || item.price == 0

        var updatedCoins = pet.coins
        if (!isUnlocked) {
            if (pet.coins < item.price) return false
            updatedCoins -= item.price
            dao.unlockAccessory(UnlockedAccessoryEntity(item.id))
        }

        val updatedPet = when (item.category) {
            AccessoryCategory.HAT -> pet.copy(
                coins = updatedCoins,
                equippedHatId = if (pet.equippedHatId == item.id && isUnlocked) null else item.id
            )
            AccessoryCategory.GLASSES -> pet.copy(
                coins = updatedCoins,
                equippedGlassesId = if (pet.equippedGlassesId == item.id && isUnlocked) null else item.id
            )
            AccessoryCategory.SHIRT -> pet.copy(
                coins = updatedCoins,
                equippedShirtId = if (pet.equippedShirtId == item.id && isUnlocked) null else item.id
            )
            AccessoryCategory.SHOES -> pet.copy(
                coins = updatedCoins,
                equippedShoesId = if (pet.equippedShoesId == item.id && isUnlocked) null else item.id
            )
            AccessoryCategory.BODY_COLOR -> pet.copy(
                coins = updatedCoins,
                bodyColorHex = item.primaryColorHex
            )
        }
        val (finalPet, _) = applyXpAndEvolution(
            updatedPet,
            addedXp = if (!isUnlocked) 15 else 2,
            addedCarePoints = if (!isUnlocked) 3 else 1
        )
        dao.savePetState(finalPet)
        return true
    }

    suspend fun recordMiniGameResult(gameId: Int, rawScore: Int, baseCoins: Int): Pair<Int, EvolutionStage?> {
        val pet = dao.getPetStateOnce() ?: PetStateEntity()
        val now = System.currentTimeMillis()
        val hasActiveBuff = pet.activeBuffExpiresAt > now && pet.activeBuffType != null
        val buff = BuffType.entries.find { it.code == pet.activeBuffType } ?: BuffType.NONE
        val foodCoinMult = if (hasActiveBuff) buff.coinMultiplier else 1.0f
        val foodXpMult = if (hasActiveBuff) buff.xpMultiplier else 1.0f
        val stageCoinMult = 1.0f + (pet.activeEvolutionStage.coinBonusPercent / 100f)

        val finalCoins = ((baseCoins + (rawScore / 4)).coerceAtLeast(10) * foodCoinMult * stageCoinMult).toInt()
        val gainedXp = ((28 + (rawScore / 4)) * foodXpMult).toInt()
        val energyCost = if (hasActiveBuff && buff == BuffType.ENERGY_SHIELD) 2 else 5

        val postGamePet = pet.copy(
            coins = pet.coins + finalCoins,
            happiness = (pet.happiness + 15).coerceIn(0, 100),
            energy = (pet.energy - energyCost).coerceIn(0, 100),
            hunger = (pet.hunger - 4).coerceIn(0, 100),
            totalMiniGamesPlayed = pet.totalMiniGamesPlayed + 1
        )

        val (finalPet, evolvedStage) = applyXpAndEvolution(
            postGamePet,
            addedXp = gainedXp,
            addedCarePoints = 5
        )
        dao.savePetState(finalPet)

        val prevScore = dao.getMiniGameScoreOnce(gameId)
        val updatedScore = MiniGameScoreEntity(
            gameId = gameId,
            highScore = maxOf(prevScore?.highScore ?: 0, rawScore),
            timesPlayed = (prevScore?.timesPlayed ?: 0) + 1,
            totalCoinsEarned = (prevScore?.totalCoinsEarned ?: 0) + finalCoins
        )
        dao.saveMiniGameScore(updatedScore)
        val totalAwarded = finalCoins + (if (evolvedStage != null) 100 else 0)
        return totalAwarded to evolvedStage
    }
}
