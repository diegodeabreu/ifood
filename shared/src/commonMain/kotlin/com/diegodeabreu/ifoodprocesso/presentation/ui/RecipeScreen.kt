package com.diegodeabreu.ifoodprocesso.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.error.ErrorNotifier
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeDetailUiState
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeUiState
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecipeScreen(viewModel: RecipeViewModel = koinViewModel()) {
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
                ErrorNotifier.notifyError("Pressione voltar novamente para sair")
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
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Spoonacular Recipe Search",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Tabs / Navigation (Search vs Favorites)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { viewModel.setTab(RecipeViewModel.RecipeTab.SEARCH) },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (currentTab == RecipeViewModel.RecipeTab.SEARCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (currentTab == RecipeViewModel.RecipeTab.SEARCH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                        ) {
                            Text("🔍 Search")
                        }
                        Button(
                            onClick = { viewModel.setTab(RecipeViewModel.RecipeTab.FAVORITES) },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                        ) {
                            Text("⭐ Saved (${favorites.size})")
                        }
                    }

                    if (currentTab == RecipeViewModel.RecipeTab.FAVORITES) {
                        if (favorites.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No saved recipes yet.\nTap the heart icon ⭐ on any recipe to save it!",
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
                                    text = "Total saved: ${favorites.size} recipes",
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

                                // Pagination controls footer for favorites
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
                                        Text("« Prev")
                                    }

                                    Text(
                                        text = "Page $favCurrentPage of $favTotalPages",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    Button(
                                        onClick = { viewModel.nextFavoritePage() },
                                        enabled = favCurrentPage < favTotalPages,
                                    ) {
                                        Text("Next »")
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.updateQuery(it) },
                            label = { Text("Search recipes (e.g. frango, massa, bolo)") },
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
                            Text("Search")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.clearSearch()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Clear Search")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (val state = uiState) {
                            is RecipeUiState.Idle -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Type a keyword and press Search to find recipes.")
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
                                        Text("No recipes found.")
                                    }
                                } else {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                    ) {
                                        Text(
                                            text = "Total found: ${state.totalResults} recipes",
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

                                        // Pagination controls footer
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
                                                Text("« Prev")
                                            }

                                            Text(
                                                text = "Page ${state.currentPage} of ${state.totalPages}",
                                                style = MaterialTheme.typography.bodyMedium,
                                            )

                                            Button(
                                                onClick = { viewModel.nextPage() },
                                                enabled = state.currentPage < state.totalPages,
                                            ) {
                                                Text("Next »")
                                            }
                                        }
                                    }
                                }
                            }
                            is RecipeUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Error: ${state.message}",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    Box(
        modifier =
            modifier
                .background(Color.Gray.copy(alpha = alpha)),
    )
}

@Composable
fun RecipeItemCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Recipe Image Thumbnail with Shimmer loading
            if (!recipe.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier =
                        Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🍽", style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                recipe.readyInMinutes?.let {
                    Text(
                        text = "⏱ Ready in $it minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                recipe.servings?.let {
                    Text(
                        text = "🍽 Servings: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Text(
                    text = if (isFavorite) "⭐" else "☆",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    detailState: RecipeDetailUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSelectSimilarRecipe: (Recipe) -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            // Fixed top bar with Back, Share, and Favorite buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onBack) {
                    Text("← Back to Results")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        val shareText =
                            buildString {
                                appendLine("🍳 ${recipe.title}")
                                recipe.readyInMinutes?.let { appendLine("⏱ Ready in $it mins") }
                                recipe.servings?.let { appendLine("🍽 Servings: $it") }
                                appendLine()

                                if (detailState is RecipeDetailUiState.Success) {
                                    val detail = (detailState as RecipeDetailUiState.Success).detail
                                    if (detail.ingredients.isNotEmpty()) {
                                        appendLine("--- Ingredients ---")
                                        detail.ingredients.forEach { ing ->
                                            appendLine("• ${ing.original ?: ing.name}")
                                        }
                                        appendLine()
                                    }
                                    if (detail.steps.isNotEmpty()) {
                                        appendLine("--- Instructions ---")
                                        detail.steps.forEach { step ->
                                            appendLine("${step.number}. ${step.step}")
                                        }
                                        appendLine()
                                    } else if (!detail.instructions.isNullOrBlank()) {
                                        appendLine("--- Instructions ---")
                                        appendLine(detail.instructions.replace(Regex("<[^>]*>"), ""))
                                        appendLine()
                                    }
                                }
                                appendLine("Shared via IFood Processo Recipe App")
                            }
                        shareRecipe(recipe.title, shareText)
                    }) {
                        Text("📤", style = MaterialTheme.typography.headlineMedium)
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Text(
                            text = if (isFavorite) "⭐" else "☆",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable content
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val imageUrl =
                        when (detailState) {
                            is RecipeDetailUiState.Success -> detailState.detail.imageUrl
                            else -> recipe.imageUrl
                        }

                    if (!imageUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = recipe.title,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                ShimmerBox(modifier = Modifier.fillMaxSize())
                            },
                            error = {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.LightGray),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("Failed to load image", style = MaterialTheme.typography.bodyMedium)
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        recipe.readyInMinutes?.let {
                            Text(
                                text = "⏱ $it mins",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        recipe.servings?.let {
                            Text(
                                text = "🍽 $it servings",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                when (val state = detailState) {
                    is RecipeDetailUiState.Idle, is RecipeDetailUiState.Loading -> {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is RecipeDetailUiState.Error -> {
                        item {
                            Text(
                                text = "Failed to load details: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    is RecipeDetailUiState.Success -> {
                        val detail = state.detail

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingredients",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

                        items(detail.ingredients) { ingredient ->
                            Text(
                                text = "• ${ingredient.original ?: ingredient.name}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        if (detail.steps.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instructions",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }

                            items(detail.steps) { step ->
                                Text(
                                    text = "${step.number}. ${step.step}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        } else if (!detail.instructions.isNullOrBlank()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instructions",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = detail.instructions.replace(Regex("<[^>]*>"), ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        detail.summary?.let { summary ->
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = summary.replace(Regex("<[^>]*>"), ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        if (detail.similarRecipes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "💡 You might also like (Similar Recipes):",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )

                                        detail.similarRecipes.forEach { similar ->
                                            OutlinedButton(
                                                onClick = {
                                                    onSelectSimilarRecipe(
                                                        Recipe(
                                                            id = similar.id,
                                                            title = similar.title,
                                                            imageUrl = null,
                                                            summary = null,
                                                            readyInMinutes = null,
                                                            servings = null,
                                                        ),
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = similar.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
