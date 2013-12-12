/*
 * Copyright (c) 2013 Kinvey Inc.
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
package com.kinvey.sample.contentviewr;

import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.kinvey.sample.contentviewr.core.ContentFragment;

/**
 * @author edwardf
 */
public class NotificationFragment extends ContentFragment {


    TextView notifyTitle;
    TextView updatesLabel;
    Switch updates;

    private Typeface roboto;

    @Override
    public int getViewID() {
        return R.layout.fragment_notification;
    }

    @Override
    public void bindViews(View v) {
        roboto = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Thin.ttf");

        notifyTitle = (TextView) v.findViewById(R.id.notify_update_title);
        updatesLabel = (TextView) v.findViewById(R.id.notify_update_label);
        updates = (Switch) v.findViewById(R.id.notify_updates);

        notifyTitle.setTypeface(roboto);
        updatesLabel.setTypeface(roboto);
    }

    @Override
    public String getTitle() {
        return "Notifications";
    }


}
