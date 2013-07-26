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

import com.kinvey.sample.kitchensink.FeatureActivity;
import com.kinvey.sample.kitchensink.UseCaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 * @since 2.0
 */
public class AppDataActivity extends FeatureActivity {


    @Override
    public List<UseCaseFragment> getFragments() {

        ArrayList<UseCaseFragment> ret = new ArrayList<UseCaseFragment>();
        PutFragment put = new PutFragment();
        ret.add(put);

        GetFragment get = new GetFragment();
        ret.add(get);

        QueryFragment query = new QueryFragment();
        ret.add(query);

        AggregateFragment agg = new AggregateFragment();
        ret.add(agg);

        return ret;
    }
}
