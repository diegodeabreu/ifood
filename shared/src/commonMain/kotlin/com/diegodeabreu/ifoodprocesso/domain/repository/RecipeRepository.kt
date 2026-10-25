package com.diegodeabreu.ifoodprocesso.domain.repository

import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeSearchResult

interface RecipeRepository {
    suspend fun searchRecipes(
        query: String,
        offset: Int = 0,
    ): DomainResult<RecipeSearchResult>

    suspend fun getRecipeDetails(id: Long): DomainResult<RecipeDetail>
}
