/**
 * LoginActivityImpl.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.avaya.clientplatform.api.ClientPlatform;
import com.avaya.mobilevideo.MobileVideoApplication;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.api.LoginHandler;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.IssueReporter;
import com.avaya.mobilevideo.utils.Logger;
import com.avaya.mobilevideo.utils.wrapperClicktoCall;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class which contains login functionality
 *
 * @author Avaya Inc
 */
public abstract class LoginActivityImpl extends MobileVideoActivity {

    private Class<?> mDialActivityClass;

    private static final String TAG = LoginActivityImpl.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private String mServerAddress;

    private String mServer = "";

    private LoginTask mAuthTask;
    private boolean mSecureLogin;
    private String mPort;
    private boolean mMediaType;
    private boolean mTrustALlCerts;
    public static String port;
    public static Boolean secureLogin;

    public static Context sContext;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        setReferencedClass();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.report_issue_item) {
            mLogger.i("Report issue");
            IssueReporter issueReporter = new IssueReporter(this, MobileVideoApplication.getSdkVersion());
            issueReporter.reportIssue();
            return true;
        } else if (id == R.id.settings_item) {
            Intent intent = new Intent(getApplicationContext(), com.avaya.mobilevideo.utils.SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.debug_mode_item) {
            moveToDebugModeActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sContext = getApplicationContext();
        checkPermissions();
    }

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) +
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) +
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                mLogger.d("Permissions Granted");
            } else {
                mLogger.d("Permissions Not Granted");
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},1);
            }
        } else {
            mLogger.d("Permissions Granted");
        }
    }

    private void configureLogger() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean logToDisk = sharedPref.getBoolean(Constants.PREFERENCE_LOG_TO_DEVICE, true);
        String logLevel = sharedPref.getString(Constants.PREFERENCE_LOG_LEVEL, "");
        String logFileName = sharedPref.getString(Constants.PREFERENCE_LOG_FILE_NAME, "");
        String maxFileSize = sharedPref.getString(Constants.PREFERENCE_MAX_FILE_SIZE, "0");
        String maxBackUps = sharedPref.getString(Constants.PREFERENCE_MAX_BACK_UPS, "0");

        Logger.configure(logToDisk, logLevel, logFileName, maxFileSize, maxBackUps);
    }

    private void readSecurityPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mSecureLogin = secureLogin;
        mPort = port;
        mTrustALlCerts = true;

        mLogger.d("Security preferences, secure login: " + mSecureLogin);
        mLogger.d("Security preferences, port: " + mPort);
        mLogger.d("Security preferences, trust all certs: " + mTrustALlCerts);
    }

    /**
     * @param dialActivityClass
     */
    protected void setDialActivityClass(Class<?> dialActivityClass) {
        mLogger.d("Set the dial activity");
        mDialActivityClass = dialActivityClass;
    }

    /**
     * Login
     *
     * @param username
     * @param server
     */
    @SuppressWarnings("unchecked")
    public void login(boolean isVideoDesired, String username, String displayName, String server) {
        mLogger.d("Login");

        configureLogger();

        readSecurityPreferences();

        if (validateInput(username, server)) {
            this.mServer = server;

            if (mSecureLogin) {
                mServerAddress = Constants.SECURE_LOGIN_URL.replace(Constants.SERVER_PLACEHOLDER, server);
            } else {
                mServerAddress = Constants.REGULAR_LOGIN_URL.replace(Constants.SERVER_PLACEHOLDER, server);
            }

            mServerAddress = mServerAddress.replace(Constants.PORT_PLACEHOLDER, mPort);

            mMediaType = isVideoDesired;

            Map<String, String> params = new HashMap<String, String>();

            if (displayName == null) {
                displayName = "";
            }
            params.put("displayName", displayName);
            params.put("userName", username);

            mAuthTask = new LoginTask();

            mAuthTask.execute(params);
        }
    }

    /**
     * Move to dial activity
     *
     * @param loginData
     */
    private void moveToDialActivity(Bundle loginData) {
        Intent intent = new Intent(this, mDialActivityClass);
        intent.putExtras(loginData);
        intent.putExtra(Constants.DATA_KEY_SERVER, mServer);
        intent.putExtra(Constants.DATA_KEY_SECURE, mSecureLogin);
        intent.putExtra(Constants.DATA_KEY_PORT, mPort);
        intent.putExtra(Constants.DATA_KEY_MEDIA_TYPE, mMediaType);

        startActivityForResult(intent, Constants.RESULT_LOGOUT);
    }

    /**
     * Validate input
     *
     * @param username
     * @param server
     * @return isValid
     */
    private boolean validateInput(String username, String server) {
        boolean isValid = false;

        if (validateStringInput(username, getResources().getString(R.string.username)) && validateStringInput(server, getResources().getString(R.string.server))) {
            isValid = true;
        }

        return isValid;
    }

    /**
     * Validate string
     *
     * @param value
     * @param valueName
     * @return isValid
     */
    private boolean validateStringInput(String value, String valueName) {
        boolean isValid = true;

        if (value == null || value.trim().length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.specify) + " " + valueName, Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Login task
     */
    private class LoginTask extends AsyncTask<Map<String, String>, Void, Bundle> {

        @Override
        protected Bundle doInBackground(final Map<String, String>... params) {
            mLogger.d("LoginTask doInBackground");

            StringBuilder stringBuilder = new StringBuilder(mServerAddress);

            Map<String, String> param = params[0];
            Set<String> keySet = param.keySet();

            if (keySet.size() > 0) {
                stringBuilder.append("?");

                for (String key : keySet) {
                    String value = param.get(key);
                    stringBuilder.append(key).append("=").append(URLEncoder.encode(value)).append("&");
                }

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }

            return LoginHandlerImpl.getInstance().login(stringBuilder.toString(), null, mTrustALlCerts, false);
        }

        @Override
        protected void onPostExecute(final Bundle loginData) {
            mLogger.d("LoginTask onPostExecute");

            mAuthTask = null;
            final Integer loginResponse = loginData.getInt(Constants.DATA_KEY_ERROR);

            if (loginResponse == null || loginResponse == 0) {
                mLogger.i("Login success");

                moveToDialActivity(loginData);

            } else {
                String detailedMessage = null;

                int responseCode = loginData.getInt(Constants.DATA_KEY_RESPONSE_CODE);
                String responseMessage = loginData.getString(Constants.DATA_KEY_RESPONSE_MESSAGE);
                String exceptionMessage = loginData.getString(Constants.DATA_KEY_EXCEPTION_MESSAGE);
                if (responseMessage == null) {
                    responseMessage = "";
                }

                if (responseCode > 0) {
                    detailedMessage = responseCode + ":" + responseMessage;
                } else if (exceptionMessage != null && !exceptionMessage.trim().equals("")) { // add exception message if there is one
                    detailedMessage = exceptionMessage;
                }

                switch (loginResponse) {
                    case LoginHandler.ERROR_CONNECTION_FAILED:
                        if (detailedMessage != null) {
                            GeneralDialogFragment.displayMessage(LoginActivityImpl.this, detailedMessage, getResources().getString(R.string.failed_to_connect));
                        } else {
                            GeneralDialogFragment.displayMessage(LoginActivityImpl.this, getResources().getString(R.string.failed_to_connect));
                        }
                        //handle failed to connect (avaya downtime)
                        dismissCTC();
                        break;

                    case LoginHandler.ERROR_LOGIN_FAILED:
                        if (detailedMessage != null) {
                            GeneralDialogFragment.displayMessage(LoginActivityImpl.this, detailedMessage, getResources().getString(R.string.failed_to_login));
                        } else {
                            GeneralDialogFragment.displayMessage(LoginActivityImpl.this, getResources().getString(R.string.failed_to_login));
                        }
                        // handle bad gateway (avaya down)
                        dismissCTC();
                        // end
                        break;

                    default:
                        GeneralDialogFragment.displayMessage(LoginActivityImpl.this, R.string.unknown_failure);
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Constants.RESULT_LOGOUT == requestCode) {
            ClientPlatform clientPlatform = ClientPlatformManager.getClientPlatform(getApplicationContext());
            clientPlatform.getUser().terminate();
        }
        finish();
    }

    private void moveToDebugModeActivity() {
        Intent intent = new Intent(this, ManualSessionActivity.class);

        startActivity(intent);
    }

    private void dismissCTC(){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("code", "502");
            map.put("description", "bad gateway");
            List<Map> listMap = new ArrayList<>();
            listMap.add(map);
            wrapperClicktoCall.goBack(listMap);
        }catch(Exception e){
            e.getMessage();

        }finally {
            finish();
        }

    }
}
