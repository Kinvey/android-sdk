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

import android.view.View;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 * @since 2.0
 */
public class AggregateFragment extends UseCaseFragment{
    @Override
    public int getViewID() {
        return R.layout.feature_appdata_aggregate;
    }

    @Override
    public void bindViews(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTitle() {
        return "Aggregates";
    }
}
