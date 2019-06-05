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

import java.util.HashMap
import java.util.logging.Level

import com.google.api.client.http.HttpTransport

/**
 * This class is used statically throughout the library.
 * It will delegate to an instance of a `KinveyLogger`, which does the actual log writing.
 *
 * @author edward
 */
class Logger {

    fun network(enabled: Boolean = true): Logger {
        activeMap[NETWORK] = enabled
        java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.FINEST
        return this
    }

    /**
     * Enable info level logging
     * @return the Logger
     */
    fun info(enabled: Boolean = true): Logger {
        activeMap[INFO] = enabled
        return this
    }

    /**
     * Enable debug level logging
     * @return the Logger
     */
    fun debug(enabled: Boolean = true): Logger {
        activeMap[DEBUG] = enabled
        return this
    }

    /**
     * Enable trace level logging
     * @return the Logger
     */
    fun trace(enabled: Boolean = true): Logger {
        activeMap[TRACE] = enabled
        return this
    }

    /**
     * Enable warning level logging
     * @return the Logger
     */
    fun warning(enabled: Boolean = true): Logger {
        activeMap[WARNING] = enabled
        return this
    }

    /**
     * Enable error level logging
     * @return the Logger
     */
    fun error(enabled: Boolean = true): Logger {
        activeMap[ERROR] = enabled
        return this
    }

    fun all(enabled: Boolean = true): Logger {
        activeMap[INFO] = enabled
        activeMap[DEBUG] = enabled
        activeMap[TRACE] = enabled
        activeMap[NETWORK] = enabled
        java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.FINEST
        activeMap[WARNING] = enabled
        activeMap[ERROR] = enabled
        return this
    }


    /***
     * This interface defines the behaivor of a platform specific logger
     * @author edward
     */
    interface KinveyLogger {

        /**
         * Time to write an info message to the output
         * @param message
         */
        fun info(message: String)

        /**
         * Time to write a debug message to the output
         * @param message
         */
        fun debug(message: String)

        /**
         * Time to write a trace message to the output
         * @param message
         */
        fun trace(message: String)

        /**
         * Time to write a warning message to the output
         * @param message
         */
        fun warning(message: String)

        /**
         * Time to write an error message to the output
         * @param message
         */
        fun error(message: String)

    }

    companion object {

        /**
         * The KinveyLogger which does the actual writing
         */
        private var platformLogger: KinveyLogger? = null

        /**
         * Has the Logger been initialized?
         */
        private var isInitialized = false

        /**
         * This map determines if a log level is on or off.
         */
        private var activeMap: HashMap<String, Boolean> = HashMap()

        /**
         * log levels
         */
        private val INFO = "INFO"
        private val DEBUG = "DEBUG"
        private val TRACE = "TRACE"
        private val NETWORK = "NETWORK"
        private val WARNING = "WARNING"
        private val ERROR = "ERROR"

        /**
         * Initialize this Logger with an instance of a KinveyLogger, defaults to no logging at all.
         * @param log
         */
        @JvmStatic
        fun init(log: KinveyLogger?) {
            if (log == null) {
                throw KinveyException("Logger can't be null!")
            }
            if (platformLogger != null) {
                return
            }

            platformLogger = log

            activeMap[NETWORK] = false
            java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.OFF
            activeMap[INFO] = false
            activeMap[DEBUG] = false
            activeMap[TRACE] = false
            activeMap[WARNING] = false
            activeMap[ERROR] = false

            isInitialized = true
        }

        /**
         * Log an info message
         * @param message
         */
        @JvmStatic
        fun INFO(message: String) {
            if (!isInitialized) {
                return
            }
            if (activeMap[INFO] == true) {
                platformLogger?.info(message)
            }
        }

        /***
         * Log a debug message
         * @param message
         */
        @JvmStatic
        fun DEBUG(message: String) {
            if (!isInitialized) {
                return
            }
            if (activeMap[DEBUG] == true) {
                platformLogger?.debug(message)
            }
        }

        /**
         * Log a trace message
         * @param message
         */
        @JvmStatic
        fun TRACE(message: String) {
            if (!isInitialized) {
                return
            }
            if (activeMap[TRACE] == true) {
                platformLogger?.trace(message)
            }
        }

        /**
         * log a warning message
         * @param message
         */
        @JvmStatic
        fun WARNING(message: String) {
            if (!isInitialized) {
                return
            }
            if (activeMap[WARNING] == true) {
                platformLogger?.warning(message)
            }
        }

        /**
         * Log an error message
         * @param message
         */
        @JvmStatic
        fun ERROR(message: String) {
            if (!isInitialized) {
                return
            }
            if (activeMap[ERROR] == true) {
                platformLogger?.error(message)
            }
        }
    }
}
