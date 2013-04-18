//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.app.AlertDialog;
//import android.os.Bundle;
//import android.support.v4.app.FragmentTransaction;
//import android.util.Log;
//
//import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.facebook.FacebookRequestError;
//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.facebook.Session.StatusCallback;
//import com.facebook.SessionState;
//import com.facebook.model.GraphObject;
//import com.kinvey.samples.citywatch.CityWatchEntity;
//import com.kinvey.samples.citywatch.R;
//
//public class CityWatchDetailsActivity extends SherlockFragmentActivity {
//
//	// This Activity manages two fragments: edit and view
//	// The action bar is dependant on the fragment.
//
//	private static final String TAG = CityWatchDetailsActivity.class.getSimpleName();
//
//	public static final String EXTRA_FRAG_TARGET = "EXTRA_FRAG_TARGET";
//	public static final String EXTRA_ENTITY = "EXTRA_ENTITY";
//
//	public static final int FRAG_LOGIN = 0;
//	public static final int FRAG_EDIT = 1;
//	public static final int FRAG_VIEW = 2;
//
//	public CityWatchEntity curEntity = null;
//
//
//	//facebook OG
//    static final String applicationId = "307234779396415";
//    static final String PENDING_REQUEST_BUNDLE_KEY = "com.facebook.samples.graphapi:PendingRequest";
//    Session session;
//    boolean pendingRequest;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		session = createSession();
//
//		if (getIntent().getExtras().containsKey(EXTRA_ENTITY)) {
//			this.curEntity = (CityWatchEntity) getIntent().getExtras().getSerializable(EXTRA_ENTITY);
//		}
//		if (this.curEntity == null) {
//			Log.e(TAG, "Gonna have to pass EXTRA_ENTITY to this Activity so it has location data...");
//		}
//
//
//		if (getIntent().getExtras().containsKey(EXTRA_FRAG_TARGET)) {
//			int w = getIntent().getExtras().getInt(EXTRA_FRAG_TARGET);
//
//			switch (w) {
//			case FRAG_LOGIN:
//				showLoginFragment();
//				break;
//			case FRAG_EDIT:
//				showEditDetailsFragment();
//				break;
//			case FRAG_VIEW:
//				showViewDetailsFragment();
//				break;
//			default:
//				Log.e(TAG, "EXTRA_FRAG_TARGET has to be either FRAG_LOGIN, FRAG_EDIT, FRAG_VIEW");
//				break;
//
//			}
//
//		} else {
//			Log.e(TAG, "Gonna have to pass EXTRA_FRAG_TARGET to this Activity so it knows which fragment to show...");
//		}
//
//	}
//
//	public void showViewDetailsFragment() {
//
//		CityWatchViewDetailsFragment frag = CityWatchViewDetailsFragment.newInstance();
//		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//		transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//		transaction.replace(android.R.id.content, frag);
//		transaction.commit();
//	}
//
//	public void showLoginFragment() {
//		CityWatchLoginFragment frag = CityWatchLoginFragment.newInstance();
//		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//		transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//		transaction.replace(android.R.id.content, frag);
//		transaction.commit();
//	}
//
//	public void showEditDetailsFragment() {
//
//		CityWatchEditDetailsFragment frag = CityWatchEditDetailsFragment.newInstance();
//
//		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//		transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//		transaction.replace(android.R.id.content, frag);
//		transaction.commit();
//	}
//
//
//    private void onClickRequest() {
//        if (this.session.isOpened()) {
//            sendRequests();
//        }
//            else {
//            StatusCallback callback = new StatusCallback() {
//                public void call(Session session, SessionState state, Exception exception) {
//                    if (exception != null) {
//                        new AlertDialog.Builder(CityWatchDetailsActivity.this)
//                                .setTitle(R.string.login_failed_dialog_title)
//                                .setMessage(exception.getMessage())
//                                .setPositiveButton(R.string.ok_button, null)
//                                .show();
//                        CityWatchDetailsActivity.this.session = createSession();
//                    }
//                }
//            };
//            pendingRequest = true;
//            this.session.openForRead(new Session.OpenRequest(this).setCallback(callback));
//        }
//    }
//
//    private void sendRequests() {
////        textViewResults.setText("");
//
////        String requestIdsText = editRequests.getText().toString();
////        String[] requestIds = requestIdsText.split(",");
//
////        List<Request> requests = new ArrayList<Request>();
////        for (final String requestId : requestIds) {
////            requests.add(new Request(session, requestId, null, null, new Request.Callback() {
////                public void onCompleted(Response response) {
////                    GraphObject graphObject = response.getGraphObject();
////                    FacebookRequestError error = response.getError();
////                    String s = textViewResults.getText().toString();
////                    if (graphObject != null) {
////                        if (graphObject.getProperty("id") != null) {
////                            s = s + String.format("%s: %s\n", graphObject.getProperty("id"), graphObject.getProperty(
////                                    "name"));
////                        } else {
////                            s = s + String.format("%s: <no such id>\n", requestId);
////                        }
////                    } else if (error != null) {
////                        s = s + String.format("Error: %s", error.getErrorMessage());
////                    }
////                    textViewResults.setText(s);
////                }
////            }));
////        }
////        pendingRequest = false;
////        Request.executeBatchAndWait(requests);
////    }
//    }
//    private Session createSession() {
//        Session activeSession = Session.getActiveSession();
//        if (activeSession == null || activeSession.getState().isClosed()) {
//            activeSession = new Session.Builder(this).setApplicationId(applicationId).build();
//            Session.setActiveSession(activeSession);
//        }
//        return activeSession;
//    }
//
//
//
//}
