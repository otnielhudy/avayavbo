package com.avaya.mobilevideo.panicar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.avaya.mobilevideo.R;

public class MainActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startARAutoOrienting(View view)
    {
        PanicARFragment.arLocations = "[{\"title\":\"ATM Alfamart Arjuna Selatan 0.6 mi\",\"desc\":\"Jl. Arjuna Selatan, Kebon Jeruk\",\"lat\":\"-6.189547\",\"lon\":\"106.771965\"},{\"title\":\"Branch Alfamart Kemanggisan 2 - CV. Gorlen 0.8 mi\",\"desc\":\"Jl. Kemanggisan Raya No. 60 RT. 005. RW.010\",\"lat\":\"-6.2029\",\"lon\":\"106.78783\"}]";
        Intent intent = new Intent(this, ARActivity.class);
        startActivity(intent);
    }
}
