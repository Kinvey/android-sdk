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
package com.kinvey.sample.kitchensink.appData;

import android.view.View;
import android.widget.*;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.File;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.sample.kitchensink.*;
import com.kinvey.sample.kitchensink.R;

import java.io.IOException;
import java.util.Random;

/**
 * @author edwardf
 * @since 2.0
 */
public class PutFragment extends UseCaseFragment implements View.OnClickListener {

    private Spinner countToAdd;
    private Button putIt;
    private Button deleteAll;
    private TextView totalCount;

    private int addCount;

    private String[] ids;


    @Override
    public int getViewID() {
        return R.layout.feature_appdata_put;
    }

    @Override
    public void bindViews(View v) {
        countToAdd = (Spinner) v.findViewById(R.id.appdata_put_count_spinner);

        putIt = (Button) v.findViewById(R.id.appdata_put_button);
        putIt.setOnClickListener(this);

        deleteAll = (Button) v.findViewById(R.id.appdata_put_delete);
        deleteAll.setOnClickListener(this);

        countToAdd.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"1", "2", "3"}));
        countToAdd.setSelection(0);

        totalCount = (TextView) v.findViewById(R.id.appdata_put_total_count);
        totalCount.setText("0");
        getCount();







    }


    @Override
    public String getTitle() {
        return "Put";
    }

    public void putIt(final int howMany) {
        addCount = 0;

        if ( 100 < (howMany + Integer.valueOf(totalCount.getText().toString()))){
            AndroidUtil.toast(PutFragment.this, "Try something besides just creating new entities!  Delete some first.");
            return;
        }

        for (int i = 0; i < howMany; i++) {


            MyEntity ent = new MyEntity();
            ent.setName("name" + new Random().nextInt(10000));
            ent.getAccess().setGloballyWriteable(true);
            ent.getAccess().setGloballyReadable(true);

            getClient().appData(KitchenSink.collectionName, MyEntity.class).save(ent, new KinveyClientCallback<MyEntity>() {
                @Override
                public void onSuccess(MyEntity result) {
                    addCount++;


                    totalCount.setText(String.valueOf ((Integer.valueOf(totalCount.getText().toString()) + 1)));
                    if (addCount == howMany) {
                        AndroidUtil.toast(PutFragment.this, "Successfully saved " + addCount);
                        countToAdd.setSelection(0);
                    }
                }


                @Override
                public void onFailure(Throwable error) {
                    AndroidUtil.toast(PutFragment.this, "something went wrong on put ->" + error.getMessage());
                }

            });
        }


    }

    private void deleteAll(){

        if (ids == null || ids.length < 1){
            return;
        }


       getApplicationContext().getClient().appData(KitchenSink.collectionName, MyEntity.class).delete(ids[0], new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse result) {
                AndroidUtil.toast(getSherlockActivity(), "deleted " + result.getCount() + "entities!");
                getCount();
            }

            @Override
            public void onFailure(Throwable error) {
                AndroidUtil.toast(getSherlockActivity(), "something went wrong ->" + error.getMessage());
            }
        });




    }


    @Override
    public void onClick(View v) {
        if (v == putIt) {
            putIt(Integer.valueOf(countToAdd.getSelectedItem().toString()));
        }  else if(v == deleteAll){
            deleteAll();

        }
    }

    private void getCount() {

        getApplicationContext().getClient().appData(KitchenSink.collectionName, MyEntity.class).get(new KinveyListCallback<MyEntity>() {

            @Override
            public void onSuccess(MyEntity[] result) {
                totalCount.setText(String.valueOf(result.length));

                ids = new String[result.length];

                for (int i = 0 ; i < ids.length; i++){

                    ids[i] = result[i].getId();



                }

            }

            @Override
            public void onFailure(Throwable error) {

                AndroidUtil.toast(getSherlockActivity(), "something went wrong ->" + error.getMessage());
            }
        });


    }
}
