/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.samples.statusshare.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import com.kinvey.android.Client;
import com.kinvey.java.LinkedResources.LinkedFile;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.samples.statusshare.R;
import com.kinvey.samples.statusshare.StatusShare;
import com.kinvey.samples.statusshare.model.UpdateEntity;

import java.io.*;

/**
 * @author edwardf
 * @since 2.0
 */
public class ShareFragment extends KinveyFragment implements View.OnClickListener {


    private Boolean mLocked;

    private String mPath;
    private ImageView mImageView;
    private AlertDialog mDialog;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;

    private EditText updateText;
    private Bitmap image;

    public ShareFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        final String[] items = new String[]{"From Camera", "From SD Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    ((StatusShare) getSherlockActivity()).startCamera();


                    dialog.cancel();
                } else {

                    ((StatusShare) getSherlockActivity()).startFilePicker();
                    dialog.cancel();


                }
            }
        });

        mDialog = builder.create();


    }

    @Override
    public void onResume() {
        super.onResume();
        this.image = ((StatusShare) getSherlockActivity()).bitmap;
        this.mPath = ((StatusShare) getSherlockActivity()).path;


        if (this.image != null) {
            Log.i(Client.TAG, "not setting imageview");
            mImageView.setImageBitmap(this.image);
        } else {

            Log.i(Client.TAG, "not setting imageview");
        }
    }
//
//    public static ShareFragment newInstance(Bitmap bitmap, String path) {
//        ShareFragment ret = new ShareFragment();
//        ret.mPath = path;
//        ret.image = bitmap;
//        return ret;
//    }


    @Override
    public int getViewID() {
        return R.layout.write_update;
    }

    @Override
    public void bindViews(View v) {

        mImageView = (ImageView) v.findViewById(R.id.preview);
        updateText = (EditText) v.findViewById(R.id.update);

        mImageView.setOnClickListener(this);


        mLocked = false;
        mPath = null;


        final String[] items = new String[]{"From Camera", "From SD Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    ((StatusShare) getSherlockActivity()).startCamera();


                    dialog.cancel();
                } else {

                    ((StatusShare) getSherlockActivity()).startFilePicker();
                    dialog.cancel();


                }
            }
        });

        mDialog = builder.create();


    }


    public void doAttachement() {
        mDialog.show();
    }

    public void doUpdate() {
        final ProgressDialog progressDialog = ProgressDialog.show(getSherlockActivity(), "",
                "Posting. Please wait...", true);





        //createBlocking a file to write bitmap data
        File f = new File(getSherlockActivity().getCacheDir(), getClient().getClientUsers().getCurrentUser() + "_attachment_" + System.currentTimeMillis() + ".png");
        try{
        f.createNewFile();
        }catch(Exception e ){}







        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        try{
            stream.close();
        }catch (IOException e){

        }



        saveUpdateAttachment(progressDialog, byteArray, getClient().getClientUsers().getCurrentUser() + "_attachment_" + System.currentTimeMillis() + ".png");
    }


    public void saveUpdateAttachment(final ProgressDialog progressDialog, byte[] bytes, String filename) {
        UpdateEntity updateEntity = new UpdateEntity();
        updateEntity.setText(updateText.getText().toString());
        updateEntity.getAcl().setGloballyReadable(!mLocked);

        android.util.Log.d(Client.TAG, "updateEntity.getMeta().isGloballyReadable() = " + updateEntity.getAcl().isGloballyReadable());

        if (bytes != null && filename != null) {
            Log.i(Client.TAG, "there is an attachment!");
            //TODO edwardf this is sloppy.  Once it works clean up stream management.

            updateEntity.putFile("attachment", new LinkedFile(filename));
            updateEntity.getFile("attachment").setInput(new ByteArrayInputStream(bytes));


            getClient().linkedData("Updates", UpdateEntity.class).save(updateEntity, new KinveyClientCallback<UpdateEntity>() {

                @Override
                public void onSuccess(UpdateEntity result) {
                    android.util.Log.d(Client.TAG, "postUpdate: SUCCESS _id = " + result.getId() + ", gr = " + result.getAcl().isGloballyReadable());
                    android.util.Log.d(Client.TAG, "success complete!");
                    progressDialog.dismiss();
                    if (getSherlockActivity() != null){
                        ((StatusShare) getSherlockActivity()).replaceFragment(new ShareListFragment(), false);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    ((StatusShare) getSherlockActivity()).replaceFragment(new ShareListFragment(), false);
                }

            }, null, null);
        } else {
            Log.i(Client.TAG, "there is no attachment");
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.update, menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_post:
                doUpdate();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (v == mImageView) {
            doAttachement();
        }
    }
}
