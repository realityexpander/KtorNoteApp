package com.realityexpander.ktornoteapp.ui

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment(layoutId: Int): Fragment(layoutId) {
    fun showSnackbar(message: String) {
       Snackbar.make(
//           requireActivity().findViewById(android.R.id.content), // compiles but correct?
          requireView().rootView,
//           requireActivity().rootLayout, // philips way
           message,
           Snackbar.LENGTH_LONG
       ).show()
    }
}