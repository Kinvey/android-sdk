package com.kinvey.java.store


import com.kinvey.java.Query
import com.kinvey.java.model.KinveyReadResponse

class PullTaskResponse<T>(val kinveyReadResponse: KinveyReadResponse<T>, val query: Query)
