package com.diegodeabreu.ifoodprocesso

import androidx.compose.material3.MaterialTheme
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.model.SimilarRecipe
import com.diegodeabreu.ifoodprocesso.presentation.ui.RecipeDetailScreen
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeDetailUiState
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-480dpi")
class RecipeDetailScreenSnapshotTest {

    @Test
    fun testRecipeDetailScreenSuccess() {
        val sampleRecipe = Recipe(
            id = 1L,
            title = "Bolo de Cenoura com Cobertura de Chocolate",
            imageUrl = null,
            summary = "Um clássico bolo de cenoura fofinho com calda cremosa.",
            readyInMinutes = 50,
            servings = 10
        )

        val sampleDetail = RecipeDetail(
            id = 1L,
            title = sampleRecipe.title,
            imageUrl = null,
            summary = sampleRecipe.summary,
            readyInMinutes = sampleRecipe.readyInMinutes,
            servings = sampleRecipe.servings,
            healthScore = 75.0,
            pricePerServing = 3.2,
            instructions = "Bata no liquidificador...",
            ingredients = emptyList(),
            steps = emptyList(),
            similarRecipes = listOf(
                SimilarRecipe(2L, "Bolo de Chocolate"),
                SimilarRecipe(3L, "Pudim de Leite Condensado")
            )
        )

        captureRoboImage("src/test/snapshots/RecipeDetailScreen_Success.png") {
            MaterialTheme {
                RecipeDetailScreen(
                    recipe = sampleRecipe,
                    detailState = RecipeDetailUiState.Success(sampleDetail),
                    isFavorite = true,
                    onToggleFavorite = {},
                    onSelectSimilarRecipe = {},
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun testRecipeDetailScreenLoading() {
        val sampleRecipe = Recipe(
            id = 1L,
            title = "Bolo de Cenoura",
            imageUrl = null,
            summary = "Carregando...",
            readyInMinutes = 30,
            servings = 4
        )

        captureRoboImage("src/test/snapshots/RecipeDetailScreen_Loading.png") {
            MaterialTheme {
                RecipeDetailScreen(
                    recipe = sampleRecipe,
                    detailState = RecipeDetailUiState.Loading,
                    isFavorite = false,
                    onToggleFavorite = {},
                    onSelectSimilarRecipe = {},
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun testRecipeDetailScreenError() {
        val sampleRecipe = Recipe(
            id = 1L,
            title = "Bolo de Cenoura",
            imageUrl = null,
            summary = "Erro.",
            readyInMinutes = 30,
            servings = 4
        )

        captureRoboImage("src/test/snapshots/RecipeDetailScreen_Error.png") {
            MaterialTheme {
                RecipeDetailScreen(
                    recipe = sampleRecipe,
                    detailState = RecipeDetailUiState.Error("Sem conexão com a internet"),
                    isFavorite = false,
                    onToggleFavorite = {},
                    onSelectSimilarRecipe = {},
                    onBack = {}
                )
            }
        }
    }
}
