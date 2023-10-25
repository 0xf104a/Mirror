package com.polar.mirror;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Controls freezing camera view
 */
public class FreezeController {
    private static final String TAG = "FreezeController";
    private final FloatingActionButton mFreezeButton;
    private final PreviewView mCameraView;
    private final ImageView mFreezeView;
    private final ImageCapture mImageCapture;
    private final Context mContext;
    private boolean mCameraFrozen = false;

    FreezeController(Context context, FloatingActionButton freezeButton, PreviewView cameraView,
                     ImageView freezeView){
        mFreezeButton = freezeButton;
        mCameraView = cameraView;
        mFreezeView = freezeView;
        mContext = context;
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
    }

    /**
     * Should be called when camera is ready
     * @param provider camera provider
     * @param lcOwner lifecycle owner used for binding camera usecases
     */
    public void onCameraInitialized(ProcessCameraProvider provider, LifecycleOwner lcOwner){
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();
        provider.bindToLifecycle(lcOwner, cameraSelector, mImageCapture);
        Log.d(TAG, "completed onCameraInitialized");
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
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(270);
                        bitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
                        );
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
            mCameraFrozen = false;
        } else {
            setFrozenImage();
            mCameraView.setVisibility(View.GONE);
            mFreezeView.setVisibility(View.VISIBLE);
            mCameraFrozen = true;
        }
    }
}
