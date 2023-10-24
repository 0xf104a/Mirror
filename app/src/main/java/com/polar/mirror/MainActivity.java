package com.polar.mirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;
import androidx.camera.lifecycle.ProcessCameraProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView mCameraView;
    private boolean mCameraFrozen = false;
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();

        mCameraView = findViewById(R.id.preview_view);
        try {
            startCamera();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupView();
    }


    /**
     * Makes app fullscreen and applies other cosmetic options
     */
    private void setupView(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        } else {
            Log.e(TAG, "Can not hide action bar: got null from getSupportActionBar()");
        }
        setupFloatingButtons();
        hideSystemUi();
    }

    /**
     * Setups actions for floating buttons
     */
    private void setupFloatingButtons(){
        FloatingActionButton exitButton = findViewById(R.id.exit_button);
        FloatingActionButton freezeButton = findViewById(R.id.freeze_button);
        exitButton.setClickable(true);
        exitButton.setOnClickListener(this);
        freezeButton.setClickable(true);
        freezeButton.setOnClickListener(this);
    }

    /**
     * Hides SystemUI elements such as navigation and status bars
     */
    private void hideSystemUi(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Initializes camera stream
     * @throws ExecutionException in case of task errors
     * @throws InterruptedException in case of thread interruption
     */
    private void startCamera() throws ExecutionException, InterruptedException {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mCameraView.getSurfaceProvider());
        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
        try {
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles camera freeze mode
     */
    private void toggleCameraFreeze(){
        if(!mCameraFrozen) {
            mCameraView.setVisibility(View.GONE);
            mCameraFrozen = true;
        } else {
            mCameraView.setVisibility(View.VISIBLE);
            mCameraFrozen = false;
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        int viewId = v.getId();
        if(viewId == R.id.exit_button){
            Log.d(TAG, "Exit button pressed");
            super.finish();
        } else if(viewId == R.id.freeze_button) {
            toggleCameraFreeze();
        } else {
            Log.w(TAG, "Unknown id of view: " + viewId);
        }
    }
}

