package com.realityexpander.ktornoteapp.ui.common

import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import com.realityexpander.ktornoteapp.ui.BaseFragment
import kotlinx.coroutines.runBlocking


// To be called from the ViewModel
fun logoutFromViewModel(
    isLogoutDestructive: Boolean = false,
    repository: NoteRepository
): Boolean {
    return runBlocking {

        // Only clear the DB if there are no unsynced notes, or
        // the user wants to logout destructively.
        if (isLogoutDestructive ||
            repository.getUnsyncedNotesDb().isEmpty()
        ) {
            repository.deleteAllNotesDb()

            return@runBlocking true
        }

        false
    }
}


// To be called from the Fragment
fun BaseFragment.logoutFromFragment(
    isLogoutDestructive: Boolean = false,
    viewModelLogout: (isDestructive: Boolean) -> Boolean,
    sharedPref: SharedPreferences,
    basicAuthInterceptor: BasicAuthInterceptor,
    navigationOnLogoutSuccess: () -> Unit,
) {
    // show dialog to confirm destructive logout
    fun showLogoutWarningDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage(
                "There are unsynced/unsaved notes that will be deleted if you log out now." +
                        "\n\nAre you sure you want to logout?"
            )
            .setPositiveButton("Yes") { _, _ ->
                // Try logout again, but this time with destructive=true
                logoutFromFragment(
                    isLogoutDestructive = true,  // logout destructively
                    viewModelLogout,
                    sharedPref,
                    basicAuthInterceptor,
                    navigationOnLogoutSuccess
                )
            }
            .setNegativeButton("No") { _, _ ->
                // do nothing
            }
            .create()
        dialog.show()
    }


    val isLogoutSafe = viewModelLogout(isLogoutDestructive)

    if (isLogoutDestructive || isLogoutSafe) {
        showSnackbar("Logging out...")

        viewModelLogout(true)
        removeAllCredentials(sharedPref, basicAuthInterceptor)

        navigationOnLogoutSuccess()
    } else {
        showLogoutWarningDialog()
    }
}


