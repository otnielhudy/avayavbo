/**
 * AVCallActivityImpl.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.widget.ToggleButton;

import com.avaya.clientplatform.api.AudioOnlyClientPlatform;
import com.avaya.clientplatform.api.AudioOnlyDevice;
import com.avaya.clientplatform.api.AudioOnlySession;
import com.avaya.clientplatform.api.AudioOnlySessionListener;
import com.avaya.clientplatform.api.AudioOnlyUser;
import com.avaya.clientplatform.api.AudioOnlyUserListener;
import com.avaya.clientplatform.api.DTMFType;
import com.avaya.clientplatform.api.SessionError;
import com.avaya.clientplatform.api.SessionException;
import com.avaya.clientplatform.api.SessionState;
import com.avaya.mobilevideo.ClicktoCallActivity;
import com.avaya.mobilevideo.GlobalParameter;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.InternetConnectionDetector;
import com.avaya.mobilevideo.utils.Logger;
import com.konylabs.vm.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which contains the call functionality
 *
 * @author Avaya Inc
 */
public abstract class AOClickToCall extends MobileVideoActivity implements AudioOnlySessionListener, AudioOnlyUserListener {

    private static final String TAG = AOClickToCall.class.getSimpleName();

    private Logger mLogger = Logger.getLogger(TAG);
    private MediaPlayer mediaPlayer;

    private AudioOnlyDevice mDevice;
    private AudioOnlyUser mUser;
    private AudioOnlyClientPlatform mPlatform;
    private AudioOnlySession mSession;
    private TextView mDisplayNameTextView = null;
    private TextView mCalleeNumberDisplay = null;
    private boolean mCallOnHold = false;
    private boolean mStartAudioMuted;
    private String mContextId;
    public static Function callback;
    private PopupWindow mDtmfPopupWindow = null;
    public EditText textNumber;
    public static String txtNum;

    private String mStatusText = "";
    private Handler mTimerHandler;

