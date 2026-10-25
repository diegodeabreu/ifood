package com.diegodeabreu.ifoodprocesso.data.remote

import com.diegodeabreu.ifoodprocesso.data.model.RecipeDetailDto
import com.diegodeabreu.ifoodprocesso.data.model.RecipeSearchResponseDto
import com.diegodeabreu.ifoodprocesso.data.model.SimilarRecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SpoonacularApiClient(
    private val apiKey: String = "3b1294c962874f78a2ff4f7fb9b51865", // Replace with valid Spoonacular API Key or pass via config
) {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }

    private val ptToEnDictionary =
        mapOf(
            "frango" to "chicken",
            "carne" to "beef",
            "massa" to "pasta",
            "macarrão" to "pasta",
            "arroz" to "rice",
            "feijão" to "beans",
            "queijo" to "cheese",
            "ovo" to "egg",
            "ovos" to "eggs",
            "batata" to "potato",
            "peixe" to "fish",
            "porco" to "pork",
            "salada" to "salad",
            "sopa" to "soup",
            "bolo" to "cake",
            "pão" to "bread",
            "chocolate" to "chocolate",
            "legumes" to "vegetables",
            "camarão" to "shrimp",
            "carne moída" to "ground beef",
        )

    suspend fun searchRecipes(
        query: String,
        number: Int = 10,
        offset: Int = 0,
    ): RecipeSearchResponseDto {
        val trimmed = query.trim().lowercase()
        val translatedQuery = ptToEnDictionary[trimmed] ?: query

        return client.get("https://api.spoonacular.com/recipes/complexSearch") {
            parameter("query", translatedQuery)
            parameter("number", number)
            parameter("offset", offset)
            parameter("apiKey", apiKey)
            parameter("language", "pt")
        }.body()
    }

    suspend fun getRecipeInformation(id: Long): RecipeDetailDto {
        return client.get("https://api.spoonacular.com/recipes/$id/information") {
            parameter("apiKey", apiKey)
            parameter("language", "pt")
        }.body()
    }

    suspend fun getSimilarRecipes(
        id: Long,
        number: Int = 3,
    ): List<SimilarRecipeDto> {
        return client.get("https://api.spoonacular.com/recipes/$id/similar") {
            parameter("number", number)
            parameter("apiKey", apiKey)
            parameter("language", "pt")
        }.body()
    }
}
