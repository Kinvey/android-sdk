package com.kinvey.android;

import com.kinvey.java.AppData;
import com.kinvey.java.AppDataOperation;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * Builder for advanced asynchronous App Data requests.
 * <p>
 * This class uses the Builder pattern to allow extensible use of all the features of our Core App Data API.
 * </p>
 * <p>
 * These builders allow various fields to be declared independently, resulting in much simpler usage of our API for power users.
 * </p>
 *
 * @author edwardf
 * @since 2.0.2
 */
public class AsyncAppDataOperation extends AppDataOperation {

    private abstract class AsyncAppDataRequestBuilder extends AppDataRequestBuilder {

        private KinveyClientCallback callback;

        public AsyncAppDataRequestBuilder(String collectionName, Class myClass, AsyncLinkedData appData) {

            super(collectionName, myClass, appData);
        }

        public AsyncAppDataRequestBuilder setCallback(KinveyClientCallback callback) {
            this.callback = callback;
            return this;
        }
    }









}
