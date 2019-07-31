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

package com.kinvey.android;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.google.api.client.json.GenericJson;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

/**
 * @see <a href="http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id">
 *       http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
 *     </a>
 */
class UuidFactory {

    /** Constant <code>PREFS_FILE="device_id.xml"</code> */
    protected static final String PREFS_FILE = "device_id.xml";
    /** Constant <code>PREFS_DEVICE_ID="device_id"</code> */
    protected static final String PREFS_DEVICE_ID = "device_id";

    private static final String VERSION = "1";
    private static final String OS = "Android";
    private static final String SDK = "Android";

    /*Headers*/
    private static final String HEADER_VERSION = "hv";
    private static final String DEVICE_MODEL = "md";
    private static final String OS_NAME = "os";
    private static final String OS_VERSION = "ov";
    private static final String PLATFORM = "sdk";
    private static final String PLATFORM_VERSION = "pv";
    private static final String DEVICE_ID = "id";

    /** Constant <code>uuid</code> */
    protected static UUID uuid;

    /**
     * <p>Constructor for UuidFactory.</p>
     *
     * @param context a {@link android.content.Context} object.
     */
    public UuidFactory(Context context) {

        if (uuid == null) {
            synchronized (UuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context
                            .getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);

                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        uuid = UUID.fromString(id);

                    } else {

                        final String androidId =
                              Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

                        // Use the Android ID unless it's broken, in which case
                        // fallback on deviceId, unless it's not available, then
                        // fallback on a random number which we store to a prefs file
                        try {
                            if (androidId != null && !"9774d56d682e549c".equals(androidId)) {
                                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                            } else {
                                String deviceId = phoneDeviceId(context);
                                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                            }
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        // Write the value out to the prefs file
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                    }
                }
            }
        }
    }

    private String phoneDeviceId(Context context) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                deviceId = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        } else {
            deviceId = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        return deviceId;
    }

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
     * @see <a href="http://code.google.com/p/android/issues/detail?id=10603">http://code.google.com/p/android/issues/detail?id=10603</a>
     * @return a UUID that may be used to uniquely identify your device for most
     *         purposes.
     */
    public UUID getDeviceUuid() {
        return uuid;
    }


    /**
     * <p>getDeviceInfoHeader</p>
     *
     * @param context a {@link android.content.Context} object.
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceInfoHeader(final Context context) {
        // Device Manufacturer
        String ma;
        try {
            ma = Build.class.getDeclaredField("MANUFACTURER").get(Build.class).toString().replace(" ", "_");
        } catch (final Throwable e) {
            ma = "UNKNOWN";
        }
        // Device Model
        final String devModel = Build.MODEL.replace(" ", "_");
        // OS Version
        final String osVersion = Build.VERSION.RELEASE.replace(" ", "_");
        // UUID
        final UUID uuid = getDeviceUuid();
        return String.format("%s/%s %s %s %s", ma, devModel, OS_NAME, osVersion, uuid.toString());
    }

    /**
     * <p>getDeviceInfoHeader</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceInfoHeader(String sdkVersion) {
        // Device Model
        final String devModel = Build.MODEL.replace(" ", "_");
        // OS Version
        final String osVersion = Build.VERSION.RELEASE.replace(" ", "_");
        // UUID
        final UUID uuid = getDeviceUuid();

        GenericJson content = new GenericJson();
        content.put(HEADER_VERSION, VERSION);
        content.put(DEVICE_MODEL, devModel);
        content.put(OS_NAME, OS);
        content.put(OS_VERSION, osVersion);
        content.put(PLATFORM, SDK);
        content.put(PLATFORM_VERSION, sdkVersion);
        content.put(DEVICE_ID, uuid.toString());
        return new Gson().toJson(content);
    }



}
