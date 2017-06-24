package com.kara4k.moozic;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;

import java.util.ArrayList;

public class RulerView extends View {

    public static final int SEC = 1;
    public static final int TEN_SEC = SEC * 10;
    public static final int MIN = SEC * 60;
    public static final int TEN_MIN = MIN * 10;
    public static final int HOUR = MIN * 60;

    public static final String ZERO = "0";
    public static final String TEXT_TEN_SEC = "10s";
    public static final String TEXT_MIN = "1m";
    public static final String TEXT_TEN_MIN = "10m";
    public static final String TEXT_HOUR = "1h";


    private static final float RULER_START_Y = 10;
    private static final float RULER_END_Y = 32.5f;

    public static final float PAINT_STROKE_BIG = 1.5f;
    public static final float PAINT_STROKE_MIDDLE = 1;
    public static final float PAINT_STROKE_SMALL = 1;

    private int mSeekBarId;
    protected SeekBar mSeekBar;

    protected Paint mPaint;
    protected Paint mTextPaint;

    protected float mPaintStrokeBig;
    protected float mPaintStrokeMid;
    protected float mPaintStrokeSmall;

    private float[] mMainRulerPoints;
    private float[] mBigLinesPoints;
    private float[] mMiddleLinesPoints;
    private float[] mSmallLinesPoints;
    protected float mRulerWidth;
    protected float mRulerStartX;
    protected float mRulerEndX;
    private float mRulerStartY;
    private float mRulerEndY;
    private float mRulerSmallLineEndY;
    private float mRulerMiddleLineEndY;

    private boolean mIsDrawZero;
    private float mZeroX = -1f;
    private float mZeroY = -1f;

    private String mText = "";
    private float mTextX = -1f;
    private float mTextY = -1f;
    private boolean mIsDrawText;

