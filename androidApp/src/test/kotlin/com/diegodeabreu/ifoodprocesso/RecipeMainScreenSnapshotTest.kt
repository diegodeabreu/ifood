package com.diegodeabreu.ifoodprocesso

import androidx.compose.material3.MaterialTheme
import com.diegodeabreu.ifoodprocesso.di.createAppModule
import com.diegodeabreu.ifoodprocesso.presentation.ui.RecipeScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-480dpi")
class RecipeMainScreenSnapshotTest {

    @Before
    fun setUp() {
        try {
            startKoin {
                modules(createAppModule("test_key"))
            }
        } catch (e: Exception) {
            // Already started
        }
    }

    @After
    fun tearDown() {
        try {
            stopKoin()
        } catch (e: Exception) {
            // Ignore
        }
    }

    @Test
    fun testRecipeScreenIdle() {
        captureRoboImage("src/test/snapshots/RecipeScreen_Idle.png") {
            MaterialTheme {
                RecipeScreen()
            }
        }
    }
}
