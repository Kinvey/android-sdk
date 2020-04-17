package com.kinvey.androidTest.store.data.cache

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.json.GenericJson
import com.google.api.client.util.FieldInfo
import com.google.api.client.util.Key
import com.kinvey.android.cache.ClassHash.checkAclKmdFields
import com.kinvey.android.cache.ClassHash.createScheme
import com.kinvey.android.cache.ClassHash.getClassHash
import com.kinvey.android.cache.ClassHash.isAllowed
import com.kinvey.android.cache.ClassHash.migration

import io.realm.DynamicRealm
import io.realm.Realm
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

    @Test
    fun testInnerObjectsRename() {
        var isException = false
        val rc = Builder()
                .name("test_inner_second")
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
        try {
            checkAclKmdFields("test", realm, SampleGsonWithInner::class.java)
            migration("test", realm, SampleGsonWithInner::class.java)
        } catch (e: java.lang.Exception) {
            isException = true
        }
        assertTrue(isException)
    }

    @Test
    fun testInnerObjectsRdename() {
        val d = com.kinvey.androidTest.model.PersonArray::class.java.declaredFields
        val allowed = isAllowed(FieldInfo.of(d.get(0)))
        assertTrue(allowed)
    }
}