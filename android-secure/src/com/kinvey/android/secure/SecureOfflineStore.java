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

import android.content.Context;
import android.content.Intent;
import com.kinvey.android.offline.AbstractSqliteOfflineStore;
import com.kinvey.android.offline.AbstractSyncService;
import com.kinvey.android.offline.DatabaseHandler;
import com.kinvey.android.offline.KinveySyncService;

/**
 * This class is an implementation of an {@link AbstractSqliteOfflineStore}, which provides a sqlcipher encrypted sqlite3 database
 *
 * @author edwardf
 */
public class SecureOfflineStore<T> extends AbstractSqliteOfflineStore<T>{

    public SecureOfflineStore(Context context){
        super(context);
    }

    @Override
    protected DatabaseHandler getDatabaseHandler(String userid) {
        return SecureHelper.getInstance(getContext(), userid);
    }


    @Override
    public void kickOffSync(){
        Intent syncIt = new Intent(getContext(), SecureSyncService.class);
        syncIt.setAction(AbstractSyncService.ACTION_OFFLINE_SYNC);
        this.getContext().startService(syncIt);

    }
}