package com.remiverchere.mylipstick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class MyOverlayView extends SurfaceView implements SurfaceHolder.Callback {

    public List<PointF> upperLipBottomContour;
    public List<PointF> upperLipTopContour;
    public List<PointF> lowerLipBottomContour;
    public List<PointF> lowerLipTopContour;

    Path upperLips = new Path();
    Path lowerLips = new Path();

    public int analyseHeight = 0;
    public int analyseWidth = 0;
    Paint myPaint = new Paint();


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

            myPaint.setARGB(255, 255,0,0);
            myPaint.setStyle(Paint.Style.FILL);

            /*Paint debugPaint = new Paint();

            debugPaint.setARGB(255, 0,255,0);
            debugPaint.setStyle(Paint.Style.FILL);
            debugPaint.setTextSize(50f);


            canvas.drawText(String.valueOf(this.getHeight()),50,50,debugPaint);
            canvas.drawText(String.valueOf(this.getWidth()),200,50,debugPaint);
            canvas.drawText(String.valueOf(this.analyseHeight),50,150,debugPaint);
            canvas.drawText(String.valueOf(this.analyseWidth),200,150,debugPaint);
            */

            float middleImg = this.getWidth() / 2.0f;

            if (upperLipBottomContour != null && upperLipBottomContour.size() > 0) {

                upperLips.reset();



                //upperLips.moveTo(upperLipBottomContour.get(0).x, upperLipBottomContour.get(0).y);
                upperLips.moveTo(mirrorEffect(upperLipBottomContour.get(0).x, middleImg) + widthPadding(), upperLipBottomContour.get(0).y + heightPadding());
                // levre superieur bas
                for (int i = 0; i < upperLipBottomContour.size(); i++) {
                    upperLips.lineTo(mirrorEffect(upperLipBottomContour.get(i).x,middleImg) + widthPadding(), upperLipBottomContour.get(i).y + heightPadding());
                }

                // levre superieur haut
                for (int i = 0; i < upperLipTopContour.size(); i++) {
                    upperLips.lineTo(mirrorEffect(upperLipTopContour.get(i).x,middleImg) + widthPadding(), upperLipTopContour.get(i).y + heightPadding());
                }
                upperLips.lineTo(mirrorEffect(upperLipBottomContour.get(0).x,middleImg) + widthPadding(), upperLipBottomContour.get(0).y + heightPadding());
                canvas.drawPath(upperLips, myPaint);
            }


            if(lowerLipBottomContour != null && lowerLipBottomContour.size() > 0) {
                lowerLips.reset();

                lowerLips.moveTo(mirrorEffect(lowerLipBottomContour.get(0).x,middleImg) + widthPadding(), lowerLipBottomContour.get(0).y  + heightPadding());
                // levre inferieur bas
                for (int i = 0; i < lowerLipBottomContour.size(); i++) {
                    lowerLips.lineTo(mirrorEffect(lowerLipBottomContour.get(i).x,middleImg) + widthPadding(), lowerLipBottomContour.get(i).y + heightPadding());
                }

                // levre inferieur haut
                for (int i = 0; i < lowerLipTopContour.size() - 1; i++) {
                    lowerLips.lineTo(mirrorEffect(lowerLipTopContour.get(i).x,middleImg) + widthPadding(), lowerLipTopContour.get(i).y + heightPadding());
                }
                lowerLips.lineTo(mirrorEffect(lowerLipBottomContour.get(0).x, middleImg) + widthPadding(), lowerLipBottomContour.get(0).y + heightPadding());
                canvas.drawPath(lowerLips, myPaint);
            }

    }

    private float widthPadding() {
        //int widthDraw = this.getWidth();
        return -200;

    }

    private float heightPadding() {
        int heightDraw = this.getHeight();
        return  3*(this.analyseHeight - heightDraw);

    }

    private float mirrorEffect(float initialX, float middleX) {
        if (initialX < middleX) {
            return initialX + 2.0f*(middleX-initialX);
        } else {
            return initialX - 2.0f*(initialX - middleX);
        }
    }
}
