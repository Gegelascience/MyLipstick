package com.remiverchere.mylipstick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class MyOverlayView extends SurfaceView implements SurfaceHolder.Callback {

    public Bitmap maskBitmap= null;

    public List<PointF> upperLipBottomContour;
    public List<PointF> upperLipTopContour;
    public List<PointF> lowerLipBottomContour;
    public List<PointF> lowerLipTopContour;

    public MyOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void surfaceCreated(SurfaceHolder holder) {

    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    protected void onDraw(Canvas canvas) {
        if ( maskBitmap != null ) {
            Log.d("test","dans drawing");
            canvas.drawBitmap(maskBitmap, 0f , 0f , null );
            Paint myPaint = new Paint();
            myPaint.setARGB(255, 255,0,0);
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
                canvas.drawPath(upperLips, myPaint);
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
                canvas.drawPath(lowerLips, myPaint);
            }
        }
    }
}
