/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.kitchensink.file;

import com.kinvey.sample.kitchensink.FeatureActivity;
import com.kinvey.sample.kitchensink.UseCaseFragment;

import java.util.Arrays;
import java.util.List;


public class FileActivity extends FeatureActivity {

    static final String FILENAME = "sample.txt";

    @Override
    public List<UseCaseFragment> getFragments() {
        return Arrays.asList(new UseCaseFragment[] {
                new UploadFragment(),
                new DownloadFragment(),
                new DeleteFragment()
        });
    }
}