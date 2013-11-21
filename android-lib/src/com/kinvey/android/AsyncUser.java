/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.kinvey.android.callback.*;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * Wraps the {@link com.kinvey.java.User} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#user()} convenience method.
 * </p>
 *
 * <p>
 * Methods in this API use either {@link com.kinvey.android.callback.KinveyUserCallback} for authentication, login, and
 * user creation, or the general-purpose {@link com.kinvey.java.core.KinveyClientCallback} used for User data retrieval,
 * updating, and management.
 * </p>
 *
 * <p>
 * Login sample:
 * <pre>
 * {@code
       public void submit(View view) {
       kinveyClient.user().login(mEditUserName.getText().toString(), mEditPassword.getText().toString(),
               new KinveyUserCallback() {
           public void onFailure(Throwable t) {
               CharSequence text = "Wrong username or password.";
               Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
           }
           public void onSuccess(User u) {
               CharSequence text = "Welcome back," + u.getUsername() + ".";
               Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
               LoginActivity.this.startActivity(new Intent(LoginActivity.this,
                       SessionsActivity.class));
               LoginActivity.this.finish();
           }
       });
   }
 * </pre>
 *
 * </p>
 * <p>
 * Saving user data sample:
 * <pre>
 * {@code
       User user = kinveyClient.user();
        user.put("fav_food", "bacon");
        user.update(new KinveyClientCallback<User.Update>() {

            public void onFailure(Throwable e) { ... }

            public void onSuccess(User u) { ... }
       });
   }
 * </pre>
 * </p>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author edwardf
 * @author mjsalinger
 * @since 2.0
 */
public class AsyncUser<T extends User> extends AbstractAsyncUser<T> {


    /**
     * Base constructor requires the client instance and a {@link com.kinvey.java.auth.KinveyAuthRequest.Builder} to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all
     * requests constructed by this api.
     * </p>
     *
     * @param client instance of current client
     * @throws NullPointerException if the client parameter and KinveyAuthRequest.Builder is non-null
     */
    public AsyncUser(AbstractClient client, Class<T> userClass, KinveyAuthRequest.Builder builder) {
        super(client, userClass, builder);
    }
}
