package com.avaya.mobilevideo.impl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.Constants;

public class ManualSessionActivity extends MobileVideoActivity {

    private static Class<?> mDialActivityClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_session);
    }

    @Override
    protected void setReferencedClass() {

    }

    public void proceed(View v) {
        moveToDialActivity();
    }

    private void moveToDialActivity() {
        if (mDialActivityClass != null) {
            Intent intent = new Intent(this, mDialActivityClass);

            String sessionToken = readSessionToken();

            intent.putExtra(Constants.DATA_SESSION_KEY, sessionToken);

            startActivityForResult(intent, Constants.RESULT_LOGOUT);
        }
    }

    private String readSessionToken() {
        EditText editText = (EditText) findViewById(R.id.manually_enter_session_token_value);

        return editText.getText().toString().trim();
    }

    public static void setDialActivityClass(Class<?> dialActivityClass) {
        mDialActivityClass = dialActivityClass;
    }
}
