package com.realityexpander.ktornoteapp.ui

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
}