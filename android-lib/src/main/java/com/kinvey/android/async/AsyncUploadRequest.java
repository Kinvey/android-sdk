package com.kinvey.android.async;


import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.java.Logger;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class AsyncUploadRequest<T> extends AsyncRequest<T> {

    UploaderProgressListener listener;

    public AsyncUploadRequest(Object scope, Method method, final AsyncUploaderProgressListener<T> callback, Object... args) {
        super(scope, method, callback, args);
        listener = new UploaderProgressListener() {
            @Override
            public void progressChanged(final MediaHttpUploader uploader) throws IOException {
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (getCallback() != null) {
                            Logger.INFO("notifying callback");
                            try {
                                callback.progressChanged(uploader);
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
        Object[] newArgs = Arrays.copyOf(args, args.length + 1);
        newArgs[args.length] = listener;
        T ret = (T) mMethod.invoke(scope, newArgs);
        return ret;
    }
}
