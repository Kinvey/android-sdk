package com.kinvey.android.store


import android.content.Intent
import android.net.Uri

import com.google.api.client.json.GenericJson
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyListCallback
import com.kinvey.android.callback.KinveyMICCallback
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.callback.KinveyUserDeleteCallback
import com.kinvey.android.callback.KinveyUserListCallback
import com.kinvey.android.callback.KinveyUserManagementCallback
import com.kinvey.android.model.User
import com.kinvey.android.ui.MICLoginActivity
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.BaseUserStore
import com.kinvey.java.store.UserStoreRequestManager

import java.io.IOException
import java.lang.RuntimeException

class UserStore {

    /**
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.  This method is provided
     * to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @param callback [KinveyUserCallback] that contains a valid logged in user
     */
    @Deprecated("use {@link UserStore#login(String, String, AbstractClient, KinveyClientCallback<T>)}")
    fun <T : User> loginKinveyAuthToken(userId: String, authToken: String,
                                        client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
        LoginKinveyAuth(userId, authToken, client, callback).execute()
    }

    /**
     * Asynchronous Update current user info
     *
     */
    @Deprecated("use {@link User#update(KinveyClientCallback)} ()} instead.")
    fun <T : User> save(client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
        Update(client, callback).execute()
    }


    private class Login<T : BaseUser> : AsyncClientRequest<T> {

        internal lateinit var username: String
        internal lateinit var password: String
        internal lateinit var accessToken: String
        internal lateinit var refreshToken: String
        internal lateinit var accessSecret: String
        internal lateinit var consumerKey: String
        internal lateinit var consumerSecret: String
        internal lateinit var credential: Credential
        internal var type: UserStoreRequestManager.LoginType
        internal var client: AbstractClient<T>

        //Salesforce...
        internal lateinit var id: String
        internal lateinit var client_id: String

