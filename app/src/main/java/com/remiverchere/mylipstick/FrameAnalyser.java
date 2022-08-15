package com.remiverchere.mylipstick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Objects;

public class FrameAnalyser implements ImageAnalysis.Analyzer{

    public Image frameMediaImage;
    public Bitmap refBitmap;

    FaceDetectorOptions faceDetectionOptions =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .enableTracking()
                    .build();

    private MyOverlayView myDrawing;

    public FrameAnalyser(MyOverlayView drawingOverlay) {
        myDrawing = drawingOverlay;
    }

    public void setDrawingInitialSize(int initialWidth, int initialHeight) {
        myDrawing.analyseHeight = initialHeight;
        myDrawing.analyseWidth = initialWidth;
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        frameMediaImage = imageProxy.getImage();
        if (frameMediaImage != null) {

            InputImage image = InputImage.fromMediaImage(frameMediaImage, imageProxy.getImageInfo().getRotationDegrees());
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
                                                try {
                                                    Rect bounds = face.getBoundingBox();
                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                    FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                                                    if (mouthBottom != null) {
                                                        PointF mouthBottomPos = mouthBottom.getPosition();
                                                        // Toast.makeText(TestCameraX.this, "mouthBottom : " + mouthBottomPos.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    if(face.getContour(FaceContour.UPPER_LIP_BOTTOM) != null) {
                                                        myDrawing.upperLipBottomContour = Objects.requireNonNull(face.getContour(FaceContour.UPPER_LIP_BOTTOM)).getPoints();
                                                    }

                                                    if(face.getContour(FaceContour.UPPER_LIP_TOP) != null) {
                                                        myDrawing.upperLipTopContour = Objects.requireNonNull(face.getContour(FaceContour.UPPER_LIP_TOP)).getPoints();
                                                    }
                                                    if(face.getContour(FaceContour.LOWER_LIP_BOTTOM) != null) {
                                                        myDrawing.lowerLipBottomContour = Objects.requireNonNull(face.getContour(FaceContour.LOWER_LIP_BOTTOM)).getPoints();
                                                    }
                                                    if(face.getContour(FaceContour.LOWER_LIP_TOP) != null) {
                                                        myDrawing.lowerLipTopContour = Objects.requireNonNull(face.getContour(FaceContour.LOWER_LIP_TOP)).getPoints();
                                                    }



                                                    myDrawing.invalidate();
                                                } catch (NullPointerException ne) {
                                                    Log.e("error", ne.getMessage());
                                                }




                                            }


                                        }
                                    })
                            .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception

                                        }
                            })
                            .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Face>> task) {
                                    imageProxy.close();
                                }
                            });

        }
    }



}
