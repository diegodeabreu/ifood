package com.diegodeabreu.ifoodprocesso.domain.usecase

import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.repository.RecipeRepository

class GetRecipeDetailsUseCase(
    private val repository: RecipeRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<RecipeDetail> {
        return repository.getRecipeDetails(id)
    }
}
