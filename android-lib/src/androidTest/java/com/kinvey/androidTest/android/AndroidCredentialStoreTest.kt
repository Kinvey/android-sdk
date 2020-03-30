package com.kinvey.androidTest.android

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.AndroidCredentialStore
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialStore
import io.mockk.spyk
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidCredentialStoreTest {

    lateinit var store: CredentialStore
    var context: Context? = null

    val userId: String = "userId"
    val token: String = "test token"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation()?.context
        store = spyk(AndroidCredentialStore(context), recordPrivateCalls = true)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testLoad() {
        val creds = Credential.Companion.from(userId, token)
        store?.store(userId, creds)
        //verifySequence {
        //    store?.store(userId, creds)
        //    store["persistCredentialStore"]()
        //}
        //verify { store["persistCredentialStore"]() }
        val result = store?.load(userId)
        assertNotNull(result)
    }

    @Test
    fun testStore() {
        store?.store(userId, Credential.Companion.from(userId, token))
        //verify { store?.store(userId, Credential.Companion.from(userId, token)) }
        val result = store?.load(userId)
        assertNotNull(result)
    }

    @Test
    fun testDelete() {
        store?.store(userId, Credential.Companion.from(userId, token))
        val resultStore = store?.load(userId)
        assertNotNull(resultStore)

        val latch = CountDownLatch(1)
        Thread(Runnable {
            store?.delete(userId)
            Thread.sleep(500)
            latch.countDown()
        }).start()
        latch.await()

        val result = store?.load(userId)
        assertNotEquals(result?.userId, userId)
    }
}