package com.polar.mirror;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.ByteBuffer;

/**
 * Controls freezing camera view
 */
public class FreezeController {
    private static final String TAG = "FreezeController";
    private final PreviewView mCameraView;
    private final ImageView mFreezeView;
    private final ImageCapture mImageCapture;
    private final Context mContext;
    private boolean mCameraFrozen = false;
    private final FloatingActionButton mFreezeButton;
    private int selectedCamera = CameraSelector.LENS_FACING_FRONT;

    FreezeController(Context context, FloatingActionButton freezeButton, PreviewView cameraView,
                     ImageView freezeView){
        mCameraView = cameraView;
        mFreezeView = freezeView;
        mContext = context;
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        mFreezeButton = freezeButton;
    }

    /**
     * Should be called when camera is ready
     * @param provider camera provider
     * @param lcOwner lifecycle owner used for binding camera use-cases
     */
    public void onCameraInitialized(@NonNull ProcessCameraProvider provider, LifecycleOwner lcOwner){
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(selectedCamera)
                .build();
        provider.bindToLifecycle(lcOwner, cameraSelector, mImageCapture);
        Log.d(TAG, "completed onCameraInitialized");
    }

    private int getRotationAngleFromOrientation(int orientation){
        int angle;
        boolean rearCamera = getSelectedCamera() == CameraSelector.LENS_FACING_BACK;
        switch (orientation) {
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = rearCamera ? 270 : 90;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                angle = rearCamera ? 90 : 270;
                break;
        }
        return angle;
    }

    private Bitmap processFreezeImage(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        // Rotate image
        int rotation = getRotationAngleFromOrientation(Utils.getOrientation(mContext));
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        );
        //Mirror image
        matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        );
        return bitmap;
    }


    private void setFrozenImage(){
        mImageCapture.takePicture(getMainExecutor(mContext),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    @SuppressLint("UnsafeOptInUsageError")
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy){
                        Log.i(TAG, "Capture success");
                        Image image = imageProxy.getImage();
                        if(image == null){
                            Log.e(TAG, "Image is null");
                            return;
                        }
                        int format = image.getFormat();
                        if(format != ImageFormat.JPEG){
                            Log.e(TAG, "Expected JPEG format, got format " + format);
                            return;
                        }
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = processFreezeImage(bytes);
                        mFreezeView.setImageBitmap(bitmap);
                        imageProxy.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Can not capture image");
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Toggles camera freeze
     */
    public void toggleFreeze(){
        if(mCameraFrozen){
            mFreezeView.setVisibility(View.GONE);
            mCameraView.setVisibility(View.VISIBLE);
            mFreezeButton.setImageResource(android.R.drawable.ic_media_pause);
            mCameraFrozen = false;
        } else {
            setFrozenImage();
            mCameraView.setVisibility(View.GONE);
            mFreezeView.setVisibility(View.VISIBLE);
            mFreezeButton.setImageResource(android.R.drawable.ic_media_play);
            mCameraFrozen = true;
        }
    }

    public void toggleSelectedCamera() {
        if (selectedCamera == CameraSelector.LENS_FACING_FRONT) {
            selectedCamera = CameraSelector.LENS_FACING_BACK;
        } else {
            selectedCamera = CameraSelector.LENS_FACING_FRONT;
        }
    }

    public int getSelectedCamera() {
        return selectedCamera;
    }

}
