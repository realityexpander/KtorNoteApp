package com.realityexpander.ktornoteapp.common

data class Resource<out T>(
    val status: Status,
    val message: String?,
    val data: T?
) {

    companion object {
        fun <T> success(message: String = "", data: T?): Resource<T> {
            return Resource(Status.SUCCESS, message, data)
        }

        fun <T> error(message: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, message, data)
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