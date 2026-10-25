package com.diegodeabreu.ifoodprocesso.presentation.ui

import android.content.Intent
import com.diegodeabreu.ifoodprocesso.AppContextHolder

actual fun shareRecipe(
    title: String,
    content: String,
) {
    try {
        val context = AppContextHolder.context
        val intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        val chooser =
            Intent.createChooser(intent, "Share Recipe").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
