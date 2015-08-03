package com.kinvey.java.offline;

import com.google.api.client.http.UriTemplate;
import com.kinvey.java.AbstractClient;
import junit.framework.TestCase;

/**
 * Created by edward on 7/31/15.
 */
public class OfflineClientRequestTest extends TestCase {

    AbstractKinveyOfflineClientRequest or;

    public void testGenerateMongoDBID(){
        String mongoID1 = AbstractKinveyOfflineClientRequest.generateMongoDBID();
        String mongoID2 = AbstractKinveyOfflineClientRequest.generateMongoDBID();
        String mongoID3 = AbstractKinveyOfflineClientRequest.generateMongoDBID();
        assertNotSame(mongoID1, mongoID2);
        assertNotSame(mongoID1, mongoID3);
        assertNotSame(mongoID2, mongoID3);
    }

    public void testGetUUID(){
        String mongoID1 = AbstractKinveyOfflineClientRequest.getUUID();
        String mongoID2 = AbstractKinveyOfflineClientRequest.getUUID();
        String mongoID3 = AbstractKinveyOfflineClientRequest.getUUID();
        assertNotSame(mongoID1, mongoID2);
        assertNotSame(mongoID1, mongoID3);
        assertNotSame(mongoID2, mongoID3);
    }



}
