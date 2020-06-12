package com.avaya.mobilevideo.panicar;

import com.avaya.mobilevideo.MobileVideoApplication;
import com.dopanic.panicarkit.lib.PARController;

public class PARPanicARKit extends MobileVideoApplication {
    public void onCreate()
    {
        super.onCreate();
        PARController.getInstance().init(MobileVideoApplication.getAppContext(), setApiKey());
    }

    public String setApiKey()
    {
        return "Override the setApiKey method in your PARApplication class!";
    }
}
