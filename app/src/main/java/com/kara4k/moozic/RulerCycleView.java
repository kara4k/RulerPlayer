package com.kara4k.moozic;


import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.ArrayList;

public class RulerCycleView extends RulerView {


    private float mViewWidth;
    private float mViewHeight;

    private float mStartY;
    private float mEndY;
    private float mStartX = -1f;
    private float mEndX = -1f;

    private boolean mIsCycleMode;
    int mStartValue = -1;
    int mEndValue = -1;

    float[] mLinesPoints;
    private boolean mIsDrawText;
    private float mTextX;
    private float mTextY;

    private String mText;


    public RulerCycleView(Context context, SeekBar seekBar) {
        super(context, seekBar);
    }

    public RulerCycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void drawRuler(Canvas canvas) {
        initRulerParams();
        initDrawBorders(canvas);
        if (mStartX != -1f) {
            canvas.drawLine(mStartX, mStartY, mStartX, mEndY, mPaint);
        }
        if (mEndX != -1f) {
            canvas.drawLine(mEndX, mStartY, mEndX, mEndY, mPaint);
            canvas.drawLine(mStartX, mEndY, mEndX, mEndY, mPaint);
            canvas.drawLines(mLinesPoints, mPaint);
            if (!mIsDrawText) return;
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
        }
    }

    private void initDrawBorders(Canvas canvas) {
        mViewWidth = canvas.getWidth();
        mViewHeight = canvas.getHeight();
        mEndY = mViewHeight - calcDp(10);
        mStartY = mViewHeight - calcDp(25);
    }


    @Override
    protected void setup() {
        super.setup();
        mIsCycleMode = false;
        mPaint.setStrokeWidth(mPaintStrokeBig);
        mLinesPoints = new float[0];
    }


    public void setCycleStart() {
        float position = getProgressPosition();
        if (mEndX == -1f || position < mEndX) {
            mStartX = position;
            mStartValue = mSeekBar.getProgress();
        }
        if (mEndX != -1f) {
            setupLines();
        }

        invalidate();
    }

    public void setCycleEnd() {
        float position = getProgressPosition();
        if (mStartX == -1f) {
            mStartX = mRulerStartX;
            mStartValue = 0;
        }
        if (position <= mStartX) {
            return;
        }

        mEndX = position;
        mEndValue = mSeekBar.getProgress();
        mIsCycleMode = true;
        setupLines();

        invalidate();
    }



    private void setupLines() {
        int cycleSec = (mEndValue - mStartValue) / 1000;
        float rulerWidth = mEndX - mStartX;
        float linesStartY = mStartY + (mEndY - mStartY) / 2;
        if (rulerWidth >= calcDp(50)) {
            mIsDrawText = true;
            int secPerLine = 10;
            calc(cycleSec, rulerWidth, linesStartY, secPerLine);
        } else {
            mIsDrawText = false;
            mLinesPoints = new float[0];
        }
    }

    private void calc(int cycleSec, float rulerWidth, float mlinesStartY, int secPerLine) {

        int count = cycleSec / secPerLine;
        float step = rulerWidth / cycleSec * secPerLine;
        if (step < calcDp(25)) {
            calc(cycleSec, rulerWidth, mlinesStartY, 10 + secPerLine);
            return;
        }

        mText = String.format("%ds", secPerLine);
        float firstPoint = mStartX + step;
        float textWidth = mTextPaint.measureText(mText);
        mTextX = firstPoint - textWidth / 2;
        mTextY = mStartY;

        ArrayList<Float> linesPoints = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            linesPoints.add(firstPoint);
            linesPoints.add(mlinesStartY);
            linesPoints.add(firstPoint);
            linesPoints.add(mEndY);
            firstPoint += step;
        }

        mLinesPoints = new float[linesPoints.size()];
        for (int i = 0; i < linesPoints.size(); i++) {
            mLinesPoints[i] = linesPoints.get(i);
        }
    }

    public void stopCycle() {
        mIsCycleMode = false;
        resetPointValues();
        invalidate();
    }

    private void resetPointValues() {
        mStartValue = -1;
        mEndValue = -1;
        mStartX = -1f;
        mEndX = -1f;
        mLinesPoints = new float[0];
    }

    public boolean isCycleMode() {
        return mIsCycleMode;
    }

    public int getStartValue() {
        return mStartValue;
    }

    public int getEndValue() {
        return mEndValue;
    }
}
