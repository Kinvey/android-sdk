/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink.push;

import com.kinvey.sample.kitchensink.FeatureActivity;
import com.kinvey.sample.kitchensink.UseCaseFragment;

import java.util.Arrays;
import java.util.List;

public class PushActivity extends FeatureActivity {

    @Override
    public List<UseCaseFragment> getFragments() {
        return Arrays.asList(new UseCaseFragment[] {
                new PushFragment()
        });
    }
}