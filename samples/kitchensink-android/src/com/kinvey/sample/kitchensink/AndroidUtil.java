/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
