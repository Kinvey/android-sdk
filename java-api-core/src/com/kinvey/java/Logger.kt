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

import com.google.api.client.http.HttpTransport
import java.util.*
import java.util.logging.Level

/**
 * This class is used statically throughout the library.
 * It will delegate to an instance of a `KinveyLogger`, which does the actual log writing.
 *
 * @author edward
 */
class Logger {
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
    private var activeMap: HashMap<String, Boolean>? = null

    fun network(): Logger? {
        instance?.activeMap?.apply { this[NETWORK] = true }
        java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.FINEST
        return instance
    }

    /**
     * Enable info level logging
     * @return the Logger
     */
    fun info(): Logger? {
        instance?.activeMap?.apply { this[INFO] = true }
        return instance
    }

    /**
     * Enable debug level logging
     * @return the Logger
     */
    fun debug(): Logger? {
        instance?.activeMap?.apply { this[DEBUG] = true }
        return instance
    }

    /**
     * Enable trace level logging
     * @return the Logger
     */
    fun trace(): Logger? {
        instance?.activeMap?.apply { this[TRACE] = true }
        return instance
    }

    /**
     * Enable warning level logging
     * @return the Logger
     */
    fun warning(): Logger? {
        instance?.activeMap?.apply { this[WARNING] = true }
        return instance
    }

    /**
     * Enable error level logging
     * @return the Logger
     */
    fun error(): Logger? {
        instance?.activeMap?.apply { this[ERROR] = true }
        return instance
    }

    fun all(): Logger? {
        java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.FINEST
        instance?.activeMap?.apply {
            this[INFO] = true
            this[DEBUG] = true
            this[TRACE] = true
            this[NETWORK] = true
            this[WARNING] = true
            this[ERROR] = true
        }
        return instance
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
         * log levels
         */
        const val INFO = "INFO"
        const val DEBUG = "DEBUG"
        const val TRACE = "TRACE"
        const val NETWORK = "NETWORK"
        const val WARNING = "WARNING"
        const val ERROR = "ERROR"
        /***
         * Singleton pattern
         * @return
         */
        private val instance: Logger?
            private get() {
                if (_instance == null) {
                    _instance = Logger()
                }
                return _instance
            }

        private var _instance: Logger? = null
        /**
         * Initialize this Logger with an instance of a KinveyLogger, defaults to no logging at all.
         * @param log
         */
        fun init(log: KinveyLogger?) {
            if (log == null) {
                throw KinveyException("Logger can't be null!")
            }
            if (instance?.platformLogger != null) {
                return
            }
            instance?.platformLogger = log
            instance?.activeMap = HashMap()
            java.util.logging.Logger.getLogger(HttpTransport::class.java.name).level = Level.OFF
            instance?.activeMap?.apply {
                this[NETWORK] = false
                this[INFO] = false
                this[DEBUG] = false
                this[TRACE] = false
                this[WARNING] = false
                this[ERROR] = false
                instance?.isInitialized = true
            }
        }

        fun configBuilder(): Logger? {
            return instance
        }

        /**
         * Log an info message
         * @param message
         */
        @JvmStatic
        fun INFO(message: String?) {
            if (instance?.isInitialized == false) {
                return
            }
            instance?.activeMap?.let {
                if (it[INFO] == true && !message.isNullOrBlank()) {
                    instance?.platformLogger?.info(message)
                }
            }
        }

        /***
         * Log a debug message
         * @param message
         */
        @JvmStatic
        fun DEBUG(message: String?) {
            if (instance?.isInitialized == false) {
                return
            }
            instance?.activeMap?.let {
                if (it[DEBUG] == true && !message.isNullOrBlank()) {
                    instance?.platformLogger?.debug(message)
                }
            }
        }

        /**
         * Log a trace message
         * @param message
         */
        @JvmStatic
        fun TRACE(message: String?) {
            if (instance?.isInitialized == false) {
                return
            }
            instance?.activeMap?.let {
                if (it[TRACE] == true && !message.isNullOrBlank()) {
                    instance?.platformLogger?.trace(message)
                }
            }
        }

        /**
         * log a warning message
         * @param message
         */
        @JvmStatic
        fun WARNING(message: String?) {
            if (instance?.isInitialized == false) {
                return
            }
            instance?.activeMap?.let {
                if (it[WARNING] == false && !message.isNullOrBlank()) {
                    instance?.platformLogger?.warning(message)
                }
            }
        }

        /**
         * Log an error message
         * @param message
         */
        @JvmStatic
        fun ERROR(message: String?) {
            if (instance?.isInitialized == false) {
                return
            }
            instance?.activeMap?.let {
                if (it[ERROR] == false && !message.isNullOrBlank()) {
                    instance?.platformLogger?.error(message)
                }
            }
        }
    }
}