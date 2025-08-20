package io.f104a.mirror.controllers;

import android.annotation.SuppressLint;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.MirrorMode;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import java.util.concurrent.ExecutionException;

import io.f104a.mirror.MainActivity;

/**
 * Controls everything associated with camera streaming.
 * Acts on behalf of MainActivity.
 */
public class CameraStreamController {
    private final static String TAG = "CameraStreamController";
    private ProcessCameraProvider mCameraProvider;
    private final PreviewView mCameraView;
    private Preview mPreview;
    private final MainActivity mMainActivity;
    private int currentCamera = CameraSelector.LENS_FACING_BACK;

    public CameraStreamController(
            ProcessCameraProvider cameraProvider,
            PreviewView cameraView,
            Preview preview,
            MainActivity activity
    ){
        mCameraProvider = cameraProvider;
        mCameraView = cameraView;
        mPreview = preview;
        mMainActivity = activity;
    }

    /**
     * Initializes camera stream
     * @throws ExecutionException in case of task errors
     * @throws InterruptedException in case of thread interruption
     */
    @SuppressLint("RestrictedApi")
    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalMirrorMode.class)
    public void startCamera() throws ExecutionException, InterruptedException {
        mCameraView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        mCameraView.addOnLayoutChangeListener(this::onLayoutChange);
        mPreview = new Preview.Builder()
                .setMirrorMode(MirrorMode.MIRROR_MODE_ON)
                .build();
        mPreview.setSurfaceProvider(mCameraView.getSurfaceProvider());
        mCameraProvider = ProcessCameraProvider.getInstance(mMainActivity).get();
        try {
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .build();
            mCameraProvider.unbindAll();
            mCameraProvider.bindToLifecycle(mMainActivity, cameraSelector, mPreview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when device is rotated(or resized which is more rare case)
     */
    private void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        int orientation = mCameraView.getDisplay().getRotation();
        if (currentCamera == CameraSelector.LENS_FACING_BACK) {
            int o;
            switch (orientation) {
                case Surface.ROTATION_0:
                    o = Surface.ROTATION_0;
                    break;
                case Surface.ROTATION_90:
                    o = Surface.ROTATION_270;
                    break;
                case Surface.ROTATION_180:
                    o = Surface.ROTATION_180;
                    break;
                default: // Surface.ROTATION_270:
                    o = Surface.ROTATION_90;
                    break;
            }
            mPreview.setTargetRotation(o);
        } else {
            mPreview.setTargetRotation(orientation);
        }
    }
}
