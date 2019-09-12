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

import android.content.Context
import android.content.SharedPreferences

import com.kinvey.java.auth.ClientUser

/**
 * @author mjsalinger
 * @since 2.0
 */
internal class AndroidUserStore private constructor(context: Context?) : ClientUser {
    private var userID: String? = null
    var userPreferences: SharedPreferences?
    var appContext: Context? = null

    init {
        appContext = context?.applicationContext
        userPreferences = appContext?.getSharedPreferences(
                appContext?.packageName, Context.MODE_PRIVATE)

        userID = userPreferences?.getString("userID", "")
    }

    private fun persistData() {
        val editor = userPreferences?.edit()
        editor?.putString("userID", userID)
        editor?.commit()
    }

    /** {@inheritDoc}  */
    override fun setUser(userID: String) {
        this.userID = userID
        persistData()
    }

    /** {@inheritDoc}  */
    override fun getUser(): String? {
        return userID
    }

    override fun clear() {
        userID = null
    }

    companion object {
        private var _instance: AndroidUserStore? = null

        @JvmStatic
        fun getUserStore(context: Context?): AndroidUserStore {
            if (_instance == null) {
                _instance = AndroidUserStore(context)
            }
            return _instance!!
        }
    }

}
