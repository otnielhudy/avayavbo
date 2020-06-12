package com.avaya.mobilevideo.panicar;

import android.content.Intent;
import android.util.Log;

import com.konylabs.android.KonyMain;

public class ARWrapper {
    private static final String TAG = "ARWrapper";

    public static void initializeAR(String arLocations)
    {
        Log.i("ARWrapper", "inside initializeAR: " + arLocations);
        PanicARFragment.arLocations = arLocations;
        KonyMain appContext = KonyMain.getActivityContext();
        Intent intent = new Intent(appContext, ARActivity.class);
        Log.i("ARWrapper", "starting ARActivity");
        appContext.startActivity(intent);
    }
}
