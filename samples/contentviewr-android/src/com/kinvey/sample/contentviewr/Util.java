/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.sample.contentviewr;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author edwardf
 */
public class Util {


    public static void Error(Context context, Throwable msg){
        Toast.makeText(context, msg.getMessage(), Toast.LENGTH_SHORT);
        Log.e(Contentviewr.TAG, "something went wrong ->" + msg.getMessage());
        msg.printStackTrace();
    }

    public static void Error(SherlockFragment frag, Throwable msg){
        Util.Error(frag.getSherlockActivity(), msg);
    }


}
