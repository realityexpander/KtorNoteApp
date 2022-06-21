package com.realityexpander.ktornoteapp.common

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK

data class Resource<out T>(
    val status: Status,
    val message: String?,
    val data: T?,
    val statusCode: HttpStatusCode = OK
) {

    companion object {
        fun <T> success(message: String = "", data: T?): Resource<T> {
            return Resource(Status.SUCCESS, message, data)
        }

        fun <T> error(message: String,
                      statusCode: HttpStatusCode = BadRequest,
                      data: T? = null
        ): Resource<T> {
            return Resource(Status.ERROR, message, data, statusCode)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, null, data)
        }
    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}