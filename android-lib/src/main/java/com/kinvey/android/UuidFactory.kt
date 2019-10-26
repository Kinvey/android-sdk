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

package com.kinvey.android

import android.content.Context
import android.os.Build
import android.provider.Settings.Secure
import com.google.api.client.json.GenericJson
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * @see [
 * http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
](http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id) *
 */
class UuidFactory(context: Context?) {

    /** Constant `uuid`  */
    var deviceUuid: UUID? = null
        protected set

    /**
     *
     * Constructor for UuidFactory.
     *
     * @param context a [android.content.Context] object.
     */
    init {
        if (deviceUuid == null) {
            synchronized(UuidFactory::class.java) {
                if (deviceUuid == null) {
                    val prefs = context?.getSharedPreferences(PREFS_FILE, 0)
                    val id = prefs?.getString(PREFS_DEVICE_ID, null)
                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        deviceUuid = UUID.fromString(id)
                    } else {
                        val androidId: String? = Secure.getString(context?.contentResolver, Secure.ANDROID_ID)
                        // Use the Android ID unless it's broken, in which case
                        // fallback on deviceId, unless it's not available, then
                        // fallback on a random number which we store to a prefs file
                        deviceUuid = try {
                            if (androidId != null && "9774d56d682e549c" != androidId) {
                                UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
                            } else {
                                UUID.randomUUID()
                            }
                        } catch (e: UnsupportedEncodingException) {
                            throw RuntimeException(e)
                        }
                        // Write the value out to the prefs file
                        prefs?.edit()?.putString(PREFS_DEVICE_ID, deviceUuid.toString())?.apply()
                    }
                }
            }
        }
    }

    /**
     *
     * getDeviceInfoHeader
     *
     * @param context a [android.content.Context] object.
     * @return a [java.lang.String] object.
     */
    fun getDeviceInfoHeader(context: Context?): String {
        // Device Manufacturer
        val ma = try {
            Build::class.java.getDeclaredField("MANUFACTURER")
                    .get(Build::class.java)?.toString()
                    ?.replace(" ", "_") ?: ""
        } catch (e: Throwable) {
            "UNKNOWN"
        }
        // Device Model
        val devModel = Build.MODEL.replace(" ", "_")
        // OS Version
        val osVersion = Build.VERSION.RELEASE.replace(" ", "_")
        // UUID
        val uuid = deviceUuid
        return String.format("%s/%s %s %s %s", ma, devModel, OS_NAME, osVersion, uuid.toString())
    }

    /**
     *
     * getDeviceInfoHeader
     *
     * @return a [java.lang.String] object.
     */
    fun getDeviceInfoHeader(sdkVersion: String?): String {
        // Device Model
        val devModel = Build.MODEL.replace(" ", "_")
        // OS Version
        val osVersion = Build.VERSION.RELEASE.replace(" ", "_")
        // UUID
        val uuid = deviceUuid
        val content = GenericJson()
        content[HEADER_VERSION] = VERSION
        content[DEVICE_MODEL] = devModel
        content[OS_NAME] = OS
        content[OS_VERSION] = osVersion
        content[PLATFORM] = SDK
        content[PLATFORM_VERSION] = sdkVersion
        content[DEVICE_ID] = uuid.toString()
        return Gson().toJson(content)
    }

    companion object {
        /** Constant `PREFS_FILE="device_id.xml"`  */
        protected const val PREFS_FILE = "device_id.xml"
        /** Constant `PREFS_DEVICE_ID="device_id"`  */
        protected const val PREFS_DEVICE_ID = "device_id"
        private const val VERSION = "1"
        private const val OS = "Android"
        private const val SDK = "Android"
        /*Headers*/
        private const val HEADER_VERSION = "hv"
        private const val DEVICE_MODEL = "md"
        private const val OS_NAME = "os"
        private const val OS_VERSION = "ov"
        private const val PLATFORM = "sdk"
        private const val PLATFORM_VERSION = "pv"
        private const val DEVICE_ID = "id"
        /**
         * Returns a unique UUID for the current android device. As with all UUIDs,
         * this unique ID is "very highly likely" to be unique across all Android
         * devices. Much more so than ANDROID_ID is.
         *
         * The UUID is generated by using ANDROID_ID as the base key if appropriate,
         * falling back on TelephonyManager.getDeviceID() if ANDROID_ID is known to
         * be incorrect, and finally falling back on a random UUID that's persisted
         * to SharedPreferences if getDeviceID() does not return a usable value.
         *
         * In some rare circumstances, this ID may change. In particular, if the
         * device is factory reset a new device ID may be generated. In addition, if
         * a user upgrades their phone from certain buggy implementations of Android
         * 2.2 to a newer, non-buggy version of Android, the device ID may change.
         * Or, if a user uninstalls your app on a device that has neither a proper
         * Android ID nor a Device ID, this ID may change on reinstallation.
         *
         * Note that if the code falls back on using TelephonyManager.getDeviceId(),
         * the resulting ID will NOT change after a factory reset. Something to be
         * aware of.
         *
         * Works around a bug in Android 2.2 for many devices when using ANDROID_ID
         * directly.
         *
         * @see [http://code.google.com/p/android/issues/detail?id=10603](http://code.google.com/p/android/issues/detail?id=10603)
         *
         * @return a UUID that may be used to uniquely identify your device for most
         * purposes.
         */

    }
}