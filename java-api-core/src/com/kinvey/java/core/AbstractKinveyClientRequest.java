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

package com.kinvey.java.core;

import java.io.*;
import java.util.Locale;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.store.UserStoreRequestManager;

/**
 * @author m0rganic
 */
public abstract class AbstractKinveyClientRequest<T> extends GenericData {


    /**
     * Kinvey JSON client *
     */
    private final AbstractClient client;

    /**
     * HTTP method *
     */
    private final String requestMethod;

    /**
     * URI template of the path relative to the base url *
     */
    protected String uriTemplate;

    /**
     * http headers to be sent along with the request *
     */
    private HttpHeaders requestHeaders = new HttpHeaders();

    /**
     * http content or {@code null} if none is set *
     */
    private final HttpContent httpContent;

    /**
     * response headers of the last executed request, {@code null} before the request is made *
     */
    private HttpHeaders lastResponseHeaders;

    /**
     * response status code of the last executed request, {@code -1} before the request is made *
     */
    private int lastResponseCode = -1;

    /**
     * response status message of the last executed request, {@code null} before the request is made *
     */
    private String lastResponseMessage;

    /**
     * Response class to parse into *
     */
    private final Class<T> responseClass;

    /**
     * NetworkFileManager downloader or {@code null} if none is set *
     */
    private MediaHttpDownloader downloader;

    /**
     * {@link BackOffPolicy} to use for retries
     */
    private BackOffPolicy requestBackoffPolicy;

    /**
     * Does this request require the appkey/appsecret for authentication or does it require a user context
     */
    private boolean requireAppCredentials = false;
    
    /**
     * Should the request use the default template expansion for encoding the URL
     */
    private boolean templateExpand = true;
    
    /**
     * Should the request intercept redirects and route them to an override
     */
    private boolean overrideRedirect = false;

    @Key
    private String appKey;

    private KinveyClientCallback<T> callback;

    /**
     * The message received when a user has been locked down
     */
    private static final String LOCKED_DOWN = "UserLockedDown";
    
    private final String hostName;
    
    /***
     * Used for MIC to indicate if a request has been repeated after getting a refresh token
     */
    private boolean hasRetryed = false;
    
    /**
     * @param abstractKinveyClient the abstract kinvey client
     * @param requestMethod the request method, PUT, GET, POST, or DELETE
     * @param uriTemplate valid uri template
     * @param httpContent object to send as the message body or {@code null} if none
     * @param responseClass expected type in the response of this request
     */
    protected AbstractKinveyClientRequest(AbstractClient abstractKinveyClient,
                                          String requestMethod, String uriTemplate, HttpContent httpContent,
                                          Class<T> responseClass) {
    	this(abstractKinveyClient, abstractKinveyClient.getBaseUrl(), requestMethod, uriTemplate, httpContent, responseClass);
    }
    
    /**
     * @param abstractKinveyClient the abstract kinvey client
     * @param requestMethod the request method, PUT, GET, POST, or DELETE
     * @param uriTemplate valid uri template
     * @param httpContent object to send as the message body or {@code null} if none
     * @param responseClass expected type in the response of this request
     */
    protected AbstractKinveyClientRequest(AbstractClient abstractKinveyClient, String hostName,
                                          String requestMethod, String uriTemplate, HttpContent httpContent,
                                          Class<T> responseClass) {
        Preconditions.checkNotNull(abstractKinveyClient, "abstractKinveyClient must not be null");
        Preconditions.checkNotNull(requestMethod, "requestMethod must not be null");
        this.client = abstractKinveyClient;
        this.requestMethod = requestMethod;
        this.uriTemplate = uriTemplate;
        this.responseClass = responseClass;
        this.httpContent = httpContent;
        this.requestBackoffPolicy = abstractKinveyClient.getBackoffPolicy();
        this.hostName = hostName;
    }
    
    

    /**
     * @return the abstractKinveyClient
     */
    public AbstractKinveyClient getAbstractKinveyClient() {
        return client;
    }

    /**
     * @return the requestMethod
     */
    public final String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @return the uriTemplate
     */
    public final String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * @return the httpHeaders
     */
    public final HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    public AbstractKinveyClientRequest<T> setRequestHeaders(HttpHeaders headers) {
        this.requestHeaders = headers;
        return this;
    }

