/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.auth;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

/**
 * Used to provide credentials to Kinvey for authorization to a 3rd party authenticated user.
 *
 * <p>
 * Use {@link ThirdPartyIdentity#createThirdPartyIdentity(com.kinvey.java.auth.ThirdPartyIdentity.Type, String...)} to create new instance.
 * </p>
 *
 * <p>
 * This class is not thread safe.
 * </p>
 *
 * @author m0rganic
 * @since 2.0
 */
public class ThirdPartyIdentity extends GenericJson{

    public enum Type {
        FACEBOOK,
        TWITTER,
        GOOGLE,
        LINKED_IN,
        MOBILE_IDENTITY,
        SALESFORCE,
        AUTH_LINK
    }

    @Key("_socialIdentity")
    private final Provider provider;

    private ThirdPartyIdentity(Provider provider) {
        this.provider = provider;
    }

    private final static class Provider<T extends AccessToken> extends GenericJson {

        private Provider(Type type, T credential) {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(credential);
            switch (type) {
                case FACEBOOK:
                    this.put("facebook", credential);
                    break;
                case TWITTER:
                    this.put("twitter", credential);
                    break;
                case GOOGLE:
                    this.put("google", credential);
                    break;
                case LINKED_IN:
                    this.put("linkedIn", credential);
                    break;
                case AUTH_LINK:
                    this.put("authlink", credential);
                    break;
                case SALESFORCE:
                    this.put("salesforce", credential);
                    break;
                case MOBILE_IDENTITY:
                    this.put("kinveyAuth", credential);
                    break;
                default:
                    throw new IllegalArgumentException("No known third party identity type was specified");
            }
        }
    }

    private abstract static class AccessToken extends GenericJson {

        @Key("access_token")
        private final String accessToken;

        protected AccessToken(String accessToken) {
            this.accessToken = Preconditions.checkNotNull(accessToken, "accessToken must not be null");
            Preconditions.checkArgument(!("".equals(accessToken)), "accessToken must not be empty");
        }
    }

    /**
     * Base class for OAuth2 providers
     */
    private abstract static class OAuth2 extends AccessToken {

        @Key("refresh_token")
        private String refreshToken;

        protected OAuth2(String accessToken) {
            super(accessToken);
        }

        protected OAuth2(String accessToken, String refreshToken) {
            super(accessToken);
            this.refreshToken = refreshToken;
        }
    }

    /**
     * Base class for OAuth1 providers
     */
    private abstract static class OAuth1 extends AccessToken {

        @Key("access_token_secret")
        protected final String accessTokenSecret;

        @Key("consumer_key")
        protected final String consumerKey;

        @Key("consumer_secret")
        protected final String consumerSecret;

        protected OAuth1(String accessToken, String accessTokenSecret, String consumerKey, String consumerSecret) {
            super(accessToken);
            this.accessTokenSecret = Preconditions.checkNotNull(accessTokenSecret);
            Preconditions.checkArgument(!("".equals(accessTokenSecret)), "accessTokenSecret must not be empty");
            this.consumerKey = Preconditions.checkNotNull(consumerKey);
            Preconditions.checkArgument(!("".equals(consumerKey)), "consumerKey must not be empty");
            this.consumerSecret = Preconditions.checkNotNull(consumerSecret);
            Preconditions.checkArgument(!("".equals(consumerSecret)), "consumerSecret must not be empty");
        }
    }

    /**
     * Facebook credential
     */
    private static class FacebookCredential extends OAuth2 {

        public FacebookCredential (String accessToken) {
            super(accessToken);
        }
    }

    private static class AuthLinkCredential extends OAuth2 {

        public AuthLinkCredential (String accessToken, String refreshToken) {
            super(accessToken, refreshToken);
        }
    }

    /**
     * Google credential
     */
    private static class GoogleCredential extends OAuth2 {

        public GoogleCredential(String accessToken) {
            super(accessToken);
        }

    }

    /**
     * SalesFoce credential
     */
    private static class SalesForceCredential extends OAuth2 {

        @Key
        private String client_id;
        @Key
        private String id;





        public SalesForceCredential(String accessToken, String reauthToken, String client_id, String id) {
            super(accessToken, reauthToken);
            this.client_id = client_id;
            this.id = id;
        }


        public String getClient_id() {
            return client_id;
        }