        internal constructor(client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {

            this.client = client
            this.type = UserStoreRequestManager.LoginType.IMPLICIT
        }

        internal constructor(username: String, password: String, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.username = username
            this.password = password

            this.client = client
            this.type = UserStoreRequestManager.LoginType.KINVEY
        }

        internal constructor(accessToken: String, type: UserStoreRequestManager.LoginType, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.accessToken = accessToken
            this.type = type

            this.client = client
        }

        internal constructor(accessToken: String, refreshToken: String, type: UserStoreRequestManager.LoginType, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            this.type = type

            this.client = client
        }

        internal constructor(accessToken: String, accessSecret: String, consumerKey: String, consumerSecret: String, client: AbstractClient<T>,
                            type: UserStoreRequestManager.LoginType, callback: KinveyClientCallback<T>) : super(callback) {
            this.accessToken = accessToken
            this.accessSecret = accessSecret
            this.consumerKey = consumerKey
            this.consumerSecret = consumerSecret
            this.type = type
            this.client = client
        }

        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        internal constructor(accessToken: String, clientId: String, refresh: String, id: String, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.accessToken = accessToken
            this.refreshToken = refresh
            this.client_id = clientId
            this.id = id

            this.client = client
            this.type = UserStoreRequestManager.LoginType.SALESFORCE
        }

        internal constructor(credential: Credential, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.credential = credential

            this.client = client
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE
        }

        @Throws(IOException::class)
        override fun executeAsync(): T {
            when (this.type) {
                UserStoreRequestManager.LoginType.IMPLICIT -> return BaseUserStore.login(client)
                UserStoreRequestManager.LoginType.KINVEY -> return BaseUserStore.login(username, password, client)
                UserStoreRequestManager.LoginType.FACEBOOK -> return BaseUserStore.loginFacebook(accessToken, client)
                UserStoreRequestManager.LoginType.GOOGLE -> return BaseUserStore.loginGoogle(accessToken, client)
                UserStoreRequestManager.LoginType.TWITTER -> return BaseUserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client)
                UserStoreRequestManager.LoginType.LINKED_IN -> return BaseUserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client)
                UserStoreRequestManager.LoginType.AUTH_LINK -> return BaseUserStore.loginAuthLink(accessToken, refreshToken, client)
                UserStoreRequestManager.LoginType.SALESFORCE -> return BaseUserStore.loginSalesForce(accessToken, client_id, refreshToken, id, client)
                UserStoreRequestManager.LoginType.MOBILE_IDENTITY -> return BaseUserStore.loginMobileIdentity(accessToken, client)
                UserStoreRequestManager.LoginType.CREDENTIALSTORE -> return BaseUserStore.login(credential, client)
                else -> {
                    throw RuntimeException("UserStoreRequestManager.LoginType ${this.type.toString()} not handled")
                }
            }

        }
    }

    private class Create<T : User> internal constructor(internal var username: String, internal var password: String, private val user: T?, private val client: AbstractClient<T>, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            return if (user == null) {
                BaseUserStore.signUp(username, password, client)
            } else {
                BaseUserStore.signUp(username, password, user, client)
            }
        }
    }

    private class Delete internal constructor(internal var hardDelete: Boolean, private val client: AbstractClient<BaseUser>, callback: KinveyUserDeleteCallback) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.destroy(hardDelete, client)
            return null
        }
    }

    private class Logout internal constructor(private val client: AbstractClient<BaseUser>, callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.logout(client)
            return null
        }
    }

    private class PostForAccessToken<T : User>(private val client: AbstractClient<T>, private val redirectURI: String?, private val token: String, private val clientId: String?, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            val requestManager = UserStoreRequestManager(client, createBuilder(client))
            requestManager.micRedirectURI = redirectURI
            val result = requestManager.getMICToken(token, clientId).execute()

            val ret = BaseUserStore.loginMobileIdentity(result!!["access_token"]!!.toString(), client)

            val currentCred = client.store?.load(client.activeUser?.id)
            if (result[REFRESH_TOKEN] != null) {
                currentCred?.refreshToken = result["refresh_token"]!!.toString()
            }
            currentCred?.clientId = clientId
            client.store?.store(client.activeUser?.id, currentCred)

            return ret
        }
    }

    private class PostForOAuthToken<T : User>(private val client: AbstractClient<T>, private val clientId: String?, private val redirectURI: String?, internal var username: String, internal var password: String, callback: KinveyUserCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            val requestManager = UserStoreRequestManager(client, createBuilder(client))
            requestManager.micRedirectURI = redirectURI
            val result = requestManager.getOAuthToken(clientId, username, password).execute()
            val ret = BaseUserStore.loginMobileIdentity(result!![ACCESS_TOKEN]!!.toString(), client)
            val currentCred = client.store?.load(client.activeUser?.id)
            if (result[REFRESH_TOKEN] != null) {
                currentCred?.refreshToken = result[REFRESH_TOKEN]!!.toString()
            }
            currentCred?.clientId = clientId
            client.store?.store(client.activeUser?.id, currentCred)
            return ret
        }
    }


    private class Retrieve<T : User> : AsyncClientRequest<T> {

        private var resolves: Array<String>? = null
        private val client: AbstractClient<T>

        internal constructor(client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.client = client
        }

        internal constructor(resolves: Array<String>, client: AbstractClient<T>, callback: KinveyClientCallback<T>) : super(callback) {
            this.resolves = resolves
            this.client = client
        }

        @Throws(IOException::class)
        public override fun executeAsync(): T? {
            return if (resolves == null) {
                BaseUserStore.retrieve(client)
            } else {
                BaseUserStore.retrieve(resolves!!, client)
            }
        }
    }

    private class RetrieveUserList<T : BaseUser> : AsyncClientRequest<List<T>> {

        private var query: Query? = null
        private val client: AbstractClient<T>
        private var resolves: Array<String>? = null

        internal constructor(query: Query, client: AbstractClient<T>, callback: KinveyListCallback<T>) : super(callback) {
            this.query = query
            this.client = client
        }

        internal constructor(query: Query, resolves: Array<String>, client: AbstractClient<T>, callback: KinveyListCallback<T>) : super(callback) {
            this.query = query
            this.resolves = resolves
            this.client = client
        }

        @Throws(IOException::class)
        public override fun executeAsync(): List<T> {
            return if (resolves == null) {
                BaseUserStore.retrieve(query!!, client)
            } else {
                BaseUserStore.retrieve(query!!, resolves!!, client)
            }
        }
    }

    private class RetrieveUserArray<T : User> internal constructor(query: Query, resolves: Array<String>, private val client: AbstractClient<T>, callback: KinveyClientCallback<Array<T>>) : AsyncClientRequest<Array<T>>(callback) {

        private var query: Query? = null
        private var resolves: Array<String>? = null

        init {
            this.query = query
            this.resolves = resolves
        }

        @Throws(IOException::class)
        override fun executeAsync(): Array<T> {
            val users =  BaseUserStore.retrieve(query!!, resolves!!, client).map { it as User }
            return users.toTypedArray() as Array<T>
        }
    }

    private class RetrieveMetaData<T : User> internal constructor(private val client: AbstractClient<T>, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            return BaseUserStore.convenience(client)
        }
    }

    private class Update<T : User> internal constructor(client: AbstractClient<T>, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        internal var client: AbstractClient<T>? = null


        init {
            this.client = client

        }

        @Throws(IOException::class)
        override fun executeAsync(): T? {
            return BaseUserStore.save(client!!)
        }
    }

    private class ChangePassword internal constructor(private val password: String, client: AbstractClient<*>, callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {
        internal var client: AbstractClient<*>? = null


        init {
            this.client = client

        }

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.changePassword(password, client!!)
            return null
        }
    }


    private class ResetPassword internal constructor(internal var usernameOrEmail: String, private val client: AbstractClient<*>, callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.resetPassword(usernameOrEmail, client)
            return null
        }
    }

    private class ExistsUser internal constructor(internal var username: String, private val client: AbstractClient<*>, callback: KinveyClientCallback<Boolean>) : AsyncClientRequest<Boolean>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Boolean? {
            return BaseUserStore.exists(username, client)
        }
    }

    private class GetUser<T : User> internal constructor(internal var userId: String, private val client: AbstractClient<*>, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            return BaseUserStore.get(userId, client) as T
        }
    }

    private class EmailVerification internal constructor(private val client: AbstractClient<*>, callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.sendEmailConfirmation(client)
            return null
        }
    }

    private class ForgotUsername internal constructor(private val client: AbstractClient<*>, private val email: String, callback: KinveyClientCallback<Void>) : AsyncClientRequest<Void>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Void? {
            BaseUserStore.forgotUsername(client, email)
            return null
        }
    }

    private inner class LoginKinveyAuth<T : User> internal constructor(private val userID: String, private val authToken: String, private val client: AbstractClient<T>, callback: KinveyClientCallback<T>) : AsyncClientRequest<T>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): T {
            return BaseUserStore.loginKinveyAuthToken(userID, authToken, client)

        }
    }

    companion object {

        private val clearStorage = true
        private var MICCallback: KinveyUserCallback<*>? = null
        private var MICRedirectURI: String? = null
        private val MICClientId: String? = null
        val ACCESS_TOKEN = "access_token"
        val REFRESH_TOKEN = "refresh_token"

        /**
         * Asynchronous request to signUp.
         *
         *
         * Creates an asynchronous request to create new User at the kinvey backend.
         * If signUp was successful user will be login automatically.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.signUp("userName", "password", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param username [String] the userName of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         */
        @JvmStatic
        fun signUp(username: String, password: String,
                   client: AbstractClient<User>, callback: KinveyClientCallback<User>) {
            signUp(username, password, null, client, callback)
        }

        @JvmStatic
        fun <T : User> signUp(username: String, password: String,
                              user: T?, client: AbstractClient<T>,
                              callback: KinveyClientCallback<T>) {
            Create(username, password, user, client, callback).execute()
        }

        /**
         * Asynchronous request to login the implicit user.
         *
         *
         * Creates an asynchronous request to login at the kinvey backend.
         * Login with the implicit user. If the implicit user does not exist, the user is created.
         * After calling this method, the application should retrieve and store the userID using getId().
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.login(mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> login(client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(client, callback).execute()
        }

        /**
         * Asynchronous request to login with the existing user.
         *
         *
         * Creates an asynchronous request to login new User at kinvey backend.
         * Login with the with existing user.
         * If user does not exist, returns a error response.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.login("userID", "password", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param userId [String] the userId of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> login(userId: String, password: String,
                             client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(userId, password, client, callback).execute()
        }

        /**
         * Asynchronous request to login with the Facebook application.
         *
         *
         * Creates an asynchronous request to login with the Facebook accessToken.
         * Before this request you must authorize in Facebook application and get Facebook access token.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginFacebook("accessToken", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the Facebook access token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)} or {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginFacebook(accessToken: String, client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute()
        }

        /**
         * Asynchronous request to login with the Google application.
         *
         *
         * Creates an asynchronous request to login with the Google accessToken.
         * Before this request you must authorize in Google application and get Google access token.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginGoogle("accessToken", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the Facebook access token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)} or {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginGoogle(accessToken: String, client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, UserStoreRequestManager.LoginType.GOOGLE, client, callback).execute()
        }

        /**
         * Asynchronous request to login with the Twitter application.
         *
         *
         * Creates an asynchronous request to login with the Twitter.
         * Before this request you must authorize in Twitter application and get Twitter accessToken,
         * accessSecret,consumerKey and consumerSecret.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginTwitter("accessToken", "accessSecret", "consumerKey", "consumerSecret", mClient,
         * new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the Twitter access token
         * @param accessSecret [String] the Twitter accessSecret token
         * @param consumerKey [String] the Twitter consumerKey token
         * @param consumerSecret [String] the Twitter consumerSecret token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)} or {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginTwitter(accessToken: String, accessSecret: String,
                                    consumerKey: String, consumerSecret: String,
                                    client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.TWITTER, callback).execute()
        }

        /**
         * Asynchronous request to login with the LinkedIn application.
         *
         *
         * Creates an asynchronous request to login with the LinkedIn.
         * Before this request you must authorize in LinkedIn application and get LinkedIn accessToken,
         * accessSecret,consumerKey and consumerSecret.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginLinkedIn("accessToken", "accessSecret", "consumerKey", "consumerSecret", mClient,
         * new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the LinkedIn access token
         * @param accessSecret [String] the LinkedIn accessSecret token
         * @param consumerKey [String] the LinkedIn consumerKey token
         * @param consumerSecret [String] the LinkedIn consumerSecret token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)} or {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginLinkedIn(accessToken: String, accessSecret: String,
                                     consumerKey: String, consumerSecret: String,
                                     client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.LINKED_IN, callback).execute()
        }

        /**
         * Asynchronous request to login with login link.
         *
         *
         * Creates an asynchronous request to login with login link.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginAuthLink("accessToken", "refreshToken", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the access token
         * @param refreshToken [String] the refresh token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginAuthLink(accessToken: String, refreshToken: String,
                                     client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, refreshToken, UserStoreRequestManager.LoginType.AUTH_LINK, client, callback).execute()
        }

        /**
         * Asynchronous request to login with the SalesForce application.
         *
         *
         * Creates an asynchronous request to login with the SalesForce.
         * Before this request you must authorize in LinkedIn application and get SalesForce accessToken,
         * client_id, refreshToken and id.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginSalesForce("accessToken", "client_id", "refreshToken", "id", mClient,
         * new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the SalesForce access token
         * @param client_id [String] the SalesForce client id
         * @param refreshToken [String] the SalesForce refresh token
         * @param id [String] the SalesForce id
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)} or {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginSalesForce(accessToken: String, client_id: String,
                                       refreshToken: String, id: String,
                                       client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(accessToken, client_id, refreshToken, id, client, UserStoreRequestManager.LoginType.SALESFORCE, callback).execute()
        }

        /**
         * Asynchronous request to login with the MobileIdentity accessToken.
         *
         *
         * Creates an asynchronous request to login with the MobileIdentity accessToken.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.loginMobileIdentity("accessToken", mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param accessToken [String] the MobileIdentity access token
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> loginMobileIdentity(accessToken: String, client: AbstractClient<T>,
                                           callback: KinveyClientCallback<T>) {
            Login(accessToken, UserStoreRequestManager.LoginType.MOBILE_IDENTITY, client, callback).execute()
        }

        /**
         * Asynchronous request to login with kinvey Credential object.
         *
         *
         * Creates an asynchronous request to login with kinvey Credential object.
         * You can get Credential object from CredentialStorage, if user was logged before.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.login(credential, mClient, new KinveyClientCallback<User>() {
         * void onSuccess(User user){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param credential [Credential] the credential of kinvey user
         * @param client [Client] an instance of the client
         * @param callback [<] the callback
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <T : User> login(credential: Credential, client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Login(credential, client, callback).execute()
        }

        /**
         * Synchronous request to logout.
         *
         *
         * Creates an Synchronous request to logout.
         * Storage will be cleared in this request. To keep data in storage need to call keepOfflineStorageOnLogout()
         * before this method.
         * Uses [<] to return a User.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.logout(mClient, new KinveyClientCallback<Vodid>() {
         * void onSuccess(Void aVoid){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param client [Client] an instance of the client
         */
        @JvmStatic
        fun logout(client: AbstractClient<BaseUser>, callback: KinveyClientCallback<Void>) {
            Logout(client, callback).execute()
        }

        /**
         * Asynchronous request to destroy user from kinvey backend.
         *
         *
         * Creates an Asynchronous request to destroy user from kinvey backend.
         * If isHard is true user will be deleted from kinvey backend.
         * If isHard is false user will be disabled from kinvey backend, but it can be enabled again.
         * Uses [KinveyUserDeleteCallback] to return a status of request execution.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.destroy("true", mClient, new KinveyUserDeleteCallback() {
         * void onSuccess(Void aVoid){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param isHard flag for detect hard/soft deleting user
         * @param client [Client] an instance of the client
         * @param callback [KinveyUserDeleteCallback] the callback
         */
        @JvmStatic
        fun destroy(isHard: Boolean, client: AbstractClient<BaseUser>, callback: KinveyUserDeleteCallback) {
            Delete(isHard, client, callback).execute()
        }


        /**
         * Asynchronous request to send email confirmation.
         *
         *
         * Creates an Asynchronous request to send email confirmation.
         * Uses [KinveyUserManagementCallback] to return a status of request execution.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.sendEmailConfirmation(mClient, new KinveyUserManagementCallback() {
         * void onSuccess(Void aVoid){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param client [Client] an instance of the client
         * @param callback [KinveyUserManagementCallback] the callback
         */
        @JvmStatic
        fun sendEmailConfirmation(client: AbstractClient<*>, callback: KinveyUserManagementCallback) {
            EmailVerification(client, callback).execute()
        }

        /**
         * Asynchronous request to forgot username.
         *
         *
         * Creates an Asynchronous request to forgot username.
         * Uses [KinveyUserManagementCallback] to return a status of request execution.
         *
         *
         *
         * Sample Usage:
         * <pre>
         * `UserStore.forgotUsername(mClient, "email", new KinveyUserManagementCallback() {
         * void onSuccess(Void aVoid){...};
         * void onFailure(Throwable t){...};
         * });
        ` *
        </pre> *
         *
         * @param client [Client] an instance of the client
         * @param email [String] a user's email
         * @param callback [KinveyUserManagementCallback] the callback
         */
        @JvmStatic
        fun forgotUsername(client: AbstractClient<*>, email: String, callback: KinveyUserManagementCallback) {
            ForgotUsername(client, email, callback).execute()
        }

        @JvmStatic
        fun resetPassword(usernameOrEmail: String, client: AbstractClient<*>, callback: KinveyUserManagementCallback) {
            ResetPassword(usernameOrEmail, client, callback).execute()
        }

        @JvmStatic
        fun exists(username: String, client: AbstractClient<*>, callback: KinveyClientCallback<Boolean>) {
            ExistsUser(username, client, callback).execute()
        }

        @JvmStatic
        fun changePassword(password: String, client: AbstractClient<*>, callback: KinveyUserManagementCallback) {
            ChangePassword(password, client, callback).execute()
        }

        @JvmStatic
        operator fun <T : User> get(userId: String, client: AbstractClient<*>, callback: KinveyClientCallback<T>) {
            GetUser(userId, client, callback).execute()
        }

        /**
         * Asynchronous Retrieve Metadata
         *
         *
         *
         * Convenience method for retrieving user metadata and updating the current user with the metadata.  Used
         * when initializing the client.
         *
         *
         * @param callback KinveyUserCallback
         */
        @JvmStatic
        fun <T : User> convenience(client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            RetrieveMetaData(client, callback).execute()
        }

        /**
         * Asynchronous Call to Save the current user
         *
         *
         * Constructs an asynchronous request to save the current Kinvey user.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `User user = kinveyClient.getActiveUser();
         * user.update(new KinveyUserCallback() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(User result) { ... }
         * });
        ` *
        </pre> *
         *
         * @param callback [KinveyUserCallback] containing an updated User instance.
         */
        /*    public void update(AbstractClient client,KinveyClientCallback callback) {
        new Update(client, callback).execute();
    }*/

        /**
         * Asynchronous Call to Retrieve (refresh) the current user
         *
         *
         * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `UserStore.retrieve(kinveyClient, new KinveyClientCallback<User> callback() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(User result) { ... }
         * });
        ` *
        </pre> *
         *
         * @param callback [<] containing a refreshed User instance.
         * @param client [Client] an instance of the client
         */
        @JvmStatic
        fun retrieve(client: AbstractClient<User>, callback: KinveyClientCallback<User>) {
            Retrieve(client, callback).execute()
        }

        /**
         * Asynchronous call to retrieve (refresh) the current user, and resolve KinveyReferences
         *
         *
         * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `UserStore.retrieve(new String[]{"myKinveyReferencedField"}, kinveyClient, new KinveyClientCallback<User> callback() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(User result) { ... }
         * });
        ` *
        </pre> *
         *
         * @param resolves an array of json keys maintaining KinveyReferences to be resolved
         * @param client [Client] an instance of the client
         * @param callback [KinveyUserCallback] containing refreshed user instance
         */
        @JvmStatic
        fun <T : User> retrieve(resolves: Array<String>, client: AbstractClient<T>, callback: KinveyClientCallback<T>) {
            Retrieve<T>(resolves, client, callback).execute()
        }

        /**
         * Asynchronous call to retrieve (refresh) the users by query, and resolve KinveyReferences
         *
         *
         * Constructs an asynchronous request to retrieve User objects via a Query.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `UserStore.retrieve(query, new String[]{"myKinveyReferenceField"}, kinveyClient, new KinveyListCallback<User>() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(User[] result) { ... }
         * });
        ` *
        </pre> *
         *
         *
         *
         * @param query [Query] the query to execute defining users to return
         * @param resolves an array of json keys maintaining KinveyReferences to be resolved
         * @param client [Client] an instance of the client
         * @param callback [<] containing an array of queried users
         */
        @JvmStatic
        fun retrieve(query: Query, resolves: Array<String>, client: AbstractClient<User>, callback: KinveyListCallback<User>) {
            RetrieveUserList(query, resolves, client, callback).execute()
        }

        /**
         * Asynchronous Call to Retrieve users via a Query
         *
         *
         * Constructs an asynchronous request to retrieve User objects via a Query.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `UserStore.retrieve(query, kinveyClient, new KinveyListCallback<User>() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(List<User> result) { ... }
         * });
        ` *
        </pre> *
         * @param q [Query] the query to execute defining users to return
         * @param client [Client] an instance of the client
         * @param callback [<] for retrieved users
         */
        @JvmStatic
        fun retrieve(q: Query, client: AbstractClient<User>, callback: KinveyListCallback<User>) {
            RetrieveUserList(q, client, callback).execute()
        }

        /**
         * Asynchronous call to retrieve (refresh) the users by query, and resolve KinveyReferences
         *
         *
         * Constructs an asynchronous request to retrieve User objects via a Query.
         *
         *
         *
         * Sample Usage:
         *
         * <pre>
         * `UserStore.retrieve(query, new String[]{"myKinveyReferenceField"}, kinveyClient, new KinveyUserListCallback() {
         * public void onFailure(Throwable e) { ... }
         * public void onSuccess(User[] result) { ... }
         * });
        ` *
        </pre> *
         *
         *
         *
         * @param query [Query] the query to execute defining users to return
         * @param resolves an array of json keys maintaining KinveyReferences to be resolved
         * @param client [Client] an instance of the client
         * @param callback [com.kinvey.android.callback.KinveyUserListCallback] containing an array of queried users
         *
         */
        @Deprecated("use {@link UserStore#retrieve(Query, String[], AbstractClient, KinveyListCallback)}")
        @JvmStatic
        fun retrieve(query: Query, resolves: Array<String>, client: AbstractClient<User>, callback: KinveyUserListCallback) {
            RetrieveUserArray(query, resolves, client, callback).execute()
        }


        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
         *
         * @param client Client object
         * @param redirectURI redirectURI
         * @param callback KinveyMICCallback
         */
        @Deprecated("Use {@link #loginWithAuthorizationCodeLoginPage(Client, String, String, KinveyMICCallback)}")
        @JvmStatic
        fun loginWithAuthorizationCodeLoginPage(client: Client<*>, /*Class userClass, */redirectURI: String, callback: KinveyMICCallback<*>) {
            loginWithAuthorizationCodeLoginPage(client, null, redirectURI, callback)
        }

        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
         *
         * @param redirectURI redirectURI
         * @param callback KinveyMICCallback
         */
        @Deprecated("Use {@link #loginWithMIC(Client, String, String, KinveyMICCallback)}")
        @JvmStatic
        fun loginWithAuthorizationCodeLoginPage(client: Client<*>, clientId: String?, /*Class userClass, */
                                                redirectURI: String, callback: KinveyMICCallback<*>) {
            loginWithMIC(client, clientId, redirectURI, callback)
        }

        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
         *
         * @param redirectURI redirectURI
         * @param callback KinveyMICCallback
         */
        fun loginWithMIC(client: Client<*>, clientId: String?, /*Class userClass, */
                         redirectURI: String, callback: KinveyMICCallback<*>) {
            //return URL for login page
            //https://auth.kinvey.com/oauth/auth?client_id=<your_app_id>i&redirect_uri=<redirect_uri>&response_type=code
            val appkey = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
            var host = client.micHostName
            val apiVersion = client.micApiVersion
            if (apiVersion != null && apiVersion.length > 0) {
                host = client.micHostName + apiVersion + "/"
            }
            var myURLToRender = host + "oauth/auth?client_id=" + appkey
            if (clientId != null) {
                myURLToRender = "$myURLToRender.$clientId"
            }
            myURLToRender = "$myURLToRender&redirect_uri=$redirectURI&response_type=code&scope=openid"
            ///keep a reference to the callback and redirect uri to use later

            MICCallback = callback
            MICRedirectURI = redirectURI

            callback?.onReadyToRender(myURLToRender)

        }

        /**
         * Used by the MIC login flow, this method should be called after a successful login in the onNewIntent Method of your activity.  See the MIC guide for more information.
         *
         * @param intent The intent provided to the application from the redirect
         * @param clientId ClientId
         * @param client Client object
         */
        @Throws(KinveyException::class)
        @JvmStatic
        fun onOAuthCallbackReceived(intent: Intent?, clientId: String?, client: AbstractClient<User>) {
            if (intent == null || intent.data == null) {
                throw KinveyException("Intent or data from intent from MIC login page is null")
            }
            val uri = intent.data
            val accessToken = uri!!.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            val errorDescription = uri.getQueryParameter("error_description")
            if (accessToken != null && error == null) {
                getMICAccessToken(accessToken, clientId, client)
            } else {
                throw KinveyException("$error: $errorDescription")
            }
        }

        /**
         * Used by the MIC login flow, this method should be called after a successful login in the onNewIntent Method of your activity.  See the MIC guide for more information.
         *
         * @param intent The intent provided to the application from the redirect
         */
        @Deprecated("Use {@link #onOAuthCallbackReceived(Intent, String, AbstractClient)}")
        fun onOAuthCallbackRecieved(intent: Intent?, client: AbstractClient<User>) {
            onOAuthCallbackReceived(intent, null, client)
        }

        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
         *
         * @param username [String] the userName of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param redirectURI redirectURI
         * @param callback [KinveyUserCallback]
         */
        @Deprecated("Use {@link #loginWithAuthorizationCodeAPI(AbstractClient, String, String, String, String, KinveyUserCallback)}")
        @JvmStatic
        fun loginWithAuthorizationCodeAPI(client: AbstractClient<User>, username: String,
                                          password: String, redirectURI: String,
                                          callback: KinveyUserCallback<User>) {
            loginWithAuthorizationCodeAPI(client, username, password, null, redirectURI, callback)
        }

        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
         *
         * @param username [String] the userName of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param redirectURI redirectURI
         * @param callback [KinveyUserCallback]
         */
        @Deprecated("Use {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        @JvmStatic
        fun loginWithAuthorizationCodeAPI(client: AbstractClient<User>, username: String,
                                          password: String, clientId: String?,
                                          redirectURI: String, callback: KinveyUserCallback<User>) {
            loginWithMIC(client, username, password, clientId, redirectURI, callback)
        }

        /***
         *
         * Login with the MIC service, using the resource owner grant flow.
         *
         * @param username [String] the userName of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param callback [KinveyUserCallback]
         */
        fun <T: User> loginWithMIC(client: AbstractClient<T>, username: String,
                         password: String, clientId: String, callback: KinveyUserCallback<T>) {
            MICCallback = callback
            PostForOAuthToken(client, clientId, null, username, password, callback).execute()
        }

        /***
         *
         * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
         *
         * @param username [String] the userName of Kinvey user
         * @param password [String] the password of Kinvey user.
         * @param redirectURI redirectURI
         * @param callback [KinveyUserCallback]
         */
        @Deprecated("Use {@link #loginWithMIC(AbstractClient, String, String, String, KinveyUserCallback)}")
        fun loginWithMIC(client: AbstractClient<User>, username: String,
                         password: String, clientId: String?,
                         redirectURI: String, callback: KinveyUserCallback<User>) {
            MICCallback = callback
            PostForOAuthToken(client, clientId, redirectURI, username, password, callback).execute()
        }

        /**
         * Posts for a MIC login Access token
         *
         * @param token the access code returned from the MIC Auth service
         * @param clientId clientId
         * @param client Client object
         */
        fun getMICAccessToken(token: String, clientId: String?, client: AbstractClient<User>) {
            PostForAccessToken(client, MICRedirectURI, token, clientId, MICCallback as KinveyClientCallback<User>).execute()
        }

        /**
         * Posts for a MIC login Access token
         *
         * @param token the access code returned from the MIC Auth service
         * @param client Client object
         */
        @Deprecated("use {@link #getMICAccessToken(String, String, AbstractClient)} ()} instead.")
        fun getMICAccessToken(token: String, client: AbstractClient<User>) {
            getMICAccessToken(token, null, client)
        }

        /***
         * Initiate the MIC login flow with an Activity containing a Webview
         *
         * @param client Client object
         * @param redirectURI redirectURI
         * @param callback callback
         */
        @Deprecated("use {@link #presentMICLoginActivity(Client, String, String, KinveyUserCallback)} ()} instead.")
        fun presentMICLoginActivity(client: Client<*>, redirectURI: String,
                                    callback: KinveyUserCallback<User>) {
            presentMICLoginActivity(client, null, redirectURI, callback)
        }

        /***
         * Initiate the MIC login flow with an Activity containing a Webview
         *
         * @param client Client object
         * @param clientId clientId
         * @param redirectURI redirectURI
         * @param callback callback
         */
        fun presentMICLoginActivity(client: Client<*>, clientId: String?,
                                    redirectURI: String, callback: KinveyUserCallback<User>) {

            loginWithAuthorizationCodeLoginPage(client, clientId, redirectURI, object : KinveyMICCallback<User> {
                override fun onReadyToRender(myURLToRender: String) {
                    val i = Intent(client.context, MICLoginActivity::class.java)
                    i.putExtra(MICLoginActivity.KEY_LOGIN_URL, myURLToRender)
                    i.putExtra(MICLoginActivity.KEY_CLIENT_ID, clientId)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    client.context?.startActivity(i)
                }

                override fun onSuccess(result: User?) {
                    callback?.onSuccess(result)
                }

                override fun onFailure(error: Throwable) {
                    callback?.onFailure(error)
                }
            })
        }

        private fun <T : BaseUser> createBuilder(client: AbstractClient<T>): KinveyAuthRequest.Builder<T> {
            val appKey = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
            val appSecret = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appSecret

            return KinveyAuthRequest.Builder(client.requestFactory.transport,
                    client.jsonFactory, client.baseUrl, appKey, appSecret, null)
        }
    }

}
