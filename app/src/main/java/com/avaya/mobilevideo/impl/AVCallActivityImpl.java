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
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SignalStrength;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avaya.clientplatform.api.CameraType;
import com.avaya.clientplatform.api.ClientPlatform;
import com.avaya.clientplatform.api.DTMFType;
import com.avaya.clientplatform.api.Device;
import com.avaya.clientplatform.api.Orientation;
import com.avaya.clientplatform.api.Participant;
import com.avaya.clientplatform.api.Session;
import com.avaya.clientplatform.api.SessionError;
import com.avaya.clientplatform.api.SessionException;
import com.avaya.clientplatform.api.SessionListener2;
import com.avaya.clientplatform.api.SessionState;
import com.avaya.clientplatform.api.User;
import com.avaya.clientplatform.api.UserListener2;
import com.avaya.clientplatform.api.VideoListener;
import com.avaya.clientplatform.api.VideoResolution;
import com.avaya.clientplatform.api.VideoSurface;
import com.avaya.clientplatform.impl.SessionImpl;
import com.avaya.clientplatform.impl.VideoSurfaceImpl;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.InternetConnectionDetector;
import com.avaya.mobilevideo.utils.Logger;
import com.konylabs.vm.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which contains the call functionality
 *
 * @author Avaya Inc
 */
public abstract class AVCallActivityImpl extends MobileVideoActivity implements SessionListener2, UserListener2 {

    private static final String TAG = AVCallActivityImpl.class.getSimpleName();
    private static VideoResolution mPreferredVideoResolution = VideoResolution.RESOLUTION_1280x720;

    private Logger mLogger = Logger.getLogger(TAG);
    private MediaPlayer mediaPlayer;
    private OrientationEventListener mOrientationEventListener;

    private Device mDevice;
    private User mUser;
    private ClientPlatform mPlatform;
    private SessionImpl mSession;
    private VideoSurface mRemoteVideoSurface;
    private VideoSurface mPreviewView;
    private TextView mDisplayNameTextView = null;
    private TextView mCalleeNumberDisplay = null;
    private ToggleButton mMuteVideo = null;
    private boolean mCallOnHold = false;

    private boolean mEnableVideo;
    private boolean mStartAudioMuted;
    private boolean mStartVideoMuted;
    private String mContextId;
    private CameraType mCamera;

    private PopupWindow mDtmfPopupWindow = null;

    private String mStatusText = "";
    private Handler mTimerHandler;
    public static Function callback;
//    public static Function signalstrength;

    public static String strSessionState;
    public LinearLayout layoutCall;
    public RelativeLayout layoutEndCall;
    public RelativeLayout layoutConnLost;
    public TextView popupHeader;
    public TextView popupBody;
    public Button buttonNo;
    public Button buttonYes;
    public static String avayaPopupHeader;
    public static String avayaPopupBody;
    public static String avayaYesButton;
    public static String avayaNoButton;
    public ToggleButton btnMuteAudioNormal;
    public ToggleButton btnMuteAudioSlashed;

    public static List<Map> callbackObjectAvaya = new ArrayList<Map>();
    public static List<Map> callbackSignal = new ArrayList<Map>();

    public static int qualityCall = 100;
    public static int signalSupport = 0;
    public static SignalStrength signal;
    public static WifiManager wifiSignal;
    public static ConnectivityManager connect;
    public int type;

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
        setContentView(R.layout.activity_call);
        layoutCall = (LinearLayout)findViewById(R.id.linearLayoutMain);
        layoutEndCall = (RelativeLayout)findViewById(R.id.relativeLayoutEndCall);
        popupHeader = (TextView)findViewById(R.id.endCallTitle);
        popupBody = (TextView)findViewById(R.id.endCallBody);
        buttonYes = (Button)findViewById(R.id.btnYesEndCall);
        buttonNo = (Button)findViewById(R.id.btnNoEndCall);
        btnMuteAudioNormal = (ToggleButton)findViewById(R.id.btnMuteAudio);
        btnMuteAudioSlashed = (ToggleButton) findViewById(R.id.btnMuteAudioSlashed);
        popupHeader = (TextView) findViewById(R.id.endCallTitle);
        popupBody = (TextView) findViewById(R.id.endCallBody);
        layoutConnLost = (RelativeLayout) findViewById(R.id.relativeLayoutConnLost);

