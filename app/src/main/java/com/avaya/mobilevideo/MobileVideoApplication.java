package com.avaya.mobilevideo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

//import com.appsflyer.AppsFlyerConversionListener;
//import com.appsflyer.AppsFlyerLib;
import com.avaya.clientplatform.api.ClientPlatformFactory;
import com.avaya.mobilevideo.panicar.PanicARApp;
import com.avaya.mobilevideo.utils.IssueReporter;
import com.avaya.mobilevideo.utils.Logger;
import com.konylabs.android.KonyApplication;

import java.util.Map;

import static org.acbrtc.ContextUtils.getApplicationContext;


public class MobileVideoApplication extends PanicARApp {
    private static final String TAG = MobileVideoApplication.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private static final String AF_DEV_KEY = "7dQprYbPf3sNEFcaj9fZ8Q";

    private Activity mCurrentActivity = null;

    private static String sSdkVersion = "";

    public static Context context;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public void onCreate() {
        super.onCreate();
        mLogger.d("onCreate()");

        sSdkVersion = ClientPlatformFactory.getClientPlatformInterface(getAppContext()).getVersion();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });


        //denny 20190521 - Appsflyer
//        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
//            @Override
//            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
//                for (String attrName : conversionData.keySet()) {
//                    Log.d("AppsFlyer", "attribute: " + attrName + " = " + conversionData.get(attrName));
//                }
//            }
//
//            @Override
//            public void onInstallConversionFailure(String errorMessage) {
//                Log.d("AppsFlyer", "error getting conversion data: " + errorMessage);
//            }
//
//            /* Called only when a Deep Link is opened */
//            @Override
//            public void onAppOpenAttribution(Map<String, String> conversionData) {
//                for (String attrName : conversionData.keySet()) {
//                    Log.d("AppsFlyer", "attribute: " + attrName + " = " + conversionData.get(attrName));
//                }
//            }
//
//            @Override
//            public void onAttributionFailure(String errorMessage) {
//                Log.d("AppsFlyer", "error onAttributionFailure : " + errorMessage);
//            }
//        };
//
//        Log.d("AppsFlyer", "onCreate in");
//        AppsFlyerLib.getInstance().init(AF_DEV_KEY , conversionListener , getApplicationContext());
//        AppsFlyerLib.getInstance().setDebugLog(true);
//        AppsFlyerLib.getInstance().startTracking(this, AF_DEV_KEY);
//        //end

        context = getApplicationContext();
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        mLogger.e("Uncaught exception thrown", e);

        launchReportIssue();

        System.exit(1); // kill off the crashed app
    }

    public void launchReportIssue() {
        mLogger.d("launchReportIssue");

        try {
            if (mCurrentActivity != null) {
                mLogger.i("Report issue");
                IssueReporter issueReporter = new IssueReporter(mCurrentActivity, sSdkVersion, true);
                issueReporter.reportIssue();
            }
        } catch (Exception e) {
            mLogger.e("Exception in launchReportIssue()", e);
        }
    }

    public static String getSdkVersion() {
        return sSdkVersion;
    }
}

