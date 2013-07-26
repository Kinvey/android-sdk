/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink.push;

import android.content.Context;
import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * @author edwardf
 * @since 2.0
 */
public class GCMReciever extends GCMBroadcastReceiver {

    @Override
    public String getGCMIntentServiceClassName(Context context){
        return "com.kinvey.sample.kitchensink.push.GCMLoggingReceiver";
    }


}