        mTimerHandler = new Handler();
        mediaPlayer = MediaPlayer.create(AVCallActivityImpl.this, R.raw.ringback);
        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == 0) {
                    mDevice.setCameraCaptureOrientation(Orientation.TWO_SEVENTY);
                } else if (orientation == 90) {
                    mDevice.setCameraCaptureOrientation(Orientation.ONE_EIGHTY);
                } else if (orientation == 180) {
                    mDevice.setCameraCaptureOrientation(Orientation.NINETY);
                } else if (orientation == 270) {
                    mDevice.setCameraCaptureOrientation(Orientation.ZERO);
                }
            }

        };
        if (mOrientationEventListener.canDetectOrientation()) {
          //  mOrientationEventListener.enable();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogger.d("onDestroy()");

        //mUser.terminate();
        //mLogger.d("terminated user connection");

        mPlatform = null;
        mUser = null;
        if (mDevice != null) {
            mDevice.setLocalVideoView(null);
            mDevice = null;
        }

        mRemoteVideoSurface = null;
        mPreviewView = null;
        mOrientationEventListener.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mLogger.d("onResume()");

            Bundle extras = getIntent().getExtras();
            mContextId = extras.getString(Constants.KEY_CONTEXT).trim();
            mEnableVideo = extras.getBoolean(Constants.KEY_ENABLE_VIDEO);
            mStartAudioMuted = extras.getBoolean(Constants.KEY_START_MUTED_AUDIO);
            mStartVideoMuted = extras.getBoolean(Constants.KEY_START_MUTED_VIDEO);

            mCamera = CameraType.FRONT;

            // First instantiate a client platform interface
            // The factory is the top level object that provides access to the
            // services exposed by the SDK.
            mPlatform = ClientPlatformManager.getClientPlatform(this.getApplicationContext());
            mUser = mPlatform.getUser();

            // Create user object that is used to encapsulate the call session.
            String token = extras.getString(Constants.DATA_SESSION_KEY);

            boolean tokenAccepted = mUser.setSessionAuthorizationToken(token);

            if (tokenAccepted) {
                mUser.registerListener(this);
                mUser.acceptAnyCertificate(true);

                // Create a device object that this application
                mPlatform.getDevice();
                if (mDevice != null) {
                    mLogger.d("IsMediaAccessible : " + mDevice.couldMediaBeAccessible(getApplicationContext()));
                }

                if (mSession == null) {
                    if (mUser.isServiceAvailable()) {
                        mLogger.d("service available, make call now");
                        call();
                    } else {
                        mLogger.e("service not available");
                        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));
                        //hangup();
                        //displayMessage(message, true);
                    }
                } else {
                    mLogger.d("session state: " + mSession.getState() + ", not placing call now");
                }
            } else {
                mLogger.w("Invalid token used");
 //               displayMessage(getResources().getString(R.string.invalid_token), true);
            }
        } catch (Exception e) {
            mLogger.e("Exception in onResume(): " + e.getMessage(), e);
//            displayMessage("Call activity resume exception: " + e.getMessage());
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
            ClientPlatform clientPlatform = ClientPlatformManager.getClientPlatform(this.getApplicationContext());
            String browser = clientPlatform.getUserAgentBrowser();
            String version = clientPlatform.getUserAgentVersion();
            mSession = (SessionImpl) mUser.createSession();
            mSession.registerListener(this);
            mSession.enableAudio(true);
            mSession.enableVideo(mEnableVideo);
            mSession.muteAudio(mStartAudioMuted);
            mSession.muteVideo(mStartVideoMuted);

            if (mStartVideoMuted) {
                uncheckMuteVideoControl();
            }

            if (mContextId != null && mContextId.length() > 0) {
                mLogger.d("Context ID:" + mContextId);
                mSession.setContextId(mContextId);
            }

            String numberToDial = getIntent().getExtras().getString(Constants.KEY_NUMBER_TO_DIAL);
            mSession.setRemoteAddress(numberToDial);
            if (mEnableVideo) {
                // Initialize video transmission components
                // Select the camera device (front or back)
                mDevice.selectCamera(mCamera);
                mDevice.setCameraCaptureResolution(mPreferredVideoResolution);
            }

            // Lock into portrait mode// This may differ device to device
            // Set this in case the user escalates to a video call after initiating a voice call
            // The default orientation (0) refers to landscape right, so as this app is portrait only
            // change the orientation
            mDevice.setCameraCaptureOrientation(Orientation.TWO_SEVENTY);

            if(type == connect.TYPE_MOBILE){
                onSignalStrengthsChanged(signal);
            }else if(type == connect.TYPE_WIFI){
                onReceive(wifiSignal);
            }
            mLogger.d("type value: "+type);
            mLogger.d("signal value: "+signal);
            // Start the call session.
            // During the call establishment, the one-time session ID is obtained
            // from the server behind the scenes.
            // ICE candidate list is obtained and the correct local IP address is
            // determined.
            // The Session object implementation controls the media channels of the
            // call based on the signaling protocol feedback.
            mSession.start();
            this.callbackObjectAvaya = new ArrayList<Map>();

            setCalleeDisplayInformation(mSession.getState());
            mLogger.d("Browser: " + browser + ", version: " + version);
            mLogger.d("Orientation: " + mDevice.getCameraCaptureOrientation());
            mLogger.d("Camera capture resolution: " + mDevice.getCameraCaptureResolution());
            mLogger.d("Session authorisation token: " + mUser.getSessionAuthorizationToken());

        } catch (Exception e) {
            mLogger.e("Exception occurred in AVCallActivityImpl.call(): " + e.getMessage(), e);
    //        displayMessage(getResources().getString(R.string.call_failed) + e.getMessage());
            hangup();
            finish();
        }
    }

