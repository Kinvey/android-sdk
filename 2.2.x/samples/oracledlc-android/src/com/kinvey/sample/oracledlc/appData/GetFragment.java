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
package com.kinvey.sample.oracledlc.appData;

import android.util.Log;
import android.view.View;
import android.widget.*;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.oracledlc.*;
import com.kinvey.sample.oracledlc.R;

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

        Query q = new Query();
        q.equals("_id", viewingID.getSelectedItem());
        q.setLimit(5);
        getApplicationContext().getClient().appData(OracleDLC.collectionName, MyPerson.class).get(q, new KinveyListCallback<MyPerson>() {


            @Override
            public void onSuccess(MyPerson[] result) {
                if (result.length > 0){
                    currentName.setText(result[0].getName());
                    currentID.setText(result[0].getId().toString());
                }
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

    private void updateSpinner(MyPerson[] result) {
        Integer[] ids = new Integer[result.length];

        for (int i = 0; i < result.length; i++) {
            ids[i] = result[i].getId();
        }

        viewingID.setAdapter(new ArrayAdapter<Integer>(getSherlockActivity(), android.R.layout.simple_spinner_dropdown_item, ids));


    }

    private void getCount() {

        getApplicationContext().getClient().appData(OracleDLC.collectionName, MyPerson.class).get(new KinveyListCallback<MyPerson>() {
            @Override
            public void onSuccess(MyPerson[] result) {
                Log.d(OracleDLC.TAG, ">>>>>> result count: "+result.length);
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
