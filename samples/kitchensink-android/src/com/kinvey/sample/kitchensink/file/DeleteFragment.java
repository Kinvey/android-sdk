/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
