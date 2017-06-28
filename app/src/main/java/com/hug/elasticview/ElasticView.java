package com.hug.elasticview;

import android.animation.ValueAnimator;
import android.content.Context;
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
 */

public class ElasticView extends View {

    private Path mPath;
    private Paint mPaint;
    private Paint point;
    private Paint text;
    private float maxRadius = 22f;
    private float minRadius = 10f;
    private float mRadius = 22f;
    private Point mMovingCircle;
    private float maxLength = 200;
    private int count = 100;

    private ValueAnimator animator;

    private boolean cleaned = false;
    private boolean readyClear = false;
    private boolean canMove = false;
    private int mColor = Color.RED;
    private UnreadMsgDotRemovedListener unreadMsgDotRemovedListener;

    public interface UnreadMsgDotRemovedListener {
        void removed(boolean removed, int count);
    }

    public ElasticView(Context context) {
        super(context, null);
    }

    public ElasticView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public ElasticView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mMovingCircle = new Point();
        mMovingCircle.x = 0;
        mMovingCircle.y = 0;

        point = new Paint();
        point.setColor(Color.DKGRAY);
        point.setTextSize(30);
        point.setStrokeWidth(5);
        point.setStrokeCap(Paint.Cap.ROUND);

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        text = new Paint();
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(23);
        text.setFakeBoldText(true);
        text.setColor(Color.WHITE);
    }

    private void initAnimation() {
        final int x = mMovingCircle.x;
        final int y = mMovingCircle.y;
        final int i = x > y ? x : y;
        animator = ValueAnimator.ofInt(i, 0);
        animator.setDuration(300);
        animator.setInterpolator(new OvershootInterpolator(4f));
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
                    double a = 0;
                    if (starty < 0) {
                        a = Math.atan(startx / starty) + Math.PI;
                    } else {
                        a = Math.atan(startx / starty);
                    }
                    float x = mRadius * (float) Math.cos(a);
                    float y = mRadius * (float) Math.sin(a);
                    mPath.moveTo(x, -y);
                    mPath.quadTo(startx / 2, starty / 2, x + startx, -y + starty);
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
                } else {
                    canMove = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove) {
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

}