        public void setClient_id(String client_id) {
            this.client_id = client_id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * LinkedIn credential
     */
    private static class LinkedInCredential extends OAuth1 {
        protected LinkedInCredential(String accessToken, String accessTokenSecret, String consumerKey, String consumerSecret) {
            super(accessToken, accessTokenSecret, consumerKey, consumerSecret);
        }
    }

    /**
     * Twitter credential
     */
    private static class TwitterCredential extends OAuth1 {

        public TwitterCredential(String accessToken, String accessTokenSecret, String consumerKey, String consumerSecret) {
            super(accessToken, accessTokenSecret, consumerKey, consumerSecret);
        }

    }

    public static class MobileIdentityCredential extends OAuth2 {

        public MobileIdentityCredential(String accessToken){
            super(accessToken);
        }

    }

    /**
     * A factory method to use when constructor authentication provider and credential link objects that will be used
     * by the Kinvey system. This class follows the standard documented in the Kinvey
     * <a href="http://devcenter.kinvey.com/rest/guides/users#usingsocialidentities">REST API documentation</a>.
     *
     * <p>
     * There are four different types of identities to construct using this method. All of the objects returned
     * can be used to obtain an authorization response from Kinvey.
     * </p>
     *
     * <p> Facebook: </p>
     * <pre>
     * ThirdPartyIdentity facebook = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.FACEBOOK, accessToken);
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
     *     .setThirdPartyAuthToken(facebook)
     *     .build()
     *     .execute();
     * </pre>
     *
     *
     * <p> Google: </p>
     * <pre>
     * ThirdPartyIdentity google = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.GOOGLE, accessToken);
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
     *     .setThirdPartyAuthToken(google)
     *     .build()
     *     .execute();
     * </pre>
     *
     * <p> Twitter: </p>
     * <pre>
     * ThirdPartyIdentity twitter = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.TWITTER
     *          , accessToken
     *          , accessSecret
     *          , twitterConsumerKey
     *          , twitterConsumerSecret);
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
     *     .setThirdPartyAuthToken(twitter)
     *     .build()
     *     .execute();
     * </pre>
     *
     * <p> LinkedIn: </p>
     * <pre>
     * ThirdPartyIdentity linkedIn = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.LINKED_IN
     *          , accessToken
     *          , accessSecret
     *          , linkedInConsumerKey
     *          , linkedInConsumerSecret);
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
     *     .setThirdPartyAuthToken(linkedIn)
     *     .build()
     *     .execute();
     *
     * <p> Auth Link: </p>
     * <pre>
     * ThirdPartyIdentity authlinkIdentity = ThirdPartyIdentity.createThirdPartyIdentity(ThirdPartyIdentity.Type.AUTHLINK, accessToken, refreshToken);
     * KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,appKey,appSecret)
     *     .setThirdPartyAuthToken(authlinkIdentity)
     *     .build()
     *     .execute();
     * </pre>
     *
     * @param type authentication provider that will be used to link the user
     * @param params the parameters passed in to construct the 3rd party provider credential
     * @return a thirdparty provider object used to associate a Kinvey user with a 3rd party auth provider
     */
    public static ThirdPartyIdentity createThirdPartyIdentity(Type type, String... params) {

        Preconditions.checkNotNull(type, "Type argument must not be null");
        Preconditions.checkNotNull(params, "Params must not be null");

        switch (type) {
            case FACEBOOK:
                Preconditions.checkArgument(params.length == 1, "Expected %s arguments for facebook but found %s", 1, params.length);
                Provider<FacebookCredential> credentialProvider = new Provider<FacebookCredential>(type, new FacebookCredential(params[0]));
                return new ThirdPartyIdentity(credentialProvider);

            case GOOGLE:
                Preconditions.checkArgument(params.length > 0, "Expected %s arguments for google but found %s", 1, params.length);
                Provider<GoogleCredential> googleCredentialProvider = new Provider<GoogleCredential>(type, new GoogleCredential(params[0]));
                return new ThirdPartyIdentity(googleCredentialProvider);

            case TWITTER:
                Preconditions.checkArgument(params.length == 4, "Expected %s arguments for twitter but found %s", 4, params.length);
                Provider<TwitterCredential> twitterCredentialProvider = new Provider<TwitterCredential>(type, new TwitterCredential(params[0], params[1], params[2], params[3]));
                return new ThirdPartyIdentity(twitterCredentialProvider);

            case LINKED_IN:
                Preconditions.checkArgument(params.length == 4, "Expected %s arguments for linkedIn but found %s", 4, params.length);
                Provider<LinkedInCredential> linkedInCredentialProvider = new Provider<LinkedInCredential>(type, new LinkedInCredential(params[0], params[1], params[2], params[3]));
                return new ThirdPartyIdentity(linkedInCredentialProvider);

            case AUTH_LINK:
                Preconditions.checkArgument(params.length == 2, "Expected %s arguments for linkedIn but found %s", 2, params.length);
                Provider<AuthLinkCredential> authLinkCredentialProvider = new Provider<AuthLinkCredential>(type, new AuthLinkCredential(params[0], params[1]));
                return new ThirdPartyIdentity(authLinkCredentialProvider);
            case SALESFORCE:
                Preconditions.checkArgument(params.length == 4, "Expected %s arguments for SalesForce but found %s", 4, params.length);
                Provider<SalesForceCredential> salesForceCredentialProvider = new Provider<SalesForceCredential>(type, new SalesForceCredential(params[0], params[1], params[2], params[3]));
                return new ThirdPartyIdentity(salesForceCredentialProvider);

            case MOBILE_IDENTITY:
                Preconditions.checkArgument(params.length == 1, "Expected %s arguments for Mobile Identity but found %s", 1, params.length);
                Provider<MobileIdentityCredential> mobileIdentityCredentialProvider = new Provider<MobileIdentityCredential>(type, new MobileIdentityCredential(params[0]));
                return new ThirdPartyIdentity(mobileIdentityCredentialProvider);


            default:
                throw new IllegalArgumentException("Could not recognize type passed in");
        }

    }


}
