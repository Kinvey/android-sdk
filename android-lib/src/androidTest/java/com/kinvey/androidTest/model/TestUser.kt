package com.kinvey.androidTest.model

import com.google.api.client.util.Key
import com.kinvey.android.model.User

class TestUser(
    @Key("companyName")
    var companyName: String? = null,
    @Key
    var internalUserEntity: InternalUserEntity? = null
) : User()