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

import java.util.*

class Metrics {

    private val events: HashMap<String, Metric> = HashMap()

    private var metricMap: HashMap<String, Metrics>? = null

    fun start(event: String) {
        val start = System.currentTimeMillis()
        events[event]?.start = start
    }

    fun end(event: String) {
        val end = System.currentTimeMillis()
        events[event]?.end = end
    }

    fun getMetrics(className: String): Metrics? {
        if (metricMap == null) {
            metricMap = HashMap()
        }
        metricMap?.let {
            if (!it.containsKey(className)) {
                it[className] = Metrics()
            }
            return it[className]
        }
        return null
    }

    private class Metric {
        var start = -1L
        var end = -1L
            set(end) {
                field = end
                if (this.start == -1L) {
                    return
                }
                isCalculated = true
                delta = this.end - this.start
            }
        var delta = -1L
            private set
        var isCalculated = false
            private set
    }
}

