package com.diegodeabreu.ifoodprocesso.domain.usecase

import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeSearchResult
import com.diegodeabreu.ifoodprocesso.domain.repository.RecipeRepository

class SearchRecipesUseCase(
    private val repository: RecipeRepository,
) {
    suspend operator fun invoke(
        query: String,
        offset: Int = 0,
    ): DomainResult<RecipeSearchResult> {
        return repository.searchRecipes(query, offset)
    }
}
