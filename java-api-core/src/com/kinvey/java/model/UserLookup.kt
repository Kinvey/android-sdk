/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.java.model

/**
 * @author mjsalinger
 * @since 2.0
 */

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

/**
 * Construct a user lookup object via [com.kinvey.java.UserDiscovery.userLookup].
 *
 *
 * After configuring the lookup set it using [com.kinvey.java.UserDiscovery.lookupBlocking]
 */
data class UserLookup(
    @Key("_id")
    var id: String? = null,
    @Key
    var username: String? = null,
    @Key("first_name")
    var firstName: String? = null,
    @Key("last_name")
    var lastName: String? = null,
    @Key
    var email: String? = null,
    @Key("_socialIdentity.facebook.id")
    var facebookID: String? = null
) : GenericJson()
