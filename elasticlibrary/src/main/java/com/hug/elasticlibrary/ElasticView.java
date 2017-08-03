package com.hug.elasticlibrary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by HStan on 2017/6/27.
 *
 */

public class ElasticView extends View {

    private Path mPath;
    private Paint mPaint;
    private Paint text;
    private float maxRadius;
    private float minRadius;
    private float mRadius;
    private Point mMovingCircle;
    private float maxLength;
    private int count = 100;

    private ValueAnimator animator;

    private boolean cleaned = false;
    private boolean readyClear = false;
    private boolean canMove = false;
    private int mColor;
    private int textColor;
    private UnreadMsgDotRemovedListener unreadMsgDotRemovedListener;

    public interface UnreadMsgDotRemovedListener {
        void removed(boolean removed, int count);
    }

    public ElasticView(Context context) {
        this(context, null);
    }

    public ElasticView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElasticView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        initAttrs(context,attrs);
        mMovingCircle = new Point();
        mMovingCircle.x = 0;
        mMovingCircle.y = 0;

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        text = new Paint();
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(23);
        text.setFakeBoldText(true);
        text.setColor(textColor);
    }

    private void initAttrs(Context context,AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ElasticView, 0, 0);
        mColor = typedArray.getColor(R.styleable.ElasticView_color,Color.RED);
        maxRadius = typedArray.getFloat(R.styleable.ElasticView_maxRadius,22f);
        minRadius = typedArray.getFloat(R.styleable.ElasticView_minRadius,8f);
        maxLength = typedArray.getFloat(R.styleable.ElasticView_maxLength,200f);
        count = typedArray.getInt(R.styleable.ElasticView_count,0);
        textColor = typedArray.getColor(R.styleable.ElasticView_textColor,Color.WHITE);
        typedArray.recycle();

    }

    private void initAnimation() {
        final int x = mMovingCircle.x;
        final int y = mMovingCircle.y;
        final int i = x > y ? x : y;
        animator = ValueAnimator.ofInt(i, 0);
        animator.setDuration(200);
        animator.setInterpolator(new OvershootInterpolator(3f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (int) animation.getAnimatedValue();
                if (i == x && i != 0) {
                    int y1 = (currentValue * y) / i;
                    update(currentValue, y1);
                } else if (i == y && i != 0) {
                    int x1 = (currentValue * x) / i;
                    update(x1, currentValue);
                }
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        canvas.drawColor(Color.TRANSPARENT);
        if (!cleaned && count > 0) {
            mPath = new Path();
            canvas.drawCircle(mMovingCircle.x, mMovingCircle.y, maxRadius, mPaint);
            if (mMovingCircle.x != 0 || mMovingCircle.y != 0) {
                int xx = mMovingCircle.x * mMovingCircle.x;
                int yy = mMovingCircle.y * mMovingCircle.y;
                if (Math.sqrt(xx + yy) <= maxLength) {
                    readyClear = false;
                    float startx = mMovingCircle.x;
                    float starty = mMovingCircle.y;
                    double a;
                    if (starty < 0) {
                        a = Math.atan(startx / starty) + Math.PI;
                    } else {
                        a = Math.atan(startx / starty);
                    }
                    float x = mRadius * (float) Math.cos(a);
                    float y = mRadius * (float) Math.sin(a);
                    mPath.moveTo(x, -y);
                    mPath.quadTo(startx / +2, starty / 2, x + startx, -y + starty);
                    mPath.lineTo(-x + startx, y + starty);
                    mPath.quadTo(startx / 2, starty / 2, -x, y);
                    mPath.lineTo(x, -y);
                    mPath.addCircle(0, 0, mRadius, Path.Direction.CW);
                } else {
                    readyClear = true;
                }
            }
            canvas.drawPath(mPath, mPaint);
            if (count > 99) {
                canvas.drawText("99+", mMovingCircle.x, mMovingCircle.y + 9, text);
            } else {
                canvas.drawText(count + "", mMovingCircle.x, mMovingCircle.y + 9, text);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX() - getMeasuredWidth() / 2;
        float ey = event.getY() - getMeasuredHeight() / 2;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ex < maxRadius &&
                        ex > -maxRadius &&
                        ey < maxRadius &&
                        ey > -maxRadius) {
                    canMove = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    canMove = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float currentRadius = maxRadius * (float) (1 - (Math.sqrt(ex * ex + ey * ey) / maxLength));
                    mRadius = currentRadius < minRadius ? minRadius : currentRadius;
                    update((int) ex, (int) ey);
                }
                break;
            case MotionEvent.ACTION_UP: {
                if (readyClear) {
                    remove();
                } else {
                    initAnimation();
                }
            }
            if (ex < maxRadius &&
                    ex > -maxRadius &&
                    ey < maxRadius &&
                    ey > -maxRadius) {
                mRadius = maxRadius * (float) (Math.sqrt(ex + ey) / maxLength);
                update((int) ex, (int) ey);
            }
        }
        return true;
    }

    public void setUnreadMsgDotRemovedListener(UnreadMsgDotRemovedListener unreadMsgDotRemovedListener) {
        this.unreadMsgDotRemovedListener = unreadMsgDotRemovedListener;
    }

    public void setMaxRadius(float maxRadius){
        this.maxRadius = maxRadius;
        postInvalidate();
    }

    public void setMinRadius(float minRadius){
        this.minRadius = minRadius;
        postInvalidate();
    }

    public void setMaxLength(float maxLength){
        this.maxLength = maxLength;
        postInvalidate();
    }

    public void setTextColor(int textColor){
        this.textColor = textColor;
        postInvalidate();
    }

    public void setColor(int color){
        mColor = color;
        postInvalidate();
    }

    public void setUnReadCount(int count) {
        this.count = count;
        cleaned = false;
        postInvalidate();
    }

    private void update(int x, int y) {
        mMovingCircle.x = x;
        mMovingCircle.y = y;
        postInvalidate();
    }

    private void remove() {
        cleaned = true;
        if (unreadMsgDotRemovedListener != null) {
            unreadMsgDotRemovedListener.removed(true, count);
        }
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(2 * (int) maxRadius,2 * (int) maxRadius);
    }
}
