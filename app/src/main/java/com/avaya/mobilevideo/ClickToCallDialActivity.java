///**
// * ExampleAVDialActivity.java <br>
// * Copyright 2014-2015 Avaya Inc. <br>
// * All rights reserved. Usage of this source is bound to the terms described the file
// * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
// * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
// */
//package com.avaya.mobilevideo;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Layout;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.avaya.mobilevideo.api.AODialActivity;
//import com.avaya.mobilevideo.api.AVDialActivity;
//import com.avaya.mobilevideo.impl.AOCallActivityImpl;
//import com.avaya.mobilevideo.impl.AOClickToCall;
////import com.avaya.mobilevideo.impl.AODialActivityImpl;
//import com.avaya.mobilevideo.impl.AODialClickToCall;
//import com.avaya.mobilevideo.impl.AVDialActivityImpl;
//import com.avaya.mobilevideo.utils.Constants;
//import com.avaya.mobilevideo.utils.Logger;
//import com.avaya.vivaldi.internal.Z;
//import com.konylabs.android.KonyMain;
//
///**
// * Example dial activity class, extends {@link //AODialActivityImpl} and extends {@link AODialActivity}
// *
// * @author Avaya Inc
// */
//public class ClickToCallDialActivity extends AODialClickToCall implements AODialActivity {
//
//    private ProgressDialog mProgress;
//    public static String refNum;
//    public static String callNumber;
//    private Logger mLogger;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_audio_dial);
//        setReferencedClass();
//
//        Bundle extras = getIntent().getExtras();
//        setServer(extras.getString(Constants.DATA_KEY_SERVER));
//        dialAudioOnly(getNumber(),getUui());
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        dismissProgressDialog();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        dismissProgressDialog();
//    }
//
//    /**
//     * Make audio call, where video can be added or removed
//     */
//    @Override
//    public void dialAudioOnly(View v) {
//        displayProgressDialog();
//
//        if (validateNumber(getNumber())&& validateUui(getUui())) {
//            mLogger.d("if condition");
//            dialAudioOnly(getNumber(), getUui());
//        } else {
//            mLogger.d("else condition");
//            dismissProgressDialog();
//
//        }
//    }
//
//
//    private void displayProgressDialog() {
//        mProgress = ProgressDialog.show(this, getResources().getString(R.string.progress_dialog_title),
//                getResources().getString(R.string.progress_dialog_message), true);
//    }
//
//    /**
//     * Logout
//     */
//    @Override
//    public void logout(View v) {
//        logout();
//    }
//
//    /**
//     * @return number
//     */
//    private String getNumber() {
//        TextView tv = (TextView) findViewById(R.id.dial_number_field);
//        tv.setText(callNumber);
//        return tv.getText().toString();
//    }
//
//    /**
//     * @return UUI
//     */
//    private String getUui() {
//        TextView tv = (TextView) findViewById(R.id.uui_field);
//        String uui = tv.getText().toString();
//
//        if (uui.length() > Constants.MAX_CONTEXT_ID_LENGTH) {
//            uui = uui.substring(0, Constants.MAX_CONTEXT_ID_LENGTH);
//        }
//
//        return uui;
//    }
//
//    /**
//     * Need to reference the implementing call activity class
//     */
////    @Override
////    protected void setReferencedClass() {
////        setCallActivityClass(ExampleAOCallActivity.class);
////    }
//    @Override
//    protected void setReferencedClass() {
//        setCallActivityClass(ClicktoCallActivity.class);
//    }
//
//
//
//    private void dismissProgressDialog() {
//        if (mProgress != null) {
//            mProgress.dismiss();
//        }
//    }
//
//    @Override
//    protected boolean validateNumber(String number) {
//        boolean isValid = true;
//
//        if (!super.validateNumber(number)) {
//            Toast.makeText(this, getResources().getString(R.string.specify_number), Toast.LENGTH_LONG).show();
//            isValid = false;
//        }
//
//        return isValid;
//    }
//
//    @Override
//    protected boolean validateUui(String uui) {
//        boolean isValid = true;
//
//        if (!super.validateUui(uui)) {
//            Toast.makeText(this, getResources().getString(R.string.enter_valid_uui), Toast.LENGTH_LONG).show();
//            isValid = false;
//        }
//
//        return isValid;
//    }
//}

/**
 * ExampleAVDialActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.avaya.mobilevideo.api.AVDialActivity;
import com.avaya.mobilevideo.impl.AVDialActivityImpl;
import com.avaya.mobilevideo.impl.AVDialClickToCall;
import com.avaya.mobilevideo.utils.Constants;

/**
 * Example dial activity class, extends {@link AVDialActivityImpl} and extends {@link AVDialActivity}
 *
 * @author Avaya Inc
 */
public class ClickToCallDialActivity extends AVDialClickToCall implements AVDialActivity {

    private ProgressDialog mProgress;
    public static String refNum;
    public static String callNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dial);
        setReferencedClass();

        Bundle extras = getIntent().getExtras();
        setServer(extras.getString(Constants.DATA_KEY_SERVER));

        //hit video call
        Log.d("masuk AV DIAL", "yess");
        //         dialVideo(callNumber, refNum);
        dialAudio(callNumber,refNum);

    }





    @Override
    protected void onResume() {
        super.onResume();
        dismissProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressDialog();
    }



    /**
     * Make video call
     */
    @Override
    public void dialVideo(View v) {
        displayProgressDialog();

        if (validateNumber(getNumber()) && validateUui(getUui())) {
            dialVideo(callNumber, refNum);
        } else {
            dismissProgressDialog();
        }
    }

    /**
     * Make one-way video call, app receives video but doesn't broadcast it
     */
    @Override
    public void dialOneWayVideo(View v) {
        displayProgressDialog();
        dialOneWayVideo(getNumber(), getUui());

    }

    /**
     * Make audio call, where video can be added or removed
     */
    @Override
    public void dialAudio(View v) {
        displayProgressDialog();

        if (validateNumber(getNumber())&& validateUui(getUui())) {
            dialAudio(getNumber(), getUui());
        } else {
            dismissProgressDialog();
        }
    }

    private void displayProgressDialog() {
        mProgress = ProgressDialog.show(this, getResources().getString(R.string.progress_dialog_title),
                getResources().getString(R.string.progress_dialog_message), true);
    }

    /**
     * Logout
     */
    @Override
    public void logout(View v) {
        logout();
    }

    /**
     * @return number
     */
    private String getNumber() {
        TextView tv = (TextView) findViewById(R.id.dial_number_field);
        String number = callNumber;
//        return tv.getText().toString();
        return number;
    }

    /**
     * @return UUI
     */
    private String getUui() {
        TextView tv = (TextView) findViewById(R.id.uui_field);
        String uui = refNum;
        //     String uui = "ABCD1234";
        return uui;
    }

    /**
     * Need to reference the implementing call activity class
     */
    @Override
    protected void setReferencedClass() {
        setCallActivityClass(ClicktoCallActivity.class);
    }

    private void dismissProgressDialog() {
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    @Override
    protected boolean validateNumber(String number) {
        boolean isValid = true;

        if (!super.validateNumber(number)) {
            Toast.makeText(this, getResources().getString(R.string.specify_number), Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected boolean validateUui(String uui) {
        boolean isValid = true;

        if (!super.validateUui(uui)) {
            Toast.makeText(this, getResources().getString(R.string.enter_valid_uui), Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }
}