//    public static HashMap[] appendValue(HashMap newObj) {
//        Log.d("TAG", "Append Value");
//        Map temp = Arrays.asList(obj);
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
        map.put("qualityCall", String.valueOf(qualityCall));
        map.put("wifi signal",String.valueOf(wifiSignal));
        map.put("mobile signal",String.valueOf(signal));
        callbackObjectAvaya.add(map);
    }

//    public static void modActivity(int quality){
//        for (int i = 0; i < callbackObjectAvaya.size(); i++) {
//            callbackObjectAvaya.get(i).put("qualityCall", quality);
//        }
//    }

    public static void logCallbackValue(){
        for(Map<String, String> t : callbackObjectAvaya){
            Log.d("TAG", "callback avaya value" + t.toString());
        }
    }

//    public static void main(String[] args){
//        System.out.println(callbackObjectAvaya.length);
//        HashMap map = new HashMap();
//        map.put("code", "03");
//        map.put("dateTime", "20190807");
//        map.put("description", "focus changed");
//        callbackObjectAvaya = appendValue(callbackObjectAvaya, map);
//
//        HashMap map1 = new HashMap();
//        map1.put("code", "04");
//        map1.put("dateTime", "20190808");
//        map1.put("description", "focus");
//        callbackObjectAvaya = appendValue(callbackObjectAvaya, map1);
//        System.out.println(callbackObjectAvaya.length);
//
//        for(HashMap t : callbackObjectAvaya){
//            System.out.println(t.toString());
//        }
//    }

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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        createVideoComponents();
        mLogger.d("Focus change value "+hasFocus);

        //call activity
        creatActivity("03", "Focus Changed" + hasFocus);
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
     * Set mute video button
     *
     * @param muteVideo
     */
    protected void setMuteVideo(ToggleButton muteVideo) {
        this.mMuteVideo = muteVideo;
    }

    /**
     * End call
     *
     * @param V
     */
    public void endCall(View V) {
        endCall();
    }

    protected void endCall() {
        try {
//            this.callbackObjectAvaya = new ArrayList<>();
            creatActivity("02","endCall");
            logCallbackValue();

            hangup();
//            this.callbackObjectAvaya = new ArrayList<Map>();
            finish();
            mLogger.d("success new array list");
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
            if (btnMuteAudioNormal.getVisibility() == View.VISIBLE) {
                btnMuteAudioNormal.setVisibility(View.GONE);
                btnMuteAudioSlashed.setVisibility(View.VISIBLE);
            } else {
                btnMuteAudioNormal.setVisibility(View.VISIBLE);
                btnMuteAudioSlashed.setVisibility(View.GONE);
            }
            mLogger.d("Toggle mute audio");
            boolean wasMuted = mSession.isAudioMuted();
            boolean nowMuted = !wasMuted;

            mSession.muteAudio(nowMuted);
        } catch (Exception e) {
        }
    }

    public void closeCall(View v)
    {
        closeCallHandler();
    }

    public void onClickNoEndCall(View v)
    {
        onClickNoEndCall();
    }

    protected void onClickNoEndCall()
    {
        layoutCall.setVisibility(View.VISIBLE);
    }

    public void onClickYesEndCall(View v)
    {
        creatActivity("02","endCall");
        logCallbackValue();
        hangup();
        finish();
    }

    protected void closeCallHandler()
    {
        try
        {
            layoutCall.setVisibility(View.GONE);
            //           layoutEndCall.setVisibility(View.VISIBLE);
            popupHeader.setText(avayaPopupHeader);
            popupBody.setText(avayaPopupBody);
            buttonYes.setText(avayaYesButton);
            buttonNo.setText(avayaNoButton);
        }
        catch (Throwable e)
        {
            this.mLogger.e("Throwable in closecall()", e);
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
 //           displayMessage("Hold exception: " + e.getMessage());
        }
    }

    /**
     * Toggle mute video
     *
     * @param v
     */
    public void toggleMuteVideo(View v) {
        try {
            mLogger.d("Toggle mute video");
            boolean wasMuted = mSession.isVideoMuted();
            boolean nowMuted = !wasMuted;

            mSession.muteVideo(nowMuted);
        } catch (Exception e) {
            mLogger.e("Exception in toggleMuteVideo()", e);
     //       displayMessage("Mute video exception: " + e.getMessage());
        }
    }

    /**
     * Toggle enable audio
     *
     * @param v
     */
    public void toggleEnableAudio(View v) {
        try {
            mLogger.d("Toggle enable audio");
            boolean wasEnabled = mSession.isAudioEnabled();
            boolean nowEnabled = !wasEnabled;

            mSession.enableAudio(nowEnabled);
        } catch (Exception e) {
            mLogger.e("Exception in toggleEnableAudio()", e);
   //         displayMessage("Enable audio exception: " + e.getMessage());
        }
    }

    /**
     * Switch video between front and back camera
     *
     * @param v
     */
    public void switchVideo(View v) {
        try {
            mLogger.d("Switch camera");

            switch (mDevice.getSelectedCamera()) {
                case BACK:
                    mLogger.d("Using back camera, switch to front");
                    mCamera = CameraType.FRONT;
                    break;

                case FRONT:
                    mLogger.d("Using front camera, switch to back");
                    mCamera = CameraType.BACK;
                    break;

                default:
                    mLogger.d("Camera DEFAULT: " + mDevice.getSelectedCamera());
                    break;
            }

            mDevice.selectCamera(mCamera);
            mLogger.d("Camera after switch: " + mDevice.getSelectedCamera());
        } catch (Exception e) {
            mLogger.e("Exception in switchVideo()", e);
   //         displayMessage("Switch camera exception: " + e.getMessage());
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
  //          displayMessage("Launch DTMF exception: " + e.getMessage());
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
            String digit = (String) v.getTag();
            mLogger.d("DTMF: " + digit);
            mSession.sendDTMF(DTMFType.get(digit), true);
        } catch (Exception e) {
            mLogger.e("Exception in dtmf()", e);
 //           displayMessage("DTMF exception: " + e.getMessage());
        }
    }

    /**
     * Toggle enable video
     *
     * @param v
     */
    public void toggleEnableVideo(View v) {
        try {
            mLogger.d("Toggle enable video");
            boolean isEnabled = mSession.isVideoEnabled();

            mSession.enableVideo(!isEnabled);
        } catch (Exception e) {
            mLogger.e("Exception in toggleEnableVideo()", e);
    //        displayMessage("Enable video exception: " + e.getMessage());
        }
    }

    /**
     * Uncheck mute video control
     */
    private void uncheckMuteVideoControl() {
        try {
            if (mMuteVideo != null) {
                mMuteVideo.setChecked(false);
            }
        } catch (Exception e) {
            mLogger.e("Exception in uncheckMuteVideoControl()", e);
  //          displayMessage(e.getMessage());
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
//            displayMessage("Set callee display exception: " + e.getMessage());
        }
    }

    /**
     * Hang-up the call
     */
    private void hangup() {
        try {
            mLogger.i("Hang-up");
            if (mSession != null) {
                if (mSession.isVideoEnabled()) {

                    mDevice.setLocalVideoView(null);
                    mDevice.setRemoteVideoView(null);
                }

                mSession.unregisterListener(this);
                mUser.unregisterListener(this);
                mUser.terminate();
                mLogger.d("terminated user connection");

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
//            displayMessage("Hang-up exception: " + e.getMessage());
            mLogger.e("Exception in hang-up", e);
        }
    }

    @Override
    public void onSessionRemoteAlerting(Session session, boolean hasEarlyMedia) {
        setCalleeDisplayInformation(mSession.getState());
        mLogger.i("Session remote alerting");
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
//        this.callbackObjectAvaya = new ArrayList<Map>();
    }

    @Override
    public void onSessionRedirected(Session session) {
        mLogger.i("Session redirected");
    }

    @Override
    public void onSessionQueued(Session session) {
        mLogger.i("Session queued");
        creatActivity("07","session queue");
        logCallbackValue();
    }

    @Override
    public void onSessionEstablished(Session session) {
        try {
            SessionState sessionState = mSession.getState();

            setCalleeDisplayInformation(sessionState);

            mediaPlayer.stop();

            mLogger.i("Session established");
            mLogger.d("Session state: " + sessionState);
            mLogger.d("Service available: " + mSession.isServiceAvailable());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startCallTimer();
                    onSessionRemoteAddressChanged(mSession, mSession.getRemoteAddress(), mSession.getRemoteDisplayName());
                }
            });
        } catch (Exception e) {
            mLogger.e("Exception in onSessionEstablished()", e);
  //          displayMessage("Session established exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionRemoteAddressChanged(Session session, String newAddress, final String newDisplayName) {
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
 //           displayMessage("Session remote address changed exception: " + e.getMessage());
        }
    }

    @Override
    public void onSessionEnded(Session session) {
        mLogger.i("Session ended");
        creatActivity("01","session ended");
        logCallbackValue();
        hangup();
        finish();
        this.callbackObjectAvaya = new ArrayList<Map>();
        mLogger.d("success make new array list in session ended");
    }

    @Override
    public void onSessionFailed(Session session, SessionError sessionError) {
        mLogger.e("Session failed: " + sessionError);

        hangup();
        finish();

  //      displayMessage(sessionError.toString(), true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopCallTimer();
            }
        });
    }

    //20190830-otniel-add function signal strength

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//            super.onSignalStrengthsChanged(signalStrength);
            try {
            mLogger.d("calling function onSignalStrength");
            signalSupport = signalStrength.getGsmSignalStrength();
            String val= String.valueOf(signalSupport);
            Object[] objectKony = new Object[]{ val };
            Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + signalSupport);

            if (signalSupport > 30) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Good");
                callback.execute(objectKony);

            } else if (signalSupport > 20 && signalSupport < 30) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Avarage");
                hangup();
                finish();

            } else if (signalSupport < 20 && signalSupport > 3) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Weak");
                hangup();
                finish();

            } else if (signalSupport < 3) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Very weak");
                hangup();
                finish();
            }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    public void onReceive(WifiManager wifiManager) {
        int numberOfLevels=5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level= WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        String value = String.valueOf(level);
        Object[] objectKony = new Object[]{ value };
        mLogger.d("Bars =" +level);
        if (level <= 0 && level >= -50) {
            //Best signal;
            try {
                callback.execute(objectKony);
            } catch (Exception e) {
                e.printStackTrace();
            }
            call();
        } else if (level < -50 && level >= -70) {
            //Good signal
            call();
        } else if (level < -70 && level >= -80) {
            //Low signal
            hangup();

        } else if (level < -80 && level >= -100) {
            //Very weak signal
            hangup();
        } else {
            // no signals
            hangup();
        }
    }

    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = AVCallActivityImpl.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }




    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = AVCallActivityImpl.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }



    @Override
    public void onSessionAudioMuteStatusChanged(Session session, boolean muted) {
        mLogger.i("Session audio mute status changed");
    }

    @Override
    public void onSessionAudioMuteFailed(Session session, boolean requestedMuteStatus, SessionException exception) {
        mLogger.w("Session audio mute failed");
  //      displayMessage("Session audio mute failed: " + exception);
    }

    @Override
    public void onSessionVideoMuteStatusChanged(Session session, boolean muted) {
        mLogger.i("Session video mute status changed");
    }

    @Override
    public void onSessionVideoMuteFailed(Session session, boolean requestedMuteStatus, SessionException exception) {
        mLogger.w("Session video mute failed");
//        displayMessage("Session video mute failed: " + exception);
    }

    @Override
    public void onSessionVideoRemovedRemotely(Session session) {
        mLogger.w("Session video removed remotely");
    }

    @Override
    public void onSessionServiceAvailable(Session session) {
        mLogger.i("Session service available");
    }

    @Override
    public void onSessionServiceUnavailable(Session session) {
        mLogger.e("Session service unavailable");
  //      displayMessage("Session service unavailable", false);
    }

    @Override
    public void onSessionRemoteDisplayNameChanged(Session session, String s) {
        mLogger.e("Session remote display name changed: " + s);
    }

    @Override
    public void onConnectionInProgress(User arg0) {
        mLogger.i("Connection in progress");
        creatActivity("05","connection in progress");
        logCallbackValue();
    }

    @Override
    public void onServiceAvailable(final User user) {
        mLogger.d("onServiceAvailable");
        call();
    }

    @Override
    public void onServiceUnavailable(User arg0) {
        mLogger.e("Service unavailable");
        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));
        displayToast(message);
    }

    @Override
    public void onCallError(Session session, SessionError sessionError, String message, String reason) {
        displayError("Call error", sessionError, message, reason);
    }

    @Override
    public void onDialError(Session session, SessionError sessionError, String message, String reason) {
        displayError("Dial error", sessionError, message, reason);
    }

    @Override
    public void onConnLost(User user) {
        mLogger.w("Connection lost");
        displayToast("Connection lost");
        creatActivity("04","Connection Lost ");
        logCallbackValue();
        hangup();
        finish();

    }

    @Override
    public void onConnRetry(User user) {
        mLogger.i("Connection retry");
        creatActivity("06","connection retry");
        logCallbackValue();
    }

    @Override
    public void onNetworkError(User user) {
        mLogger.w("Network error");
        hangup();
        finish();
        displayToast("Network error");
    }

    @Override
    public void onConnReestablished(User user) {
        mLogger.i("Connection reestablished");
        displayToast("Connection reestablished");
    }

    @Override
    public void onCriticalError(User user) {
        mLogger.w("Critical error");
  //      displayMessage("Critical error");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mLogger.i("Configuration changed");
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
    public void onQualityChanged(Session session, final int i) {
        mLogger.d("Quality changed: " + i);

        try {
            qualityCall = i;
            creatActivity("08","quality change " + i);

            logCallbackValue();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                   /*
                    ProgressBar progress = (ProgressBar) findViewById(R.id.call_quality_bar);

                    if (i >= Constants.MINIMUM_CALL_QUALITY && i <= Constants.MAXIMUM_CALL_QUALITY) {
                        progress.setProgress(i);
                    }
                    */
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception in onQualityChanged()", e);
        }
    }

    @Override
    public void onCapacityReached(Session session) {
        mLogger.w("Capacity reached");
 //       displayMessage(getResources().getString(R.string.capacity_reached));
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

    private void createVideoComponents() {
        try {
            if (mRemoteVideoSurface == null) {
                mLogger.d("Creating Video Components");

                ClientPlatform clientPlatform = ClientPlatformManager.getClientPlatform(this);

                RelativeLayout rlRemote = (RelativeLayout) findViewById(R.id.remoteLayout);
                RelativeLayout rlLocal = (RelativeLayout) findViewById(R.id.localLayout);

                mDevice = clientPlatform.getDevice();
                Point remoteSize = new Point(rlRemote.getWidth(), rlRemote.getHeight());
                Point localSize = new Point(rlLocal.getWidth(), rlLocal.getHeight());

                mRemoteVideoSurface = new VideoSurfaceImpl(this, remoteSize, new VideoListenerClass());
                mPreviewView = new VideoSurfaceImpl(this, localSize, new VideoListenerClass());

                mRemoteVideoSurface.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rlRemote.addView(mRemoteVideoSurface);

                mPreviewView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rlLocal.addView(mPreviewView);

                mDevice.setLocalVideoView(mPreviewView);
                mDevice.setRemoteVideoView(mRemoteVideoSurface);

                /*
                 * We need to keep the screen on to prevent it sleeping during video
                 * calls
                 */
                getWindow()
                        .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception e) {
            mLogger.e("Exception in createVideoComponents()", e);
    //        displayMessage("Create video components exception: " + e.getMessage());
        }
    }

    /**
     * Get display
     *
     * @return display
     */
    private Display getDisplay() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private class VideoListenerClass implements VideoListener {
        @Override
        public void renderingStart(VideoSurface videoSurface) {
            try {
                if (videoSurface == mPreviewView) {
                    mDevice.setLocalVideoView(videoSurface);
                } else if (videoSurface == mRemoteVideoSurface) {
                    mDevice.setRemoteVideoView(videoSurface);
                } else {
                    mLogger.e("Unknown surface");
                }
            } catch (Exception e) {
                mLogger.e("Exception in renderingStart()", e);
     //           displayMessage("Rendering start exception: " + e.getMessage());
            }
        }

        @Override
        public void frameSizeChanged(final int width, final int height, final Participant endpoint, final VideoSurface videoView) {
        }
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
  //          displayMessage("Network connection message exception: " + e.getMessage());
        }

        return message;
    }

    private void startCallTimer() {
        try {
            mCallTimeChecker.run();
        } catch (Exception e) {
            mLogger.e("Exception in startCallTimer()", e);
    //        displayMessage("Call timer exception: " + e.getMessage());
        }
    }

    private void stopCallTimer() {
        try {
            mTimerHandler.removeCallbacks(mCallTimeChecker);
        } catch (Exception e) {
            mLogger.e("Exception in stopCallTimer()", e);
    //        displayMessage("Stop call timer exception: " + e.getMessage());
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
       //     displayMessage("Update call timer exception: " + e.getMessage());
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
     //       displayMessage("Update status exception: " + e.getMessage());
        }
    }

    public static void setPreferredVideoResolution(VideoResolution preferredVideoResolution) {
        if (preferredVideoResolution != null) {
            mPreferredVideoResolution = preferredVideoResolution;
        }
    }

    protected boolean getCallOnHold() {
        return mCallOnHold;
    }

    @Override
    public void onGetMediaError(Session session) {
        mLogger.e("Get media error");
//        displayMessage(getResources().getString(R.string.get_media_error));
    }
}
