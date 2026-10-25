package com.diegodeabreu.ifoodprocesso

import androidx.lifecycle.SavedStateHandle
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.diegodeabreu.ifoodprocesso.db.RecipeDatabase
import com.diegodeabreu.ifoodprocesso.di.createAppModule
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkKoinModules
import kotlin.test.Test

class KoinModuleTest : KoinTest {
    @Test
    fun checkAllModules() {
        val testModule =
            module {
                single<app.cash.sqldelight.db.SqlDriver> {
                    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                        RecipeDatabase.Schema.create(this)
                    }
                }
                factory { SavedStateHandle() }
            }
        checkKoinModules(listOf(createAppModule("dummy_api_key"), testModule))
    }
}
