package com.polar.mirror;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;


/**
 * Controls panel with floating action buttons
 */
public class ActionPanelController implements View.OnClickListener {
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
}
