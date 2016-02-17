package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.offline.MockOfflineStore;

/**
 * Created by edward on 8/3/15.
 */
public class OfflinePolicyTest extends KinveyMockUnitTest {

    MockOfflineStore mos;

    private final String collection = "collection";

    public void testPostLocalFirst() throws Exception{
        init();
        OfflinePolicy p = OfflinePolicy.LOCAL_FIRST;
        NetworkManager.Save request = getGenericAppData(GenericJson.class).saveBlocking(new GenericJson());

        request.setStore(mos, p);

        GenericJson ret = (GenericJson) p.execute(request);

        assertEquals("save", ret.get("hello"));

    }

    public void testPutLocalFirst() throws Exception{
        init();
        OfflinePolicy p = OfflinePolicy.LOCAL_FIRST;
        GenericJson s = new GenericJson();
        s.put("_id", "id");
        NetworkManager.Save request = getGenericAppData(GenericJson.class).saveBlocking(s);

        request.setStore(mos, p);

        GenericJson ret = (GenericJson) p.execute(request);

        assertEquals("save", ret.get("hello"));

    }


    public void testGetLocalFirst() throws Exception{
        init();
        OfflinePolicy p = OfflinePolicy.LOCAL_FIRST;
        NetworkManager.GetEntity request = getGenericAppData(GenericJson.class).getEntityBlocking("123");
        request.setStore(mos, p);

        GenericJson ret = (GenericJson) p.execute(request);

        assertEquals("get", ret.get("hello"));
    }

    public void testGetLocalFirstNull() throws Exception{
        init();
        OfflinePolicy p = OfflinePolicy.LOCAL_FIRST;
        NetworkManager.GetEntity request = getGenericAppData(GenericJson.class).getEntityBlocking("123");
        request.setStore(new MockOfflineStore.NullStore(), p);

        GenericJson ret = (GenericJson) p.execute(request);

        assertNull(ret);
        //assertEquals("get", ret.get("hello"));
    }


    public void testDeleteLocalFirst() throws Exception{
        init();
        OfflinePolicy p = OfflinePolicy.LOCAL_FIRST;
        NetworkManager.Delete request = getGenericAppData(GenericJson.class).deleteBlocking("123");

        request.setStore(mos, p);

        KinveyDeleteResponse ret = (KinveyDeleteResponse) p.execute(request);

        assertEquals(1, ret.getCount());
    }



    private <T> NetworkManager<T> getGenericAppData(Class<? extends Object> myClass) {
        NetworkManager appData = new NetworkManager("myCollection", myClass, getClient());
        return appData;
    }



    private void init(){
        if (mos == null){
            mos = new MockOfflineStore();
        }
    }

}
