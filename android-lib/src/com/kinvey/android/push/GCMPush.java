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

import android.app.Application;
import com.kinvey.android.Client;

/**
 * @author edwardf
 * @since 2.2
 */
public class GCMPush extends AbstractPush{


    protected GCMPush(Client client) {
        super(client);
    }

    @Override
    public AbstractPush initialize(PushOptions options, Application currentApp) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPushId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPushEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disablePush() throws PushRegistrationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PushOptions getPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
