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
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.camera.core.Preview;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Implements low-light mode
 */
public class LowLightController {
    private final Activity mActivity;
    private float lastBrightness = 1;
    public boolean isLowLightModeEnabled = false;
    private final FloatingActionButton mLowLightModeButton;
    private final ImageView mLowLightOverlay;
    private static final String TAG = "LowLightController";
    private static final int WHITENING_VALUE = 128;


    LowLightController(Activity activity, FloatingActionButton lowLightModeButton,
                       ImageView lowLightOverlay){
        mActivity = activity;
        mLowLightModeButton = lowLightModeButton;
        mLowLightOverlay = lowLightOverlay;
    }

    private void enableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        lastBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_off);
        //Enable whitening overlay
        mLowLightOverlay.setVisibility(View.VISIBLE);
    }

    private void disableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        Log.d(TAG, "Setting brightness " + lastBrightness);
        layout.screenBrightness = lastBrightness;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_on);
        //Disable whitening overlay
        mLowLightOverlay.setVisibility(View.GONE);
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
