package com.kinvey.java.auth;

import com.kinvey.java.User;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.testing.MockKinveyAuthRequest;

import java.lang.reflect.Field;
import java.util.Hashtable;

/**
 * Created by edward on 10/13/15.
 */
public class KinveyAuthRequestTest extends KinveyMockUnitTest {

    private User<User> currentUser;

    private void initializeUser() {
        currentUser = new User<User>(getClient(), User.class, new MockKinveyAuthRequest.MockBuilder(getClient().getRequestFactory().getTransport(),
                getClient().getJsonFactory(), "mockAppKey","mockAppSecret",null));
    }

    public void testHeaders() {
        initializeUser();
        try {
            User.LoginRequest login = currentUser.loginBlocking();

            login.buildAuthRequest();

            Field f = User.LoginRequest.class.getDeclaredField("request");
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
