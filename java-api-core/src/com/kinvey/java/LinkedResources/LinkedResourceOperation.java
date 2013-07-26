/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.LinkedResources;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.kinvey.java.AppData;
import com.kinvey.java.AppDataOperation;
import com.kinvey.java.LinkedData;
import com.kinvey.java.Query;
import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.cache.Cache;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * Builder for advanced App Data requests
 *
 * @author edwardf
 * @since 2.0.2
 */
public class LinkedResourceOperation extends AppDataOperation{



}