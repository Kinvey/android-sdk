/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.java.AbstractClient
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.network.NetworkManager

/**
 * Wraps the [NetworkManager] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 *
 * This functionality can be accessed through the [com.kinvey.android.Client.appData] convenience method.  NetworkManager
 * gets and saves entities that extend [com.google.api.client.json.GenericJson].  A class that extends GenericJson
 * can map class members to KinveyCollection properties using [com.google.api.client.util.Key] attributes.  For example,
 * the following will map a string "city" to a Kinvey collection attributed named "city":
 *
 *
 *
 * <pre>
 * @Key
 * private String city;
</pre> *
 *
 *
 *
 * The @Key attribute also can take an optional name, which will map the member to a different attribute name in the Kinvey
 * collection.
 *
 *
 *
 * <pre>
 * @Key("_id")
 * private String customerID;
</pre> *
 *
 *
 *
 * Methods in this API use either [com.kinvey.android.callback.KinveyListCallback] for retrieving entity sets,
 * [com.kinvey.android.callback.KinveyDeleteCallback] for deleting appData, or  the general-purpose
 * [com.kinvey.java.core.KinveyClientCallback] used for retrieving single entites or saving Entities.
 *
 *
 *
 *
 *
 * Entity Set sample:
 * <pre>
 * `NetworkManager<EventEntity> myAppData = kinveyClient.appData("myCollection",EventEntity.class);
 * myAppData.get(appData().query, new KinveyListCallback<EventEntity> {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(EventEntity[] entities) { ... }
 * });
` *
</pre> *
 *
 *
 *
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
class AndroidNetworkManager<T : GenericJson>
/**
 * Constructor to instantiate the NetworkManager class.
 *
 * @param collectionName Name of the appData collection
 * @param myClass        Class Type to marshall data between.
 * @param client
 */(collectionName: String?, myClass: Class<T>, client: AbstractClient<*>?)
    : NetworkManager<T>(collectionName, myClass, client) {
    /**
     * Method to check if the current runtime environment has an active connection to the internet, this implementation is tightly coupled with the Android Operating System
     *
     * @return true if the device is connected or connecting
     */
    override val isOnline: Boolean
        get() {
            val cm = (client as Client<*>?)?.context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo: NetworkInfo? = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnectedOrConnecting) {
                INFO("Device is online")
                return true
            }
            INFO("Device is offline")
            return false
        }
}