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

package com.kinvey.java.auth

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.common.base.Preconditions

/**
 * Used to provide credentials to Kinvey for authorization to a 3rd party authenticated user.
 *
 *
 *
 * Use [ThirdPartyIdentity.createThirdPartyIdentity] to create new instance.
 *
 *
 *
 *
 * This class is not thread safe.
 *
 *
 * @author m0rganic
 * @since 2.0
 */
class ThirdPartyIdentity private constructor(@field:Key("_socialIdentity") private val provider: Provider<*>) : GenericJson() {
    enum class Type { FACEBOOK, TWITTER, GOOGLE, LINKED_IN, MOBILE_IDENTITY, SALESFORCE, AUTH_LINK }

    class Provider<T : AccessToken?> constructor(type: Type, credential: T) : GenericJson() {
        init {
            Preconditions.checkNotNull(type)
            Preconditions.checkNotNull(credential)
            when (type) {
                Type.FACEBOOK -> this["facebook"] = credential
                Type.TWITTER -> this["twitter"] = credential
                Type.GOOGLE -> this["google"] = credential
                Type.LINKED_IN -> this["linkedIn"] = credential
                Type.AUTH_LINK -> this["authlink"] = credential
                Type.SALESFORCE -> this["salesforce"] = credential
                Type.MOBILE_IDENTITY -> this["kinveyAuth"] = credential
                else -> throw IllegalArgumentException("No known third party identity type was specified")
            }
        }
    }

    abstract class AccessToken protected constructor(accessToken: String) : GenericJson() {
        @Key("access_token")
        private val accessToken: String

        init {
            this.accessToken = Preconditions.checkNotNull(accessToken, "accessToken must not be null")
            Preconditions.checkArgument(accessToken != "", "accessToken must not be empty")
        }
    }

    /**
     * Base class for OAuth2 providers
     */
    abstract class OAuth2 : AccessToken {
        @Key("refresh_token")
        private var refreshToken: String? = null

        protected constructor(accessToken: String) : super(accessToken) {}
        protected constructor(accessToken: String, refreshToken: String) : super(accessToken) {
            this.refreshToken = refreshToken
        }
    }

    /**
     * Base class for OAuth1 providers
     */
    private abstract class OAuth1 protected constructor(accessToken: String, accessTokenSecret: String, consumerKey: String, consumerSecret: String) : AccessToken(accessToken) {
        @Key("access_token_secret")
        protected val accessTokenSecret: String
        @Key("consumer_key")
        protected val consumerKey: String
        @Key("consumer_secret")
        protected val consumerSecret: String

        init {
            this.accessTokenSecret = Preconditions.checkNotNull(accessTokenSecret)
            Preconditions.checkArgument("" != accessTokenSecret, "accessTokenSecret must not be empty")
            this.consumerKey = Preconditions.checkNotNull(consumerKey)
            Preconditions.checkArgument("" != consumerKey, "consumerKey must not be empty")
            this.consumerSecret = Preconditions.checkNotNull(consumerSecret)
            Preconditions.checkArgument("" != consumerSecret, "consumerSecret must not be empty")
        }
    }

    /**
     * Facebook credential
     */
    private class FacebookCredential(accessToken: String) : OAuth2(accessToken)

    private class AuthLinkCredential(accessToken: String, refreshToken: String) : OAuth2(accessToken, refreshToken)
    /**
     * Google credential
     */
    private class GoogleCredential(accessToken: String) : OAuth2(accessToken)

    /**
     * SalesFoce credential
     */
    private class SalesForceCredential(accessToken: String, reauthToken: String, @field:Key var client_id: String, @field:Key var id: String) : OAuth2(accessToken, reauthToken)

    /**
     * LinkedIn credential
     */
    private class LinkedInCredential(accessToken: String, accessTokenSecret: String, consumerKey: String, consumerSecret: String) : OAuth1(accessToken, accessTokenSecret, consumerKey, consumerSecret)

    /**
     * Twitter credential
     */
    private class TwitterCredential(accessToken: String, accessTokenSecret: String, consumerKey: String, consumerSecret: String) : OAuth1(accessToken, accessTokenSecret, consumerKey, consumerSecret)

    class MobileIdentityCredential(accessToken: String) : OAuth2(accessToken)

