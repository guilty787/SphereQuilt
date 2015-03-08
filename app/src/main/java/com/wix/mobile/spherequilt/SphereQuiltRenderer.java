package com.wix.mobile.spherequilt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created by Gil_Moshayof on 10/30/14.
 */
public class SphereQuiltRenderer implements GLSurfaceView.Renderer
{
    public static final long TIME_TO_ROTATE_360 =  5000;
    public static final long TIME_TO_ANIMATE_TO_SPHERE = 500;

    public static final float SPHERE_FIELD_ROTATION = 50;
    public static final float CAMERA_DISTANCE = 150;

    private BackgroundView mBackground;
    private List<Sphere> mSpheres;
    private PointF mLookAtPosition;

    private Context mContext;

    private long mLastAnimationTick;
    private long mElapsedRotationTime;

    private long mLastAnimateToSphereTime;
    private long mElapsedAnimateToSphereTime;

    private Sphere mSelectedSphere = null;
    private PointF mStartPosition = new PointF();

    private float mAspectRatio;

    private static int[] resIds = new int[]
            {
                    R.drawable.black_widow,
                    R.drawable.captain_america,
                    R.drawable.groot,
                    R.drawable.hawkeye,
                    R.drawable.hulk,
                    R.drawable.iron_man,
                    R.drawable.iron_patriot,
                    R.drawable.loki,
                    R.drawable.nick_fury,
                    R.drawable.rocket,
                    R.drawable.spider_man,
                    R.drawable.starlord,
                    R.drawable.thor,
                    R.drawable.winter_soldier
            };

    private static void generateTextures(GL10 gl, Context context)
    {
        Sphere.kTextures = new int[resIds.length];

        for (int i = 0; i < resIds.length; i++)
        {
            loadSingleGlTexture(gl, context, i, resIds[i]);
        }

        loadBgTexture(gl, context);
    }