    public static List<Map> callbackObjectAvaya = new ArrayList<Map>();
    private Runnable mCallTimeChecker = new Runnable() {
        @Override
        public void run() {
            updateCallTime();
            mTimerHandler.postDelayed(mCallTimeChecker, Constants.TIMER_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimerHandler = new Handler();
        mediaPlayer = MediaPlayer.create(AOClickToCall.this, R.raw.ringback);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogger.d("onDestroy()");

        mPlatform = null;
        mUser = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mLogger.d("onResume()");

            Bundle extras = getIntent().getExtras();
            mContextId = extras.getString(Constants.KEY_CONTEXT).trim();
            mStartAudioMuted = extras.getBoolean(Constants.KEY_START_MUTED_AUDIO);


            // First instantiate a client platform interface
            // The factory is the top level object that provides access to the
            // services exposed by the SDK.
            mPlatform = ClientPlatformManager.getAudioOnlyClientPlatform(this.getApplicationContext());
            mUser = mPlatform.getUser();

            // Create user object that is used to encapsulate the call session.
            String token = extras.getString(Constants.DATA_SESSION_KEY);

            boolean tokenAccepted = mUser.setSessionAuthorizationToken(token);

            if (tokenAccepted) {
                mUser.registerListener(this);
                mUser.acceptAnyCertificate(true);

                // Create a device object that this application
                mDevice = mPlatform.getDevice();
                if (mDevice != null) {
                    mLogger.d("IsMediaAccessible : " + mDevice.couldMediaBeAccessible(getApplicationContext()));
                }

                if (mSession == null) {
                    if (mUser.isServiceAvailable()) {
                        mLogger.d("service available, make call now");
                        call();
                    }
                } else {
                    mLogger.d("session state: " + mSession.getState() + ", not placing call now");
                }
            } else {
                mLogger.w("Invalid token used");
                displayMessage(getResources().getString(R.string.invalid_token), true);
            }
        } catch (Exception e) {
            mLogger.e("Exception in onResume(): " + e.getMessage(), e);
            displayMessage("Call activity resume exception: " + e.getMessage());
        }
    }

    /**
     * This is the method illustrating how the SDK APIs are used. End customer is interacting with, for instance a
     * banking application. User decides to speak with a live agent. This is a method implemented by the 3rd party
     * application developer. The point is that the one time connection authorization is obtained by the SDK internally
     * when user decides to call the agent. The application does not need to do this separately before requesting a call
     * setup.
     */
    private void call() {
        mLogger.d("Placing call");

        try {
            mDevice = mPlatform.getDevice();

            AudioOnlyClientPlatform clientPlatform = ClientPlatformManager.getAudioOnlyClientPlatform(this.getApplicationContext());
            String browser = clientPlatform.getUserAgentBrowser();
            String version = clientPlatform.getUserAgentVersion();

            mSession = mUser.createSession();
            mSession.registerListener(this);
            mSession.muteAudio(mStartAudioMuted);

            if (mContextId != null && mContextId.length() > 0) {
                mLogger.d("Context ID:" + mContextId);
                mSession.setContextId(mContextId);
            }

            String numberToDial = getIntent().getExtras().getString(Constants.KEY_NUMBER_TO_DIAL);

            mSession.setRemoteAddress(numberToDial);

            // Start the call session.
            // During the call establishment, the one-time session ID is obtained
            // from the server behind the scenes.
            // ICE candidate list is obtained and the correct local IP address is
            // determined.
            // The Session object implementation controls the media channels of the
            // call based on the signaling protocol feedback.
            mSession.start();
            this.callbackObjectAvaya = new ArrayList<Map>();
            //setCalleeDisplayInformation(mSession.getState());
            mLogger.d("Device: " + browser + ", version: " + version);
            mLogger.d("Session authorisation token: " + mUser.getSessionAuthorizationToken());

        } catch (Exception e) {
            mLogger.e("Exception occurred in AOCallActivityImpl.call(): " + e.getMessage(), e);
            displayMessage(getResources().getString(R.string.call_failed) + e.getMessage());
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        mLogger.d("onStop()");

        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLogger.d("onBackPressed()");
        hangup();
        finish();
        mediaPlayer.stop();
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Abstract set controls method
     */
    protected abstract void setControls();

    /**
     * Set display name field
     *
     * @param displayNameTextView
     */
    protected void setDisplayNameField(TextView displayNameTextView) {
        this.mDisplayNameTextView = displayNameTextView;
    }

    /**
     * Set calee number display
     *
     * @param calleeNumberDisplay
     */
    protected void setCalleeNumberDisplay(TextView calleeNumberDisplay) {
        this.mCalleeNumberDisplay = calleeNumberDisplay;
    }


    /**
     * End call
     *
     * @param V
     */
    public void endCall(View V) {
        endCall();
    }

    public void endCallCTC() {
        hangup();
        creatActivity("502", "Timeout");
    }
    protected void endCall() {
        try {
            hangup();

            finish();
            mediaPlayer.stop();
            creatActivity("02","endCall");
        } catch (Throwable e) {
            mLogger.e("Throwable in endCall()", e);
        }
    }


    /**
     * Toggle mute audio
     *
     * @param v
     */
    public void toggleMuteAudio(View v) {
        try {
            mLogger.d("Toggle mute audio");
            boolean wasMuted = mSession.isAudioMuted();
            boolean nowMuted = !wasMuted;
            //otniel-modify toggle mute audio
            ToggleButton tb = (ToggleButton)findViewById(R.id.btnMuteAudio);
            Calendar cal1 = Calendar.getInstance();
            int Hours = cal1.get(Calendar.HOUR_OF_DAY);
            mSession.muteAudio(nowMuted);
            if(nowMuted){
                if(Hours > 5 && Hours < 18) {
                    tb.setBackgroundResource(R.drawable.on_day);
                }else{
                    tb.setBackgroundResource(R.drawable.on_night);
                }
            }else{
                if(Hours > 5 && Hours < 18){
                    tb.setBackgroundResource(R.drawable.off_day);
                }else{
                    tb.setBackgroundResource(R.drawable.off_night);
                }
            }
        } catch (Exception e) {
            mLogger.e("Exception in toggleMuteAudio()", e);
            displayMessage("Mute audio exception: " + e.getMessage());
        }
    }

    //otniel-add function toggle mute loudspeaker
    public void  toggleMuteSpeaker(View v){
        try{
            AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
            ToggleButton tb = (ToggleButton) findViewById(R.id.loud_day_on);
            Calendar cal1 = Calendar.getInstance();
            int Hours = cal1.get(Calendar.HOUR_OF_DAY);
            boolean isOn = tb.isChecked();

            if (!isOn) {
                mLogger.d("true condition");
//                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setMode(AudioManager.MODE_NORMAL);

                if(Hours > 5 && Hours < 18) {
                    tb.setBackgroundResource(R.drawable.loud_day_on);
                }else{
                    tb.setBackgroundResource(R.drawable.loud_night_on);
                }

                audioManager.setSpeakerphoneOn(true);
            }else {
                mLogger.d("false condition");
//                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setMode(AudioManager.MODE_IN_CALL);

                if(Hours > 5 && Hours < 18) {
                    tb.setBackgroundResource(R.drawable.loud_day_off);
                }else{
                    tb.setBackgroundResource(R.drawable.loud_night_off);
                }


            }
            audioManager.setSpeakerphoneOn(isOn);

        }catch (Exception e){

        }
    }

    public void toggleHold(View v) {
        try {
            mLogger.d("Toggle hold");

            if (!mCallOnHold) {
                mLogger.d("Put call on hold");
                mSession.hold();
                mCallOnHold = true;
            } else {
                mLogger.d("Take call off hold");
                mSession.resume();
                mCallOnHold = false;
            }
        } catch (Exception e) {
            mLogger.e("Exception in toggleHold()", e);
            displayMessage("Hold exception: " + e.getMessage());
        }
    }


    public void launchDtmf(View v) {
        mLogger.d("Pop a DTMF window");

        try {
            if (mDtmfPopupWindow == null) {
                final Button btnOpenPopup = (Button) findViewById(R.id.btnDtmf);

                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.dtmf_popup, null);

                mDtmfPopupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                mDtmfPopupWindow.showAsDropDown(btnOpenPopup, 50, -30);
            }
        } catch (Exception e) {
            mLogger.e("Exception in launchDtmf()", e);
            displayMessage("Launch DTMF exception: " + e.getMessage());
        }
    }

    public void dismissDtmf(View v) {
        if (mDtmfPopupWindow != null) {
            mDtmfPopupWindow.dismiss();
        }

        mDtmfPopupWindow = null;
    }

    public void dtmf(View v) {
        try {
            TextView tN = (TextView) findViewById(R.id.inputNumber);

            int i = 14;
            String digit = (String) v.getTag();
            //handle button keypad
            if(mSession == null){
                mLogger.d("session value: "+mSession.getState());

            }else{
                mLogger.d("DTMF: " + digit);
                mSession.sendDTMF(DTMFType.get(digit), true);
                tN.setText(tN.getText() + digit);
                if(digit.length() > i){
                    tN.setGravity(Gravity.RIGHT);
                }
            }
            //end
        } catch (Exception e) {
            mLogger.e("Exception in dtmf()", e);
            displayMessage("DTMF exception: " + e.getMessage());
        }
    }

    /**
     * Set calee display information
     */
    private void setCalleeDisplayInformation(final SessionState sessionState) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mCalleeNumberDisplay != null) {
                        mStatusText = convertSessionState(sessionState) + " "
                                + getIntent().getExtras().getString(Constants.KEY_NUMBER_TO_DIAL);

                        mCalleeNumberDisplay.setText(mStatusText);
                    }
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in setCalleeDisplayInformation()", e);
            displayMessage("Set callee display exception: " + e.getMessage());
        }
    }

    /**
     * Hang-up the call
     */
    private void hangup() {
        try {
            if (mSession != null) {
                mLogger.i("Hang-up");

                mSession.unregisterListener(this);
                mUser.unregisterListener(this);

                mSession.end();

                mediaPlayer.stop();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopCallTimer();
                    }
                });
            }
            goBackToKonyWithResultCode();
        } catch (Exception e) {
            displayMessage("Hang-up exception: " + e.getMessage());
            mLogger.e("Exception in hang-up", e);
        }
    }

