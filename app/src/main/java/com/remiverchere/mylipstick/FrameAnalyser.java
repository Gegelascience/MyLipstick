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

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        frameMediaImage = imageProxy.getImage();
        if (frameMediaImage != null) {
            InputImage image = InputImage.fromMediaImage(frameMediaImage, imageProxy.getImageInfo().getRotationDegrees());
            refBitmap = toBitmap(frameMediaImage);
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
                                                if(face.getContour(FaceContour.UPPER_LIP_BOTTOM) != null) {
                                                    myDrawing.upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                }

                                                if(face.getContour(FaceContour.UPPER_LIP_TOP) != null) {
                                                    myDrawing.upperLipTopContour = face.getContour(FaceContour.UPPER_LIP_TOP).getPoints();
                                                }
                                                if(face.getContour(FaceContour.LOWER_LIP_BOTTOM) != null) {
                                                    myDrawing.lowerLipBottomContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM).getPoints();
                                                }
                                                if(face.getContour(FaceContour.LOWER_LIP_TOP) != null) {
                                                    myDrawing.lowerLipTopContour = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();
                                                }



                                                myDrawing.maskBitmap = refBitmap.copy(Bitmap.Config.ARGB_8888,true);;
                                                myDrawing.invalidate();


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
        else if (planes.length == 3){
            ByteBuffer yBuffer = planes[0].getBuffer(); // Y
            ByteBuffer vuBuffer = planes[2].getBuffer(); // VU

            int ySize = yBuffer.remaining();
            int vuSize = vuBuffer.remaining();

            byte[] nv21 = new byte[ySize + vuSize];

            yBuffer.get(nv21, 0, ySize);
            vuBuffer.get(nv21, ySize, vuSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        }
        return null;
    }

}
