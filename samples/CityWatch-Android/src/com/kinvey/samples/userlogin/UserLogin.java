package com.kinvey.samples.userlogin;//package com.kinvey.samples.userlogin;
//
//import com.kinvey.KCSClient;
//import com.kinvey.KinveySettings;
//
//import android.app.Application;
//
///**
// * Global application class.  Instantiates the KCS Client and sets global constants.
// *
// */
//public class UserLogin extends Application {
//    private KCSClient service;
//
//    // Enter your Kinvey app credentials
//    private static final String APP_KEY = "kid_PeAgA6XoK5";
//    private static final String APP_SECRET = "e1fa22b7617243809a3fc33a2fc2ffed";
//    
//    // Application Constants
//    public static final String AUTHTOKEN_TYPE = "com.kinvey.myapplogin";
//    public static final String ACCOUNT_TYPE = "com.kinvey.myapplogin";
//    public static final String LOGIN_TYPE_KEY = "loginType";
//    
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        initialize();
//    }
//   
//
//    private void initialize() {
//		// Enter your app credentials here
//		service = KCSClient.getInstance(this.getApplicationContext(), new KinveySettings(APP_KEY, APP_SECRET));
//    }
//
//    public KCSClient getKinveyService() {
//        return service;
//    }
//}
