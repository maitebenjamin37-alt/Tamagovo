package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AccessoryCategory
import com.example.data.CatalogData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and catalogs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OvoPet", appName)

        // Verify 30 playable mini-games
        assertEquals(30, CatalogData.miniGames.size)

        // Verify at least 10 foods (14 defined)
        assertTrue(CatalogData.foods.size >= 10)

        // Verify accessories include hats, glasses, shirts, and shoes
        val categories = CatalogData.accessories.map { it.category }.toSet()
        assertTrue(categories.contains(AccessoryCategory.HAT))
        assertTrue(categories.contains(AccessoryCategory.GLASSES))
        assertTrue(categories.contains(AccessoryCategory.SHIRT))
        assertTrue(categories.contains(AccessoryCategory.SHOES))

        // Verify 5 creature evolution stages based on level and care points
        assertEquals(com.example.data.EvolutionStage.EGG, com.example.data.EvolutionStage.fromLevelAndCare(1, 0))
        assertEquals(com.example.data.EvolutionStage.BABY, com.example.data.EvolutionStage.fromLevelAndCare(1, 12))
        assertEquals(com.example.data.EvolutionStage.TEEN, com.example.data.EvolutionStage.fromLevelAndCare(4, 15))
        assertEquals(com.example.data.EvolutionStage.ADULT, com.example.data.EvolutionStage.fromLevelAndCare(6, 50))
        assertEquals(com.example.data.EvolutionStage.MYTHIC, com.example.data.EvolutionStage.fromLevelAndCare(10, 80))
    }
}
