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
import android.os.AsyncTask
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.google.common.base.Preconditions
import com.kinvey.java.Logger
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialStore
import java.io.*
import java.util.*

/**
 * @author mjsalinger
 * @since 2.0
 */
class AndroidCredentialStore(context: Context) : CredentialStore {
    private var credentials: HashMap<String, Credential>
    private val appContext: Context
    /** {@inheritDoc}  */
    @Throws(IOException::class)
    override fun load(userId: String): Credential {
        return credentials[userId]!!
    }

    /** {@inheritDoc}  */
    @Throws(IOException::class)
    override fun store(userId: String, credential: Credential) {
        Preconditions.checkNotNull(credential, "credential must not be null")
        Preconditions.checkNotNull(userId, "userId must not be null")
        credentials[userId] = credential
        persistCredentialStore()
    }

    /** {@inheritDoc}  */
    override fun delete(userId: String) {
        credentials.remove(userId)
        persistCredentialStore()
    }

    private fun persistCredentialStore() {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            PersistCredentialStore(appContext, credentials).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            PersistCredentialStore(appContext, credentials).execute()
        }
    }

    @Throws(ClassNotFoundException::class)
    private fun retrieveCredentialStore() {
        var fIn: FileInputStream? = null
        var objIn: ObjectInputStream? = null
        if (appContext.getFileStreamPath("kinveyCredentials.bin").exists()) {
            try {
                fIn = appContext.openFileInput("kinveyCredentials.bin")
                objIn = ObjectInputStream(fIn)
                credentials = objIn.readObject() as HashMap<String, Credential>
            } catch (ex: IOException) {
                Logger.WARNING("Corrupt credential store detected")
            } finally {
                try {
                    fIn?.close()
                    objIn?.close()
                } catch (ioe: IOException) {
                    Logger.WARNING("Could not clean up resources")
                }
            }
        } else {
            persistCredentialStore()
        }
    }

    private class PersistCredentialStore(val context: Context, val credentials: HashMap<String, Credential>)
        : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg args: Void?): Void? {
            try {
                val fStream: FileOutputStream = context.openFileOutput("kinveyCredentials.bin", Context.MODE_PRIVATE)
                val oStream = ObjectOutputStream(fStream)
                oStream.writeObject(credentials)
                oStream.flush()
                fStream.fd.sync()
                oStream.close()
                Logger.INFO("Serialization success")
            } catch (e: Exception) {
                Logger.ERROR("Error on persisting credential store")
            }
            return null
        }
    }

    companion object {
        private val TAG = AndroidCredentialStore::class.java.simpleName
    }

    init {
        appContext = context.applicationContext
        credentials = HashMap()
        try {
            retrieveCredentialStore()
        } catch (ex: ClassNotFoundException) {
            credentials = HashMap()
            persistCredentialStore()
            throw AndroidCredentialStoreException("Credential store corrupted and was rebuilt")
        }
    }
}
