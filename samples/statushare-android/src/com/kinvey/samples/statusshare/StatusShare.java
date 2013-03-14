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
package com.kinvey.samples.statusshare;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.http.HttpTransport;
import com.kinvey.android.Client;
import com.kinvey.samples.statusshare.fragments.LoginFragment;
import com.kinvey.samples.statusshare.fragments.ShareFragment;
import com.kinvey.samples.statusshare.model.Update;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Main Activity, it starts login then redirects to list view of status updates.
 *
 * @author edwardf
 * @since 2.0
 */
public class StatusShare extends SherlockFragmentActivity {

    private static final Level LOGGING_LEVEL = Level.FINEST;

    public static final String TAG = "Kinvey - StatusShare";
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
    private static final int THUMBNAIL_SIZE = 64;

    private List<Update> mUpdates;
    private Uri mImageCaptureUri;



    public Bitmap bitmap = null;
    public Bitmap thumbnail = null;
    public String path = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_share);
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);
        addFragment(new LoginFragment());




    }

    @Override
    public void onResume(){
        super.onResume();
//        if (bitmap != null && path != null) {
//            replaceFragment(ShareFragment.newInstance(bitmap, path), false);
//        } else{
//        }

    }

    public void addFragment(SherlockFragment frag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragmentBox, frag, "login");
        Log.i(StatusShare.TAG, "showing login fragment");
        ft.commit();
    }

    public void replaceFragment(SherlockFragment frag, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentBox, frag);
        if(addToBackStack){
            ft.addToBackStack("");
        }
        ft.commit();
    }

//    public void removeFragment(SherlockFragment frag){
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.remove(frag);
//        ft.commit();
//
//
//    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }


        if (requestCode == PICK_FROM_FILE) {
//            mImageCaptureUri = data.getData();
            path = getRealPathFromURI(data.getData()); //from Gallery

            if (path == null) {
                path = data.getData().getPath(); //from File Manager
            }

            if (path != null) {
                bitmap = BitmapFactory.decodeFile(path);
            }
            Log.i(Client.TAG,  "activity result, bitmap from file is -> " + String.valueOf(bitmap == null)) ;

        } else if(requestCode == PICK_FROM_CAMERA){
            path = mImageCaptureUri.getPath();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            options.inJustDecodeBounds = true;


            // First decode with inJustDecodeBounds=true to check dimensions
            bitmap = BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 200 , 150);

            options.inJustDecodeBounds = false;

            // Decode bitmap with inSampleSize set
//            bitmap = BitmapFactory.decodeFile(path, options);

            int h = 64; // height in pixels
            int w = 64; // width in pixels
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path, options), h, w, false);

            //bitmap = BitmapFactory.decodeFile(mImageCaptureUri.getPath());
            Log.i(Client.TAG,  "activity result, bitmap from camera is -> " + String.valueOf(bitmap == null)) ;
            Log.i(Client.TAG,  "activity result, path from camera is -> " + String.valueOf(path == null)) ;

        } else{
            Log.e(TAG, "That's not a valid request code! -> " + requestCode);
        }



    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor  = managedQuery(contentUri, proj, null, null, null);

        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(columnIndex);
    }


    public List<Update> getmUpdates() {
        return mUpdates;
    }

    public void setmUpdates(List<Update> mUpdates) {
        this.mUpdates = mUpdates;
    }

    public void startCamera(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),
                "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        mImageCaptureUri = Uri.fromFile(file);

        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);

            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea < resultArea) {
                    result=size;
                }
            }
        }

        return(result);
    }

    public void startFilePicker(){
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int stretch_width = Math.round((float)width / (float)reqWidth);
        int stretch_height = Math.round((float)height / (float)reqHeight);

        if (stretch_width <= stretch_height) return stretch_height;
        else return stretch_width;
    }
}

