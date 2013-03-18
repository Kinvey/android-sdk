/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
