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

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author edwardf
 * @since 2.2
 */
public class GCMPushOptions implements PushOptions {

    private String pushAppKey;
    private String pushAppSecret;
    private String gcmSender;
    private boolean inProduction;


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
    public String getAPIKey() {
        return gcmSender;
    }

    @Override
    public void setPushAppKey(String appKey) {
        this.pushAppKey = appKey;
    }

    @Override
    public void setPushAppSecret(String appSecret) {
        this.pushAppSecret = appSecret;
    }

    public void setAPIKey(String gcmSender) {
        this.gcmSender = gcmSender;
    }


    public static class PushConfig extends GenericJson{

        @Key("GCM")
        private PushConfigField gcm;
        @Key("GCM_dev")
        private PushConfigField gcmDev;

        public PushConfig(){}


        public PushConfigField getGcm() {
            return gcm;
        }

        public void setGcm(PushConfigField gcm) {
            this.gcm = gcm;
        }

        public PushConfigField getGcmDev() {
            return gcmDev;
        }

        public void setGcmDev(PushConfigField gcmDev) {
            this.gcmDev = gcmDev;
        }
    }

    public static class PushConfigField extends GenericJson{
        @Key
        private String[] ids;
        @Key("notification_key")
        private String notificationKey;

        public PushConfigField(){}

        public String[] getIds() {
            return ids;
        }

        public void setIds(String[] ids) {
            this.ids = ids;
        }

        public String getNotificationKey() {
            return notificationKey;
        }

        public void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }
    }
}
