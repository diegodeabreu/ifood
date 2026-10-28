package com.diegodeabreu.ifoodprocesso.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.diegodeabreu.ifoodprocesso.error.ErrorNotifier
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeUiState
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val detailState by viewModel.recipeDetailState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        ErrorNotifier.errors.collect { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    // Lifted list states to preserve scroll position when returning from details
    val searchListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()

    val favCurrentPage = viewModel.favoriteCurrentPageNum

    LaunchedEffect(uiState) {
        if (uiState is RecipeUiState.Success) {
            searchListState.scrollToItem(0)
        }
    }

    LaunchedEffect(favCurrentPage) {
        favoritesListState.scrollToItem(0)
    }

    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }

    if (selectedRecipe == null) {
        PlatformBackHandler(enabled = true) {
            if (doubleBackToExitPressedOnce) {
                exitApp()
            } else {
                doubleBackToExitPressedOnce = true
                ErrorNotifier.notifyError(Strings.exitPrompt)
                scope.launch {
                    kotlinx.coroutines.delay(2000L)
                    doubleBackToExitPressedOnce = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedRecipe != null) {
                PlatformBackHandler(enabled = true) {
                    viewModel.selectRecipe(null)
                }
                RecipeDetailScreen(
                    recipe = selectedRecipe!!,
                    detailState = detailState,
                    isFavorite = favoriteIds.contains(selectedRecipe!!.id),
                    onToggleFavorite = { viewModel.toggleFavorite(selectedRecipe!!) },
                    onSelectSimilarRecipe = { similarRecipe ->
                        viewModel.selectRecipe(similarRecipe)
                    },
                    onBack = { viewModel.selectRecipe(null) },
                )
            } else {
                MainContentCoordinator(
                    viewModel = viewModel,
                    uiState = uiState,
                    query = query,
                    currentTab = currentTab,
                    favorites = favorites,
                    favoriteIds = favoriteIds,
                    searchListState = searchListState,
                    favoritesListState = favoritesListState,
                    keyboardController = keyboardController,
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun MainContentCoordinator(
    viewModel: RecipeViewModel,
    uiState: RecipeUiState,
    query: String,
    currentTab: RecipeViewModel.RecipeTab,
    favorites: List<com.diegodeabreu.ifoodprocesso.domain.model.Recipe>,
    favoriteIds: Set<Long>,
    searchListState: LazyListState,
    favoritesListState: LazyListState,
    keyboardController: SoftwareKeyboardController?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = Strings.appTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        RecipeTabSelector(currentTab = currentTab, onTabSelected = { viewModel.setTab(it) }, favoritesSize = favorites.size)

        if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) {
            FavoritesContent(
                viewModel = viewModel,
                favorites = favorites,
                favoriteIds = favoriteIds,
                favoritesListState = favoritesListState,
                keyboardController = keyboardController,
            )
        } else {
            SearchContent(
                viewModel = viewModel,
                uiState = uiState,
                query = query,
                searchListState = searchListState,
                keyboardController = keyboardController,
            )
        }
    }
}

@Composable
fun RecipeTabSelector(
    currentTab: RecipeViewModel.RecipeTab,
    onTabSelected: (RecipeViewModel.RecipeTab) -> Unit,
    favoritesSize: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onTabSelected(RecipeViewModel.RecipeTab.SEARCH) },
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (currentTab == RecipeViewModel.RecipeTab.SEARCH) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    contentColor =
                        if (currentTab == RecipeViewModel.RecipeTab.SEARCH) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                ),
        ) {
            Text(Strings.searchTab)
        }
        Button(
            onClick = { onTabSelected(RecipeViewModel.RecipeTab.FAVORITES) },
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    contentColor =
                        if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                ),
        ) {
            Text("${Strings.savedTabPrefix} ($favoritesSize)")
        }
    }
}

@Composable
fun ColumnScope.FavoritesContent(
    viewModel: RecipeViewModel,
    favorites: List<com.diegodeabreu.ifoodprocesso.domain.model.Recipe>,
    favoriteIds: Set<Long>,
    favoritesListState: LazyListState,
    keyboardController: SoftwareKeyboardController?,
) {
    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = Strings.noSavedRecipes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    } else {
        val paginatedFavs = viewModel.paginatedFavorites
        val favTotalPages = viewModel.favoriteTotalPages
        val favCurrentPage = viewModel.favoriteCurrentPageNum

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            Text(
                text = Strings.totalSaved(favorites.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LazyColumn(
                state = favoritesListState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(paginatedFavs) { recipe ->
                    RecipeItemCard(
                        recipe = recipe,
                        isFavorite = favoriteIds.contains(recipe.id),
                        onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                        onClick = {
                            keyboardController?.hide()
                            viewModel.selectRecipe(recipe)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { viewModel.previousFavoritePage() },
                    enabled = favCurrentPage > 1,
                ) {
                    Text(Strings.prevButton)
                }

                Text(
                    text = Strings.pageIndicator(favCurrentPage, favTotalPages),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Button(
                    onClick = { viewModel.nextFavoritePage() },
                    enabled = favCurrentPage < favTotalPages,
                ) {
                    Text(Strings.nextButton)
                }
            }
        }
    }
}

@Composable
fun ColumnScope.SearchContent(
    viewModel: RecipeViewModel,
    uiState: RecipeUiState,
    query: String,
    searchListState: LazyListState,
    keyboardController: SoftwareKeyboardController?,
) {
    OutlinedTextField(
        value = query,
        onValueChange = { viewModel.updateQuery(it) },
        label = { Text(Strings.searchHint) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            keyboardController?.hide()
            viewModel.searchRecipes()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(Strings.searchButton)
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = {
            keyboardController?.hide()
            viewModel.clearSearch()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(Strings.clearButton)
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (val state = uiState) {
        is RecipeUiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.typeKeywordPrompt)
            }
        }
        is RecipeUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is RecipeUiState.Success -> {
            if (state.recipes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(Strings.noRecipesFound)
                }
            } else {
                RecipeSuccessList(
                    viewModel = viewModel,
                    state = state,
                    searchListState = searchListState,
                    keyboardController = keyboardController,
                )
            }
        }
        is RecipeUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = Strings.errorPrefix(state.message),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun ColumnScope.RecipeSuccessList(
    viewModel: RecipeViewModel,
    state: RecipeUiState.Success,
    searchListState: LazyListState,
    keyboardController: SoftwareKeyboardController?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
    ) {
        Text(
            text = Strings.totalFound(state.totalResults),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyColumn(
            state = searchListState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.recipes) { recipe ->
                RecipeItemCard(
                    recipe = recipe,
                    isFavorite = viewModel.favoriteIds.value.contains(recipe.id),
                    onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                    onClick = {
                        keyboardController?.hide()
                        viewModel.selectRecipe(recipe)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { viewModel.previousPage() },
                enabled = state.currentPage > 1,
            ) {
                Text(Strings.prevButton)
            }

            Text(
                text = Strings.pageIndicator(state.currentPage, state.totalPages),
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                onClick = { viewModel.nextPage() },
                enabled = state.currentPage < state.totalPages,
            ) {
                Text(Strings.nextButton)
            }
        }
    }
}
