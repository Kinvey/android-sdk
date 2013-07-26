/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.push;

/**
 *
 * @author mjsalinger
 * @since 2.0
 */
public class UrbanAirshipPushOptions implements PushOptions {
    private String pushAppKey;
    private String pushAppSecret;
    private String gcmSender;
    private boolean inProduction;
    private TransportType transport;

    public enum TransportType {
        GCM,
        HELIUM,
        HYBRID
    }


    UrbanAirshipPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction, String gcmSender,
                                   TransportType type) {
        this.pushAppKey = pushAppKey;
        this.pushAppSecret = pushAppSecret;
        this.gcmSender = gcmSender;
        this.inProduction = inProduction;
        this.transport = type;
    }

    public String getAPIKey() {
        return gcmSender;
    }

    public void setAPIKey(String gcmSender) {
        this.gcmSender = gcmSender;
    }


    @Override
    public boolean isInProduction() {
        return inProduction;
    }

    @Override
    public void setInProduction(boolean inProduction) {
        this.inProduction = inProduction;
    }

    @Override
    public String getPushAppKey() {
        return pushAppKey;
    }

    @Override
    public String getPushAppSecret() {
        return pushAppSecret;
    }

    @Override
    public void setPushAppKey(String pushAppKey) {
        this.pushAppKey = pushAppKey;
    }

    @Override
    public void setPushAppSecret(String pushAppSecret) {
        this.pushAppSecret = pushAppSecret;
    }


    public String getTransportType() {
        return transport.toString();
    }
}
