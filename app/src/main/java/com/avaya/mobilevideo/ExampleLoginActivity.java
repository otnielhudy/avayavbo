/**
 * /** ExampleLoginActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.avaya.clientplatform.api.ClientPlatformFactory;
import com.avaya.mobilevideo.api.LoginActivity;
import com.avaya.mobilevideo.impl.LoginActivityImpl;
import com.avaya.mobilevideo.impl.ManualSessionActivity;
import com.avaya.mobilevideo.utils.Constants;
import com.avaya.mobilevideo.utils.GeneralDialogFragment;
import com.avaya.mobilevideo.utils.InternetConnectionDetector;

/**
 * Example login activity class, extends {@link LoginActivityImpl} and extends {@link LoginActivity}
 *
 * @author Avaya Inc
 */
public class ExampleLoginActivity extends LoginActivityImpl implements LoginActivity {

    private boolean isAudioDesired;
    public static String username;
    public static String server;
    public static String displayName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        Switch sw = (Switch) findViewById(R.id.login_media_type);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                TextView label = (TextView) findViewById(R.id.login_media_type_label);

                if (on) {
                    label.setText("Audio Only Call");
                } else {
                    label.setText("Video Call");
                }
            }

        });

        EditText tv = (EditText) findViewById(R.id.login_username_field);
    //    String username = username;

        tv = (EditText) findViewById(R.id.login_server_field);
    //    String server = tv.getText().toString().trim();

        tv = (EditText) findViewById(R.id.login_displayname_field);
    //    String displayName = tv.getText().toString().trim();

        isAudioDesired = sw.isChecked();
        setReferencedClass();

        login(isAudioDesired, username, displayName, server);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        TextView tv = (TextView) findViewById(R.id.txtViewVersion);

        tv.setText(getResources().getString(R.string.sdk_version)
                + ClientPlatformFactory.getClientPlatformInterface(this).getVersion() + "." + Constants.APP_VERSION);

        //Do connectivity test
        displayConnectivityInformation();
    }

    /**
     * Login
     */
    @Override
    public void login(View v) {

        EditText tv = (EditText) findViewById(R.id.login_username_field);
        String username = tv.getText().toString().trim();

        tv = (EditText) findViewById(R.id.login_server_field);
        String server = tv.getText().toString().trim();

        tv = (EditText) findViewById(R.id.login_displayname_field);
        String displayName = tv.getText().toString().trim();

        Switch sw = (Switch) findViewById(R.id.login_media_type);
        isAudioDesired = sw.isChecked();
        setReferencedClass();

        login(isAudioDesired, username, displayName, server);
    }

    protected void displayConnectivityInformation() {
        InternetConnectionDetector internetConnectionDetector = new InternetConnectionDetector(this);

        InternetConnectionDetector.InternetConnection internetConnection = internetConnectionDetector.getConnectionInformation();

        switch (internetConnection) {
            case WIFI:
                toastConnectionType(getResources().getString(R.string.wifi_connection));
                break;
            case MOBILE_DATA:
                toastConnectionType(getResources().getString(R.string.mobile_connection));
                break;
            case NO_CONNECTION:
                GeneralDialogFragment.displayMessage(this, getResources().getString(R.string.no_internet_connection_detected));
                break;
            default:
                GeneralDialogFragment.displayMessage(this, getResources().getString(R.string.no_internet_connection_detected));
                break;
        }
    }

    private void toastConnectionType(String connectionType) {
        String connectionTypeMessage = getResources().getString(R.string.internet_connection_type);
        connectionTypeMessage = connectionTypeMessage.replace(Constants.PLACEHOLDER1, connectionType);
        String message = getResources().getString(R.string.internet_connection_detected) + " " + connectionTypeMessage;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Need to reference the implementing dial activity class
     */
    @Override
    protected void setReferencedClass() {
        if (isAudioDesired) {
            setDialActivityClass(ExampleAODialActivity.class);
            ManualSessionActivity.setDialActivityClass(ExampleAODialActivity.class);
        } else {
            setDialActivityClass(ExampleAVDialActivity.class);
            ManualSessionActivity.setDialActivityClass(ExampleAVDialActivity.class);

        }
    }
}
