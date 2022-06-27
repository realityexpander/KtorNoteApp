package com.realityexpander.ktornoteapp.ui.common

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.textfield.TextInputEditText

// When "Done" button on soft keyboard is pressed, run the given action
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
    tintColorStr: String = "#FFFFFF",
    resources: Resources
) {
    // Make sure its in hex format
    val tintColorConformed =
        if(!tintColorStr.startsWith("#")) {
            "#$tintColorStr"
        } else
            tintColorStr

    val drawable = ResourcesCompat.getDrawable(resources, drawableId, null)
    drawable?.let {
        try {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor(tintColorConformed)
            DrawableCompat.setTint(wrappedDrawable, color)
            view.background = wrappedDrawable
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Add s to the end of the string if it's greater than 1
fun <T> addPostfixS(it: List<T>) =
    if (it.size > 1) "s" else ""

fun prependHashIfNotPresent(it: String) =
    if(it.startsWith("#")) it else "#$it"

fun removeHashIfPresent(it: String) =
    if(it.startsWith("#")) it.substring(1) else it

// Accepts a hex color string (with or without "#" prefix) and returns
//   a "#"-prefixed hex color string representing the inverted color.
fun invertColor(colorHexStr: String, forceLightOrDark: Boolean): String {
    // conform the color string to a "#RRGGBB" hex string
    val colorHex = prependHashIfNotPresent(colorHexStr).substring(1)

    val r = Integer.parseInt(colorHex.substring(0, 2), 16)
    val g = Integer.parseInt(colorHex.substring(2, 4), 16)
    val b = Integer.parseInt(colorHex.substring(4, 6), 16)
    val a = Integer.parseInt(colorHex.substring(6, 8), 16)

    return if (forceLightOrDark) {
        if (((r + g + b)/3.0) < 128 || a < 128 ) "#DDDDDD" else "#222222"
    } else {
        "#${String.format("%02x%02x%02x", 255 - r, 255 - g, 255 - b)}FF"  // Forces alpha to be 255
        // return "#${String.format("%02X", r)}${String.format("%02X", g)}${String.format("%02X", b)}"
    }
}