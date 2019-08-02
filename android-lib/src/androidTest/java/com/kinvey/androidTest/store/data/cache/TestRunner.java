package com.kinvey.androidTest.store.data.cache;

import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.test.runner.AndroidJUnitRunner;

/**
 * Created by Prots on 1/27/16.
 */
public class TestRunner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());
        super.onCreate(arguments);
    }
}
