package com.diegodeabreu.ifoodprocesso.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeSearchResponseDto(
    val results: List<RecipeDto> = emptyList(),
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0,
)

@Serializable
data class RecipeDto(
    val id: Long,
    val title: String,
    val image: String? = null,
    val imageType: String? = null,
    val summary: String? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
)

@Serializable
data class RecipeDetailDto(
    val id: Long,
    val title: String,
    val image: String? = null,
    val summary: String? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
    val healthScore: Double? = null,
    val pricePerServing: Double? = null,
    val instructions: String? = null,
    val extendedIngredients: List<IngredientDto> = emptyList(),
    val analyzedInstructions: List<InstructionDto> = emptyList(),
)

@Serializable
data class SimilarRecipeDto(
    val id: Long,
    val title: String,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
)

@Serializable
data class IngredientDto(
    val id: Long? = null,
    val name: String,
    val amount: Double? = null,
    val unit: String? = null,
    val original: String? = null,
)

@Serializable
data class InstructionDto(
    val name: String? = null,
    val steps: List<StepDto> = emptyList(),
)

@Serializable
data class StepDto(
    val number: Int,
    val step: String,
)
