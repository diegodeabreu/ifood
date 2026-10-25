package com.diegodeabreu.ifoodprocesso

import androidx.compose.material3.MaterialTheme
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.presentation.ui.RecipeItemCard
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-480dpi")
class RecipeSearchScreenSnapshotTest {

    @Test
    fun testRecipeItemCardAtomic() {
        captureRoboImage("src/test/snapshots/RecipeItemCard.png") {
            MaterialTheme {
                RecipeItemCard(
                    recipe = Recipe(
                        id = 1L,
                        title = "Bolo de Chocolate",
                        imageUrl = null,
                        summary = "Delicioso bolo.",
                        readyInMinutes = 40,
                        servings = 6
                    ),
                    isFavorite = true,
                    onToggleFavorite = {},
                    onClick = {}
                )
            }
        }
    }
}
