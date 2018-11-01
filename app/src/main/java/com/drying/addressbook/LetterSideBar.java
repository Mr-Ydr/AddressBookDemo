package com.drying.addressbook;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: drying
 * E-mail: drying@erongdu.com
 * Date: 2018/11/1 11:36
 * <p/>
 * Description:侧边字母滑动栏
 */
public class LetterSideBar extends View {
    private static final String TAG = "LetterSideBar";
    private OnTouchLetterChangeListener mListener;
    // 滑动数据列表
    private List<String>                mLetters;
    // 当前选中的位置
    private int mCheckPosition = 0;
    private int mOldPosition;
    private int mNewPosition;
    // 字母列表画笔
    private Paint mLettersPaint = new Paint();
    // 提示字母画笔
    private Paint mTextPaint    = new Paint();
    // 波浪画笔
    private Paint mWavePaint    = new Paint();
    //文字大小
    private int mTextSize;
    //滑动提示文字大小
    private int mHintTextSize;
    //文字颜色
    private int mTextColor;
    private int mWaveColor;
    //选择文字颜色
    private int mCheckTextColor;
    private int mWidth;
    private int mHeight;
    //item高度
    private int mItemHeight;
    private int mPadding;
    // 波浪路径
    private Path mWavePath   = new Path();
    // 圆形路径
    private Path mCirclePath = new Path();
    // 手指滑动的Y点作为中心点
    private int           mCenterY; //中心点Y
    // 可滑动范围
    private int           sideWidth;
    // 圆形半径
    private int           mCircleRadius;
    // 用于过渡效果计算
    private ValueAnimator mRatioAnimator;
    // 用于绘制贝塞尔曲线的比率
    private float         mRatio;
    // 选中字体的坐标
    private float         mPointX, mPointY;
    // 圆形中心点X
    private float mCircleCenterX;
    //上下边距
    private int topAndBottonY = 100;

    public LetterSideBar(Context context) {
        this(context, null);
    }

