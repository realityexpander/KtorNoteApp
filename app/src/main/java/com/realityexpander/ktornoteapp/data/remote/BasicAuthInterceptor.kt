package com.realityexpander.ktornoteapp.data.remote

import com.realityexpander.ktornoteapp.common.Constants.PUBLIC_ENDPOINTS
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class BasicAuthInterceptor: Interceptor {
    var email: String? = null
    var password: String? = null

//    var email: String? = "test@123.com"
//    var password: String? = "test123"

//    var email = "test@1234.com"
//    var password = "12345678"

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        // Check for publicly-accessible API endpoints
        if (request.url.encodedPath in PUBLIC_ENDPOINTS) {
            return chain.proceed(request)
        }

        try {
            // Add basic auth credentials to request
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", Credentials.basic(email ?: "", password ?: ""))
                .build()
            return chain.proceed(authenticatedRequest)
        } catch (e: TimeoutException) {
            return chain.withConnectTimeout(10, TimeUnit.SECONDS).proceed(request)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun setCredentials(email: String, password: String) {
        if(email.isNotEmpty() && password.isNotEmpty()) {
            this.email = email
            this.password = password
        } else {
            throw IllegalArgumentException("Email and password must not be empty")
        }
    }

    fun clearCredentials() {
        this.email = null
        this.password = null
    }
}