package com.diegodeabreu.ifoodprocesso.data.repository

import com.diegodeabreu.ifoodprocesso.data.remote.SpoonacularApiClient
import com.diegodeabreu.ifoodprocesso.domain.error.AppError
import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.Ingredient
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeSearchResult
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeStep
import com.diegodeabreu.ifoodprocesso.domain.model.SimilarRecipe
import com.diegodeabreu.ifoodprocesso.domain.repository.RecipeRepository
import com.diegodeabreu.ifoodprocesso.error.ErrorNotifier

class RecipeRepositoryImpl(
    private val apiClient: SpoonacularApiClient,
) : RecipeRepository {
    // In-memory cache for offline fallback
    private val searchCache = mutableMapOf<String, RecipeSearchResult>()
    private val detailCache = mutableMapOf<Long, RecipeDetail>()

    override suspend fun searchRecipes(
        query: String,
        offset: Int,
    ): DomainResult<RecipeSearchResult> {
        val trimmedQuery = query.trim().lowercase()
        return try {
            val response = apiClient.searchRecipes(query, number = 10, offset = offset)
            val recipes =
                response.results.map { dto ->
                    Recipe(
                        id = dto.id,
                        title = dto.title,
                        imageUrl = dto.image,
                        summary = dto.summary,
                        readyInMinutes = dto.readyInMinutes,
                        servings = dto.servings,
                    )
                }
            val searchResult =
                RecipeSearchResult(
                    recipes = recipes,
                    totalResults = response.totalResults,
                )

            // Cache search result for offline use
            if (offset == 0) {
                searchCache[trimmedQuery] = searchResult
            } else {
                val existing = searchCache[trimmedQuery]
                if (existing != null) {
                    searchCache[trimmedQuery] =
                        existing.copy(
                            recipes = existing.recipes + recipes,
                            totalResults = searchResult.totalResults,
                        )
                } else {
                    searchCache[trimmedQuery] = searchResult
                }
            }

            DomainResult.Success(searchResult)
        } catch (e: Exception) {
            val cached = searchCache[trimmedQuery]
            if (cached != null) {
                DomainResult.Success(cached)
            } else {
                ErrorNotifier.notifyError(e.message ?: "Network error occurred")
                DomainResult.Error(AppError.Network(e.message))
            }
        }
    }

    override suspend fun getRecipeDetails(id: Long): DomainResult<RecipeDetail> {
        return try {
            val dto = apiClient.getRecipeInformation(id)
            val similarDtos =
                try {
                    apiClient.getSimilarRecipes(id, 3)
                } catch (e: Exception) {
                    emptyList()
                }
            val similarRecipes =
                similarDtos.map { sim ->
                    SimilarRecipe(
                        id = sim.id,
                        title = sim.title,
                    )
                }

            val ingredients =
                dto.extendedIngredients.map { ing ->
                    Ingredient(
                        name = ing.name,
                        original = ing.original,
                    )
                }
            val steps =
                dto.analyzedInstructions.flatMap { instruction ->
                    instruction.steps.map { step ->
                        RecipeStep(
                            number = step.number,
                            step = step.step,
                        )
                    }
                }

            val detail =
                RecipeDetail(
                    id = dto.id,
                    title = dto.title,
                    imageUrl = dto.image,
                    summary = dto.summary,
                    readyInMinutes = dto.readyInMinutes,
                    servings = dto.servings,
                    healthScore = dto.healthScore,
                    pricePerServing = dto.pricePerServing,
                    instructions = dto.instructions,
                    ingredients = ingredients,
                    steps = steps,
                    similarRecipes = similarRecipes,
                )
            detailCache[id] = detail
            DomainResult.Success(detail)
        } catch (e: Exception) {
            val cached = detailCache[id]
            if (cached != null) {
                DomainResult.Success(cached)
            } else {
                ErrorNotifier.notifyError(e.message ?: "Network error occurred")
                DomainResult.Error(AppError.Network(e.message))
            }
        }
    }
}