//    public static List appendValue(HashMap[] obj, HashMap newObj) {
//        Log.d("TAG", "Append Value");
//        ArrayList<HashMap> temp = new ArrayList<HashMap>(Arrays.asList(obj));
//        temp.add(newObj);
//        return temp.toArray(callbackObjectAvaya);
//
//    }

    public static void creatActivity(String code, String description){
        Log.d("TAG", "Create activity, code :"+code+", desc :"+description);
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("dateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        map.put("description", description);

        callbackObjectAvaya.add(map);
    }

    public static void logCallbackValue(){
        for(Map<String, String> t : callbackObjectAvaya){
            Log.d("TAG", "callback avaya value" + t.toString());
        }
    }

    public static String stringActivity(List<Map> list){
        JSONArray jsonArray = new JSONArray();
        int i = 0;
        for(Map<String, String> t : list){
            Log.d("TAG", "callback avaya value" + t.toString());
            JSONObject jsonObject = new JSONObject();
            for(Map.Entry<String, String> pair : t.entrySet()){
                String key = pair.getKey();
                String val = pair.getValue();
                try {
                    jsonObject.put(key, val);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                jsonArray.put(i, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }

        return jsonArray.toString();
    }

    public static void goBackToKonyWithResultCode() {

        String result = stringActivity(callbackObjectAvaya);
        Log.d("Log Result JSON", result);
        Object[] objectKony = new Object[]{ result };
        Log.d("TAG", "callback kony ");
        logCallbackValue();
        try {
            callback.execute(objectKony);
//            callback.execute(callbackObjectAvaya.toArray());
        } catch (Exception e) {}
    }



    @Override
    public void onSessionRemoteAlerting(AudioOnlySession session, boolean hasEarlyMedia) {
        //setCalleeDisplayInformation(mSession.getState());
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //otniel-add Connecting text when session remote alerting
        TextView tv = (TextView)findViewById(R.id.txtConnected1);
//        TextView tv2 = (TextView)findViewById(R.id.txtConnected);
//        tv2.setVisibility(View.GONE);
//        tv.setVisibility(View.VISIBLE);
        mLogger.i("Session remote alerting");
        audioManager.setSpeakerphoneOn(false);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

    }

    @Override
    public void onSessionRedirected(AudioOnlySession session) {
        mLogger.i("Session redirected");
    }

    @Override
    public void onSessionQueued(AudioOnlySession session) {
        mLogger.i("Session queued");
        creatActivity("07","session queue");
    }

    @Override
    public void onSessionEstablished(AudioOnlySession session) {
        try {
            SessionState sessionState = mSession.getState();

            //setCalleeDisplayInformation(sessionState);

            mLogger.i("Session established");
            mLogger.d("Session state: " + sessionState);
            mLogger.d("Service available: " + mSession.isServiceAvailable());
            mediaPlayer.stop();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startCallTimer();
//                    tv.setVisibility(View.VISIBLE);
                    //otniel-display Connected when connection established
                    TextView tv2 = (TextView)findViewById(R.id.txtConnected1);
                    tv2.setText(ClicktoCallActivity.connectedAvaya);
                    //tv2.setText("Connected");
                    //onSessionRemoteAddressChanged(mSession, mSession.getRemoteAddress(), mSession.getRemoteDisplayName());
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in onSessionEstablished()", e);
            displayMessage("Session established exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionRemoteAddressChanged(AudioOnlySession session, String newAddress, final String newDisplayName) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mDisplayNameTextView != null) {
                        mDisplayNameTextView.setText(newDisplayName);
                    }
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in onSessionRemoteAddressChanged()", e);
            displayMessage("Session remote address changed exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionEnded(AudioOnlySession session) {
        mLogger.i("Session ended");
        hangup();

        finish();
    }

    @Override
    public void onSessionFailed(AudioOnlySession session, SessionError sessionError) {
        mLogger.e("Session failed: " + sessionError);
        creatActivity("11","license full");
        hangup();

        displayMessage(sessionError.toString(), true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopCallTimer();
            }
        });
    }

    @Override
    public void onSessionAudioMuteStatusChanged(AudioOnlySession session, boolean muted) {
        mLogger.i("Session audio mute status changed");
    }

    @Override
    public void onSessionAudioMuteFailed(AudioOnlySession session, boolean requestedMuteStatus, SessionException exception) {
        mLogger.w("Session audio mute failed");
        displayMessage("Session audio mute failed: " + exception);
    }

    @Override
    public void onSessionRemoteDisplayNameChanged(AudioOnlySession session, String s) {
        mLogger.e("Session remote display name changed: " + s);
    }

    @Override
    public void onSessionServiceAvailable(AudioOnlySession session) {
        mLogger.i("Session service available");
    }

    @Override
    public void onSessionServiceUnavailable(AudioOnlySession session) {
        mLogger.e("Session service unavailable");
        displayMessage("Session service unavailable", false);

    }

    @Override
    public void onConnectionInProgress(AudioOnlyUser arg0) {
        mLogger.i("Connection in progress");
    }

    @Override
    public void onServiceAvailable(final AudioOnlyUser user) {
        mLogger.d("onServiceAvailable");
        if (mSession == null) {
            if (mUser.isServiceAvailable()) {
                mLogger.d("service available, make call now");
                 call();
            } else {
                mLogger.e("service not available");
                String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));
                hangup();
                displayMessage(message, true);
            }
        } else {
            mLogger.d("session state: " + mSession.getState() + ", not placing call now");
        }

    }

    @Override
    public void onServiceUnavailable(AudioOnlyUser arg0) {
        mLogger.e("Service unavailable");

        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));

        displayToast(message);
        creatActivity("01","service unavailable");
    }

    @Override
    public void onConnLost(AudioOnlyUser user) {
        mLogger.w("Connection lost");
        hangup();
        displayToast("Connection lost");
        creatActivity("02","connection lost");

        finish();
    }

    @Override
    public void onConnRetry(AudioOnlyUser user) {
        mLogger.i("Connection retry");
    }

    @Override
    public void onConnReestablished(AudioOnlyUser user) {
        mLogger.i("Connection reestablished");
        displayToast("Connection reestablished");
    }

    @Override
    public void onNetworkError(AudioOnlyUser user) {
        mLogger.w("Network error");

        hangup();

        displayToast("Network error");
    }

    @Override
    public void onCriticalError(AudioOnlyUser user) {
        mLogger.w("Critical error");
        displayMessage("Critical error");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mLogger.i("Configuration changed");
    }

    @Override
    public void onCallError(AudioOnlySession session, SessionError sessionError, String message, String reason) {
        displayError("Call error", sessionError, message, reason);
    }

    @Override
    public void onDialError(AudioOnlySession session, SessionError sessionError, String message, String reason) {
        displayError("Dial error", sessionError, message, reason);
    }

    private void displayError(String errorType, SessionError sessionError, String message, String reason) {
        if (message == null) {
            message = "";
        }

        if (reason == null) {
            reason = "";
        }

        String errorMessage = errorType + ": " + sessionError + ", " + message + ", " + reason;

        mLogger.w(errorMessage);

        displayMessage(errorMessage);
    }

    @Override
    public void onQualityChanged(AudioOnlySession session, final int i) {
        mLogger.d("Quality changed: " + i);

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ImageView imgConn = (ImageView)findViewById(R.id.imgView);

                    if (i >= 0 && i <= 25) {
                        imgConn.setBackgroundResource(R.drawable.signal_red);
                    }else if(i>25 && i < 60){
                        imgConn.setBackgroundResource(R.drawable.signal_orange);
                    }else if(i>= 60 && i <= 70){
                        imgConn.setBackgroundResource(R.drawable.signal_green);
                    }else{
                        imgConn.setBackgroundResource(R.drawable.signal_full);
                    }
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in onQualityChanged()", e);
        }
    }

    @Override
    public void onCapacityReached(AudioOnlySession session) {
        mLogger.w("Capacity reached");
        displayMessage(getResources().getString(R.string.capacity_reached));
        creatActivity("10","capacity reached");
    }



    /**
     * Convert the session state to a string representation
     *
     * @param sessionState
     * @return
     */
    private String convertSessionState(SessionState sessionState) {
        String strSessionState;

        switch (sessionState) {
            case ENDED:
                strSessionState = "Ended";
                break;
            case ESTABLISHED:
                strSessionState = "Established";
                break;
            case ENDING:
                strSessionState = "Ending";
                break;
            case FAILED:
                strSessionState = "Failed";
                break;
            case IDLE:
                strSessionState = "Idle";
                break;
            case INITIATING:
                strSessionState = "Initiating";
                break;
            case REMOTE_ALERTING:
                strSessionState = "Remote alerting";
                break;
            default:
                strSessionState = "Unknown session state";
        }

        return strSessionState;
    }

    /**
     * Display message
     *
     * @param message
     */
    private void displayMessage(final String message) {
        displayMessage(message, false);
    }

    private void displayMessage(final String message, final boolean finishActivity) {
        mLogger.d("Display message: " + message);
        final Activity activity = this;

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    GeneralDialogFragment.displayMessage(activity, message, finishActivity);
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in displayMessage()", e);
        }
    }

    private void displayToast(final String message) {
        final Activity activity = this;

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in displayToast()", e);
        }
    }


    private String addNetworkConnectionMessage(String message) {
        try {
            InternetConnectionDetector internetConnectionDetector = new InternetConnectionDetector(this.getApplicationContext());

            if (!internetConnectionDetector.getWifiConnected()) {
                message += "\n" + getResources().getString(R.string.no_internet_connection_detected);
            }
        } catch (Exception e) {
            mLogger.e("Exception in addNetworkConnectionMessage()", e);
            displayMessage("Network connection message exception: " + e.getMessage());
        }

        return message;
    }

    private void startCallTimer() {
        try {
            mCallTimeChecker.run();
        } catch (Exception e) {
            mLogger.e("Exception in startCallTimer()", e);
            displayMessage("Call timer exception: " + e.getMessage());
        }
    }

    private void stopCallTimer() {
        try {
            mTimerHandler.removeCallbacks(mCallTimeChecker);
        } catch (Exception e) {
            mLogger.e("Exception in stopCallTimer()", e);
            displayMessage("Stop call timer exception: " + e.getMessage());
        }
    }

    private void updateCallTime() {
        try {
            long callTimeElapsed = mSession.getCallTimeElapsed();

            int callTimeSeconds;
            if (callTimeElapsed == -1L) {
                callTimeSeconds = 0;
            } else {
                callTimeSeconds = (int) (callTimeElapsed / 1000);
            }

            int seconds = callTimeSeconds % 60;
            int minutes = ((callTimeSeconds - seconds) / 60);

            String title = String.format(Constants.CALL_TIME_ELAPSED_FORMAT, mStatusText, Constants.CALL_TIME_ELAPSED_SEPARATOR, minutes, seconds, Constants.CALL_TIME_ELAPSED_END);

            updateStatus(title);
        } catch (Exception e) {
            mLogger.e("Exception in updateCallTime()", e);
            displayMessage("Update call timer exception: " + e.getMessage());
        }
    }

    private void updateStatus(final String message) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCalleeNumberDisplay != null) {
                        mCalleeNumberDisplay.setText(message);
                    }
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in updateStatus()", e);
            displayMessage("Update status exception: " + e.getMessage());
        }
    }

    protected boolean getCallOnHold() {
        return mCallOnHold;
    }

    @Override
    public void onGetMediaError(AudioOnlySession session) {
        mLogger.e("Get media error");
        displayMessage(getResources().getString(R.string.get_media_error));
    }

}
