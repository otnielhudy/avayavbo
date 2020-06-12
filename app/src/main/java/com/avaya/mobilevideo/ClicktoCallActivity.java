///**
// * ExampleAVCallActivity.java <br>
// * Copyright 2014-2015 Avaya Inc. <br>
// * All rights reserved. Usage of this source is bound to the terms described the file
// * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
// * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
// */
//package com.avaya.mobilevideo;
//
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.GridLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import com.avaya.mobilevideo.api.AOCallActivity;
//import com.avaya.mobilevideo.api.AVCallActivity;
//import com.avaya.mobilevideo.impl.AOCallActivityImpl;
//import com.avaya.mobilevideo.impl.AOClickToCall;
//import com.avaya.mobilevideo.impl.AVCallActivityImpl;
//import com.avaya.mobilevideo.utils.Constants;
//import com.google.android.gms.common.util.Hex;
//import com.google.firebase.annotations.PublicApi;
//
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
///**
// * Example call activity class, extends {@link AOCallActivityImpl} and extends {@link AOCallActivity}
// *
// * @author Avaya Inc
// */
//public class ClicktoCallActivity extends AOClickToCall implements AOCallActivity {
//    public LinearLayout layoutCall;
//    public RelativeLayout layoutEndCall;
//    public RelativeLayout layoutConnLost;
//    public LinearLayout layoutKeypad,layoutTxt2,layoutTxt,layoutTxt3;
//    public TextView textNumber, textTime, textConn,textConn1, textContactCenter1, textContactCenter2;
//    public TextView txtCIMB;
//    public ImageView imgView;
//    public Button
//            btnStar,
//            btnHash,
//            btnKeypad0,
//            btnKeypad1,
//            btnKeypad2,
//            btnKeypad3,
//            btnKeypad4,
//            btnKeypad5,
//            btnKeypad6,
//            btnKeypad7,
//            btnKeypad8,
//            btnKeypad9;
//    public ToggleButton btnMute;
//    public ToggleButton btnEndCall;
//    public ToggleButton btnLoudspeaker;
//    public static String connectingAvaya;
//    public static String connectedAvaya;
//
//    @Override
//    public void endCallCTC(){
//        super.endCallCTC();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//
//        setReferencedClass();
//
//        setContentView(R.layout.activity_clicktocall);
//        layoutCall = (LinearLayout)findViewById(R.id.parent_layout);
//        layoutEndCall = (RelativeLayout)findViewById(R.id.RelEndCall);
//        layoutKeypad = (LinearLayout)findViewById(R.id.LinLayoutParent);
//        layoutKeypad = (LinearLayout) findViewById(R.id.LinLayout1);
//        layoutTxt2 = (LinearLayout)findViewById(R.id.LinLayout2);
//        layoutTxt = (LinearLayout) findViewById(R.id.LinLayoutContactCenter);
//        layoutTxt3=(LinearLayout)findViewById(R.id.LinLayoutContactChild);
//        textContactCenter1 = (TextView)findViewById(R.id.contactCenter);
//        textContactCenter2 = (TextView)findViewById(R.id.txtCallCenter);
//        textNumber = (TextView) findViewById(R.id.inputNumber);
//        textTime = (TextView)findViewById(R.id.txtTime);
//        //textConn = (TextView) findViewById(R.id.txtConnected);
//        textConn1 = (TextView)findViewById(R.id.txtConnected1);
//        imgView = (ImageView)findViewById(R.id.imgView);
//        txtCIMB = (TextView)findViewById(R.id.txtCIMB);
//        btnKeypad0 = (Button)findViewById(R.id.btnNumber1);
//        btnKeypad1 = (Button)findViewById(R.id.btnNumber2);
//        btnKeypad2 = (Button)findViewById(R.id.btnNumber3);
//        btnKeypad3 = (Button)findViewById(R.id.btnNumber4);
//        btnKeypad4 = (Button)findViewById(R.id.btnNumber5);
//        btnKeypad5 = (Button)findViewById(R.id.btnNumber6);
//        btnKeypad6 = (Button)findViewById(R.id.btnNumber7);
//        btnKeypad7 = (Button)findViewById(R.id.btnNumber8);
//        btnKeypad8 = (Button)findViewById(R.id.btnNumber9);
//        btnKeypad9 = (Button)findViewById(R.id.btnNumber0);
//        btnStar = (Button)findViewById(R.id.btnAsterisk);
//        btnHash = (Button)findViewById(R.id.btnHash);
//        btnMute = (ToggleButton) findViewById(R.id.btnMuteAudio);
//        btnEndCall = (ToggleButton)findViewById(R.id.end_call);
//        btnLoudspeaker = (ToggleButton)findViewById(R.id.loud_day_on);
//
//
//        Calendar calendar1 = Calendar.getInstance();
//        int currentHour = calendar1.get(Calendar.HOUR_OF_DAY);
//
//        if(currentHour > 5 && currentHour < 18){
//            layoutCall.setBackgroundColor(Color.WHITE);
//            txtCIMB.setTextColor(Color.BLACK);
//            textTime.setTextColor(Color.BLACK);
//            textNumber.setTextColor(Color.BLACK);
//            textContactCenter1.setTextColor(Color.BLACK);
//            textContactCenter2.setTextColor(Color.BLACK);
////            btnKeypad.setTextColor(Color.BLACK);
//            btnKeypad0.setTextColor(Color.BLACK);
//            btnKeypad1.setTextColor(Color.BLACK);
//            btnKeypad2.setTextColor(Color.BLACK);
//            btnKeypad3.setTextColor(Color.BLACK);
//            btnKeypad4.setTextColor(Color.BLACK);
//            btnKeypad5.setTextColor(Color.BLACK);
//            btnKeypad6.setTextColor(Color.BLACK);
//            btnKeypad7.setTextColor(Color.BLACK);
//            btnKeypad8.setTextColor(Color.BLACK);
//            btnKeypad9.setTextColor(Color.BLACK);
//            btnStar.setTextColor(Color.BLACK);
//            btnHash.setTextColor(Color.BLACK);
//            btnMute.setBackgroundResource(R.drawable.off_day);
//            btnLoudspeaker.setBackgroundResource(R.drawable.loud_day_off);
//            //textConn.setText(connectedAvaya);
//            textConn1.setText(connectingAvaya);
//
//        }else{
//            layoutCall.setBackgroundColor(Color.BLACK);
//            txtCIMB.setTextColor(Color.WHITE);
//            textTime.setTextColor(Color.WHITE);
//            textNumber.setTextColor(Color.WHITE);
//            textContactCenter1.setTextColor(Color.WHITE);
//            textContactCenter2.setTextColor(Color.WHITE);
////            btnKeypad.setTextColor(Color.WHITE);
//            btnKeypad0.setTextColor(Color.BLACK);
//            btnKeypad1.setTextColor(Color.BLACK);
//            btnKeypad2.setTextColor(Color.BLACK);
//            btnKeypad3.setTextColor(Color.BLACK);
//            btnKeypad4.setTextColor(Color.BLACK);
//            btnKeypad5.setTextColor(Color.BLACK);
//            btnKeypad6.setTextColor(Color.BLACK);
//            btnKeypad7.setTextColor(Color.BLACK);
//            btnKeypad8.setTextColor(Color.BLACK);
//            btnKeypad9.setTextColor(Color.BLACK);
//            btnStar.setTextColor(Color.BLACK);
//            btnHash.setTextColor(Color.BLACK);
//            btnMute.setBackgroundResource(R.drawable.off_night);
//            btnLoudspeaker.setBackgroundResource(R.drawable.loud_night_off);
//            //textConn.setText(connectedAvaya);
//            textConn1.setText(connectingAvaya);
//        }
//    }
//
//
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//        setControls();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    public void toggleHold(View v) {
//        if (getCallOnHold()) {
//            showControls();
//        } else {
//            hideControls();
//        }
//
//        super.toggleHold(v);
//    }
//
//    private void hideControls() {
//        setButtonsVisibility(View.INVISIBLE);
//    }
//
//    private void showControls() {
//        setButtonsVisibility(View.VISIBLE);
//    }
//
//    private void setButtonsVisibility(int visibility) {
//        setVisibility(findViewById(R.id.btnMuteAudio), visibility);
//        setVisibility(findViewById(R.id.btnDtmf), visibility);
//        setVisibility(findViewById(R.id.end_call), visibility);
//    }
//
//    private void setVisibility(View button, int visibility) {
//        if (visibility == View.VISIBLE || visibility == View.INVISIBLE) {
//            button.setVisibility(visibility);
//        }
//    }
//
//    /**
//     * Set the controls that {@link AOCallActivityImpl} will reference
//     */
//    @Override
//    protected void setControls() {
//        setDisplayNameField((TextView) findViewById(R.id.displayNameField));
//        setCalleeNumberDisplay((TextView) findViewById(R.id.txtTime));
//    }
//
//    /**
//     * The call class doesn't need to reference any other class
//     */
//    @Override
//    protected void setReferencedClass() {
//    }
//}


/**
 * ExampleAVCallActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avaya.mobilevideo.api.AVCallActivity;
import com.avaya.mobilevideo.api.AVDialActivity;
import com.avaya.mobilevideo.impl.AVCallActivityImpl;
import com.avaya.mobilevideo.impl.AVClickToCall;
import com.avaya.mobilevideo.utils.Constants;

import java.util.Calendar;

/**
 * Example call activity class, extends {@link AVCallActivityImpl} and extends {@link AVCallActivity}
 *
 * @author Avaya Inc
 */
public class ClicktoCallActivity extends AVClickToCall implements AVCallActivity {

    public static Activity callActivity;


//    @Override
//    public void endCallCTC(){
//        super.endCallCTC();
//    }

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
        setCalleeNumberDisplay((TextView) findViewById(R.id.txtTime));
        //      setMuteVideo((ToggleButton) findViewById(R.id.mute_video));
    }

    /**
     * The call class doesn't need to reference any other class
     */
    @Override
    protected void setReferencedClass() {
    }

}
