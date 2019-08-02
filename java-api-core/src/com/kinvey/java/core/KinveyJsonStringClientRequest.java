package com.kinvey.java.core;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.json.Json;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

public class KinveyJsonStringClientRequest<T> extends AbstractKinveyClientRequest<T> {

    /** raw json data **/
    private final String jsonContent;
    private AsyncExecutor executor;
    private Type responseClassType;

    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     * @param jsonString              POJO that can be serialized into JSON content or {@code null} for none
     */
    protected KinveyJsonStringClientRequest(AbstractClient abstractKinveyJsonClient, String requestMethod,
                                            String uriTemplate, String jsonString, Class responseClass, Class myClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonString == null
                ? null : new ByteArrayContent(Json.MEDIA_TYPE, jsonString.getBytes()), responseClass);
        if (jsonString != null) {
            super.getRequestHeaders().setContentType(Json.MEDIA_TYPE);
        }
        this.jsonContent = jsonString;
        this.responseClassType = getType(responseClass, myClass);
    }

    private Type getType(Class<?> rawClass, Class<?> parameter) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { parameter };
            }
            @Override
            public Type getRawType() {
                return rawClass;
            }
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * @return the jsonContent
     */
    public String getJsonContent() {
        return jsonContent;
    }


    @Override
    public AbstractKinveyJsonClient getAbstractKinveyClient() {
        return (AbstractKinveyJsonClient) super.getAbstractKinveyClient();
    }

    @Override
    protected KinveyJsonResponseException newExceptionOnError(HttpResponse response) {
        return KinveyJsonResponseException.from(getAbstractKinveyClient().getJsonFactory(), response);
    }

    public AsyncExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncExecutor executor) {
        this.executor = executor;
    }

    public static String getUUID(){
        String id = UUID.randomUUID().toString();
        String ret = id.replace("-", "");
        return ret;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public T execute() throws IOException {
        Logger.INFO("Start execute for network request");
        HttpResponse response = executeUnparsed();

        if (getOverrideRedirect()){
            Logger.INFO("overrideRedirect == true");
            return onRedirect(response.getHeaders().getLocation());
        }

        // special class to handle void or empty responses
        if (Void.class.equals(responseClass) || response.getContent() == null) {
            response.ignore();
            return null;
        }

        try {
            int statusCode = response.getStatusCode();
            if (response.getRequest().getRequestMethod().equals(HttpMethods.HEAD) || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore();
                return null;

            } else {
                Object parsedContent = response.parseAs(responseClassType);
                response.disconnect();
                return (T) parsedContent;
            }

        } catch (IllegalArgumentException e) {
            Logger.ERROR("unable to parse response -> " + e.toString());
            throw new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString());

        } catch (NullPointerException ex){
            Logger.WARNING(ex.getMessage());
            return null;
        }
    }
}
