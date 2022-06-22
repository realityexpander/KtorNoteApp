package com.realityexpander.ktornoteapp.data.remote.responses

import io.ktor.http.*

interface BaseSimpleResponse {
    val isSuccessful: Boolean
        get() = true
    val statusCode: HttpStatusCode
        get() = HttpStatusCode.OK
    val message: String
        get() = "OK"
}

data class SimpleResponse(
    override val isSuccessful: Boolean,
    override val statusCode: HttpStatusCode = HttpStatusCode.OK,
    override val message: String = "OK",
) : BaseSimpleResponse

data class SimpleResponseWithData<T>(
    override val isSuccessful: Boolean,
    override val statusCode: HttpStatusCode = HttpStatusCode.OK,
    override val message: String = "OK",
    val data: T? = null
) : BaseSimpleResponse
