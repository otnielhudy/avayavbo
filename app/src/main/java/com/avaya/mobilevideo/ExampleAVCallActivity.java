/**
 * ExampleAVCallActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avaya.mobilevideo.api.AVCallActivity;
import com.avaya.mobilevideo.api.AVDialActivity;
import com.avaya.mobilevideo.impl.AVCallActivityImpl;
import com.avaya.mobilevideo.utils.Constants;

/**
 * Example call activity class, extends {@link AVCallActivityImpl} and extends {@link AVCallActivity}
 *
 * @author Avaya Inc
 */
public class ExampleAVCallActivity extends AVCallActivityImpl implements AVCallActivity {

    public static Activity callActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        setReferencedClass();

//        setContentView(R.layout.activity_call);

        callActivity = this;

/*
        if(ExampleAVDialActivity.dialActivity != null) {
            try {
                ExampleAVDialActivity.dialActivity.finish();
            } catch (Exception e) {}
        }
*/

//buat dismiss dialog progress
        //     ExampleAVDialActivity.nDialogDial.dismiss();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setControls();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        boolean enableVideo = extras.getBoolean(Constants.KEY_ENABLE_VIDEO);

        //      ToggleButton toggleButton = (ToggleButton) findViewById(R.id.btnEnableVideo);
        //      toggleButton.setChecked(enableVideo);

    }

    public void toggleHold(View v) {
        if (getCallOnHold()) {
            showControls();
        } else {
            hideControls();
        }

        super.toggleHold(v);
    }

    private void hideControls() {
        setButtonsVisibility(View.INVISIBLE);
    }

    private void showControls() {
        setButtonsVisibility(View.VISIBLE);
    }

    private void setButtonsVisibility(int visibility) {
        setVisibility(findViewById(R.id.btnMuteAudio), visibility);
//        setVisibility(findViewById(R.id.btnEnableVideo), visibility);
//        setVisibility(findViewById(R.id.mute_video), visibility);
        setVisibility(findViewById(R.id.btnSwitchVideo), visibility);
        setVisibility(findViewById(R.id.btnDtmf), visibility);
        setVisibility(findViewById(R.id.end_call), visibility);
    }

    private void setVisibility(View button, int visibility) {
        if (visibility == View.VISIBLE || visibility == View.INVISIBLE) {
            button.setVisibility(visibility);
        }
    }

    /**
     * Set the controls that {@link AVCallActivityImpl} will reference
     */
    @Override
    protected void setControls() {
        setDisplayNameField((TextView) findViewById(R.id.displayNameField));
        setCalleeNumberDisplay((TextView) findViewById(R.id.callee_number_display));
        //      setMuteVideo((ToggleButton) findViewById(R.id.mute_video));
    }

    /**
     * The call class doesn't need to reference any other class
     */
    @Override
    protected void setReferencedClass() {
    }

}