    public LetterSideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LetterSideBar);
            mTextColor = a.getColor(R.styleable.LetterSideBar_textColor, Color.BLACK);
            mCheckTextColor = a.getColor(R.styleable.LetterSideBar_checkTextColor, Color.WHITE);
            mTextSize = a.getDimensionPixelSize(R.styleable.LetterSideBar_textSize, 12);
            mHintTextSize = a.getDimensionPixelSize(R.styleable.LetterSideBar_hintTextSize, 32);
            mWaveColor = a.getColor(R.styleable.LetterSideBar_backgroundColor, Color.BLUE);
            sideWidth = a.getDimensionPixelSize(R.styleable.LetterSideBar_sideWidth, 20);
            mCircleRadius = a.getDimensionPixelSize(R.styleable.LetterSideBar_circleRadius, 24);
            a.recycle();
        }

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mWaveColor);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mCheckTextColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mHintTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mLetters = new ArrayList<>();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        final float x = event.getX();
        mOldPosition = mCheckPosition;
        if (mLetters == null || mLetters.size() == 0) {
            return true;
        }
        mNewPosition = (int) (y / mHeight * mLetters.size());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断是否在滑动范围
                if (x < mWidth - 1.5 * sideWidth) {
                    return false;
                }
                mCenterY = (int) y;
                startAnimator(1.0f);

                break;
            case MotionEvent.ACTION_MOVE:

                mCenterY = (int) y;
                if (mOldPosition != mNewPosition) {
                    if (mNewPosition >= 0 && mNewPosition < mLetters.size()) {
                        mCheckPosition = mNewPosition;
                        if (mListener != null) {
                            mListener.onLetterChange(mLetters.get(mNewPosition));
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                startAnimator(0f);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = getMeasuredWidth();
        if (mLetters != null && mLetters.size() > 0) {
            mItemHeight = (mHeight - mPadding - topAndBottonY * 2) / mLetters.size();
        }
        mPointX = mWidth - 1.5f * mTextSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制字母列表
        drawLetters(canvas);
        //绘制提示圆形
        drawCirclePath(canvas);
        //绘制选中的样式
        drawCheckText(canvas);
    }

    /**
     * 绘制字母列表
     *
     * @param canvas
     */
    private void drawLetters(Canvas canvas) {

        RectF rectF = new RectF();
        rectF.left = mPointX - mTextSize;
        rectF.right = mPointX + mTextSize;
        rectF.top = mTextSize / 2;
        rectF.bottom = mHeight - mTextSize / 2;

        mLettersPaint.reset();
        mLettersPaint.setStyle(Paint.Style.FILL);
        mLettersPaint.setColor(Color.TRANSPARENT);
        mLettersPaint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);
        //绘制边框
        //        mLettersPaint.reset();
        //        mLettersPaint.setStyle(Paint.Style.STROKE);
        //        mLettersPaint.setColor(mTextColor);
        //        mLettersPaint.setAntiAlias(true);
        //        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);

        if (mLetters == null) {
            return;
        }
        for (int i = 0; i < mLetters.size(); i++) {
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColor);
            mLettersPaint.setAntiAlias(true);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetrics fontMetrics = mLettersPaint.getFontMetrics();
            float             baseline    = Math.abs(-fontMetrics.bottom - fontMetrics.top);

            float pointY = mItemHeight * i + baseline / 2 + mPadding + topAndBottonY;

            if (i == mCheckPosition) {
                mPointY = pointY;
            } else {
                canvas.drawText(mLetters.get(i), mPointX, pointY, mLettersPaint);
            }
        }
    }

    /**
     * 绘制选中的字母
     *
     * @param canvas
     */
    private void drawCheckText(Canvas canvas) {
        if (mCheckPosition != -1 && mLetters != null) {

            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(mWaveColor);
            canvas.drawCircle(mPointX, mPointY - 10, 20, p);

            // 绘制右侧选中字符
            mLettersPaint.reset();
            mLettersPaint.setColor(Color.WHITE);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mLetters.get(mCheckPosition), mPointX, mPointY, mLettersPaint);

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                String            target      = mLetters.get(mCheckPosition);
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float             baseline    = Math.abs(-fontMetrics.bottom - fontMetrics.top);
                float             x           = mCircleCenterX;
                float             y           = mCenterY + baseline / 2;
                canvas.drawText(target, x, y, mTextPaint);
            }
        }
    }

    /**
     * 绘制左边提示的圆
     *
     * @param canvas
     */
    private void drawCirclePath(Canvas canvas) {
        //x轴的移动路径
        mCircleCenterX = (mWidth + mCircleRadius) - (2.0f * sideWidth + 2.0f * mCircleRadius) * mRatio;

        mCirclePath.reset();
        mCirclePath.addCircle(mCircleCenterX, mCenterY, mCircleRadius, Path.Direction.CW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mCirclePath.op(mWavePath, Path.Op.DIFFERENCE);
        }

        mCirclePath.close();
        canvas.drawPath(mCirclePath, mWavePaint);
    }

    private void startAnimator(float value) {
        if (mRatioAnimator == null) {
            mRatioAnimator = new ValueAnimator();
        }
        mRatioAnimator.cancel();
        mRatioAnimator.setFloatValues(value);
        mRatioAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {

                mRatio = (float) value.getAnimatedValue();
                //球弹到位的时候，并且点击的位置变了，即点击的时候显示当前选择位置
                if (mRatio == 1f && mOldPosition != mNewPosition) {
                    if (mNewPosition >= 0 && mNewPosition < mLetters.size()) {
                        mCheckPosition = mNewPosition;
                        if (mListener != null) {
                            mListener.onLetterChange(mLetters.get(mNewPosition));
                        }
                    }
                }
                invalidate();
            }
        });
        mRatioAnimator.start();
    }

    public void setCheckItem(String txt) {
        int postion = getCheckPostion(txt);
        if (postion >= 0) {
            mCheckPosition = postion;
            invalidate();
        }
    }

    private int getCheckPostion(String txt) {
        if (mLetters != null && mLetters.size() > 0) {
            for (int i = 0; i < mLetters.size(); i++) {
                if (mLetters.get(i).equals(txt)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setOnTouchLetterChangeListener(OnTouchLetterChangeListener listener) {
        this.mListener = listener;
    }

    public List<String> getLetters() {
        return mLetters;
    }

    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }

    public interface OnTouchLetterChangeListener {
        void onLetterChange(String letter);
    }
}
