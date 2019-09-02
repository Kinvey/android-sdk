package com.kinvey.java.store


import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.dto.LiveServiceRegisterResponse

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

object BaseUserStore {

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> signUp(userId: String, password: String, user: T, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).createBlocking(userId, password, user).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> signUp(userId: String, password: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).createBlocking(userId, password).execute()
    }

    /*Deletes a 'BaseUser'*/
    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> destroy(isHard: Boolean, client: AbstractClient<T>) {
        UserStoreRequestManager<T>(client, createBuilder(client)).deleteBlocking(isHard).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> login(username: String, password: String,
                             client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginBlocking(username, password).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> login(client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginBlocking().execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginFacebook(accessToken: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginGoogle(accessToken: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginTwitter(accessToken: String, accessSecret: String, consumerKey: String,
                                    consumerSecret: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginLinkedIn(accessToken: String, accessSecret: String, consumerKey: String,
                                     consumerSecret: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginAuthLink(accessToken: String, refreshToken: String,
                                     client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginSalesForce(accessToken: String, clientId: String,
                                       refreshToken: String, id: String,
                                       client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginMobileIdentity(authToken: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> login(credential: Credential, client: AbstractClient<T>?): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).login(credential).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> loginKinveyAuthToken(userId: String, authToken: String, client: AbstractClient<T>): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> logout(client: AbstractClient<T>?) {
        UserStoreRequestManager<T>(client, createBuilder(client)).logout().execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> sendEmailConfirmation(client: AbstractClient<T>) {
        UserStoreRequestManager<T>(client, createBuilder(client)).sendEmailVerificationBlocking().execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> resetPassword(usernameOrEmail: String, client: AbstractClient<T>) {
        UserStoreRequestManager<T>(client, createBuilder(client)).resetPasswordBlocking(usernameOrEmail).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> changePassword(password: String, client: AbstractClient<T>) {
        UserStoreRequestManager<T>(client, createBuilder(client)).changePassword(password).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> convenience(client: AbstractClient<T>?): T {
        return UserStoreRequestManager<T>(client, createBuilder(client)).retrieveMetadataBlocking()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> retrieve(client: AbstractClient<T>): T? {
        return UserStoreRequestManager<T>(client, createBuilder(client)).retrieveBlocking().execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> retrieve(query: Query, client: AbstractClient<T>): List<T> {
        return ArrayList(Arrays.asList(*(UserStoreRequestManager<T>(client, createBuilder(client)).retrieveBlocking(query).execute() as Array<T>?)!!))
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> retrieve(resolves: Array<String>, client: AbstractClient<T>): T? {
        return UserStoreRequestManager<T>(client, createBuilder(client)).retrieveBlocking(resolves).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> retrieve(query: Query, resolves: Array<String>, client: AbstractClient<T>): List<T> {
        return ArrayList(Arrays.asList(*( UserStoreRequestManager<T>(client, createBuilder(client)).retrieveBlocking(query, resolves).execute() as Array<T>?)!!))
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> forgotUsername(client: AbstractClient<T>, email: String) {
        UserStoreRequestManager<T>(client, createBuilder(client)).forgotUsername(email).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> exists(username: String, client: AbstractClient<T>): Boolean {
        return UserStoreRequestManager<T>(client, createBuilder(client)).exists(username).execute()!!.doesUsernameExist()
    }

    @Throws(IOException::class)
    @JvmStatic
    operator fun <T : BaseUser> get(userId: String, client: AbstractClient<T>): T? {
        return UserStoreRequestManager<T>(client, createBuilder(client)).getUser(userId).execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> save(client: AbstractClient<T>): T? {
        return UserStoreRequestManager<T>(client, createBuilder(client)).save().execute()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> update(): T? {
        val client = AbstractClient.sharedInstance as AbstractClient<T>
        return UserStoreRequestManager<T>(client, createBuilder(client)).save().execute()
    }

    /**
     * Register the active user for LiveService messaging.
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> registerLiveService() {
        if (AbstractClient.sharedInstance?.activeUser == null) {
            throw KinveyException("User object has to be the active user in order to register for LiveService messages")
        }
        if (LiveServiceRouter.instance?.isInitialized == false) {
            val response = UserStoreRequestManager<T>(AbstractClient.sharedInstance,
            createBuilder(AbstractClient.sharedInstance))
            .liveServiceRegister(AbstractClient.sharedInstance?.activeUser?.id ?: "",
            AbstractClient.sharedInstance?.deviceId ?: "").execute()
            LiveServiceRouter.instance?.initialize(
                    response?.userChannelGroup,
                    response?.publishKey,
                    response?.subscribeKey,
                    AbstractClient.sharedInstance?.activeUser?.authToken,
                    AbstractClient.sharedInstance as AbstractClient)
        }
    }

    /**
     * Unregister the active user from LiveService messaging.
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun <T : BaseUser> unRegisterLiveService() {
        if (AbstractClient.sharedInstance?.activeUser == null) {
            throw KinveyException("User object has to be the active user in order to register for LiveService messages")
        }
        if (LiveServiceRouter.instance?.isInitialized == true) {
            LiveServiceRouter.instance?.uninitialize()
            UserStoreRequestManager<T>(AbstractClient.sharedInstance,
            createBuilder(AbstractClient.sharedInstance))
            .liveServiceUnregister(AbstractClient.sharedInstance?.activeUser?.id ?: "",
            AbstractClient.sharedInstance?.deviceId ?: "").execute()
        }
    }

    private fun <T : BaseUser> createBuilder(client: AbstractClient<*>?): KinveyAuthRequest.Builder<T>? {
        client?.let { c ->
            val appKey = (c.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
            val appSecret = (c.kinveyRequestInitializer as KinveyClientRequestInitializer).appSecret
            return KinveyAuthRequest.Builder(c.requestFactory.transport,
                    c.jsonFactory, c.baseUrl, appKey, appSecret, null)
        }
        return null
    }

}
