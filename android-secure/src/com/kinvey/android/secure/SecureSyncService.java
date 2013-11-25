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
package com.kinvey.android.secure;

import android.content.Intent;
import com.kinvey.android.offline.AbstractSyncService;
import com.kinvey.android.offline.DatabaseHandler;

/**
 * @author edwardf
 */
public class SecureSyncService extends AbstractSyncService {

    public SecureSyncService(){
        super("Kinvey Secure Sync");
    }

    @Override
    protected DatabaseHandler getDatabaseHandler(String userid) {
        return SecureHelper.getInstance(getApplicationContext(), userid);
    }
}
