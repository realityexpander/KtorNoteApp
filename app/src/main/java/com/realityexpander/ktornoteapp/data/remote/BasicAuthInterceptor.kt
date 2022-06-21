package com.realityexpander.ktornoteapp.data.remote

import com.realityexpander.ktornoteapp.common.Constants.PUBLIC_ENDPOINTS
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor: Interceptor {
//    var email: String? = null
//    var password: String? = null

//    var email: String? = "test@123.com"
//    var password: String? = "test123"

    var email = "test@1234.com"
    var password = "12345678"

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        // Check for publicly-accessible API endpoints
        if(request.url.encodedPath in PUBLIC_ENDPOINTS) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", Credentials.basic(email ?: "", password ?: ""))
            .build()
        return chain.proceed(authenticatedRequest)
    }
}