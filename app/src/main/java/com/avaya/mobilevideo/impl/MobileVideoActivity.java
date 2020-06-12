/**
 * MobileVideoActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.impl;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.avaya.mobilevideo.MobileVideoApplication;
import com.avaya.mobilevideo.R;
import com.avaya.mobilevideo.utils.IssueReporter;
import com.avaya.mobilevideo.utils.Logger;

/**
 * Abstract class which the other abstract activity classes in the package extend
 *
 * @author Avaya Inc
 */
public abstract class MobileVideoActivity extends Activity {

    protected MobileVideoApplication mMobileVideoApplication;

    private static final String TAG = MobileVideoActivity.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    protected abstract void setReferencedClass();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.issue_report, menu);

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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMobileVideoApplication = (MobileVideoApplication) this.getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMobileVideoApplication.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReference();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReference();
        super.onDestroy();
    }

    private void clearReference() {
        Activity currentActivity = mMobileVideoApplication.getCurrentActivity();
        if (currentActivity != null && currentActivity.equals(this))
            mMobileVideoApplication.setCurrentActivity(null);
    }
}
