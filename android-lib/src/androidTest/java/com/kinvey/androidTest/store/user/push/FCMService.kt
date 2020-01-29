package com.kinvey.androidTest.store.user.push

import com.kinvey.android.push.KinveyFCMService

class FCMService : KinveyFCMService() {
    private fun displayNotification(message: String?) {}
    override fun onMessage(r: String?) {}
}