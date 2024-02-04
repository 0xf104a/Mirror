package com.polar.mirror;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

class ActionPanelControllerData implements Parcelable{
    public Boolean isPanelVisible;
    public Boolean isFirstTimeHide;

    public static final String PARCELABLE_NAME = "ActionPanelControllerData";

    public ActionPanelControllerData(boolean _isPanelVisible, boolean _isFirstTimeHide){
        isPanelVisible = _isPanelVisible;
        isFirstTimeHide = _isFirstTimeHide;
    }

    protected ActionPanelControllerData(Parcel in) {
        isPanelVisible = in.readInt() != 0;
        isFirstTimeHide = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // We use int here to keep our code compatiable with API level < 29
        if(isPanelVisible){
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        if(isFirstTimeHide){
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActionPanelControllerData> CREATOR =
            new Creator<ActionPanelControllerData>() {
        @Override
        public ActionPanelControllerData createFromParcel(Parcel in) {
            return new ActionPanelControllerData(in);
        }

        @Override
        public ActionPanelControllerData[] newArray(int size) {
            return new ActionPanelControllerData[size];
        }
    };
}

/**
 * Controls panel with floating action buttons
 */
public class ActionPanelController implements View.OnClickListener {
    private final static String TAG = "ActionPanelController";
    private final View mPanelView;
    private final View mOverlayView;
    private final Animation mSlideDownAnimation;
    private final Animation mSlideUpAnimation;
    private boolean mPanelVisible = true;
    private Handler mHideHandler;
    private Runnable mHideRunnable;
    private final int hideMs;
    private boolean isFirstTimeHide = true;
    private final Context mContext;
    public ActionPanelController(Context context, View panelView, View overlayView){
        mPanelView = panelView;
        mOverlayView = overlayView;
        mSlideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        mSlideUpAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        hideMs = context.getResources().getInteger(R.integer.autohide_action_panel_ms);
        if(hideMs < 0){
            throw new RuntimeException("Bad configuration: negative hideMs");
        }
        mContext = context;
        setupAnimations();
        setupAutoHide();
    }

    private void setupAutoHide(){
        mHideHandler = new Handler();
        mHideRunnable = this::hidePanel;
        scheduleHide();
    }

    private void scheduleHide(){
        mHideHandler.postDelayed(mHideRunnable, hideMs);
    }

    private void cancelHide(){
        mHideHandler.removeCallbacksAndMessages(null);
    }

    private void setupAnimations(){
        mSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                /*stub*/
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPanelView.setVisibility(View.GONE);
                mOverlayView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                /*stub*/
            }
        });
        mSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mPanelView.setVisibility(View.VISIBLE);
                mOverlayView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*stub*/
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                /*stub*/
            }
        });
    }

    private void hidePanel(){
        if(isFirstTimeHide){
            final String toastText = mContext.getString(R.string.tap_to_show_actions);
            Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
            isFirstTimeHide = false;
        }
        mPanelView.startAnimation(mSlideDownAnimation);
        mOverlayView.startAnimation(mSlideDownAnimation);
        mPanelVisible = false;
        mHideHandler.removeCallbacks(mHideRunnable);
    }

    private void showPanel(){
        mPanelView.startAnimation(mSlideUpAnimation);
        mOverlayView.startAnimation(mSlideUpAnimation);
        mPanelVisible = true;
        scheduleHide();
    }

    private void togglePanelVisibility(){
        if(mPanelVisible){
            hidePanel();
        } else {
            showPanel();
        }
    }
    @Override
    public void onClick(@NonNull View v) {
        final int viewId = v.getId();
        if(viewId == R.id.preview_view || viewId == R.id.stop_view){
            togglePanelVisibility();
        }
    }

    /**
     * Hides or shows panel immediately
     * @param isVisible whether panel should be visible
     */
    private void setPanelVisible(boolean isVisible){
        Log.d(TAG, "setting visibility to " + isVisible);
        mPanelVisible = isVisible;
        if(!isVisible){
            hidePanel();
            cancelHide(); //Cancel timer, so we would not show useless toasts
        } else {
            mPanelView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
        }
    }


    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ActionPanelControllerData.PARCELABLE_NAME,
                new ActionPanelControllerData(mPanelVisible, isFirstTimeHide));
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ActionPanelControllerData controllerData =
                savedInstanceState.getParcelable(ActionPanelControllerData.PARCELABLE_NAME);
        if(controllerData == null){
            Log.w(TAG, "activityData is null, ignoring restoring instance state");
            return;
        }
        isFirstTimeHide = controllerData.isFirstTimeHide;
        setPanelVisible(controllerData.isPanelVisible);
    }
}
