package com.polar.mirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView mCameraView;
    private FreezeController mFreezeController;
    private ActionPanelController mActionPanelController;
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View panelView = findViewById(R.id.action_panel_layout);
        View overlayView = findViewById(R.id.overlay_view);
        mActionPanelController = new ActionPanelController(this, panelView, overlayView);
        mCameraView = findViewById(R.id.preview_view);

        setupView();

        //Initialize freeze controller
        FloatingActionButton freezeButton = findViewById(R.id.freeze_button);
        ImageView freezeView = findViewById(R.id.stop_view);
        mFreezeController = new FreezeController(this, freezeButton, mCameraView,
                freezeView);

        //Start camera
        try {
            startCamera();
        } catch (ExecutionException | InterruptedException e) {
            final String toastText = getString(R.string.can_not_start_camera);
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
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
        setupPanel();
    }

    /**
     * Setups logic of action panel in a general
     */
    private void setupPanel(){
        //Setup action panel
        View stopView = findViewById(R.id.stop_view);
        mCameraView.setClickable(true);
        mCameraView.setOnClickListener(mActionPanelController);
        stopView.setClickable(true);
        stopView.setOnClickListener(mActionPanelController);
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
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            mFreezeController.onCameraInitialized(cameraProvider, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles camera freeze mode
     */
    private void toggleCameraFreeze(){
        mFreezeController.toggleFreeze();
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

