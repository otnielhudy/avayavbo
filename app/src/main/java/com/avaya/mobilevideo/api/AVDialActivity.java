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
 * Interface that your audio/video dial activity should implement
 * @author Avaya Inc
 */
public interface AVDialActivity {

    /**
     * Make video call
     */
    void dialVideo(View v);

    /**
     * Make one-way video call, app receives video but doesn't broadcast it
     */
    void dialOneWayVideo(View v);

    /**
     * Make audio call, where video can be added or removed
     */
    void dialAudio(View v);

    /**
     * Logout
     */
    void logout(View v);
}
