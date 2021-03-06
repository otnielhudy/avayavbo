/**
 * AVDialActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.api;

import android.view.View;

/**
 * Interface that your audio only dial activity should implement
 * @author Avaya Inc
 */
public interface AODialActivity {

    /**
     * Make audio only call
     */
    void dialAudioOnly(View v);

    /**
     * Logout
     */
    void logout(View v);
}
