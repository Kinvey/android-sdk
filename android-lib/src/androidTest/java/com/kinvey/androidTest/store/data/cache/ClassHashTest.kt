package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.kinvey.android.cache.ClassHash.createScheme
import com.kinvey.android.cache.ClassHash.getClassHash
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmConfiguration.Builder
import io.realm.RealmSchema
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ClassHashTest {

    var context: Context? = null

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Realm.init(context)
    }

    @Test
    fun testHashNotFail() {
        //Correct case
        getClassHash(object : GenericJson() {
            @Key("_id")
            private val _id: String? = null
        }.javaClass)

        //Correct case
        getClassHash(object : GenericJson() {
            @Key("_id")
            protected var _id: String? = null
        }.javaClass)

        //Correct case
        getClassHash(object : GenericJson() {
            @Key("_id")
            var _id: String? = null
        }.javaClass)

        //Correct case Object should be skipped
        getClassHash(object : GenericJson() {
            @Key("_id")
            var _id: Any? = null
        }.javaClass)

        //Correct case Context should be skipped
        getClassHash(object : GenericJson() {
            @Key("_id")
            var _id: Context? = null
        }.javaClass)

        //Correct case field without annotation should be skipped
        getClassHash(object : GenericJson() {
            var _id: String? = null
        }.javaClass)
    }

    @Test
    fun testHashShouldMatch() {
        //Correct case
        assertEquals(
                getClassHash(object : GenericJson() {
                    @Key("_id")
                    private val _id: String? = null
                }.javaClass),
                getClassHash(object : GenericJson() {
                    @Key("_id")
                    protected var _id: String? = null
                    var test: Any? = null
                }.javaClass)
        )
        assertNotEquals(
                getClassHash(object : GenericJson() {
                    @Key("_id")
                    private val _id: String? = null
                }.javaClass),
                getClassHash(object : GenericJson() {
                    @Key("_id")
                    protected var _id: String? = null
                    @Key("_test")
                    var test: String? = null
                }.javaClass)
        )
    }

    @Test
    fun testInnerObjects() {
        val rc = Builder()
                .name("test_inner")
                .build()
        val realm: DynamicRealm = DynamicRealm.getInstance(rc)
        realm.beginTransaction()
        try {
            createScheme("sample", realm, SampleGsonWithInner::class.java)
        } catch (e: Exception) {
            realm.commitTransaction()
        }
        val schema: RealmSchema? = realm.schema
        assertNotNull(schema)
    }
}