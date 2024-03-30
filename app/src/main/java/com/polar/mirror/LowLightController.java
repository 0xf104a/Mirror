package com.polar.mirror;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.WindowManager;

import androidx.camera.core.Preview;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Implements logic of controlling low-light mode
 */
public class LowLightController {
    private final Activity mActivity;
    private float lastBrightness = 1;
    public boolean isLowLightModeEnabled = false;
    private FloatingActionButton mLowLightModeButton;
    private static final String TAG = "LowLightController";
    private static final int WHITENING_VALUE = 128;


    LowLightController(Activity activity, FloatingActionButton lowLightModeButton){
        mActivity = activity;
        mLowLightModeButton = lowLightModeButton;
    }

    private void enableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        lastBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_off);
    }

    private void disableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        Log.d(TAG, "Setting brightness " + lastBrightness);
        layout.screenBrightness = lastBrightness;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_on);
    }

    /**
     * Toggles low-light mode
     * @return whether low-light mode is enabled
     */
    public boolean toggleLowLightMode(){
        if(isLowLightModeEnabled){
            disableLowLightMode();
        }else{
            enableLowLightMode();
        }
        isLowLightModeEnabled = !isLowLightModeEnabled;
        return isLowLightModeEnabled;
    }
}
