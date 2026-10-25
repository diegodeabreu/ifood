package com.diegodeabreu.ifoodprocesso.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val summary: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
)
