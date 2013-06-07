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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.kinvey.java.Query;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 * @since 2.0
 */
public class QueryFragment extends UseCaseFragment implements View.OnClickListener{

    private Query q;

    private Button resetQuery;
    private Button lessThan;
    private Button greaterThan;
    private Button equals;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        q = new Query();
    }

    @Override
    public int getViewID() {
        return R.layout.feature_appdata_query;
    }

    @Override
    public void bindViews(View v) {
        resetQuery = (Button) v.findViewById(R.id.appdata_query_reset);
        lessThan = (Button) v.findViewById(R.id.appdata_query_less_than);
        greaterThan = (Button) v.findViewById(R.id.appdata_query_greater_than);
        equals = (Button) v.findViewById(R.id.appdata_query_equals);

        resetQuery.setOnClickListener(this);
        lessThan.setOnClickListener(this);
        greaterThan.setOnClickListener(this);
        equals.setOnClickListener(this);


    }

    @Override
    public String getTitle() {
        return "Query";
    }

    @Override
    public void onClick(View v) {
        if (v == resetQuery){
            q = new Query();
        }else if (v == lessThan){
            q.greaterThan("", "");
        }else if (v == greaterThan){
            q.lessThan("","");
        }else if (v == equals){
            q.equals("","");
        }
    }
}
