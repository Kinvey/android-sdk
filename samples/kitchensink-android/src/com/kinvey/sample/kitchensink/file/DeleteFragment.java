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
import android.widget.Toast;

import java.io.File;

import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author m0rganic
 * @since 2.0
 */
public class DeleteFragment extends UseCaseFragment implements View.OnClickListener {

    private Button bDelete;

    @Override
    public int getViewID() {
        return R.layout.feature_file_delete;
    }

    @Override
    public void bindViews(View v) {
        bDelete = (Button) v.findViewById(R.id.file_delete_button);
        bDelete.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Delete";
    }

    private File getTarget() {
        return(new File(getSherlockActivity().getFilesDir(), FileActivity.FILENAME));
    }

    private void delete() {

        Query q = new Query();
//        q.equals("_filename", FileActivity.FILENAME);
        FileMetaData meta = new FileMetaData(FileActivity.FILENAME);
        meta.setId(FileActivity.FILENAME);
        getApplicationContext().getClient().file().delete(meta, new KinveyClientCallback<KinveyDeleteResponse>() {
            @Override
            public void onSuccess(KinveyDeleteResponse result) {
                File file = getTarget();

                if (file.exists()) {
                    file.delete();
                }

                Log.i(KitchenSink.TAG, "deleted " + file.getName() + " file.");
                Toast.makeText(getSherlockActivity(), "Delete finished.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(KitchenSink.TAG, "failed to delete: " + getTarget().getName() + " file.", error);
                Toast.makeText(getSherlockActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == bDelete) {
            delete();
        }
    }
}
