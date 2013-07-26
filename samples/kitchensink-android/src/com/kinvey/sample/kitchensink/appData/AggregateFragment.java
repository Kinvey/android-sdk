/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
