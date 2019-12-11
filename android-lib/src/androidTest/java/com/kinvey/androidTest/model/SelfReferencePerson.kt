package com.kinvey.androidTest.model

import com.google.api.client.util.Key

/**
 * Created by yuliya on 10/20/17.
 */

class SelfReferencePerson(
   @Key("selfReferencePerson")
   var person: SelfReferencePerson? = null
): Person() {
    constructor(username: String?): this() {
        this.username = username
    }
}