package com.diegodeabreu.ifoodprocesso.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.diegodeabreu.ifoodprocesso.AppContextHolder

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(RecipeDatabase.Schema, AppContextHolder.context, "recipe.db")
    }
}
