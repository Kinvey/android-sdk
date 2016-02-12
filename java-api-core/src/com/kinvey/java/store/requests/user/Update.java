package com.kinvey.java.store.requests.user;

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.kinvey.java.auth.KinveyAuthResponse;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;

import java.io.IOException;

/**
 * Update Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object for
 * Update User requests.
 */
public final class Update<T extends User> extends AbstractKinveyJsonClientRequest<T> {
    private static final String REST_PATH = "user/{appKey}/{userID}";

    private UserStore<T> userStore;
    @Key
    private String userID;

    public Update(UserStore<T> userStore, User user, Class<T> myClass) {
        super(userStore.getClient(), "PUT", REST_PATH, user, myClass);
        this.userStore = userStore;
        this.userID = user.getId();
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStore.getClientAppVersion());
        if (userStore.getCustomRequestProperties() != null && !userStore.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStore.getCustomRequestProperties()) );
        }

    }

    public T execute() throws IOException {

        T u = super.execute();

        if (u.getId() == null || u.getId() == null){
            return u;
        }

        if (u.getId().equals(userStore.getCurrentUser().getId())){
            KinveyAuthResponse auth = new KinveyAuthResponse();
            auth.put("_id", u.get("_id"));
            KinveyAuthResponse.KinveyUserMetadata kmd = new KinveyAuthResponse.KinveyUserMetadata();
            kmd.put("lmt", u.get("_kmd.lmt")) ;
            kmd.put("authtoken", u.get("_kmd.authtoken"));
            kmd.putAll((ArrayMap) u.get("_kmd"));
            auth.put("_kmd", kmd);
            auth.put("username", u.get("username"));
            for (Object key : u.keySet()){
                if (!key.toString().equals("_kmd")){
                    auth.put(key.toString(), u.get(key));
                }
            }
            String userType = userStore.getClient().getClientUsers().getCurrentUserType();
            return userStore.initUser(auth, userType, u);
        }else{
            return u;
        }
    }


}
