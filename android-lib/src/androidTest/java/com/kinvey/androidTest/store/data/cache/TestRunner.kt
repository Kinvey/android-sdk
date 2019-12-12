package com.kinvey.androidTest.store.data.cache

import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.runner.AndroidJUnitRunner

/**
 * Created by Prots on 1/27/16.
 */
class TestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        MultiDex.install(targetContext)
        super.onCreate(arguments)
    }
}