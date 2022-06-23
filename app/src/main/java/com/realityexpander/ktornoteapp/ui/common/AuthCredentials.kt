package com.realityexpander.ktornoteapp.ui.common

import android.content.SharedPreferences
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor

// Remove from shared preferences and api when user logs out
fun removeAllCredentials(
    sharedPref: SharedPreferences,
    basicAuthInterceptor: BasicAuthInterceptor
) {
    sharedPref.edit()
        .remove(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL)
        .remove(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD)
        .remove(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID)
        .apply()

    basicAuthInterceptor.clearCredentials()
}

// Save to shared prefs and api interceptor
fun saveAuthCredentialsToPrefs(
    sharedPref: SharedPreferences,
    basicAuthInterceptor: BasicAuthInterceptor,
    email: String?,
    password: String?,
    userId: String?
): Boolean {
    return try {
        sharedPref.edit()
            .putString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, email!!)
            .putString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, password!!)
            .putString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID, userId!!)
            .apply()

        setApiCredentials(basicAuthInterceptor, email, password)

        true
    } catch (e: Exception) {
        e.printStackTrace()

        false
    }
}

// Sets the basic auth credentials in the interceptor
fun setApiCredentials(basicAuthInterceptor: BasicAuthInterceptor,
                      email: String,
                      password: String
) {
    basicAuthInterceptor.setCredentials(email, password)
}

// Check if logged in even when offline (had to log in online at least once)
fun isLoggedIn(sharedPref: SharedPreferences): Boolean {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null) != null &&
        sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, null) != null &&
        sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID, null) != null
}

fun getLoggedInEmail(sharedPref: SharedPreferences): String? {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null)
}

fun getLoggedInPassword(sharedPref: SharedPreferences): String? {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, null)
}

fun getLoggedInUserId(sharedPref: SharedPreferences): String? {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID, null)
}