/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android;

import android.content.Context;
import com.google.api.client.util.Key;

/**
 * Standard Kinvey specific headers are added to all requests.
 * @author m0rganic
 * @since 2.0
 */
class KinveyHeaders extends com.kinvey.java.core.KinveyHeaders {

    @Key("x-kinvey-device-information")
    private String deviceInfo;

    public KinveyHeaders(Context context) {
        super();

        UdidFactory uuidFactory = new UdidFactory(context);
        deviceInfo = uuidFactory.getDeviceInfoHeader(context);
    }

}