    /**
     * @return the httpContent
     */
    public final HttpContent getHttpContent() {
        return httpContent;
    }

    /**
     * @return the lastResponseHeaders
     */
    public final HttpHeaders getLastResponseHeaders() {
        return lastResponseHeaders;
    }

    /**
     * @return the lastResponseCode
     */
    public final int getLastResponseCode() {
        return lastResponseCode;
    }

    /**
     * @return the lastResponseMessage
     */
    public final String getLastResponseMessage() {
        return lastResponseMessage;
    }

    /**
     * @return the responseClass
     */
    public final Class<T> getResponseClass() {
        return responseClass;
    }

    /**
     *
     * @return the Backoff Policy
     */
    public BackOffPolicy getRequestBackoffPolicy() {
        return requestBackoffPolicy;
    }

    /**
     * @return the downloader
     */
    public final MediaHttpDownloader getDownloader() {
        return downloader;
    }

    /**
     * @return
     */
    protected GenericUrl buildHttpRequestUrl() {
    	
    	String encodedURL = UriTemplate.expand(hostName, uriTemplate, this, true);
    	if (!templateExpand){
    		encodedURL = encodedURL.replace("%3F", "?");
    		encodedURL = encodedURL.replace("%3D", "=");
    		encodedURL = encodedURL.replace("%26", "&");
    	}
    	return new GenericUrl(encodedURL);
    }

    /**
     * @return
     * @throws IOException
     */
    public HttpRequest buildHttpRequest() throws IOException {
        HttpRequest httpRequest = getAbstractKinveyClient()
                .getRequestFactory()
                .buildRequest(requestMethod, buildHttpRequestUrl(), httpContent);
        httpRequest.setParser(getAbstractKinveyClient().getObjectParser());
        httpRequest.setSuppressUserAgentSuffix(true);
        //httpRequest.setRetryOnExecuteIOException(true);
        //httpRequest.setBackOffPolicy(this.requestBackoffPolicy);
        // custom methods may use POST with no content but require a Content-Length header
        if (httpContent == null && (requestMethod.equals(HttpMethods.POST)
                || requestMethod.equals(HttpMethods.PUT))) {
            httpRequest.setContent(new EmptyContent());
        }
        for (Entry<String, Object> entry: requestHeaders.entrySet()) {
            httpRequest.getHeaders().set(entry.getKey().toLowerCase(Locale.US), entry.getValue());
        }
        if (httpRequest.getHeaders().containsKey("x-kinvey-custom-request-properties")){
            String customHeaders = (String) httpRequest.getHeaders().get("x-kinvey-custom-request-properties");
        	if (customHeaders.getBytes("UTF-8").length > 2000){
        		throw new KinveyException("Cannot attach more than 2000 bytes of Custom Request Properties");
        	}
        }
        return httpRequest;
    }

    public HttpResponse executeUnparsed() throws IOException {
        return executeUnparsed(false);
    }

    /**
     * Executes the http request and returns the raw {@link HttpResponse}. If
     * throwExceptionOnError is {@code true} an {@link HttpResponseException} is
     * thrown, subclasses may override this method to customize the behavior.
     * <p>
     * Callers are responsible for disconnecting the HTTP response by calling
     * {@link HttpResponse#disconnect}. Example usage:
     * </p>
     *
     * <pre>
         HttpResponse response = request.executeUnparsed();
         try {
         // process response..
         } finally {
         response.disconnect();
         }
     * </pre>
     *
     * <p>
     * Subclasses may override by calling the super implementation.
     * </p>
     * @return the http response containing raw data and headers
     * @throws IOException
     */
    HttpResponse executeUnparsed(boolean upload) throws IOException {
        HttpResponse response;
        boolean throwExceptionOnError;

        // normal request
        HttpRequest request = buildHttpRequest();
        throwExceptionOnError = request.getThrowExceptionOnExecuteError();
        request.setThrowExceptionOnExecuteError(false);
        request.setNumberOfRetries(3);
        request.setParser(getAbstractKinveyClient().getObjectParser());

        if (overrideRedirect){
            request.setFollowRedirects(false);
        }

        if (initiationHeaders != null) {
            request.getHeaders().putAll(initiationHeaders);
        }


        response = request.execute();

        lastResponseCode = response.getStatusCode();
        lastResponseMessage = response.getStatusMessage();
        lastResponseHeaders = response.getHeaders();

        if (lastResponseMessage != null && lastResponseMessage.equals(LOCKED_DOWN)){
            this.client.performLockDown();
        }

        //process refresh token needed
        if (response.getStatusCode() == 401 && !hasRetryed){


            //get the refresh token
            Credential cred = client.getStore().load(client.activeUser().getId());
            String refreshToken = null;
            if (cred != null){
                refreshToken = cred.getRefreshToken();
            }

            if (refreshToken != null ){
                //logout the current user
                String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
                String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

                KinveyAuthRequest.Builder builder = new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                        client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);

                UserStoreRequestManager userStoreRequestManager = new UserStoreRequestManager(client, builder);

                userStoreRequestManager.logout().execute();

                //use the refresh token for a new access token
                GenericJson result = userStoreRequestManager.useRefreshToken(refreshToken).execute();

                //login with the access token
                userStoreRequestManager.loginMobileIdentityBlocking(result.get("access_token").toString()).execute();

                //store the new refresh token
                Credential currentCred = client.getStore().load(client.activeUser().getId());
                currentCred.setRefreshToken(result.get("refresh_token").toString());
                client.getStore().store(client.activeUser().getId(), currentCred);
                hasRetryed = true;
                return executeUnparsed();
            }

        }

