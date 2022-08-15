package com.remiverchere.mylipstick;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class VideoLiveVersion extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private static final int CAMERA_REQUEST = 100;
    private MyOverlayView drawingOverlay;
    private FrameAnalyser frameAnalyser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_live_version);
        previewView = findViewById(R.id.viewFinder);
        drawingOverlay = findViewById( R.id.camera_drawing_overlay );
        drawingOverlay.setWillNotDraw(false);
        drawingOverlay.setZOrderOnTop(true);

        frameAnalyser = new FrameAnalyser( drawingOverlay );




        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            }catch (ExecutionException | InterruptedException e) {
                Log.e("error",e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        DisplayMetrics displayMetrics =getResources().getDisplayMetrics();
        Size screenSize = new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);

        frameAnalyser.setDrawingInitialSize(displayMetrics.widthPixels,displayMetrics.heightPixels);




        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution( screenSize )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer( Executors.newSingleThreadExecutor() , frameAnalyser );



        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,imageAnalysis, preview);



    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "CameraX permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "CameraX permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}