    public RulerView(Context context, SeekBar seekBar) {
        super(context);
        mSeekBar = seekBar;
        setup();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        mSeekBarId = typedArray.getResourceId(R.styleable.RulerView_seekbar, -1);
        mIsDrawZero = typedArray.getBoolean(R.styleable.RulerView_is_draw_zero, false);
        typedArray.recycle();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mSeekBarId != -1) {
            mSeekBar = (SeekBar) getRootView().findViewById(mSeekBarId);
        }
    }

    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
    }

    protected void setup() {
        mPaintStrokeBig = calcDp(PAINT_STROKE_BIG);
        mPaintStrokeMid = calcDp(PAINT_STROKE_MIDDLE);
        mPaintStrokeSmall = calcDp(PAINT_STROKE_SMALL);
        mMainRulerPoints = new float[0];
        mBigLinesPoints = new float[0];
        mMiddleLinesPoints = new float[0];
        mSmallLinesPoints = new float[0];
        setupLinePaint();
        setupTextPaint();
        mIsDrawText = false;

    }

    public float calcDp(float sizeInPixels) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float inPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInPixels, dm);
        return inPixels;
    }

    private void setupLinePaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void setupTextPaint() {
        mTextPaint = new Paint();
        int spSize = 10;
        float scaledSizeInPixels = spSize * getResources().getDisplayMetrics().scaledDensity;
        mTextPaint.setTextSize(scaledSizeInPixels);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setTypeface(Typeface.DEFAULT);
    }


    private void setupRulerPoints() {
        initRulerParams();

        createMainRulerPoints();

        int totalSec = mSeekBar.getMax() / 1000;
        int step = TEN_SEC;
        if (totalSec < MIN) {
            mText = TEXT_TEN_SEC;
            step = TEN_SEC;
        } else if (totalSec <= TEN_MIN) {
            mText = TEXT_MIN;
            step = MIN;
        } else if (totalSec <= HOUR) {
            mText = TEXT_TEN_MIN;
            step = TEN_MIN;
        } else if (totalSec > HOUR) {
            mText = TEXT_HOUR;
            step = HOUR;
        }

        setupBigLines(step);
        setupMiddleLines(step / 2);
        setupSmallLines(step / 6);


    }

    public void setupBigLines(int secPerStep) {

        ArrayList<Float> bigRulerPointsList = calcFloats(secPerStep, mRulerEndY);

        if (bigRulerPointsList.size() > 3) {
            calcTextPosition(bigRulerPointsList);
            mIsDrawText = true;
        } else {
            mIsDrawText = false;
        }

        mBigLinesPoints = new float[bigRulerPointsList.size()];
        {
            for (int i = 0; i < bigRulerPointsList.size(); i++) {
                mBigLinesPoints[i] = bigRulerPointsList.get(i);
            }
        }
    }

    private void calcTextPosition(ArrayList<Float> bigRulerPointsList) {
        float textHeight = mTextPaint.getTextSize();
        float textWidth = mTextPaint.measureText(mText);
        mTextX = bigRulerPointsList.get(0) - textWidth / 2;
        mTextY = bigRulerPointsList.get(3) + textHeight + 5;
    }

    public void setupMiddleLines(int secPerStep) {
        ArrayList<Float> midRulerPointsList = calcFloats(secPerStep, mRulerMiddleLineEndY);

        mMiddleLinesPoints = new float[midRulerPointsList.size()];
        {
            for (int i = 0; i < midRulerPointsList.size(); i++) {
                mMiddleLinesPoints[i] = midRulerPointsList.get(i);
            }
        }
    }

    public void setupSmallLines(int secPerStep) {
        ArrayList<Float> smallRulerPointsList = calcFloats(secPerStep, mRulerSmallLineEndY);

        mSmallLinesPoints = new float[smallRulerPointsList.size()];
        {
            for (int i = 0; i < smallRulerPointsList.size(); i++) {
                mSmallLinesPoints[i] = smallRulerPointsList.get(i);
            }
        }
    }

    @NonNull
    private ArrayList<Float> calcFloats(int secPerStep, float rulerLineEndY) {
        int totalSec = mSeekBar.getMax() / 1000;
        int countLines = totalSec / secPerStep;
        ArrayList<Float> smallRulerPointsList = new ArrayList<>();
        float step = mRulerWidth / totalSec * secPerStep;
        float firstX = mRulerStartX + step;

        for (int i = 0; i < countLines; i++) {
            smallRulerPointsList.add(firstX);
            smallRulerPointsList.add(mRulerStartY);
            smallRulerPointsList.add(firstX);
            smallRulerPointsList.add(rulerLineEndY);
            firstX += step;
        }
        return smallRulerPointsList;
    }

    protected void initRulerParams() {
        mRulerStartY = calcDp(RULER_START_Y);
        mRulerEndY = calcDp(RULER_END_Y);
        float rulerHeight = mRulerEndY - mRulerStartY;
        mRulerSmallLineEndY = mRulerStartY + rulerHeight / 2;
        mRulerMiddleLineEndY = mRulerStartY + rulerHeight / 4 * 3;
        mRulerStartX = mSeekBar.getX() + mSeekBar.getPaddingLeft();
        mRulerEndX = mRulerStartX + mSeekBar.getWidth() - mSeekBar.getPaddingRight() - mSeekBar.getPaddingLeft();
        mRulerWidth = mRulerEndX - mRulerStartX;


    }

    @NonNull
    private ArrayList<Float> createMainRulerPoints() {
        ArrayList<Float> rulerPointsList = new ArrayList<>();
        rulerPointsList.add(mRulerStartX);
        rulerPointsList.add(mRulerStartY);
        rulerPointsList.add(mRulerEndX);
        rulerPointsList.add(mRulerStartY);
        rulerPointsList.add(mRulerStartX);
        rulerPointsList.add(mRulerStartY);
        rulerPointsList.add(mRulerStartX);
        rulerPointsList.add(mRulerEndY);

        calcZeroPosition();

        mMainRulerPoints = new float[rulerPointsList.size()];
        {
            for (int i = 0; i < rulerPointsList.size(); i++) {
                mMainRulerPoints[i] = rulerPointsList.get(i);
            }
        }

        return rulerPointsList;
    }

    private void calcZeroPosition() {
        float zeroWidth = mTextPaint.measureText(ZERO);
        float zeroHeight = mTextPaint.getTextSize();
        mZeroX = mRulerStartX - zeroWidth / 2;
        mZeroY = mRulerEndY + zeroHeight + 5;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawRuler(canvas);
        super.onDraw(canvas);
    }

    protected void drawRuler(Canvas canvas) {
        setupRulerPoints();
        mPaint.setStrokeWidth(mPaintStrokeBig);
        canvas.drawLines(mMainRulerPoints, mPaint);
        canvas.drawLines(mBigLinesPoints, mPaint);
        mPaint.setStrokeWidth(mPaintStrokeMid);
        canvas.drawLines(mMiddleLinesPoints, mPaint);
        mPaint.setStrokeWidth(mPaintStrokeSmall);
        canvas.drawLines(mSmallLinesPoints, mPaint);
        if (mIsDrawZero) {
            canvas.drawText(ZERO, mZeroX, mZeroY, mTextPaint);
        }
        if (mIsDrawText) {
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
        }
    }


    protected float getProgressPosition() {
        return mRulerStartX + mRulerWidth / mSeekBar.getMax() * mSeekBar.getProgress();
    }

}
