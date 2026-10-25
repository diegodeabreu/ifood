package com.diegodeabreu.ifoodprocesso.domain.model

data class RecipeSearchResult(
    val recipes: List<Recipe>,
    val totalResults: Int,
)
