package com.diegodeabreu.ifoodprocesso.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegodeabreu.ifoodprocesso.analytics.Analytics
import com.diegodeabreu.ifoodprocesso.db.RecipeDatabaseQueries
import com.diegodeabreu.ifoodprocesso.domain.error.AppError
import com.diegodeabreu.ifoodprocesso.domain.error.DomainResult
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.domain.model.RecipeDetail
import com.diegodeabreu.ifoodprocesso.domain.usecase.GetRecipeDetailsUseCase
import com.diegodeabreu.ifoodprocesso.domain.usecase.SearchRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(
        val recipes: List<Recipe>,
        val totalResults: Int,
        val currentPage: Int = 1,
        val totalPages: Int = 1,
    ) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

sealed interface RecipeDetailUiState {
    object Idle : RecipeDetailUiState
    object Loading : RecipeDetailUiState
    data class Success(val detail: RecipeDetail) : RecipeDetailUiState
    data class Error(val message: String) : RecipeDetailUiState
}

class RecipeViewModel(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val getRecipeDetailsUseCase: GetRecipeDetailsUseCase,
    private val queries: RecipeDatabaseQueries,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow(savedStateHandle["searchQuery"] ?: "")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _recipeDetailState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Idle)
    val recipeDetailState: StateFlow<RecipeDetailUiState> = _recipeDetailState.asStateFlow()

    enum class RecipeTab {
        SEARCH,
        FAVORITES,
    }

    private val _currentTab =
        MutableStateFlow(
            savedStateHandle.get<String>("currentTab")?.let { RecipeTab.valueOf(it) } ?: RecipeTab.SEARCH,
        )
    val currentTab: StateFlow<RecipeTab> = _currentTab.asStateFlow()

    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private var currentQuery = ""
    private var currentPage = 1
    private var favoriteCurrentPage = 1
    private val pageSize = 10

    init {
        val savedFavorites =
            try {
                queries.selectFavorites().executeAsList().map { entity ->
                    Recipe(
                        id = entity.id,
                        title = entity.title,
                        imageUrl = entity.imageUrl,
                        summary = entity.summary,
                        readyInMinutes = entity.readyInMinutes?.toInt(),
                        servings = entity.servings?.toInt(),
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        _favorites.value = savedFavorites
        _favoriteIds.value = savedFavorites.map { it.id }.toSet()
        Analytics.screen("RecipeScreen")
    }

    val favoriteTotalPages: Int
        get() = ((_favorites.value.size + pageSize - 1) / pageSize).coerceAtLeast(1)

    val paginatedFavorites: List<Recipe>
        get() {
            val list = _favorites.value
            if (favoriteCurrentPage > favoriteTotalPages) {
                favoriteCurrentPage = favoriteTotalPages
            }
            val start = (favoriteCurrentPage - 1) * pageSize
            val end = minOf(start + pageSize, list.size)
            return if (start < list.size) list.subList(start, end) else emptyList()
        }

    val favoriteCurrentPageNum: Int
        get() = favoriteCurrentPage

    fun nextFavoritePage() {
        if (favoriteCurrentPage < favoriteTotalPages) {
            favoriteCurrentPage++
            Analytics.track("favorite_pagination", mapOf("page" to favoriteCurrentPage))
        }
    }

    fun previousFavoritePage() {
        if (favoriteCurrentPage > 1) {
            favoriteCurrentPage--
            Analytics.track("favorite_pagination", mapOf("page" to favoriteCurrentPage))
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
        savedStateHandle["searchQuery"] = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        savedStateHandle["searchQuery"] = ""
        _uiState.value = RecipeUiState.Idle
        Analytics.track("clear_search")
    }

    fun setTab(tab: RecipeTab) {
        _currentTab.value = tab
        savedStateHandle["currentTab"] = tab.name
        Analytics.track("tab_switch", mapOf("tab" to tab.name))
    }

    fun toggleFavorite(recipe: Recipe) {
        val currentIds = _favoriteIds.value.toMutableSet()
        val currentFavs = _favorites.value.toMutableList()
        val isFav = currentIds.contains(recipe.id)
        if (isFav) {
            currentIds.remove(recipe.id)
            currentFavs.removeAll { it.id == recipe.id }
            try {
                queries.updateFavorite(isFavorite = 0L, id = recipe.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            currentIds.add(recipe.id)
            if (!currentFavs.any { it.id == recipe.id }) {
                currentFavs.add(recipe)
            }
            try {
                queries.upsertRecipe(
                    id = recipe.id,
                    title = recipe.title,
                    imageUrl = recipe.imageUrl,
                    summary = recipe.summary,
                    readyInMinutes = recipe.readyInMinutes?.toLong(),
                    servings = recipe.servings?.toLong(),
                    isFavorite = 1L,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _favoriteIds.value = currentIds
        _favorites.value = currentFavs

        Analytics.track(
            "recipe_favorite_toggle",
            mapOf("recipe_id" to recipe.id, "title" to recipe.title, "is_favorite" to !isFav),
        )
    }

    fun selectRecipe(recipe: Recipe?) {
        _selectedRecipe.value = recipe
        if (recipe != null) {
            fetchRecipeDetails(recipe.id)
            Analytics.track("recipe_view_detail", mapOf("recipe_id" to recipe.id, "title" to recipe.title))
            Analytics.screen("RecipeDetailScreen")
        } else {
            Analytics.screen("RecipeScreen")
        }
    }

    private fun fetchRecipeDetails(id: Long) {
        viewModelScope.launch {
            _recipeDetailState.value = RecipeDetailUiState.Loading
            val result = getRecipeDetailsUseCase(id)
            _recipeDetailState.value =
                when (result) {
                    is DomainResult.Success -> RecipeDetailUiState.Success(result.data)
                    is DomainResult.Error -> {
                        val message =
                            when (val err = result.error) {
                                is AppError.Network -> err.message ?: "Network error"
                                is AppError.Database -> err.message ?: "Database error"
                                is AppError.Unknown -> err.message ?: "Unknown error"
                            }
                        RecipeDetailUiState.Error(message)
                    }
                }
        }
    }

    fun searchRecipes(query: String = _searchQuery.value) {
        if (query.isBlank()) return
        currentQuery = query
        currentPage = 1
        fetchPage(currentPage)
        Analytics.track("recipe_search", mapOf("query" to query))
    }

    fun nextPage() {
        val currentState = _uiState.value
        if (currentState is RecipeUiState.Success) {
            if (currentPage < currentState.totalPages) {
                currentPage++
                fetchPage(currentPage)
                Analytics.track("search_pagination", mapOf("page" to currentPage, "query" to currentQuery))
            }
        }
    }

    fun previousPage() {
        if (currentPage > 1) {
            currentPage--
            fetchPage(currentPage)
            Analytics.track("search_pagination", mapOf("page" to currentPage, "query" to currentQuery))
        }
    }

    private fun fetchPage(page: Int) {
        val offset = (page - 1) * pageSize
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            val result = searchRecipesUseCase(currentQuery, offset = offset)
            _uiState.value =
                when (result) {
                    is DomainResult.Success -> {
                        val searchResult = result.data
                        val totalPages = kotlin.math.ceil(searchResult.totalResults.toDouble() / pageSize).toInt().coerceAtLeast(1)
                        RecipeUiState.Success(
                            recipes = searchResult.recipes,
                            totalResults = searchResult.totalResults,
                            currentPage = page,
                            totalPages = totalPages,
                        )
                    }
                    is DomainResult.Error -> {
                        val message =
                            when (val err = result.error) {
                                is AppError.Network -> err.message ?: "Network error"
                                is AppError.Database -> err.message ?: "Database error"
                                is AppError.Unknown -> err.message ?: "Unknown error"
                            }
                        RecipeUiState.Error(message)
                    }
                }
        }
    }
}
