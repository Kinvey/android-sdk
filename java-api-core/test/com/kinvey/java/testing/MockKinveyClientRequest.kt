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
package com.kinvey.java.testing

import com.google.api.client.http.HttpContent
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.testing.MockKinveyJsonClient.Builder

/**
 * @author m0rganic
 * @since 2.0
 */
class MockKinveyClientRequest<T>(requestMethod: String?, uriTemplate: String?,
                                 httpContent: HttpContent?, responseClass: Class<T>)
    : AbstractKinveyClientRequest<T>(Builder().build(), requestMethod, uriTemplate, httpContent, responseClass) {
    val mockMediaUploader: MediaHttpUploader? = null
}