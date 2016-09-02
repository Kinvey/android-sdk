package com.kinvey.java.auth;

import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.testing.MockKinveyAuthRequest;

import java.lang.reflect.Field;

/**
 * Created by edward on 10/13/15.
 */
public class KinveyAuthRequestTest extends KinveyMockUnitTest {

    private UserStoreRequestManager currentUser;

    private void initializeUser() {
        currentUser = new UserStoreRequestManager(getClient(), new MockKinveyAuthRequest.MockBuilder(getClient().getRequestFactory().getTransport(),
                getClient().getJsonFactory(), "mockAppKey","mockAppSecret",null));
    }

    public void testHeaders() {
        initializeUser();
        try {
            UserStoreRequestManager.LoginRequest login = currentUser.loginBlocking();

            login.buildAuthRequest();

            Field f = UserStoreRequestManager.LoginRequest.class.getDeclaredField("request");
            f.setAccessible(true);
            KinveyAuthRequest authRequest = (KinveyAuthRequest) f.get(login);

            f = KinveyAuthRequest.class.getDeclaredField("kinveyHeaders");
            f.setAccessible(true);
            KinveyHeaders requestHeaders = (KinveyHeaders) f.get(authRequest);

            assertFalse(requestHeaders.containsKey("Authorization"));

        }catch (Exception e){
            assertNull(e);
//            assertFalse("Shouldn't have thrown an exception", true);
        }

    }


}
