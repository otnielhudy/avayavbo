/**
 * ExampleAVCallActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avaya.mobilevideo.api.AOCallActivity;
import com.avaya.mobilevideo.api.AVCallActivity;
import com.avaya.mobilevideo.impl.AOCallActivityImpl;
import com.avaya.mobilevideo.impl.AVCallActivityImpl;
import com.avaya.mobilevideo.utils.Constants;

/**
 * Example call activity class, extends {@link AOCallActivityImpl} and extends {@link AOCallActivity}
 *
 * @author Avaya Inc
 */
public class ExampleAOCallActivity extends AOCallActivityImpl implements AOCallActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        setReferencedClass();

        setContentView(R.layout.activity_audio_call);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        setVisibility(findViewById(R.id.btnDtmf), visibility);
        setVisibility(findViewById(R.id.end_call), visibility);
    }

    private void setVisibility(View button, int visibility) {
        if (visibility == View.VISIBLE || visibility == View.INVISIBLE) {
            button.setVisibility(visibility);
        }
    }

    /**
     * Set the controls that {@link AOCallActivityImpl} will reference
     */
    @Override
    protected void setControls() {
        setDisplayNameField((TextView) findViewById(R.id.displayNameField));
        setCalleeNumberDisplay((TextView) findViewById(R.id.callee_number_display));
    }

    /**
     * The call class doesn't need to reference any other class
     */
    @Override
    protected void setReferencedClass() {
    }
}
