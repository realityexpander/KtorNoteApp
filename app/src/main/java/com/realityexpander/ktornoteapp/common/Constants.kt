package com.realityexpander.ktornoteapp.common

import com.realityexpander.ktornoteapp.BuildConfig

object Constants {

    const val SERVER_URL = BuildConfig.SERVER_URL

    // These endpoints will be accessed without authentication headers
    val PUBLIC_ENDPOINTS = listOf(
        "/login",
        "/register",
    )

    const val DATABASE_NAME = "notes_database"

    const val ENCRYPTED_SHARED_PREF_NAME =
        "encrypted_shared_pref"
    const val ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL =
        "encrypted_shared_pref_key_logged_in_email"
    const val ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_PASSWORD =
        "encrypted_shared_pref_key_logged_in_password"
    const val ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID =
        "encrypted_shared_pref_key_logged_in_user_id"

    const val DEFAULT_NOTE_COLOR = "#CCFFCC"

}