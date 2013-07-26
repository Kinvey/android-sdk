/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.core;

import java.io.IOException;


/**
 * @author morgan
 */
public interface KinveyRequestInitializer {

    public void initialize(AbstractKinveyClientRequest<?> request) throws IOException;

}
