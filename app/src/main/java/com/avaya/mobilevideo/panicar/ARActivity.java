package com.avaya.mobilevideo.panicar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.avaya.mobilevideo.R;

public class ARActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_ar);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new PanicARFragment()).commit();
        }
    }

    protected void onResume()
    {
        super.onResume();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }
}

