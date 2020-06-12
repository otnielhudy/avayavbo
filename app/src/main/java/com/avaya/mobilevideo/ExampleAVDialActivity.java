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
import com.avaya.mobilevideo.utils.Constants;

/**
 * Example dial activity class, extends {@link AVDialActivityImpl} and extends {@link AVDialActivity}
 *
 * @author Avaya Inc
 */
public class ExampleAVDialActivity extends AVDialActivityImpl implements AVDialActivity {

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
        return tv.getText().toString();
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
        setCallActivityClass(ExampleAVCallActivity.class);
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
