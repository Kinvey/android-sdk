package com.kinvey.androidTest.store.user

import com.google.api.client.http.HttpTransport
import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.json.GenericJson
import com.google.api.client.json.Json
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import com.google.gson.Gson
import com.kinvey.android.model.User

class MockHttpErrorTransport : HttpTransport() {

    public override fun buildRequest(method: String, url: String): LowLevelHttpRequest {
        return object : MockLowLevelHttpRequest() {
            override fun execute(): LowLevelHttpResponse? {
                if (url.contains("oauth/token")) {
                    return oauthToken()
                } else if (url.contains("oauth/auth")) {
                    return oauthAuth()
                } else if (url.contains("tempURL")) {
                    return tempURL()
                } else if (url.contains("/login")) {
                    return userLoginError()
                } else if (url.contains("/user/")) {
                    return userRetrieve()
                }
                return null
            }
        }
    }

    private fun oauthToken(): LowLevelHttpResponse {
        val response = MockLowLevelHttpResponse()
        val content = GenericJson()
        content["access_token"] = "myAccess"
        content["refresh_token"] = "myRefresh"
        content["token_type"] = "bearer"
        content["expires_in"] = "100"
        response.contentType = Json.MEDIA_TYPE
        response.setContent(Gson().toJson(content))
        response.statusCode = 200
        return response
    }

    private fun oauthAuth(): MockLowLevelHttpResponse? {
        return null
    }

    private fun tempURL(): MockLowLevelHttpResponse? {
        return null
    }

    private fun userLoginError(): MockLowLevelHttpResponse {
        val response = MockLowLevelHttpResponse()
        val content = GenericJson()
        content["error"] = ERROR_500
        content["description"] = DESCRIPTION_500
        response.contentType = Json.MEDIA_TYPE
        response.setContent(Gson().toJson(content))
        response.statusCode = 500
        return response
    }

    private fun userRetrieve(): MockLowLevelHttpResponse {
        val response = MockLowLevelHttpResponse()
        val content = User()
        content["error"] = ERROR_500
        content["description"] = DESCRIPTION_500
        response.contentType = Json.MEDIA_TYPE
        response.setContent(Gson().toJson(content))
        response.statusCode = 500
        return response
    }

    companion object {
        internal const val ERROR_500 = "KinveyInternalErrorRetry"
        internal const val DESCRIPTION_500 = "The Kinvey server encountered an unexpected error. Please retry your request"
    }
}