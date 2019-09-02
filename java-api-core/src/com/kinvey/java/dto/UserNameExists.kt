package com.kinvey.java.dto

import com.google.api.client.util.Key

/**
 * Created by yuliya on 06/01/17.
 */

class UserNameExists {
    @Key("usernameExists")
    private var usernameExists: Boolean = false

    fun doesUsernameExist(): Boolean {
        return usernameExists
    }

    fun setUsernameExists(usernameExists: Boolean) {
        this.usernameExists = usernameExists
    }
}
