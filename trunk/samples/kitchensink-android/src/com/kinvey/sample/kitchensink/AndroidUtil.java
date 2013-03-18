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
 package com.kinvey.sample.kitchensink;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author edwardf
 * @since 2.0
 */
public class AndroidUtil {

    /**
     * Hide the keyboard in a given context from the associated View.
     * <p/>
     * <p>
     *     Usually v will be an {@code android.widget.EditText}
     * </p>
     *
     * @param context the current activity's context
     * @param v the view object currently holding focus, with an exposed keyboard
     */
    public static void hideKeyboard(Context context, View v){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * convenience wrapper for toasting from a fragment
     *
     * @param fragment - the visible fragment
     * @param message - the message to be displayed in the toast
     */
    public static void toast(SherlockFragment fragment, String message){
        Toast.makeText(fragment.getSherlockActivity(), message, Toast.LENGTH_SHORT).show();
    }













}
