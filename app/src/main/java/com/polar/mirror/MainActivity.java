package com.polar.mirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.MirrorMode;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView mCameraView;
    private FreezeController mFreezeController;
    private LowLightController mLowLightController;
    private ActionPanelController mActionPanelController;
    private final static String TAG = "MainActivity";
    private Preview mPreview = null;
    private static final int CAMERA_PERMISSION_CODE = 858;
    ProcessCameraProvider cameraProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestCameraPermissionIfNeeded();

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

        //Get low-light FAB
        FloatingActionButton lowLightModeButton = findViewById(R.id.low_light_button);

        //Get low-light overlay
        ImageView lowLightOverlay = findViewById(R.id.low_light_overlay_view);

        //Start camera
        try {
            startCamera();
            mLowLightController = new LowLightController(this, lowLightModeButton,
                    lowLightOverlay);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mActionPanelController.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mActionPanelController.onRestoreInstanceState(savedInstanceState);
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
        FloatingActionButton lowLightButton = findViewById(R.id.low_light_button);
        FloatingActionButton cameraChooserButton = findViewById(R.id.camera_chooser_button);
        exitButton.setClickable(true);
        exitButton.setOnClickListener(this);
        freezeButton.setClickable(true);
        freezeButton.setOnClickListener(this);
        lowLightButton.setClickable(true);
        lowLightButton.setOnClickListener(this);
        cameraChooserButton.setClickable(true);
        cameraChooserButton.setOnClickListener(this);
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
    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalMirrorMode.class)
    private void startCamera() throws ExecutionException, InterruptedException {
        mPreview = new Preview.Builder()
                .setMirrorMode(MirrorMode.MIRROR_MODE_ON)
                .build();
        mPreview.setSurfaceProvider(mCameraView.getSurfaceProvider());
        cameraProvider = ProcessCameraProvider.getInstance(this).get();
        try {
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(mFreezeController.getSelectedCamera())
                    .build();
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, mPreview);
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

    private void toggleLowLightMode(){
        if(mLowLightController == null){
            Log.wtf(TAG, "Low-light mode controller is null");
            return;
        }
        mLowLightController.toggleLowLightMode();
    }

    /**
     * Toggles front/rear camera
     */
    private void toggleSelectedCamera(){
        mFreezeController.toggleSelectedCamera();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(mFreezeController.getSelectedCamera())
                .build();
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, mPreview);
        mFreezeController.onCameraInitialized(cameraProvider, this);
    }

    @Override
    public void onClick(@NonNull View v) {
        int viewId = v.getId();
        if(viewId == R.id.exit_button){
            Log.d(TAG, "Exit button pressed");
            super.finish();
        } else if(viewId == R.id.freeze_button) {
            toggleCameraFreeze();
        } else if(viewId == R.id.low_light_button){
            toggleLowLightMode();
        } else if(viewId == R.id.camera_chooser_button){
            toggleSelectedCamera();
        } else {
            Log.w(TAG, "Unknown id of view: " + viewId);
        }
    }

    public void requestCameraPermissionIfNeeded() {
        if(Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission is granted.");
                return;
            }
            Log.d(TAG, "Camera permission is not granted yet, so will request it now");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!(grantResults.length > 0) || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                final String toastText = getString(R.string.no_camera_permissions);
                Log.d(TAG, "User denied camera permission");
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
            }
        }
    }
}