        // process any other errors
        if (throwExceptionOnError && !response.isSuccessStatusCode() &&  response.getStatusCode() != 302) {
            throw newExceptionOnError(response);
        }

        return response;
    }

    /**
     * Throws new {@link HttpResponseException} containing the response message.
     *
     * @param response object returned in the event of an error
     * @return and exception containing the error message returned by the server
     */
    protected IOException newExceptionOnError(HttpResponse response) {
        return new HttpResponseException(response);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public T execute() throws IOException {
        HttpResponse response = executeUnparsed();
       
        if (overrideRedirect){
        	return onRedirect(response.getHeaders().getLocation());
        }
        
        // special class to handle void or empty responses
        if (Void.class.equals(responseClass) || response.getContent() == null) {
            response.ignore();
            return null;
        }
        
        try{
            int statusCode = response.getStatusCode();
            if (response.getRequest().getRequestMethod().equals(HttpMethods.HEAD) || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore();
                return null;

            }else{
                return getAbstractKinveyClient().getObjectParser().parseAndClose(response.getContent(), Charsets.UTF_8, responseClass);
            }
            
        }catch(IllegalArgumentException e){
            Logger.ERROR("unable to parse response -> " + e.toString());
            throw new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString());

        }catch (NullPointerException ex){
            return null;
        }
    }

    /**
     * Sends the metadata request to the server and returns the metadata content input stream of
     * {@link HttpResponse}.
     * <p/>
     * <p>
     * Callers are responsible for closing the input stream after it is processed. Example sample:
     * </p>
     * <p/>
     * <pre>
     * InputStream is = request.executeAsInputStream();
     * try {
     * // Process input stream..
     * } finally {
     * is.close();
     * }
     * </pre>
     * <p/>
     * <p>
     * Subclasses may override by calling the super implementation.
     * </p>
     *
     * @return input stream of the response content
     */
    public InputStream executeAsInputStream() throws IOException {
        return executeUnparsed().getContent();
    }

    public AbstractKinveyClientRequest<T> setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public boolean isRequireAppCredentials() {
        return requireAppCredentials;
    }
    
    public void setTemplateExpand(boolean expand){
    	this.templateExpand = expand;
    }

    public void setRequireAppCredentials(boolean requireAppCredentials) {
        this.requireAppCredentials = requireAppCredentials;
    }

    public KinveyClientCallback<T> getCallback() {
        return callback;
    }

    public void setCallback(KinveyClientCallback<T> callback) {
        this.callback = callback;
    }
    
    public String getCustomerAppVersion(){
    	Object header = getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	if (header == null){
    		return null;
    	}
    	return header.toString();
    }
    
    public String getCustomRequestProperties(){
    	Object header = getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	if (header == null){
    		return null;
    	}
    	return (String) header;
    }
    
    public void setOverrideRedirect(boolean override){
    	this.overrideRedirect = override;
    }
    
    public T onRedirect(String newLocation)  throws IOException{
    	Logger.ERROR("Override Redirect in response is expected, but not implemented!");
    	return null;
    }
}
