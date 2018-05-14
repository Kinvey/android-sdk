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
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.model.KinveyReadResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuliya on 10/26/17.
 */

public abstract class AbstractKinveyReadRequest<T> extends AbstractKinveyJsonClientRequest<KinveyReadResponse<T>> {

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
    protected AbstractKinveyReadRequest(AbstractClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, GenericJson jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, null);
        this.responseClass = responseClass;
    }

    @Override
    public KinveyReadResponse<T> execute() throws IOException {

        HttpResponse response = executeUnparsed() ;

        List<T> results = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        KinveyReadResponse<T> ret = new KinveyReadResponse<>();

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
                for (JsonElement element : jsonArray) {
                    try {
                        results.add(objectParser.parseAndClose(new ByteArrayInputStream(element.toString().getBytes(Charsets.UTF_8)), Charsets.UTF_8, responseClass));
                    } catch (IllegalArgumentException e) {
                        Logger.ERROR("unable to parse response -> " + e.toString());
                        exceptions.add(new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
                if (response.getHeaders().containsKey(Constants.X_KINVEY_REQUEST_START)) {
                    ret.setLastRequestTime(response.getHeaders().getHeaderStringValues(Constants.X_KINVEY_REQUEST_START).get(0).toLowerCase(Locale.US));
                } else if (response.getHeaders().containsKey(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)) {
                    ret.setLastRequestTime(response.getHeaders().getHeaderStringValues(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE).get(0).toLowerCase(Locale.US));
                }
                ret.setResult(results);
                ret.setListOfExceptions(exceptions);
                return ret;
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
