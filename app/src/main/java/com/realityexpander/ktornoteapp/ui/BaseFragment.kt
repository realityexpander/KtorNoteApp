package com.realityexpander.ktornoteapp.ui

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment(layoutId: Int): Fragment(layoutId) {
    fun showSnackbar(message: String) {
       Snackbar.make(
           // requireView().rootView,  // crashes upon rotation!
           requireActivity().findViewById(android.R.id.content), // survives rotation
           message,
           Snackbar.LENGTH_LONG
       ).show()
    }

    fun showToast(message: String?) {
        if(message.isNullOrBlank()) return

        Toast(requireActivity().applicationContext).apply {
            setText(message)
            duration = Toast.LENGTH_SHORT
            show()
        }
    }
}