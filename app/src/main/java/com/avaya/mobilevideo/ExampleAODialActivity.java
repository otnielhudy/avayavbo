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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avaya.mobilevideo.api.AODialActivity;
import com.avaya.mobilevideo.api.AVDialActivity;
import com.avaya.mobilevideo.impl.AODialActivityImpl;
import com.avaya.mobilevideo.impl.AVDialActivityImpl;
import com.avaya.mobilevideo.utils.Constants;

/**
 * Example dial activity class, extends {@link AODialActivityImpl} and extends {@link AODialActivity}
 *
 * @author Avaya Inc
 */
public class ExampleAODialActivity extends AODialActivityImpl implements AODialActivity {

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_dial);
        setReferencedClass();

        Bundle extras = getIntent().getExtras();
        setServer(extras.getString(Constants.DATA_KEY_SERVER));
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
     * Make audio call, where video can be added or removed
     */
    @Override
    public void dialAudioOnly(View v) {
        displayProgressDialog();

        if (validateNumber(getNumber())&& validateUui(getUui())) {
            dialAudioOnly(getNumber(), getUui());
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
        String uui = tv.getText().toString();

        if (uui.length() > Constants.MAX_CONTEXT_ID_LENGTH) {
            uui = uui.substring(0, Constants.MAX_CONTEXT_ID_LENGTH);
        }

        return uui;
    }

    /**
     * Need to reference the implementing call activity class
     */
    @Override
    protected void setReferencedClass() {
        setCallActivityClass(ExampleAOCallActivity.class);
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
