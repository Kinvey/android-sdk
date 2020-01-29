package com.kinvey.androidTest.model

import com.google.api.client.util.Key

data class PersonOver63CharsInFieldName(
    @Key(LONG_NAME)
    private val list: List<String?>? = null
) : Person()