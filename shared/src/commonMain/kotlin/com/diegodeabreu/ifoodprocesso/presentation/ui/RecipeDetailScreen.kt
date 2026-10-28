package com.diegodeabreu.ifoodprocesso.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.diegodeabreu.ifoodprocesso.domain.model.Recipe
import com.diegodeabreu.ifoodprocesso.presentation.viewmodel.RecipeDetailUiState

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
                    Text(Strings.backToResults)
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
                        Text(Strings.shareEmoji, style = MaterialTheme.typography.headlineMedium)
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Text(
                            text = if (isFavorite) Strings.starFilled else Strings.starEmpty,
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
                                    Text(Strings.imageLoadFailed, style = MaterialTheme.typography.bodyMedium)
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
                                text = Strings.readyInMinsShort(it),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        recipe.servings?.let {
                            Text(
                                text = Strings.servingsShort(it),
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
                                text = Strings.failedToLoadDetails(state.message),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    is RecipeDetailUiState.Success -> {
                        val detail = state.detail

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Strings.ingredientsHeader,
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
                                    text = Strings.instructionsHeader,
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
                                    text = Strings.instructionsHeader,
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
                                    text = Strings.summaryHeader,
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
                                            text = Strings.similarRecipesHeader,
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
