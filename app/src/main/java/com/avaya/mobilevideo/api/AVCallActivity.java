/**
 * AVCallActivity.java <br>
 * Copyright 2014-2015 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.mobilevideo.api;

import android.view.View;

/**
 * Interface that your audio/video call activity should implement
 * @author Avaya Inc
 */
public interface AVCallActivity {

    /**
     * End call
     * @param V
     */
    void endCall(View V);

    /**
     * Toggle mute audio
     * @param v
     */
    void toggleMuteAudio(View v);

    /**
     * Toggle enable audio
     * @param v
     */
    void toggleEnableAudio(View v);

    /**
     * Toggle mute video
     * @param v
     */
    void toggleMuteVideo(View v);

    /**
     * Switch video between front and back camera
     * @param v
     */
    void switchVideo(View v);

    /**
     * Toggle enable video
     * @param v
     */
    void toggleEnableVideo(View v);
}
