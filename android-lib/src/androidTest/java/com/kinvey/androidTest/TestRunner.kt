package com.kinvey.androidTest

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.runner.AndroidJUnitRunner
import com.github.tmurakami.dexopener.DexOpener

/**
 * Created by Prots on 1/27/16.
 */
class TestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        MultiDex.install(targetContext)
        super.onCreate(arguments)
    }

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        // MockK supports for mocking final classes on Android 9+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            DexOpener.install(this)
        }
        return super.newApplication(cl, className, context)
    }
}