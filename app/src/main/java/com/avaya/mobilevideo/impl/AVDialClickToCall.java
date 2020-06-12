/**
 * AVDialActivityImpl.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avaya.clientplatform.api.CameraType;
import com.avaya.clientplatform.api.ClientPlatform;
import com.avaya.clientplatform.api.Device;
import com.avaya.clientplatform.api.User;
import com.avaya.clientplatform.api.VideoResolution;
import com.avaya.clientplatform.api.VideoSpecification;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which contains dial functionality
 *
 * @author Avaya Inc
 */
public abstract class AVDialClickToCall extends MobileVideoActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = AVDialClickToCall.class.getSimpleName();
    private static final int REQUEST_CODE_DIAL = 1;

    private Logger mLogger = Logger.getLogger(TAG);

    private boolean isFirstInit = true;
    private String mServer = "";
    private Map<String, VideoResolution> mSupportedCameraCaptureResolutionMap = new HashMap<String, VideoResolution>();
    private Class<? extends AVClickToCall> mCallActivityClass;
    private VideoResolution mVideoResolution;

    @Override
    protected void onResume() {
        super.onResume();

        Thread thread = new Thread() {
            public void run() {
//                setSupportedCameraCaptureResolutions();
            }
        };

        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLogger.d("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        mLogger.d("Logging Out");
        logout();

    }

    /**
     * @param callActivityClass
     */
    protected void setCallActivityClass(Class<? extends AVClickToCall> callActivityClass) {
        mLogger.d("Set the call activity");
        mCallActivityClass = callActivityClass;
    }

    private void setSupportedCameraCaptureResolutions() {
        try {
            mLogger.d("Set supported camera capture resolutions");

            CameraType cameraType = CameraType.FRONT;
            String token = getIntent().getExtras().getString(Constants.DATA_SESSION_KEY);

            ClientPlatform platform = ClientPlatformManager.getClientPlatform(getApplicationContext());

            User user = platform.getUser();

            boolean tokenAccepted = user.setSessionAuthorizationToken(token);

            if (tokenAccepted) {
                user.acceptAnyCertificate(true);

                Device device = platform.getDevice();

                VideoSpecification[] videoSpecifications = device.getSupportedCameraCaptureResolutions(cameraType);

                mSupportedCameraCaptureResolutionMap = mapVideoSpecificationsToResolutions(videoSpecifications);

                List<String> resolutionList = new ArrayList<String>();
                resolutionList.addAll(mSupportedCameraCaptureResolutionMap.keySet());

                Collections.sort(resolutionList);
                Collections.reverse(resolutionList);

                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, resolutionList);

                final AdapterView.OnItemSelectedListener onItemSelectedListener = this;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        updateResolutionSpinner(dataAdapter, onItemSelectedListener);
                    }
                });
            } else {
                mLogger.w("Invalid token used");
                displayMessage(getResources().getString(R.string.invalid_token));
            }
        } catch (Exception e) {
            mLogger.e("Exception occurred in setSupportedCameraCaptureResolutions(): " + e.getClass() + ": " + e.getMessage(), e);
            displayToast("Set supported camera capture resolutions exception: " + e.getMessage());
        }
    }

    private void updateResolutionSpinner(final ArrayAdapter<String> dataAdapter, final AdapterView.OnItemSelectedListener onItemSelectedListener) {
        Spinner spinner = (Spinner) findViewById(R.id.resolution_spinner);

        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        findViewById(R.id.btnVideoCall).performClick();
        /*if(isFirstInit) {
            isFirstInit = false;
            findViewById(R.id.btnVideoCall).performClick();
        }*/
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        Object item = parent.getItemAtPosition(pos);

//        setCameraCaptureResolution(item);
    }


    private void setCameraCaptureResolution(Object item) {
        mLogger.d("Update camera capture resolution");

        if (item != null) {
            try {
                String itemValue = (String) item;

                if (mSupportedCameraCaptureResolutionMap.containsKey(itemValue)) {
                    mVideoResolution = mSupportedCameraCaptureResolutionMap.get(itemValue);

                    mLogger.d("Video resolution to use: " + mVideoResolution);

                } else {
                    mLogger.i("Item not found in resolution map");
                }

            } catch (Exception e) {
                mLogger.e("Exception: " + e.getMessage(), e);
                displayMessage("Set camera capture resolutions exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private Map<String, VideoResolution> mapVideoSpecificationsToResolutions(VideoSpecification[] videoSpecifications) {
        Map<String, VideoResolution> supportedCameraCaptureResolutionMap = new HashMap<String, VideoResolution>();
        if (videoSpecifications != null) {
            mLogger.d("Number of video specifications: " + videoSpecifications.length);

            for (VideoSpecification videoSpecification : videoSpecifications) {
                mLogger.d("Video specification: " + videoSpecification.getFps() + ", " + videoSpecification.getWidth() + ", " + videoSpecification.getHeight());

                if (videoSpecification.getWidth() == 176 && videoSpecification.getHeight() == 144) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_176x144)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_176x144, VideoResolution.RESOLUTION_176x144);
                        mLogger.d("Adding: " + Constants.RESOLUTION_176x144);
                    }
                } else if (videoSpecification.getWidth() == 320 && videoSpecification.getHeight() == 180) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_320x180)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_320x180, VideoResolution.RESOLUTION_320x180);
                        mLogger.d("Adding: " + Constants.RESOLUTION_320x180);
                    }
                } else if (videoSpecification.getWidth() == 352 && videoSpecification.getHeight() == 288) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_352x288)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_352x288, VideoResolution.RESOLUTION_352x288);
                        mLogger.d("Adding: " + Constants.RESOLUTION_352x288);
                    }
                } else if (videoSpecification.getWidth() == 640 && videoSpecification.getHeight() == 360) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_640x360)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_640x360, VideoResolution.RESOLUTION_640x360);
                        mLogger.d("Adding: " + Constants.RESOLUTION_640x360);
                    }
                } else if (videoSpecification.getWidth() == 640 && videoSpecification.getHeight() == 480) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_640x480)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_640x480, VideoResolution.RESOLUTION_640x480);
                        mLogger.d("Adding: " + Constants.RESOLUTION_640x480);
                    }
                } else if (videoSpecification.getWidth() == 960 && videoSpecification.getHeight() == 720) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_960x720)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_960x720, VideoResolution.RESOLUTION_960x720);
                        mLogger.d("Adding: " + Constants.RESOLUTION_960x720);
                    }
                } else if (videoSpecification.getWidth() == 1280 && videoSpecification.getHeight() == 720) {
                    if (!supportedCameraCaptureResolutionMap.containsKey(Constants.RESOLUTION_1280x720)) {
                        supportedCameraCaptureResolutionMap.put(Constants.RESOLUTION_1280x720, VideoResolution.RESOLUTION_1280x720);
                        mLogger.d("Adding: " + Constants.RESOLUTION_1280x720);
                    }
                }
            }
        } else {
            mLogger.d("Video specifications null");
        }

        return supportedCameraCaptureResolutionMap;
    }

    /**
     * Make a video call
     *
     * @param number
     * @param uui
     */
    protected void dialVideo(String number, String uui) {
        Log.d("CLICK AV DIAL", "yess");
        dial(number, uui, true);
    }

    /**
     * Make a one-way video call
     *
     * @param number
     * @param uui
     */
    protected void dialOneWayVideo(String number, String uui) {
        dial(number, uui, true, true);
    }

    /**
     * Make an audio call
     *
     * @param number
     * @param uui
     */
    protected void dialAudio(String number, String uui) {
        dial(number, uui, false);
    }

    /**
     * Logout
     */
    protected void logout() {
        try {
            mLogger.d("Logout");

            Bundle extras = getIntent().getExtras();
            boolean secure = extras.getBoolean(Constants.DATA_KEY_SECURE);
            String sessionKey = extras.getString(Constants.DATA_KEY_SESSION_ID);
            String port = extras.getString(Constants.DATA_KEY_PORT);

            String address;

            if (secure) {
                address = Constants.SECURE_LOGOUT_URL.replace(Constants.SERVER_PLACEHOLDER, mServer);
            } else {
                address = Constants.REGULAR_LOGOUT_URL.replace(Constants.SERVER_PLACEHOLDER, mServer);
            }

            address = address.replace(Constants.PORT_PLACEHOLDER, port);
            address = address.replace(Constants.SESSION_ID_PLACEHOLDER, sessionKey);

            LoginHandlerImpl.getInstance().logout(address);

            Intent returning = new Intent();
            setResult(Constants.RESULT_LOGOUT, returning);
        } catch (Exception e) {
            mLogger.w("Exception in logout(): " + e.getMessage(), e);
            displayMessage("Logout exception: " + e.getMessage());
        }

        finish();
    }

    /**
     * Dial audio/video
     *
     * @param number
     * @param uui
     * @param enableVideo
     */
    private void dial(String number, String uui, boolean enableVideo) {
        dial(number, uui, enableVideo, false);
    }

    /**
     * Dial audio/video
     *
     * @param number
     * @param uui
     * @param enableVideo
     * @param mutedVideo
     */
    private void dial(String number, String uui, boolean enableVideo, boolean mutedVideo) {
        moveToCallActivity(number, uui, enableVideo, false, mutedVideo);
    }

    /**
     * Move to audio/video call activity
     *
     * @param numberToDial
     * @param uui
     * @param enableVideo
     * @param startMutedAudio
     * @param startMutedVideo
     */
    private void moveToCallActivity(String numberToDial, String uui, boolean enableVideo, boolean startMutedAudio,
                                    boolean startMutedVideo) {

        try {
            Intent intent = new Intent(this, mCallActivityClass);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(Constants.KEY_NUMBER_TO_DIAL, numberToDial);
            intent.putExtra(Constants.KEY_ENABLE_VIDEO, enableVideo);
            intent.putExtra(Constants.KEY_START_MUTED_AUDIO, startMutedAudio);
            intent.putExtra(Constants.KEY_START_MUTED_VIDEO, startMutedVideo);
            intent.putExtra(Constants.DATA_KEY_SERVER, mServer);
            intent.putExtra(Constants.KEY_CONTEXT, uui);
            intent.putExtra(Constants.KEY_PREFERRED_VIDEO_RESOLUTION, mVideoResolution);

//            AVCallActivityImpl.setPreferredVideoResolution(mVideoResolution);

//            startActivity(intent);
            startActivityForResult(intent,1);
        } catch (Exception e) {
            displayMessage("Move to call activity exception: " + e.getMessage());
        }
    }

    /**
     * Validate number
     *
     * @param number
     * @return isValid
     */
    protected boolean validateNumber(String number) {
        boolean isValid = true;

        if (number == null || number.trim().length() == 0) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate UUI
     * Allow alphanumeric characters only
     *
     * @param uui
     * @return isValid
     */
    protected boolean validateUui(String uui) {
        boolean isValid = true;
        /*
        if (uui != null && uui.trim().length() > 0) {
            if (!uui.matches(Constants.ALPHA_NUMERIC_REGEX)) {
                isValid = false;
            }
        }
        */

        return isValid;
    }

    /**
     * Set server
     *
     * @param server
     */
    protected void setServer(String server) {
        mServer = server;
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
            mLogger.e("Exception in displayMessage", e);
        }
    }
}
