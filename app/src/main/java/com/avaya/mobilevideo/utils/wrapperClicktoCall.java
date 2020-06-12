package com.avaya.mobilevideo.utils;

import android.content.Intent;
import android.util.Log;


import com.avaya.mobilevideo.ClickToCallLoginActivity;
import com.avaya.mobilevideo.ClicktoCallActivity;
import com.avaya.mobilevideo.ClickToCallDialActivity;
import com.avaya.mobilevideo.ExampleAVDialActivity;
import com.avaya.mobilevideo.ExampleLoginActivity;
import com.avaya.mobilevideo.GlobalParameter;
import com.avaya.mobilevideo.impl.AOCallActivityImpl;
import com.avaya.mobilevideo.impl.AOClickToCall;
import com.avaya.mobilevideo.impl.AVClickToCall;
import com.avaya.mobilevideo.impl.LoginActivityImpl;
import com.konylabs.android.KonyMain;
import com.konylabs.vm.Function;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class wrapperClicktoCall {

    static Intent intent;
    private static final String TAG = wrapper.class.getSimpleName();

    private static Logger mLogger = Logger.getLogger(TAG);

    public static void wrapperCTC (
//            //params
            Function callback,
            String refNum,
            String username,
            String server,
            String displayName,
            String callNumber,
            String port,
            Boolean secureConnection,
            String connectingAvaya,
            String connectedAvaya
//            String headerPopupAvaya,
//            String bodyPopupAvaya,
//            String yesButtonAvaya,
//            String noButtonAvaya
            )
    {
        mLogger.d("entering wrapper ctc");
        AVClickToCall.callback = callback;
        ClickToCallDialActivity.refNum = refNum;
        ClickToCallLoginActivity.username = username;
        ClickToCallLoginActivity.server = server;
        ClickToCallLoginActivity.displayName = displayName;
        ClickToCallDialActivity.callNumber = callNumber;
        LoginActivityImpl.port = port;
        LoginActivityImpl.secureLogin = secureConnection;
        ClicktoCallActivity.connectingAvaya = connectingAvaya;
        ClicktoCallActivity.connectedAvaya = connectedAvaya;
        GlobalParameter.connectedValue = connectedAvaya;
        GlobalParameter.connectingValue = connectingAvaya;
//        AVCallActivityImpl.avayaPopupHeader = headerPopupAvaya;
//        AVCallActivityImpl.avayaPopupBody = bodyPopupAvaya;
//        AVCallActivityImpl.avayaYesButton = yesButtonAvaya;
//        AVCallActivityImpl.avayaNoButton = noButtonAvaya;
        //function
        intent = new Intent(KonyMain.getAppContext(), ClickToCallLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KonyMain.getAppContext().startActivity(intent);



    }

    public static void goBack(List<Map> requestMap){

        String result = AVClickToCall.stringActivity(requestMap);
        Log.d("Log Result JSON", result);
        Object[] objectKony = new Object[]{ result };
        Log.d("TAG", "callback kony ");
        try {
            AVClickToCall.callback.execute(objectKony);
//            callback.execute(callbackObjectAvaya.toArray());
        } catch (Exception e) {}


    }
}