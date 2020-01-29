package com.kinvey.androidTest.model

import com.google.api.client.util.Key

data class PersonLongListName(
    @Key("sub_industry_ids")
    var list: List<String>? = null,
    @Key("author_list_test_field")
    var authors: List<Author>? = null
) : Person()