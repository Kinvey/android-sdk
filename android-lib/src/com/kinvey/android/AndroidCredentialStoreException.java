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

/**
 * <p>AndroidCredentialStoreException class.</p>
 *
 * @author mjsalinger
 * @since 2.0
 * @version $Id: $
 */
public class AndroidCredentialStoreException extends Exception {
    /**
     * <p>Constructor for AndroidCredentialStoreException.</p>
     */
    public AndroidCredentialStoreException() {
        super("There was an error while attempting to load the credential store.");
    }

    /**
     * AndroidCredentialStoreException constructor
     *
     * @param errMsg
     *            error message to use
     */
    public AndroidCredentialStoreException(final String errMsg) {
        super(errMsg);
    }
}
