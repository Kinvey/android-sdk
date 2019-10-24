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

package com.kinvey.android

/*
 * Copyright (c) 2014, Kinvey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialStore

/**
 * Implementation of a [com.kinvey.java.auth.CredentialStore] utilizing a private instance of Android's Shared Preferences
 *
 *
 * @author edwardf
 */
class SharedPrefCredentialStore(private val context: Context) : CredentialStore {

    @Throws(IOException::class)
    override fun load(userId: String?): Credential? {
        val pref = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE)
        val id = pref.getString(PREF_ID, null)
        val auth = pref.getString(PREF_AUTH, null)
        val refresh = pref.getString(PREF_REFRESH, null)
        return if (id != null && auth != null) {
            Credential(id, auth, refresh)
        } else null
    }

    @Throws(IOException::class)
    override fun store(userId: String?, credential: Credential?) {
        val edit = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE).edit()
        edit.putString(PREF_ID, userId)
        edit.putString(PREF_AUTH, credential?.authToken)
        edit.putString(PREF_REFRESH, credential?.refreshToken)
        edit.commit()
    }

    override fun delete(userId: String?) {
        val edit = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE).edit()
        edit.remove(PREF_ID)
        edit.remove(PREF_AUTH)
        edit.remove(PREF_REFRESH)
        edit.commit()


    }

    companion object {
        private const val PREF_STORE = "kinvey_shared_preferences_"
        private const val PREF_ID = "kinvey_id"
        private const val PREF_AUTH = "kinvey_auth"
        private const val PREF_REFRESH = "kinvey_refresh"
    }
}
