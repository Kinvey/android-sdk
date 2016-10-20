package com.kinvey.android.async;


import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.java.Logger;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AsyncDownloadRequest<T> extends AsyncRequest<T> {

    DownloaderProgressListener listener;

    public AsyncDownloadRequest(Object scope, Method method, final AsyncDownloaderProgressListener<T> callback, Object... args) {
        super(scope, method, callback, args);
        listener = new DownloaderProgressListener() {
            @Override
            public void progressChanged(final MediaHttpDownloader downloader) throws IOException {
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (getCallback() != null) {
                            Logger.INFO("notifying callback");
                            try {
                                callback.progressChanged(downloader);
                            } catch (IOException e) {
                                callback.onFailure(e);
                            }
                        }

                    }
                };
                kinveyCallbackHandler.post(myRunnable);
            }
        };
    }

    @Override
    public T executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        Object[] newArgs = new Object[args.length + 1];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = args[i];
        }
        newArgs[args.length] = listener;
        T ret = (T) mMethod.invoke(scope, newArgs);
        return ret;
    }

}
