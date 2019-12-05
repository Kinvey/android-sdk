package com.kinvey.androidTest.store.data.request

import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.sync.KinveyPushBatchResponse
import com.kinvey.androidTest.model.Person
import com.kinvey.java.model.KinveyBatchInsertError
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class KinveyPushBatchResponseTest {

    @Test
    fun testKinveyPushBatchResponseProperties() {
        val response = KinveyPushBatchResponse()
        val entities = listOf(Person())
        val errors = listOf(KinveyBatchInsertError())
        response.entities = entities
        assertEquals(entities, response.entities)
        response.errors = errors
        assertEquals(errors, response.errors)
    }
}
