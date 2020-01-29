package com.kinvey.androidTest.store.data.network

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.network.AndroidNetworkManager
import com.kinvey.androidTest.model.Person
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidNetworkManagerTest {

    private var client: Client<*>? = null

    @After
    fun tearDown() {
        client?.performLockDown()
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Test
    fun testIsOnline() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        val androidNetworkManager = AndroidNetworkManager(Person.COLLECTION, Person::class.java, client)
        assertNotNull(androidNetworkManager)
        assertTrue(androidNetworkManager.isOnline)
    }
}