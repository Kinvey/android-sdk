package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.kinvey.java.Query
import com.kinvey.java.model.KinveyReadResponse

class PullTaskResponse<T: GenericJson>(val kinveyReadResponse: KinveyReadResponse<T>?, val query: Query)
