/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink.file;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author m0rganic
 * @since 2.0
 */
public class DownloadFragment extends UseCaseFragment implements View.OnClickListener {

    Button bDownload;
    TextView tProgress;

    @Override
    public void onClick(View v) {
        if (v == bDownload) {
            try {
                tProgress.setText(null);
                download(getTarget());
            } catch (IOException e) {
                boom(e);
            }
        }
    }

    private void boom(Exception e) {
        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG)
                .show();
        Log.e(KitchenSink.TAG, "Exception downloading file", e);
    }


    private File getTarget() {
        return(new File(getSherlockActivity().getFilesDir(), FileActivity.FILENAME));
    }

    private void download(File target) throws IOException {
        FileOutputStream fos= new FileOutputStream(target);

        // call kinvey specific task to perform download
        FileMetaData meta = new FileMetaData(FileActivity.FILENAME);
        meta.setId(FileActivity.FILENAME);
        getApplicationContext().getClient().file().download(meta, fos, new DownloaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                Log.i(KitchenSink.TAG, "progress updated: "+downloader.getDownloadState());
                final String state = new String(downloader.getDownloadState().toString());

                getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tProgress.setText((tProgress.getText() == null) ? state
                                : tProgress.getText() + "\n" + state);
                    }
                });
            }

            @Override
            public void onSuccess(Void result) {
                Log.i(KitchenSink.TAG, "successfully download: " + getTarget().getName() + " file.");
                Toast.makeText(getSherlockActivity(), "Download finished.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(KitchenSink.TAG, "failed to download: "+ getTarget().getName()+" file.", error);
                Toast.makeText(getSherlockActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getViewID() {
        return R.layout.feature_file_download;
    }

    @Override
    public void bindViews(View v) {
       bDownload = (Button) v.findViewById(R.id.file_download_button);
       bDownload.setOnClickListener(this);
       tProgress = (TextView) v.findViewById(R.id.file_download_progress);
    }

    @Override
    public String getTitle() {
        return "Download";
    }
}
