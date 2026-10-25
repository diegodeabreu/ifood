package com.diegodeabreu.ifoodprocesso

import androidx.lifecycle.SavedStateHandle
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.diegodeabreu.ifoodprocesso.data.remote.SpoonacularApiClient
import com.diegodeabreu.ifoodprocesso.data.repository.RecipeRepositoryImpl
import com.diegodeabreu.ifoodprocesso.db.RecipeDatabase
import com.diegodeabreu.ifoodprocesso.domain.usecase.GetRecipeDetailsUseCase
import com.diegodeabreu.ifoodprocesso.domain.usecase.SearchRecipesUseCase
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelFlowTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchQueryEmissionsWithTurbine() =
        runTest {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                RecipeDatabase.Schema.create(this)
            }
            val database = RecipeDatabase(driver)
            val apiClient = SpoonacularApiClient("test_key")
            val repository = RecipeRepositoryImpl(apiClient)
            val viewModel =
                RecipeViewModel(
                    SearchRecipesUseCase(repository),
                    GetRecipeDetailsUseCase(repository),
                    database.recipeDatabaseQueries,
                    SavedStateHandle(),
                )

            viewModel.searchQuery.test {
                assertEquals("", awaitItem())
                viewModel.updateQuery("bolo")
                assertEquals("bolo", awaitItem())
                viewModel.clearSearch()
                assertEquals("", awaitItem())
            }
        }
}
