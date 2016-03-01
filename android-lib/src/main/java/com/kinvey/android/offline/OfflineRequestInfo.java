/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android.offline;

import java.io.Serializable;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

/**
 * This class is an abstraction of a REST request.
 * <p/>
 * An instance of this class stores the relationship between an Http Verb and and an associated entity's ID.
 * <p/>
 * myRequest.getHttpVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * <p/>
 * myRequest.getEntityID() represents the id of the entity, which might be stored in the local store.
 *
 * @author edwardf
 */
public class OfflineRequestInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;



}