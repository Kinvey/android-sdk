package com.kinvey.java.auth

import com.kinvey.java.core.KinveyHeaders
import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.UserStoreRequestManager
import com.kinvey.java.store.UserStoreRequestManager.LoginRequest
import com.kinvey.java.testing.MockKinveyAuthRequest.MockBuilder
import junit.framework.TestCase
import java.lang.reflect.Field

/**
 * Created by edward on 10/13/15.
 */
class KinveyAuthRequestTest : KinveyMockUnitTest<BaseUser>() {

    private var currentUser: UserStoreRequestManager<*>? = null

    private fun initializeUser() {
        currentUser = UserStoreRequestManager(client, MockBuilder(client?.requestFactory?.transport,
            client?.jsonFactory, "mockAppKey", "mockAppSecret", null) as KinveyAuthRequest.Builder<BaseUser>)
    }

    fun testHeaders() {
        initializeUser()
        try {
            val login = currentUser?.loginBlocking()
            login?.buildAuthRequest()
            var f: Field? = LoginRequest::class.java.getDeclaredField("request")
            f?.isAccessible = true
            val authRequest = f?.get(login) as KinveyAuthRequest<*>
            f = KinveyAuthRequest::class.java.getDeclaredField("kinveyHeaders")
            f.isAccessible = true
            val requestHeaders = f.get(authRequest) as KinveyHeaders
            TestCase.assertFalse(requestHeaders.containsKey("Authorization"))
        } catch (e: Exception) {
            TestCase.assertNull(e)
//          assertFalse("Shouldn't have thrown an exception", true);
        }
    }
}