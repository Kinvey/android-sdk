package com.kinvey.androidTest.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

open class Person() : GenericJson() {

    constructor(username: String?): this() {
        this.username = username
    }

    constructor(id: String?, username: String?): this() {
        this.id = id
        this.username = username
    }

    @Key
    var age: String? = null
    @Key("_id")
    var id: String? = null
    @Key
    var height = 0f
    @Key
    var author: Author? = null
    @Key
    var weight: Long = 0
    @Key
    var intVal = 0
    @Key
    var carNumber: Int? = null
    @Key("username")
    var username: String? = null
    @Key("_geoloc")
    var geoloc: String? = null

    companion object {
        const val COLLECTION = "Persons"
        const val DELTA_SET_COLLECTION = "QuerySyncCollection"
        const val TEST_COLLECTION = "TestCollection"
        const val USER_STORE = "UserStore"
        const val DELTA_SET_OFF_COLLECTION = "DeltaSetNotEnabled"
        const val COLLECTION_WITH_EXCEPTION = "CollectionWithException"
        const val USERNAME_KEY = "username"
        const val LONG_NAME = "LoremIpsumissimplydummytextoftheprintingandtypesettingindustry"
    }
}