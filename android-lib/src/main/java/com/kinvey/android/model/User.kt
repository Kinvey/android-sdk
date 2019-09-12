package com.kinvey.android.model

import com.kinvey.android.AsyncClientRequest
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.BaseUserStore

import java.io.IOException

/**
 * Created by yuliya on 06/07/17.
 */

open class User : BaseUser() {

    fun <T: User> update(callback: KinveyClientCallback<T>) {
        Update(callback).execute()
    }

    fun registerLiveService(callback: KinveyClientCallback<Void>) {
        RegisterLiveService(callback).execute()
    }

    fun unregisterLiveService(callback: KinveyClientCallback<Void>) {
        UnregisterLiveService(callback).execute()
    }

    private class Update<T : User> (callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T? {
            return BaseUserStore.update()
        }
    }

    private class RegisterLiveService(callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.registerLiveService<User>()
            return null
        }
    }

    private class UnregisterLiveService(callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.unRegisterLiveService<User>()
            return null
        }
    }
}
