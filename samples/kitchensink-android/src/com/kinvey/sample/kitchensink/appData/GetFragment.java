/*
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
package com.kinvey.sample.kitchensink.appData;

import android.*;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.*;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.kitchensink.*;
import com.kinvey.sample.kitchensink.R;

/**
 * @author edwardf
 * @since 2.0
 */
public class GetFragment extends UseCaseFragment implements View.OnClickListener {

    private TextView currentCount;
    private Spinner viewingID;
    private TextView currentName;
    private TextView currentID;
    private Button getIt;


    @Override
    public void onResume() {
        super.onResume();
        getCount();

    }

    @Override
    public int getViewID() {
        return R.layout.feature_appdata_get;
    }

    @Override
    public void bindViews(View v) {
        currentCount = (TextView) v.findViewById(R.id.appdata_get_current_count);
        viewingID = (Spinner) v.findViewById(R.id.appdata_get_id_spinner);
        currentName = (TextView) v.findViewById(R.id.appdata_get_name_value);
        currentID = (TextView) v.findViewById(R.id.appdata_get_id_value);
        getIt = (Button) v.findViewById(R.id.appdata_get_button);

        getIt.setOnClickListener(this);
        viewingID.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"--"}));
    }


    @Override
    public String getTitle() {
        return "Get";
    }


    public void getIt() {

        getCount();

        if (viewingID.getSelectedItem() == null){
            return;
        }


        getApplicationContext().getClient().appData(KitchenSink.collectionName, MyEntity.class).getEntity(viewingID.getSelectedItem().toString(), new KinveyClientCallback<MyEntity>() {
            @Override
            public void onSuccess(MyEntity result) {
                currentName.setText(result.getName());
                currentID.setText(result.getId());
            }

            @Override
            public void onFailure(Throwable error) {
                AndroidUtil.toast(GetFragment.this, "something went wrong ->" + error.getMessage());


            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == getIt) {
            getIt();
        }
    }

    private void updateSpinner(MyEntity[] result) {


        String[] ids = new String[result.length];

        for (int i = 0; i < result.length; i++) {
            ids[i] = result[i].getId();
        }

        viewingID.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_spinner_dropdown_item, ids));


    }

    private void getCount() {

        getApplicationContext().getClient().appData(KitchenSink.collectionName, MyEntity.class).get(new KinveyListCallback<MyEntity>() {
            @Override
            public void onSuccess(MyEntity[] result) {
                currentCount.setText(String.valueOf(result.length));
                updateSpinner(result);
            }

            @Override
            public void onFailure(Throwable error) {
                AndroidUtil.toast(getSherlockActivity(), "something went wrong ->" + error.getMessage());
            }
        });


    }
}
