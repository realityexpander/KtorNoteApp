package com.realityexpander.ktornoteapp.ui.common

import android.view.inputmethod.EditorInfo
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