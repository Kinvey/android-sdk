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

package com.kinvey.java.store;

/**
 * Created by Prots on 2/4/16.
 */
public enum StoreType {
    SYNC(ReadPolicy.PREFER_LOCAL, WritePolicy.LOCAL_THEN_NETWORK, Long.MAX_VALUE),
    CACHE(ReadPolicy.FORCE_LOCAL, WritePolicy.FORCE_LOCAL, Long.MAX_VALUE),
    NETWORK(ReadPolicy.FORCE_NETWORK, WritePolicy.FORCE_NETWORK, 0L);

    public ReadPolicy readPolicy;

    public WritePolicy writePolicy;
    public long ttl;

    StoreType(ReadPolicy readPolicy, WritePolicy writePolicy, long ttl) {
        this.readPolicy = readPolicy;
        this.writePolicy = writePolicy;
        this.ttl = ttl;
    }
}
