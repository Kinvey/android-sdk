package com.kinvey.androidTest.store.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kinvey.android.AndroidUserStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidUserStoreTest {

    private val testId = "testId"

    private var mockContext: Context? = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        mockContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testAndroidUserStoreConstructor() {
        val userStore = AndroidUserStore(mockContext)
        userStore.user = testId
        assertEquals(testId, userStore.user)
    }

    @Test
    fun testStorePersistence() {
        val userStore = AndroidUserStore(mockContext)
        userStore.user = testId
        assertEquals(testId, userStore.user)
        userStore.clear()

        val userStore1 = AndroidUserStore(mockContext)
        assertEquals(testId, userStore1.user)
    }
}