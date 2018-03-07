package com.kinvey.java.core;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.model.KinveyQueryCacheResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/**
 * Created by yuliya on 03/05/17.
 */

public abstract class AbstractKinveyQueryCacheReadRequest<T> extends AbstractKinveyJsonClientRequest<KinveyQueryCacheResponse> {

    private Class<T> responseClass;

    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL.
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected AbstractKinveyQueryCacheReadRequest(AbstractClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, GenericJson jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, null);
        this.responseClass = responseClass;
    }

    @Override
    public KinveyQueryCacheResponse execute() throws IOException {

        HttpResponse response = executeUnparsed() ;

        KinveyQueryCacheResponse ret;

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

            } else {
                String jsonString = response.parseAsString();
                JsonParser jsonParser = new JsonParser();
                JsonArray jsonArray = (JsonArray) jsonParser.parse(jsonString);
                JsonObjectParser objectParser = getAbstractKinveyClient().getObjectParser();


                ret = objectParser.parseAndClose(new ByteArrayInputStream(jsonArray.toString().getBytes(Charsets.UTF_8)), Charsets.UTF_8, KinveyQueryCacheResponse.class);
                if (response.getHeaders().containsKey("X-Kinvey-Request-Start")){
                    ret.setRequestTime((String) response.getHeaders().get("X-Kinvey-Request-Start"));
                }
                return ret;
            }

        } catch (IllegalArgumentException e) {
            Logger.ERROR("unable to parse response -> " + e.toString());
            throw new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString());
        } catch (NullPointerException ex){
            return null;
        }
    }
}
