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
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.model.KinveyDeltaSetCountResponse;
import com.kinvey.java.model.KinveyQueryCacheResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuliya on 05/07/17.
 */

public abstract class AbstractKinveyCachedReadRequest extends AbstractKinveyJsonClientRequest<KinveyDeltaSetCountResponse> {

    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL.
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     */
    protected AbstractKinveyCachedReadRequest(AbstractClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, GenericJson jsonContent) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, null);
    }

    @Override
    public KinveyDeltaSetCountResponse execute() throws IOException {
        HttpResponse response = executeUnparsed();
        if (overrideRedirect) {
            return onRedirect(response.getHeaders().getLocation());
        }
        // special class to handle void or empty responses
        if (response.getContent() == null) {
            response.ignore();
            return null;
        }
        try {
            KinveyDeltaSetCountResponse ret;
            int statusCode = response.getStatusCode();
            if (response.getRequest().getRequestMethod().equals(HttpMethods.HEAD) || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore();
                return null;

            } else {
                String jsonString = response.parseAsString();
                JsonObjectParser objectParser = getAbstractKinveyClient().getObjectParser();
                ret = objectParser.parseAndClose(new ByteArrayInputStream(jsonString.getBytes(Charsets.UTF_8)), Charsets.UTF_8, KinveyDeltaSetCountResponse.class);
                if (response.getHeaders().containsKey(Constants.X_KINVEY_REQUEST_START)) {
                    ret.setLastRequestTime(response.getHeaders().getHeaderStringValues(Constants.X_KINVEY_REQUEST_START).get(0).toUpperCase(Locale.US));
                } else if (response.getHeaders().containsKey(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)) {
                    ret.setLastRequestTime(response.getHeaders().getHeaderStringValues(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE).get(0).toUpperCase(Locale.US));
                }
                return ret;
            }

        } catch (IllegalArgumentException e) {
            Logger.ERROR("unable to parse response -> " + e.toString());
            throw new KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString());
        } catch (NullPointerException ex) {
            return null;
        }
    }
}
