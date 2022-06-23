package com.realityexpander.ktornoteapp.ui.common

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.textfield.TextInputEditText

// When Done button on soft keyboard is pressed, run the given action
fun TextInputEditText.onImeDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

// Set the tint color of the given drawable
fun setDrawableColorTint(
    view: View,
    drawableId: Int,
    tintColor: String = "#FFFFFF",
    resources: Resources
) {
    val drawable = ResourcesCompat.getDrawable(resources, drawableId, null)
    drawable?.let {
        try {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor(tintColor)
            DrawableCompat.setTint(wrappedDrawable, color)
            view.background = it // wrappedDrawable
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}