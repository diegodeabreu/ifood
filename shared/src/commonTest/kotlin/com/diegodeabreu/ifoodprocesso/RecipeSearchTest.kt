package com.diegodeabreu.ifoodprocesso

import com.diegodeabreu.ifoodprocesso.domain.error.AppError
import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeSearchResult
import com.diegodeabreu.ifoodprocesso.domain.repository.RecipeRepository
import com.diegodeabreu.ifoodprocesso.domain.usecase.GetRecipeDetailsUseCase
import com.diegodeabreu.ifoodprocesso.domain.usecase.SearchRecipesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeSearchTest {
    private class FakeRecipeRepository : RecipeRepository {
        val mockRecipes =
            listOf(
                Recipe(1L, "Chicken Soup", "url", "Summary", 30, 4),
                Recipe(2L, "Pasta Carbonara", "url", "Summary", 20, 2),
            )

        override suspend fun searchRecipes(
            query: String,
            offset: Int,
        ): DomainResult<RecipeSearchResult> {
            val filtered = mockRecipes.filter { it.title.contains(query, ignoreCase = true) }
            return DomainResult.Success(RecipeSearchResult(filtered, filtered.size))
        }

        override suspend fun getRecipeDetails(id: Long): DomainResult<RecipeDetail> {
            val found = mockRecipes.find { it.id == id }
            return if (found != null) {
                DomainResult.Success(
                    RecipeDetail(
                        id = found.id,
                        title = found.title,
                        imageUrl = found.imageUrl,
                        summary = found.summary,
                        readyInMinutes = found.readyInMinutes,
                        servings = found.servings,
                        healthScore = 80.0,
                        pricePerServing = 2.5,
                        instructions = "Cook well",
                        ingredients = emptyList(),
                        steps = emptyList(),
                    ),
                )
            } else {
                DomainResult.Error(AppError.Unknown("Not found"))
            }
        }
    }

    @Test
    fun testSearchRecipesUseCase() =
        runTest {
            val repository = FakeRecipeRepository()
            val searchUseCase = SearchRecipesUseCase(repository)

            val result = searchUseCase("Chicken")
            assertTrue(result is DomainResult.Success)
            val searchResult = (result as DomainResult.Success).data
            assertEquals(1, searchResult.recipes.size)
            assertEquals("Chicken Soup", searchResult.recipes.first().title)
        }

    @Test
    fun testGetRecipeDetailsUseCase() =
        runTest {
            val repository = FakeRecipeRepository()
            val detailsUseCase = GetRecipeDetailsUseCase(repository)

            val result = detailsUseCase(2L)
            assertTrue(result is DomainResult.Success)
            val detail = (result as DomainResult.Success).data
            assertEquals("Pasta Carbonara", detail.title)
            assertEquals(20, detail.readyInMinutes)
        }
}
