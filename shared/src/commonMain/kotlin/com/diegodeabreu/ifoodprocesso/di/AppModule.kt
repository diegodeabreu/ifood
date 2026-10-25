package com.diegodeabreu.ifoodprocesso.di

import com.diegodeabreu.ifoodprocesso.data.remote.SpoonacularApiClient
import com.diegodeabreu.ifoodprocesso.data.repository.RecipeRepositoryImpl
import com.diegodeabreu.ifoodprocesso.db.DatabaseDriverFactory
import com.diegodeabreu.ifoodprocesso.db.RecipeDatabase
import com.diegodeabreu.ifoodprocesso.domain.repository.RecipeRepository
import com.diegodeabreu.ifoodprocesso.domain.usecase.GetRecipeDetailsUseCase
import com.diegodeabreu.ifoodprocesso.domain.usecase.SearchRecipesUseCase
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeViewModel
import com.diegodeabreu.ifoodprocesso.util.DefaultDispatcherProvider
import com.diegodeabreu.ifoodprocesso.util.DispatcherProvider
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun createAppModule(apiKey: String) =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
        single { DatabaseDriverFactory().createDriver() }
        single { RecipeDatabase(get()) }
        single { get<RecipeDatabase>().recipeDatabaseQueries }
        single { SpoonacularApiClient(apiKey) }
        single<RecipeRepository> { RecipeRepositoryImpl(get()) }
        factory { SearchRecipesUseCase(get()) }
        factory { GetRecipeDetailsUseCase(get()) }
        viewModelOf(::RecipeViewModel)
    }
