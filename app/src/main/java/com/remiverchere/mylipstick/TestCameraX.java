package com.remiverchere.mylipstick;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TestCameraX extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private Button buttonPhoto;
    private static final int CAMERA_REQUEST = 100;
    private ImageCapture imageCapture;

    private ImageView renderImg;


    List<PointF> upperLipBottomContour;
    List<PointF> upperLipTopContour;
    List<PointF> lowerLipBottomContour;
    List<PointF> lowerLipTopContour;

    private boolean validPhoto = false;

    private LinearLayout layoutSelection;
    Bitmap refBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera_x);
        previewView = findViewById(R.id.viewFinder);
        buttonPhoto = findViewById(R.id.image_capture_button);
        renderImg = findViewById(R.id.img_render);
        Button affetRed = findViewById(R.id.affect_red);
        Button affetGreen = findViewById(R.id.affect_green);
        Button affetBlue = findViewById(R.id.affect_blue);
        Button resetChoice = findViewById(R.id.reset_style);
        layoutSelection = findViewById(R.id.layout_selection);
        switchMode(1);
        FaceDetectorOptions faceDetectionOptions =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        affetRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validPhoto){
                    affectColor(255,0,0);
                }

            }
        });
        affetGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validPhoto){
                    affectColor(0,255,0);
                }
            }
        });
        affetBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validPhoto){
                    affectColor(0,0,255);
                }

            }
        });
        resetChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validPhoto){
                    renderImg.setImageBitmap(refBitmap);
                }

            }
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
                buttonPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Executor takePhotoExe = Executors.newSingleThreadExecutor();
                        imageCapture.takePicture(takePhotoExe,
                                new ImageCapture.OnImageCapturedCallback() {
                                    @Override
                                    @androidx.camera.core.ExperimentalGetImage
                                    public void onCaptureSuccess(ImageProxy imageProxy) {
                                        // insert your code here.
                                        Image mediaImage = imageProxy.getImage();

                                        if (mediaImage != null) {
                                            int rotationDegree  =imageProxy.getImageInfo().getRotationDegrees();
                                            refBitmap = toBitmap(mediaImage);
                                            imageProxy.close();
                                            if(refBitmap != null){
                                                FaceDetectorOptions faceDetectionOptions =
                                                        new FaceDetectorOptions.Builder()
                                                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                                                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                                                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                                                .setMinFaceSize(0.15f)
                                                                .enableTracking()
                                                                .build();
                                                checkImg(refBitmap,rotationDegree,faceDetectionOptions);
                                                Handler mainHandler = new Handler(TestCameraX.this.getMainLooper());

                                                Runnable moveToLipsChoice = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        renderImg.setImageBitmap(refBitmap);
                                                        switchMode(2);
                                                    }
                                                };
                                                mainHandler.post(moveToLipsChoice);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onError(ImageCaptureException error) {
                                        // insert your code here.
                                    }
                                }
                        );
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
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

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,imageCapture, preview);

    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length == 1 && image.getFormat() == 256) {
            try {
                ByteBuffer planeBuff = planes[0].getBuffer();
                byte[] bytes = new byte[planeBuff.remaining()];
                planeBuff.get(bytes);
                return BitmapFactory.decodeByteArray(bytes, 0,bytes.length, null);

            } catch (Exception ex) {
                Log.e("error",ex.toString());
                return null;
            }

        }
        return null;
    }

    private void switchMode(int mode){
        if (mode == 1) {
            // mode photo
            renderImg.setVisibility(View.INVISIBLE);
            layoutSelection.setVisibility(View.INVISIBLE);

            previewView.setVisibility(View.VISIBLE);
            buttonPhoto.setVisibility(View.VISIBLE);

        } else {
            // mode choix couleur
            renderImg.setVisibility(View.VISIBLE);
            layoutSelection.setVisibility(View.VISIBLE);

            previewView.setVisibility(View.INVISIBLE);
            buttonPhoto.setVisibility(View.INVISIBLE);
        }


    }

    protected void checkImg(Bitmap mediaImage, int imgRotation,FaceDetectorOptions faceDetectionOptions) {
        if (mediaImage != null) {
            InputImage image = InputImage.fromBitmap(mediaImage, imgRotation);
            FaceDetector detector = FaceDetection.getClient(faceDetectionOptions);
            Task<List<Face>> result =
                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            // Task completed successfully
                                            // ...
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                                                if (mouthBottom != null) {
                                                    PointF mouthBottomPos = mouthBottom.getPosition();
                                                    // Toast.makeText(TestCameraX.this, "mouthBottom : " + mouthBottomPos.toString(), Toast.LENGTH_SHORT).show();
                                                }

                                                upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                upperLipTopContour = face.getContour(FaceContour.UPPER_LIP_TOP).getPoints();

                                                lowerLipBottomContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM).getPoints();
                                                lowerLipTopContour = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();

                                                validPhoto = true;

                                            }


                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception

                                        }
                                    });

        }
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

    protected  void affectColor(int red, int green, int blue) {
        Bitmap bmpCanvas = refBitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas modifYLips= new Canvas(bmpCanvas);
        Paint myPaint = new Paint();
        myPaint.setARGB(255, red,green,blue);
        myPaint.setStyle(Paint.Style.FILL);




        if (upperLipBottomContour.size() > 0) {
            Path upperLips = new Path();
            upperLips.moveTo(upperLipBottomContour.get(0).x, upperLipBottomContour.get(0).y);
            // levre superieur bas
            for (int i = 0; i < upperLipBottomContour.size(); i++) {
                upperLips.lineTo(upperLipBottomContour.get(i).x, upperLipBottomContour.get(i).y);
            }

            // levre superieur haut
            for (int i = 0; i < upperLipTopContour.size(); i++) {
                upperLips.lineTo(upperLipTopContour.get(i).x, upperLipTopContour.get(i).y);
            }
            upperLips.lineTo(upperLipBottomContour.get(0).x, upperLipBottomContour.get(0).y);
            modifYLips.drawPath(upperLips, myPaint);
        }


        if(lowerLipBottomContour.size() > 0) {
            Path lowerLips = new Path();
            lowerLips.moveTo(lowerLipBottomContour.get(0).x, lowerLipBottomContour.get(0).y);
            // levre inferieur bas
            for (int i = 0; i < lowerLipBottomContour.size(); i++) {
                lowerLips.lineTo(lowerLipBottomContour.get(i).x, lowerLipBottomContour.get(i).y);
            }

            // levre inferieur haut
            for (int i = 0; i < lowerLipTopContour.size() - 1; i++) {
                lowerLips.lineTo(lowerLipTopContour.get(i).x, lowerLipTopContour.get(i).y);
            }
            lowerLips.lineTo(lowerLipBottomContour.get(0).x, lowerLipBottomContour.get(0).y);
            modifYLips.drawPath(lowerLips, myPaint);
        }





        renderImg.setImageBitmap(bmpCanvas);

    }
}