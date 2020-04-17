package com.kinvey.androidTest.store.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.LinkedDataStore
import com.kinvey.androidTest.store.data.network.LinkedPerson
import com.kinvey.java.store.StoreType
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class LinkedDataStoreTest {

    private var client: Client<*>? = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
    }

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
    fun testConstructor() {
        val linkedDataStore = LinkedDataStore(client!!, LinkedPerson.COLLECTION, LinkedPerson::class.java, StoreType.NETWORK)
        assertNotNull(linkedDataStore)
    }
}