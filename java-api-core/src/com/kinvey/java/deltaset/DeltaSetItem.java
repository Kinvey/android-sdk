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

package com.kinvey.java.deltaset;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.Date;

/**
 * Created by Prots on 12/11/15.
 */
public class DeltaSetItem extends GenericJson {
    @Key("_id")
    private String id;

    @Key("_kmd")
    private  KMD kmd;

    public String getId() {
        return id;
    }

    public KMD getKmd() {
        return kmd;
    }

    public static class KMD {
        @Key("lmt")
        private String lmt;

        public String getLmt(){
            return lmt;
        }

        public KMD(){};
        public KMD(String lmt){
            this.lmt = lmt;
        }
    }

}
