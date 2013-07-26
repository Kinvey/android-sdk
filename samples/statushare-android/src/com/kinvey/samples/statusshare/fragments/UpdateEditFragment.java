/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.statusshare.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import com.kinvey.android.Client;
import com.kinvey.java.LinkedResources.LinkedFile;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.samples.statusshare.R;
import com.kinvey.samples.statusshare.StatusShare;
import com.kinvey.samples.statusshare.model.UpdateEntity;

import java.io.*;

/**
 * @author edwardf
 * @since 2.0
 */
public class UpdateEditFragment extends KinveyFragment implements View.OnClickListener {


    private ImageView attachmentImage;
    private AlertDialog mDialog;

    private EditText updateText;
    private Bitmap image;
    private TextView title;
    private TextView attachmentTitle;

    public UpdateEditFragment() {
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


        if (this.image != null) {
            Log.i(Client.TAG, "setting imageview");
            attachmentImage.setBackgroundDrawable(null);
            attachmentImage.setImageBitmap(this.image);
        } else {

            Log.i(Client.TAG, "not setting imageview");
        }
    }

    @Override
    public int getViewID() {
        return R.layout.fragment_write_update;
    }

    @Override
    public void bindViews(View v) {

        attachmentImage = (ImageView) v.findViewById(R.id.preview);
        updateText = (EditText) v.findViewById(R.id.update);
        title = (TextView) v.findViewById(R.id.share_title);
        attachmentTitle = (TextView) v.findViewById(R.id.share_attach_title);
        title.setTypeface(getRoboto());
        attachmentTitle.setTypeface(getRoboto());
        updateText.setTypeface(getRoboto());

        attachmentImage.setOnClickListener(this);
    }


    public void doUpdate() {
        final ProgressDialog progressDialog = ProgressDialog.show(getSherlockActivity(), "",
                "Posting. Please wait...", true);

        byte[] byteArray = null;
        if (image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {

            }
        }


        saveUpdateAttachment(progressDialog, byteArray, getClient().user().getId() + "_attachment_" + System.currentTimeMillis() + ".png");
    }


    public void saveUpdateAttachment(final ProgressDialog progressDialog, byte[] bytes, String filename) {
        UpdateEntity updateEntity = new UpdateEntity(getClient().user().getId());
        updateEntity.setText(updateText.getText().toString());
        updateEntity.getAcl().setGloballyReadable(true);

        android.util.Log.d(Client.TAG, "updateEntity.getMeta().isGloballyReadable() = " + updateEntity.getAcl().isGloballyReadable());

        if (bytes != null && filename != null) {
            Log.i(Client.TAG, "there is an attachment!");
            updateEntity.putFile("attachment", new LinkedFile(filename));


        }
        final ByteArrayInputStream bais = ((bytes == null) ? null : new ByteArrayInputStream(bytes));
        if (bais != null){
            updateEntity.getFile("attachment").setInput(bais);
        }

        getClient().linkedData(StatusShare.COL_UPDATES, UpdateEntity.class).save(updateEntity, new KinveyClientCallback<UpdateEntity>() {

            @Override
            public void onSuccess(UpdateEntity result) {
                if (getSherlockActivity() == null){
                    return;
                }
                android.util.Log.d(Client.TAG, "postUpdate: SUCCESS _id = " + result.getId() + ", gr = " + result.getAcl().isGloballyReadable());
                progressDialog.dismiss();

                try {
                    bais.close();
                } catch (Exception e) {
                }

                InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(getSherlockActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(updateText.getWindowToken(), 0);

                ((StatusShare) getSherlockActivity()).bitmap = null;

                if (getSherlockActivity() != null) {
                    ((StatusShare)getSherlockActivity()).setShareList(null);

                    ((StatusShare)getSherlockActivity()).replaceFragment(new ShareListFragment(), false);

//                    ((StatusShare)getSherlockActivity()).removeFragment(UpdateEditFragment.this);
//                    ((StatusShare)((StatusShare) getSherlockActivity()).removeFragment(getSherlockActivity().getSupportFragmentManager().);)
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (getSherlockActivity() == null){
                    return;
                }
                Log.d(Client.TAG, "failed to upload linked app data");
                e.printStackTrace();
                progressDialog.dismiss();
            }

        }, null
        );
//        } else {
//            Log.i(Client.TAG, "there is no attachment");
//        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_edit_share, menu);
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
        if (v == attachmentImage) {
            mDialog.show();

        }
    }
}
