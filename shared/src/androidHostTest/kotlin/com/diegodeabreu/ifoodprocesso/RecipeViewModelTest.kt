package com.diegodeabreu.ifoodprocesso

import androidx.lifecycle.SavedStateHandle
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.diegodeabreu.ifoodprocesso.data.remote.SpoonacularApiClient
import com.diegodeabreu.ifoodprocesso.data.repository.RecipeRepositoryImpl
import com.diegodeabreu.ifoodprocesso.db.RecipeDatabase
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.domain.usecase.GetRecipeDetailsUseCase
import com.diegodeabreu.ifoodprocesso.domain.usecase.SearchRecipesUseCase
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeUiState
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: RecipeDatabase
    private lateinit var viewModel: RecipeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            RecipeDatabase.Schema.create(this)
        }
        database = RecipeDatabase(driver)
        val apiClient = SpoonacularApiClient("test_key")
        val repository = RecipeRepositoryImpl(apiClient)
        val searchUseCase = SearchRecipesUseCase(repository)
        val detailsUseCase = GetRecipeDetailsUseCase(repository)
        viewModel = RecipeViewModel(searchUseCase, detailsUseCase, database.recipeDatabaseQueries, SavedStateHandle())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() =
        runTest {
            assertEquals("", viewModel.searchQuery.value)
            assertEquals(RecipeViewModel.RecipeTab.SEARCH, viewModel.currentTab.value)
            assertTrue(viewModel.uiState.value is RecipeUiState.Idle)
        }

    @Test
    fun testUpdateQuery() =
        runTest {
            viewModel.updateQuery("pasta")
            assertEquals("pasta", viewModel.searchQuery.value)
        }

    @Test
    fun testToggleFavoriteAndPagination() =
        runTest {
            val recipe = Recipe(1L, "Test Recipe", "url", "summary", 15, 2)

            assertTrue(viewModel.favorites.value.isEmpty())
            assertFalse(viewModel.favoriteIds.value.contains(1L))

            viewModel.toggleFavorite(recipe)
            assertEquals(1, viewModel.favorites.value.size)
            assertTrue(viewModel.favoriteIds.value.contains(1L))
            assertEquals(1, viewModel.paginatedFavorites.size)
            assertEquals(1, viewModel.favoriteTotalPages)

            viewModel.toggleFavorite(recipe)
            assertTrue(viewModel.favorites.value.isEmpty())
            assertFalse(viewModel.favoriteIds.value.contains(1L))
        }

    @Test
    fun testSetTab() =
        runTest {
            assertEquals(RecipeViewModel.RecipeTab.SEARCH, viewModel.currentTab.value)

            viewModel.setTab(RecipeViewModel.RecipeTab.FAVORITES)
            assertEquals(RecipeViewModel.RecipeTab.FAVORITES, viewModel.currentTab.value)
        }

    @Test
    fun testSavedStateHandleRestoration() =
        runTest {
            val savedState = SavedStateHandle(
                mapOf("searchQuery" to "soup", "currentTab" to "FAVORITES")
            )
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                RecipeDatabase.Schema.create(this)
            }
            val db = RecipeDatabase(driver)
            val apiClient = SpoonacularApiClient("test_key")
            val repository = RecipeRepositoryImpl(apiClient)
            val vm = RecipeViewModel(
                SearchRecipesUseCase(repository),
                GetRecipeDetailsUseCase(repository),
                db.recipeDatabaseQueries,
                savedState
            )

            assertEquals("soup", vm.searchQuery.value)
            assertEquals(RecipeViewModel.RecipeTab.FAVORITES, vm.currentTab.value)
        }
}
