//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import java.io.InputStream;
//
//import android.accounts.Account;
//import android.accounts.AccountManager;
//import android.content.Context;
//import android.location.Location;
//import android.util.Log;
//
//import com.kinvey.KCSClient;
//import com.kinvey.KinveySettings;
//import com.kinvey.MappedAppdata;
//import com.kinvey.exception.KinveyException;
//import com.kinvey.samples.citywatch.*;
//import com.kinvey.util.ListCallback;
//import com.kinvey.util.ScalarCallback;
//
//public class KinveyService {
//
//	// Application Constants
//	public static final String AUTHTOKEN_TYPE = "com.kinvey.myapplogin";
//	public static final String ACCOUNT_TYPE = "com.kinvey.myapplogin";
//	public static final String LOGIN_TYPE_KEY = "loginType";
//
//	private static final String TAG = KinveyService.class.getSimpleName();
//
//	private static KinveyService instance = null;
//
//	// reference to the kinvey client, used for contacting Kinvey's BaaS
//	public KCSClient mKinveyClient;
//	// an instance of MappedAppdata is used to query a collection
//	private MappedAppdata ma;
//
//	public static final String COLLECTION_NAME = "Reports";
//
//	public static KinveyService getInstance(Context context) {
//		if (instance == null) {
//			instance = new KinveyService();
//			KinveySettings settings = KinveySettings.loadFromProperties(context.getApplicationContext());
//			instance.mKinveyClient = KCSClient.getInstance(context.getApplicationContext(), settings);
//
//			instance.ma = instance.mKinveyClient.mappeddata(com.kinvey.samples.citywatch.CityWatchEntity.class, COLLECTION_NAME);
//
//		}
//		return instance;
//
//	}
//
//	public void addEntity(com.kinvey.samples.citywatch.CityWatchEntity curEvent, ScalarCallback<com.kinvey.samples.citywatch.CityWatchEntity> callback) {
//
//		Location l = new Location(TAG);
//		l.setLatitude(curEvent.getLatitude());
//		l.setLongitude(curEvent.getLongitude());
//		curEvent.setCoords(l);
//
//		// then save the entity to kinvey
//		ma.save(curEvent, callback);
//
//	}
//
//	public void addPicture(byte[] img, String filename, ScalarCallback<Void> callback) {
//
//		mKinveyClient.resource(filename).upload(img, callback);
//
//	}
//
//	public void fetchPicture(String filename, ScalarCallback<InputStream> callback) {
//		mKinveyClient.resource(filename).openRawResource(callback);
//
//	}
//
//	/**
//	 *
//	 * @param filename
//	 * @return Filename on success OR NULL on failure
//	 */
//	public String fetchPictureURI(String filename) {
//		try {
//			return mKinveyClient.resource(getFilename(filename)).getUriForResource();
//		} catch (KinveyException e) {
//			Log.e(TAG, "failed to fetch picture URI");
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public void getCityWatchEntities(Location loc, double maxDistance, ListCallback<com.kinvey.samples.citywatch.CityWatchEntity> callback) {
//
//		// SimpleQuery query = new SimpleQuery();
//		// query.
////		 query.withinBox(FieldConstants.KEY_GEOLOCATION, loc.getLongitude(),
////		 loc.getLatitude(), loc.getLongitude() + 5,
////		 loc.getLatitude() + 5);
//
//
//		//TODO test this
////		 query.nearSphere(FieldConstants.KEY_GEOLOCATION,
////		 loc.getLongitude(), loc.getLatitude(), maxDistance);
//
//		ma.clearFilterCriteria();
//		// ma.setQuery(query);
//		ma.fetch(callback);
//
//		return;
//
//	}
//
//	public static String getFilename(String objectID) {
//		return KinveyService.COLLECTION_NAME + "_" + objectID + "_IMAGE.png";
//	}
//
//	public static boolean loggedIn(Context context) {
//		AccountManager am = AccountManager.get(context);
//		Account[] accounts = am.getAccountsByType(KinveyService.ACCOUNT_TYPE);
//
//		if (accounts.length > 0) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//}
