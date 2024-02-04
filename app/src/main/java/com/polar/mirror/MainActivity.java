package com.polar.mirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;

class MainActivityData implements Parcelable{
    public Boolean isPanelVisible = null;
    public static final String PARCELABLE_NAME = "MainActivityData";

    public MainActivityData(boolean _isPanelVisible){
        isPanelVisible = _isPanelVisible;
    }

    protected MainActivityData(Parcel in) {
        isPanelVisible = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // We use int here to keep our code compatiable with API level < 29
        if(isPanelVisible){
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MainActivityData> CREATOR = new Creator<MainActivityData>() {
        @Override
        public MainActivityData createFromParcel(Parcel in) {
            return new MainActivityData(in);
        }

        @Override
        public MainActivityData[] newArray(int size) {
            return new MainActivityData[size];
        }
    };
}

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView mCameraView;
    private FreezeController mFreezeController;
    private ActionPanelController mActionPanelController;
    private final static String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 858;

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MainActivityData.PARCELABLE_NAME,
                new MainActivityData(mActionPanelController.isPanelVisible()));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MainActivityData activityData =
                savedInstanceState.getParcelable(MainActivityData.PARCELABLE_NAME);
        if(activityData == null){
            Log.w(TAG, "activityData is null, ignoring restoring instance state");
            return;
        }
        mActionPanelController.setPanelVisible(activityData.isPanelVisible);
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

