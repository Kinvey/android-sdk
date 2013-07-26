/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.testing;

import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
* @author m0rganic
* @since 2.0
*/
public class SpyKinveyClientRequestInitializer extends KinveyClientRequestInitializer {

    boolean isCalled;

    public SpyKinveyClientRequestInitializer() {
        super(null, null, new com.kinvey.java.core.KinveyHeaders());
    }

    @Override
    public void initialize(AbstractKinveyClientRequest<?> request) {
        isCalled = true;
    }
}
