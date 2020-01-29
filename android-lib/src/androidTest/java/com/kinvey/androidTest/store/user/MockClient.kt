package com.kinvey.androidTest.store.user

import android.content.Context
import com.google.api.client.http.BackOffPolicy
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonObjectParser
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.java.auth.CredentialStore
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.core.KinveyHeaders

class MockClient<T : User> private constructor(transport: HttpTransport?,
                                                httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?, servicePath: String?,
                                                objectParser: JsonObjectParser, kinveyRequestInitializer: KinveyClientRequestInitializer?,
                                                store: CredentialStore?, requestPolicy: BackOffPolicy?, encryptionKey: ByteArray?, context: Context)
    : Client<T>(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store, requestPolicy, encryptionKey, context) {
    class Builder<T : User>(internal var context: Context) : Client.Builder<T>(context) {
        private var userClass: Class<T>? = null
        override fun setUserClass(userClass: Class<T>): Builder<T> {
            this.userClass = userClass
            return this
        }

        override fun build(): MockClient<T> {
            val client = MockClient<T>(MockHttpTransport(), httpRequestInitializer, baseUrl, servicePath,
                    objectParser!!, MockKinveyClientRequestInitializer(), null, null, null, context)
            client.userClass = if (userClass != null) userClass  as Class<T> else User::class.java as Class<T>
            return client
        }

        fun build(transport: HttpTransport): MockClient<T> {
            val client = MockClient<T>(transport, httpRequestInitializer, baseUrl, servicePath,
                    objectParser!!, MockKinveyClientRequestInitializer(), null, null, null, context)
            client.userClass = if (userClass != null) userClass  as Class<T> else User::class.java as Class<T>
            initUserFromCredentialStore(client)
            return client
        }
    }

    class MockKinveyClientRequestInitializer : KinveyClientRequestInitializer("appkey", "appsecret", KinveyHeaders()) {
        var isCalled = false
        override fun initialize(request: AbstractKinveyClientRequest<*>) {
            isCalled = true
        }
    }
}