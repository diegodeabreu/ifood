package com.diegodeabreu.ifoodprocesso.domain.model

data class RecipeDetail(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val summary: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
    val healthScore: Double?,
    val pricePerServing: Double?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val similarRecipes: List<SimilarRecipe> = emptyList(),
)

data class Ingredient(
    val name: String,
    val original: String?,
)

data class RecipeStep(
    val number: Int,
    val step: String,
)

data class SimilarRecipe(
    val id: Long,
    val title: String,
)
