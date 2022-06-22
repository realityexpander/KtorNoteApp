package com.realityexpander.ktornoteapp.common

object Constants {

    // const val BASE_URL = "http://10.0.2.2:8001" // for emulator running on local machine
    const val BASE_URL = "http://192.168.0.186:8001" // for real device running on WIFI network

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

}