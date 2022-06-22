package com.realityexpander.ktornoteapp.ui.common

import android.content.SharedPreferences
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor

fun removeAllCredentials(
    sharedPref: SharedPreferences,
    basicAuthInterceptor: BasicAuthInterceptor
) {
    sharedPref.edit()
        .remove(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL)
        .remove(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD)
        .apply()

    basicAuthInterceptor.clearCredentials()
}

fun saveAllCredentials(
    sharedPref: SharedPreferences,
    basicAuthInterceptor: BasicAuthInterceptor,
    email: String?,
    password: String?
): Boolean {
    return try {
        sharedPref.edit()
            .putString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, email!!)
            .putString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, password!!)
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

fun isLoggedIn(sharedPref: SharedPreferences): Boolean {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null) != null &&
        sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, null) != null
}

fun getLoggedInEmail(sharedPref: SharedPreferences): String? {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null)
}

fun getLoggedInPassword(sharedPref: SharedPreferences): String? {
    return sharedPref.getString(ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD, null)
}