package com.kinvey.android

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class KinveyHandlerThread : HandlerThread {

    private var mWorkerHandler: Handler? = null
    private var pendingQueue: ConcurrentLinkedQueue<Runnable>? = null
    private var executor: ExecutorService? = null

    constructor(name: String, priority: Int) : super(name, priority) {
        pendingQueue = ConcurrentLinkedQueue()
    }

    constructor(name: String) : super(name) {
        pendingQueue = ConcurrentLinkedQueue()
    }

    @Synchronized
    fun postTask(task: Runnable) {
        if (Client.sharedInstance.isClientRequestMultithreading) {
            if (executor == null) {
                executor = Executors.newFixedThreadPool(Client.sharedInstance.numberThreadPool)
            }
            executor?.submit(task)
        } else {
            if (mWorkerHandler != null) {
                mWorkerHandler?.post(task)
            } else {
                pendingQueue?.add(task)
            }
        }
    }

    @Synchronized
    override fun onLooperPrepared() {
        mWorkerHandler = Handler(looper)
        pendingQueue?.onEach { task -> postTask(task) }
    }

    @Synchronized
    internal fun stopHandlerThread() {
        if (mWorkerHandler != null) {
            mWorkerHandler?.post {
                Looper.myLooper()?.quit()
                mWorkerHandler?.removeCallbacksAndMessages(null)
            }
        }
    }

}
