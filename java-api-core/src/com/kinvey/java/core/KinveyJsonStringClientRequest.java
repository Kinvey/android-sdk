package com.kinvey.java.core;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.Json;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.model.KinveySaveBatchResponse;

import java.util.UUID;

public class KinveyJsonStringClientRequest<T> extends AbstractKinveyClientRequest<T> {

    /** raw json data **/
    private final String jsonContent;
    private AsyncExecutor executor;

    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     * @param jsonString              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected KinveyJsonStringClientRequest(AbstractClient abstractKinveyJsonClient, String requestMethod,
                                            String uriTemplate, String jsonString, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonString == null
                ? null : new ByteArrayContent(Json.MEDIA_TYPE, jsonString.getBytes()) , responseClass);
        if (jsonString != null) {
            super.getRequestHeaders().setContentType(Json.MEDIA_TYPE);
        }
        this.jsonContent = jsonString;
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
}
