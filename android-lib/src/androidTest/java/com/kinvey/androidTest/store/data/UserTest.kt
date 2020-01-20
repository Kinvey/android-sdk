package com.kinvey.androidTest.store.data

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.java.core.KinveyClientCallback
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class UserTest {

    private var client: Client<User>? = null
    private var mockContext: Context?  = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        mockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mockContext).build()
    }

    @Test
    fun testUserUpdateConstructor() {
        val callback = object: KinveyClientCallback<User> {
            override fun onSuccess(result: User?) {}
            override fun onFailure(error: Throwable?) {}
        }
        val update = User.Update(callback)
        Assert.assertEquals(update?.callback, callback)
    }

    @Test
    fun testUserRegisterLiveServiceConstructor() {
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {}
            override fun onFailure(error: Throwable?) {}
        }
        val registerLiveService = User.RegisterLiveService(callback)
        Assert.assertEquals(registerLiveService?.callback, callback)
    }

    @Test
    fun testUserUnregisterLiveServiceConstructor() {
        val callback = object: KinveyClientCallback<Void> {
            override fun onSuccess(result: Void?) {}
            override fun onFailure(error: Throwable?) {}
        }
        val unregisterLiveService = User.UnregisterLiveService(callback)
        Assert.assertEquals(unregisterLiveService?.callback, callback)
    }
}