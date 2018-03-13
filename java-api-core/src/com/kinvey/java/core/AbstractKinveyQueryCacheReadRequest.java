package com.kinvey.java.core;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.model.KinveyQueryCacheResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public KinveyQueryCacheResponse<T> execute() throws IOException {
        HttpResponse response = executeUnparsed() ;
        if (overrideRedirect){
            return onRedirect(response.getHeaders().getLocation());
        }
        // special class to handle void or empty responses
        if (response.getContent() == null) {
            response.ignore();
            return null;
        }
        try {
            KinveyQueryCacheResponse<T> ret = new KinveyQueryCacheResponse<>();
            int statusCode = response.getStatusCode();
            if (response.getRequest().getRequestMethod().equals(HttpMethods.HEAD) || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore();
                return null;

            } else {

                String jsonString = response.parseAsString();
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
                JsonArray jsonArrayChanged = jsonObject.getAsJsonArray("changed");
                JsonObjectParser objectParser = getAbstractKinveyClient().getObjectParser();
                List<T> changed = new ArrayList<>();
                List<Exception> exceptions = new ArrayList<>();

                for (JsonElement element : jsonArrayChanged) {
                    try {
                        changed.add(objectParser.parseAndClose(new ByteArrayInputStream(element.toString().getBytes(Charsets.UTF_8)), Charsets.UTF_8, responseClass));
                    } catch (IllegalArgumentException e) {
                        Logger.ERROR("unable to parse response -> " + e.toString());
                        exceptions.add(new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
                ret.setChanged(changed);

                JsonArray jsonArrayDeleted = jsonObject.getAsJsonArray("deleted");
                List<T> deleted = new ArrayList<>();
                for (JsonElement element : jsonArrayDeleted) {
                    try {
                        deleted.add(objectParser.parseAndClose(new ByteArrayInputStream(element.toString().getBytes(Charsets.UTF_8)), Charsets.UTF_8, responseClass));
                    } catch (IllegalArgumentException e) {
                        Logger.ERROR("unable to parse response -> " + e.toString());
                        exceptions.add(new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
                ret.setDeleted(deleted);
                ret.setListOfExceptions(exceptions);
                if (response.getHeaders().containsKey("x-kinvey-request-start")){
                    ret.setRequestTime(response.getHeaders().getHeaderStringValues("x-kinvey-request-start").get(0));
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
