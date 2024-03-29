package com.polar.mirror;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import androidx.camera.core.Preview;

/**
 * Implements logic of controlling low-light mode
 */
public class LowLightController {
    private final Preview mCameraPreview;
    private final Activity mActivity;
    private float lastBrightness = 1;
    public boolean isLowLightModeEnabled = false;
    private static final String TAG = "LowLightController";


    LowLightController(Activity activity, Preview cameraPreview){
        mCameraPreview = cameraPreview;
        mActivity = activity;
    }

    private void enableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        lastBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        mActivity.getWindow().setAttributes(layout);
    }

    private void disableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        Log.d(TAG, "Setting brightness " + lastBrightness);
        layout.screenBrightness = lastBrightness;
        mActivity.getWindow().setAttributes(layout);
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
