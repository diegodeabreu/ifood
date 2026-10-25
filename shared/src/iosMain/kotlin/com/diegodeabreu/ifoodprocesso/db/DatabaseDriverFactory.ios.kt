package com.diegodeabreu.ifoodprocesso.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(RecipeDatabase.Schema, "recipe.db")
    }
}
