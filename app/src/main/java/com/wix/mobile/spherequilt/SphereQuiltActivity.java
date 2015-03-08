package com.wix.mobile.spherequilt;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;


public class SphereQuiltActivity extends Activity
{
    private SphereQuiltSurfaceView mSphereQuiltSurfaceView;
    private SphereQuiltRenderer mSphereRenderer;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private float mPrevDeltaX;
    private float mPrevDeltaY;

    private float mPrevX;
    private float mPrevY;

    private boolean mIsDecellerating;

    private float mWidth;
    private float mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphere_quilt);

        mSphereQuiltSurfaceView = (SphereQuiltSurfaceView)findViewById(R.id.sphereQuiltSurfaceView);

        mSphereRenderer = new SphereQuiltRenderer(this);
        mSphereQuiltSurfaceView.setRenderer(mSphereRenderer);

        mTimer = new Timer();
        mTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (mIsDecellerating)
                    performDecellerate();

                mSphereQuiltSurfaceView.requestRender();
            }
        };

        mTimer.schedule(mTimerTask, 1000 / 40, 1000 / 40);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;

    }

    private void performDecellerate()
    {
        mPrevDeltaX *= 0.9f;
        mPrevDeltaY *= 0.9f;

        mSphereRenderer.moveBy(mPrevDeltaX * 0.1f, mPrevDeltaY * 0.1f);

        if (getDeltaDistance() <= 1f)
            mIsDecellerating = false;
    }

    private long mTouchDownTime;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                mTouchDownTime = System.currentTimeMillis();
                mIsDecellerating = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - mPrevX;
                float deltaY = event.getY() - mPrevY;

                mPrevX = event.getX();
                mPrevY = event.getY();

                mPrevDeltaX = deltaX;
                mPrevDeltaY = deltaY;

                mSphereRenderer.moveBy(deltaX * 0.1f, deltaY * 0.1f);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (getDeltaDistance() > 1f)
                    mIsDecellerating = true;

                if (System.currentTimeMillis() - mTouchDownTime < 250)
                    mSphereRenderer.setClickCoords(event.getX(), event.getY(), mWidth, mHeight);

                break;
        }

        return true;// super.onTouchEvent(event);
    }

    private float getDeltaDistance()
    {
        float distance = (float)Math.sqrt(mPrevDeltaX * mPrevDeltaX + mPrevDeltaY * mPrevDeltaY);
        return distance;
    }
}
