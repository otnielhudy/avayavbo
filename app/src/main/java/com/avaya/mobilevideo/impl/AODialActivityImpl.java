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
import android.os.Bundle;
import android.widget.Toast;

import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.Logger;

/**
 * Abstract class which contains dial functionality
 *
 * @author Avaya Inc
 */
public abstract class AODialActivityImpl extends MobileVideoActivity {

    private static final String TAG = AODialActivityImpl.class.getSimpleName();

    private Logger mLogger = Logger.getLogger(TAG);

    private String mServer = "";
    private Class<? extends AOCallActivityImpl> mCallActivityClass;

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * @param callActivityClass
     */
    protected void setCallActivityClass(Class<? extends AOCallActivityImpl> callActivityClass) {
        mLogger.d("Set the call activity");
        mCallActivityClass = callActivityClass;
    }

    /**
     * Make an audio only call
     *
     * @param number
     * @param uui
     */
    protected void dialAudioOnly(String number, String uui) {
        dial(number, uui);
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
     * Dial audio only
     *
     * @param number
     * @param uui
     */
    private void dial(String number, String uui) {
        moveToAudioCallActivity(number, uui, false);
    }

    /**
     * Move to audio only call activity
     *
     * @param numberToDial
     * @param uui
     * @param startMutedAudio
     */
    private void moveToAudioCallActivity(String numberToDial, String uui, boolean startMutedAudio) {

        try {
            Intent intent = new Intent(this, mCallActivityClass);

            intent.putExtras(getIntent().getExtras());
            intent.putExtra(Constants.KEY_NUMBER_TO_DIAL, numberToDial);
            intent.putExtra(Constants.KEY_START_MUTED_AUDIO, startMutedAudio);
            intent.putExtra(Constants.DATA_KEY_SERVER, mServer);
            intent.putExtra(Constants.KEY_CONTEXT, uui);

            startActivity(intent);
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

        if (uui != null && uui.trim().length() > 0) {
            if (!uui.matches(Constants.ALPHA_NUMERIC_REGEX)) {
                isValid = false;
            }
        }

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
