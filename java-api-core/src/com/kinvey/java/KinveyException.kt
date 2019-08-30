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

package com.kinvey.java

/**
 * @author edwardf
 */
open class KinveyException : RuntimeException {

    var reason: String? = null
    var fix: String? = null
    var explanation: String? = null

    constructor(reason: String?, fix: String?, explanation: String?) : super(formatMessage(reason, fix, explanation)) {
        this.reason = reason
        this.fix = fix
        this.explanation = explanation
    }

    constructor(reason: String?) : super(formatMessage(reason)) {
        this.reason = reason
        this.fix = ""
        this.explanation = ""
    }

    companion object {

        private fun formatMessage(reason: String?, fix: String?, explanation: String?): String {
            return "\nREASON: $reason\nFIX: $fix\nEXPLANATION: $explanation\n"
        }

        private fun formatMessage(reason: String?): String {
            return "\nREASON: $reason"
        }
    }
}
