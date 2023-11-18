package com.polar.mirror;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";

    /**
     * @param context context for getting WindowManager
     * @return orientation state as in Surface class
     */
    public static int getOrientation(Context context){
        int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "Rotation: " + rotation);
        return rotation;
    }
}
