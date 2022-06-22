package com.realityexpander.ktornoteapp.common

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK

data class Resource<out T>(
    val status: Status,
    val message: String? = null,
    val data: T? = null,
    val statusCode: HttpStatusCode? = null
) {

    companion object {
        fun <T> success(message: String = "", data: T?, statusCode: HttpStatusCode = OK): Resource<T> {
            return Resource(Status.SUCCESS, message, data, statusCode)
        }

        fun <T> error(message: String,
                      statusCode: HttpStatusCode = BadRequest,
                      data: T? = null
        ): Resource<T> {
            return Resource(Status.ERROR, message, data, statusCode)
        }

        fun <T> loading(message: String? = null, data: T? = null): Resource<T> {
            return Resource(Status.LOADING, message, data)
        }
    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}