    private static void loadSingleGlTexture(GL10 gl, Context context, int textureId, int resourceId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        gl.glGenTextures(1, Sphere.kTextures, textureId);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, Sphere.kTextures[textureId]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    private static void loadBgTexture(GL10 gl, Context context)
    {
        BackgroundView.bgTexture = new int[1];
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.marvel);
        gl.glGenTextures(1, BackgroundView.bgTexture, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, BackgroundView.bgTexture[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public SphereQuiltRenderer(Context context)
    {
        mLookAtPosition = new PointF(10, 0);
        mSpheres = new ArrayList<Sphere>();
        mContext = context;
    }

    public void addSphere(Sphere sphere)
    {
        mSpheres.add(sphere);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig)
    {
        if (Sphere.kTextures == null)
        {
            generateTextures(gl, mContext);
            createSpheres();

            mBackground = new BackgroundView();
        }

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glLoadIdentity();

        mAspectRatio = (float)width / (float)height;
        GLU.gluPerspective(gl, 45f, mAspectRatio, 50f, 700f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        performRotateAnimation();

        if (mSelectedSphere != null)
            performSelectionAnimation();

        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -CAMERA_DISTANCE, 0, 0, 0, 0, 1, 0);


        gl.glTranslatef(-mLookAtPosition.x, -mLookAtPosition.y, 0);



        mBackground.draw(gl);

        gl.glTranslatef(mLookAtPosition.x, mLookAtPosition.y, 0);
        gl.glRotatef(SPHERE_FIELD_ROTATION, 1, 0, 0);
        gl.glTranslatef(-mLookAtPosition.x, -mLookAtPosition.y, 0);







        for (Sphere sphere : mSpheres)
            sphere.draw(gl);
    }

    private void findTouchedSphere()
    {
        //GLU.gluLookAt(gl, 0, 0, -150, 0, 1, 0, 0, 1, 0);
        float[] view = new float[3];

        float[] cameraLookAt = new float[] { mLookAtPosition.x, mLookAtPosition.y, 0};
        float[] cameraPosition = new float[] { mLookAtPosition.x, mLookAtPosition.y + -CAMERA_DISTANCE * (float)Math.sin(SPHERE_FIELD_ROTATION * Math.PI / 180f), -CAMERA_DISTANCE * (float)Math.cos(SPHERE_FIELD_ROTATION * Math.PI / 180f)};



        view[0] = cameraLookAt[0] - cameraPosition[0];
        view[1] = cameraLookAt[1] - cameraPosition[1];
        view[2] = cameraLookAt[2] - cameraPosition[2];

        normalizeVector(view);

        float[] h = new float[3];
        float[] cameraUp = new float[] { 0, 1, 0};

        //(a2b3 - a3b2, a3b1 - a1b3, a1b2 - a2b1); a vector quantity

        h[0] = view[1] * cameraUp[2] - view[2] * cameraUp[1];
        h[1] = view[2] * cameraUp[0] - view[0] * cameraUp[2];
        h[2] = view[0] * cameraUp[1] - view[1] * cameraUp[0];
        normalizeVector(h);

        float[] v = new float[3];

        v[0] = h[1] * view[2] - h[2] * view[1];
        v[1] = h[2] * view[0] - h[0] * view[2];
        v[2] = h[0] * view[1] - h[1] * view[0];
        normalizeVector(v);


        float radians = 45f * (float)Math.PI / 180f;

        float vLength = (float)Math.tan(radians / 2f) * 50f;
        float hLength = vLength * mScreenWidth / mScreenHeight;

        v[0] *= vLength;
        v[1] *= vLength;
        v[2] *= vLength;

        h[0] *= hLength;
        h[1] *= hLength;
        h[2] *= hLength;

        float x = mTouchX - mScreenWidth / 2f;
        float y = mScreenHeight / 2f - mTouchY;

        x /= mScreenWidth / 2f;
        y /= mScreenHeight / 2f;

        float[] pos = new float[3];

        pos[0] = cameraPosition[0] + view[0] * 50f + h[0] * x + v[0] * y;
        pos[1] = cameraPosition[1] + view[1] * 50f + h[1] * x + v[1] * y;
        pos[2] = cameraPosition[2] + view[2] * 50f + h[2] * x + v[2] * y;

        float[] dir = new float[3];
        dir[0] = pos[0] - cameraPosition[0];
        dir[1] = pos[1] - cameraPosition[1];
        dir[2] = pos[2] - cameraPosition[2];
        //normalizeVector(dir);


        float scalar;

        float[] startingPos = new float[] { cameraPosition[0], cameraPosition[1], cameraPosition[2]};
        float[] currentPosition = new float[3];
        float distance;

        for (Sphere sphere : mSpheres)
        {
            scalar = (sphere.getZ() - cameraPosition[2]) / dir[2];
            currentPosition[0] = cameraPosition[0] + scalar * dir[0];
            currentPosition[1] = cameraPosition[1] + scalar * dir[1];
            //currentPosition[2] = cameraPosition[2] + scalar * dir[2];
            //for (int i = 0; i <  250; i+= 5)

            distance = (float)Math.sqrt((currentPosition[0] - sphere.getPosition().x) * (currentPosition[0] - sphere.getPosition().x) +
                    (currentPosition[1] - sphere.getPosition().y) * (currentPosition[1] - sphere.getPosition().y));

            if (distance <= 6)
            {
                //mLookAtPosition.set(sphere.getPosition().x, sphere.getPosition().y);
                mSelectedSphere = sphere;
                mStartPosition.set(mLookAtPosition);
                mLastAnimateToSphereTime = 0;
                mElapsedAnimateToSphereTime = 0;
                return;
                //adjustSpheresByPosition();
            }

        }
    }


    private void normalizeVector(float[] vector)
    {
        float length = (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
    }

    public void moveBy(float deltaX, float deltaY)
    {
        mSelectedSphere = null;

        mLookAtPosition.x += deltaX;
        mLookAtPosition.y += deltaY;

        adjustSpheresByPosition();
    }

    private void createSpheres()
    {
        float xPos, yPos, yAddition;

        for (int x = -10; x <= 10; x++)
        {
            yAddition = (x % 2 == 0 ? 10 : 0);

            for (int y = -10; y <= 10; y++)
            {
                xPos = x * 10;
                yPos = y * 20 + yAddition;
                addSphere(new Sphere(getRandomTexture(), xPos, yPos));
            }
        }

        adjustSpheresByPosition();
    }

    private void adjustSpheresByPosition()
    {
        float distance, ratio;
        for (Sphere sphere : mSpheres)
        {
            distance = getSphereDistance(sphere);
            ratio = distanceToTransitionRatio(distance);

            Log.i("", "ratio = " + ratio);

            sphere.setScale(0.5f + 2f * (ratio));
            sphere.setAlpha(0.75f + 0.25f * (ratio));
            sphere.setZ(20 + (ratio) * -40f);
        }
    }

    private float getSphereDistance(Sphere sphere)
    {
        float distance = (float)Math.sqrt(((sphere.getPosition().x - mLookAtPosition.x) * (sphere.getPosition().x - mLookAtPosition.x)) + ((sphere.getPosition().y - mLookAtPosition.y) * (sphere.getPosition().y - mLookAtPosition.y)));

        return distance;
    }

    private float distanceToTransitionRatio(float distance)
    {
        // 0 --> 1 (max size)
        // 40 --> 0.5
        // 80 --> 0 (min size)


        if (distance > 60)
            return 0;

        float linearRatio = ((60f - distance) / 60f);

        float sineRatio = (float)Math.sin(linearRatio * (Math.PI / 2f));

        float extraRatio = 0;

        /*
        if (linearRatio > 0.9f)
            extraRatio = (linearRatio - 0.9f) / 0.1f * 0.25f;*/


        return linearRatio * linearRatio + extraRatio;
    }

    public void performRotateAnimation()
    {
        if (mLastAnimationTick == 0)
        {
            mLastAnimationTick = System.currentTimeMillis();
            return;
        }

        long elapsedTime = System.currentTimeMillis() - mLastAnimationTick;
        mLastAnimationTick = System.currentTimeMillis();

        mElapsedRotationTime += elapsedTime;

        float rotation = (float)(((float)mElapsedRotationTime / (float)TIME_TO_ROTATE_360) * Math.PI * 2f);

        for (Sphere sphere : mSpheres)
            sphere.setRotation(rotation);
    }

    public void performSelectionAnimation()
    {
        if (mLastAnimateToSphereTime == 0)
        {
            mLastAnimateToSphereTime = System.currentTimeMillis();
            return;
        }

        long elapsedTime = System.currentTimeMillis() - mLastAnimateToSphereTime;
        mLastAnimateToSphereTime = System.currentTimeMillis();

        mElapsedAnimateToSphereTime += elapsedTime;

        float animationRatio = (float)Math.sin(Math.sqrt((float)mElapsedAnimateToSphereTime / TIME_TO_ANIMATE_TO_SPHERE) * Math.PI / 2f);

        if (mElapsedAnimateToSphereTime >= TIME_TO_ANIMATE_TO_SPHERE)
        {
            mLookAtPosition.x = mSelectedSphere.getPosition().x;
            mLookAtPosition.y = mSelectedSphere.getPosition().y;
            adjustSpheresByPosition();
            mSelectedSphere = null;
            mLastAnimateToSphereTime = 0;
            mElapsedAnimateToSphereTime = 0;
            return;
        }

        PointF vector = new PointF();
        vector.x = mSelectedSphere.getPosition().x - mStartPosition.x;
        vector.y = mSelectedSphere.getPosition().y - mStartPosition.y;

        mLookAtPosition.x = mStartPosition.x + animationRatio * vector.x;
        mLookAtPosition.y = mStartPosition.y + animationRatio * vector.y;

        adjustSpheresByPosition();
    }

    private int getRandomTexture()
    {
        return (int)(Math.random() * (float)Sphere.kTextures.length);
    }

    private float mTouchX;
    private float mTouchY;
    private float mScreenWidth;
    private float mScreenHeight;
    private boolean mTouchPerformed;

    public void setClickCoords(float touchX, float touchY, float screenWidth, float screenHeight)
    {
        mTouchX = touchX;
        mTouchY = touchY;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mTouchPerformed = true;

        findTouchedSphere();
    }
}