    companion object {
        /**
         * A factory method to use when constructor authentication provider and credential link objects that will be used
         * by the Kinvey system. This class follows the standard documented in the Kinvey
         * [REST API documentation](http://devcenter.kinvey.com/rest/guides/users#usingsocialidentities).
         *
         *
         *
         * There are four different types of identities to construct using this method. All of the objects returned
         * can be used to obtain an authorization response from Kinvey.
         *
         *
         *
         *  Facebook:
         * <pre>
         * ThirdPartyIdentity facebook = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.FACEBOOK, accessToken);
         * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
         * .setThirdPartyAuthToken(facebook)
         * .build()
         * .execute();
        </pre> *
         *
         *
         *
         *  Google:
         * <pre>
         * ThirdPartyIdentity google = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.GOOGLE, accessToken);
         * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
         * .setThirdPartyAuthToken(google)
         * .build()
         * .execute();
        </pre> *
         *
         *
         *  Twitter:
         * <pre>
         * ThirdPartyIdentity twitter = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.TWITTER
         * , accessToken
         * , accessSecret
         * , twitterConsumerKey
         * , twitterConsumerSecret);
         * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
         * .setThirdPartyAuthToken(twitter)
         * .build()
         * .execute();
        </pre> *
         *
         *
         *  LinkedIn:
         * <pre>
         * ThirdPartyIdentity linkedIn = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.LINKED_IN
         * , accessToken
         * , accessSecret
         * , linkedInConsumerKey
         * , linkedInConsumerSecret);
         * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
         * .setThirdPartyAuthToken(linkedIn)
         * .build()
         * .execute();
         *
         *
         *  Auth Link:
         * <pre>
         * ThirdPartyIdentity authlinkIdentity = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.AUTHLINK, accessToken, refreshToken);
         * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
         * .setThirdPartyAuthToken(authlinkIdentity)
         * .build()
         * .execute();
        </pre> *
         *
         * @param type authentication provider that will be used to link the user
         * @param params the parameters passed in to construct the 3rd party provider credential
         * @return a thirdparty provider object used to associate a Kinvey user with a 3rd party auth provider
        </pre> */
        @JvmStatic
        fun createThirdPartyIdentity(type: Type?, vararg params: String?): ThirdPartyIdentity {
            Preconditions.checkNotNull(type, "Type argument must not be null")
            Preconditions.checkNotNull(params.contains(null), "Params must not be null")
            var accessToken = ""
            var accessTokenSecret = ""
            var consumerKey = ""
            var consumerSecret = ""
            val parCount = params.count()
            if (parCount > 0) { accessToken = params[0] ?: "" }
            if (parCount > 1) { accessTokenSecret = params[1] ?: "" }
            if (parCount > 2) { consumerKey = params[2] ?: "" }
            if (parCount > 3) { consumerSecret = params[3] ?: "" }
            return when (type) {
                Type.FACEBOOK -> {
                    Preconditions.checkArgument(params.size == 1, "Expected %s arguments for facebook but found %s", 1, params.size)
                    val credentialProvider = Provider(type, FacebookCredential(accessToken))
                    ThirdPartyIdentity(credentialProvider)
                }
                Type.GOOGLE -> {
                    Preconditions.checkArgument(params.isNotEmpty(), "Expected %s arguments for google but found %s", 1, params.size)
                    val googleCredentialProvider = Provider(type, GoogleCredential(accessToken))
                    ThirdPartyIdentity(googleCredentialProvider)
                }
                Type.TWITTER -> {
                    Preconditions.checkArgument(params.size == 4, "Expected %s arguments for twitter but found %s", 4, params.size)
                    val twitterCredentialProvider = Provider(type, TwitterCredential(accessToken, accessTokenSecret, consumerKey, consumerSecret))
                    ThirdPartyIdentity(twitterCredentialProvider)
                }
                Type.LINKED_IN -> {
                    Preconditions.checkArgument(params.size == 4, "Expected %s arguments for linkedIn but found %s", 4, params.size)
                    val linkedInCredentialProvider = Provider(type, LinkedInCredential(accessToken, accessTokenSecret, consumerKey, consumerSecret))
                    ThirdPartyIdentity(linkedInCredentialProvider)
                }
                Type.AUTH_LINK -> {
                    Preconditions.checkArgument(params.size == 2, "Expected %s arguments for linkedIn but found %s", 2, params.size)
                    val authLinkCredentialProvider = Provider(type, AuthLinkCredential(accessToken, accessTokenSecret))
                    ThirdPartyIdentity(authLinkCredentialProvider)
                }
                Type.SALESFORCE -> {
                    Preconditions.checkArgument(params.size == 4, "Expected %s arguments for SalesForce but found %s", 4, params.size)
                    val salesForceCredentialProvider = Provider(type, SalesForceCredential(accessToken, accessTokenSecret, consumerKey, consumerSecret))
                    ThirdPartyIdentity(salesForceCredentialProvider)
                }
                Type.MOBILE_IDENTITY -> {
                    Preconditions.checkArgument(params.size == 1, "Expected %s arguments for Mobile Identity but found %s", 1, params.size)
                    val mobileIdentityCredentialProvider = Provider(type, MobileIdentityCredential(accessToken))
                    ThirdPartyIdentity(mobileIdentityCredentialProvider)
                }
                else -> throw IllegalArgumentException("Could not recognize type passed in")
            }
        }
    }
}
