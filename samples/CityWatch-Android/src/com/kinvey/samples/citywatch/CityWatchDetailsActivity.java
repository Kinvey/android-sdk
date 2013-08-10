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
package com.kinvey.samples.citywatch;

import android.os.Bundle;
import android.service.textservice.SpellCheckerService;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.HashMap;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class CityWatchDetailsActivity extends SherlockFragmentActivity {

    //	// This Activity manages two fragments: edit and view
	// The action bar is dependant on the fragment.

/*	private static final String TAG = CityWatchApplication.TAG;

	public static final String EXTRA_FRAG_TARGET = "EXTRA_FRAG_TARGET";
	public static final String EXTRA_ENTITY = "EXTRA_ENTITY";

	public static final int FRAG_LOGIN = 0;
	public static final int FRAG_EDIT = 1;
	public static final int FRAG_VIEW = 2;

	public CityWatchEntity curEntity = null;


	//facebook OG
    static final String applicationId = "307234779396415";
    static final String PENDING_REQUEST_BUNDLE_KEY = "com.facebook.samples.graphapi:PendingRequest";
    //SpellCheckerService.Session session;
    boolean pendingRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//session = createSession();

		if (getIntent().getExtras().containsKey(EXTRA_ENTITY)) {
			this.curEntity.putAll((HashMap<String, Object>) getIntent().getExtras().getSerializable(EXTRA_ENTITY));
		}
		if (this.curEntity == null) {
			Log.e(TAG, "Gonna have to pass EXTRA_ENTITY to this Activity so it has location data...");
		}


		if (getIntent().getExtras().containsKey(EXTRA_FRAG_TARGET)) {
			int w = getIntent().getExtras().getInt(EXTRA_FRAG_TARGET);

			switch (w) {
			case FRAG_EDIT:
				showEditDetailsFragment();
				break;
			case FRAG_VIEW:
				showViewDetailsFragment();
				break;
			default:
				Log.e(TAG, "EXTRA_FRAG_TARGET has to be either FRAG_EDIT or FRAG_VIEW");
				break;

			}

		} else {
			Log.e(TAG, "Gonna have to pass EXTRA_FRAG_TARGET to this Activity so it knows which fragment to show...");
		}

	}      */


//    private void sendRequests() {
//        textViewResults.setText("");

//        String requestIdsText = editRequests.getText().toString();
//        String[] requestIds = requestIdsText.split(",");

//        List<Request> requests = new ArrayList<Request>();
//        for (final String requestId : requestIds) {
//            requests.add(new Request(session, requestId, null, null, new Request.Callback() {
//                public void onCompleted(Response response) {
//                    GraphObject graphObject = response.getGraphObject();
//                    FacebookRequestError error = response.getError();
//                    String s = textViewResults.getText().toString();
//                    if (graphObject != null) {
//                        if (graphObject.getProperty("id") != null) {
//                            s = s + String.format("%s: %s\n", graphObject.getProperty("id"), graphObject.getProperty(
//                                    "name"));
//                        } else {
//                            s = s + String.format("%s: <no such id>\n", requestId);
//                        }
//                    } else if (error != null) {
//                        s = s + String.format("Error: %s", error.getErrorMessage());
//                    }
//                    textViewResults.setText(s);
//                }
//            }));
//        }
//        pendingRequest = false;
//        Request.executeBatchAndWait(requests);
//    }
//    }





}
