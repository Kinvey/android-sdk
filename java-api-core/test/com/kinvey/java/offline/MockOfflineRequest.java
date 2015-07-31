package com.kinvey.java.offline;

import com.google.api.client.http.UriTemplate;
import com.kinvey.java.AbstractClient;

/**
 * Created by edward on 7/31/15.
 */
public class MockOfflineRequest extends AbstractKinveyOfflineClientRequest {
    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     *                                 {@link UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     * @param collectionName           the name of the collection this request is associated with
     */
    protected MockOfflineRequest(AbstractClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, Object jsonContent, Class responseClass, String collectionName) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass, collectionName);
    }
}
