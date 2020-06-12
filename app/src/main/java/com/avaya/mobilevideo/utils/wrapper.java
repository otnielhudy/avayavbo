package com.avaya.mobilevideo.utils;

import android.content.Intent;
import android.util.Log;

import com.avaya.mobilevideo.ExampleAVDialActivity;
import com.avaya.mobilevideo.ExampleLoginActivity;
import com.avaya.mobilevideo.api.AVCallActivity;
import com.avaya.mobilevideo.impl.AVCallActivityImpl;
import com.avaya.mobilevideo.impl.LoginActivityImpl;
import com.konylabs.android.KonyMain;
import com.konylabs.vm.Function;

public class wrapper {

    static Intent intent;

    public static void wrapper (
            //params
            Function callback,
//            Function signalStrength,
//            String signalValue,
            String refNum,
            String username,
            String server,
            String displayName,
            String callNumber,
            String port,
            Boolean secureConnection,
            String headerPopupAvaya,
            String bodyPopupAvaya,
            String yesButtonAvaya,
            String noButtonAvaya)
    {
        AVCallActivityImpl.callback = callback;
//        AVCallActivityImpl.signalstrength = signalStrength;
        ExampleAVDialActivity.refNum = refNum;
        ExampleLoginActivity.username = username;
        ExampleLoginActivity.server = server;
        ExampleLoginActivity.displayName = displayName;
        ExampleAVDialActivity.callNumber = callNumber;
        LoginActivityImpl.port = port;
        LoginActivityImpl.secureLogin = secureConnection;
        AVCallActivityImpl.avayaPopupHeader = headerPopupAvaya;
        AVCallActivityImpl.avayaPopupBody = bodyPopupAvaya;
        AVCallActivityImpl.avayaYesButton = yesButtonAvaya;
        AVCallActivityImpl.avayaNoButton = noButtonAvaya;
        //function
        intent = new Intent(KonyMain.getAppContext(), ExampleLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KonyMain.getAppContext().startActivity(intent);


    }
}
