package com.wix.mobile.spherequilt;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gil_Moshayof on 10/30/14.
 */
public class Sphere
{
    public static int[] kTextures;

    private static final int RINGS = 15;
    private static final int SECTIONS = 15;

    private int mTextureId;

    private float[] mTextureCoords;
    private float[] mVertexes;
    private float[] mNormals;
    private char[] mIndexes;

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private CharBuffer mIndexBuffer;

    private PointF mPosition;
    private float mRotation;
    private float mScale;
    private float mAlpha;

    private float mZ;

    private boolean mVisible =  true;

    public Sphere(int textureId, float x, float y)
    {
        mTextureId = textureId;

        mVertexes = new float[RINGS * SECTIONS * 3];
        mNormals = new float[RINGS * SECTIONS * 3];
        mTextureCoords = new float[RINGS * SECTIONS * 3];
        mIndexes = new char[RINGS * SECTIONS * 6];

        generateSphereVertexes(3, mVertexes, mNormals, mTextureCoords, mIndexes);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(mVertexes.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(mVertexes);
        mVertexBuffer.position(0);


        byteBuf = ByteBuffer.allocateDirect(mIndexes.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mIndexBuffer = byteBuf.asCharBuffer();
        mIndexBuffer.put(mIndexes);
        mIndexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(mTextureCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = byteBuf.asFloatBuffer();
        mTextureBuffer.put(mTextureCoords);
        mTextureBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(mNormals.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mNormalBuffer = byteBuf.asFloatBuffer();
        mNormalBuffer.put(mNormals);
        mNormalBuffer.position(0);

        mPosition = new PointF(x, y);
        mScale = 1;
        mAlpha = 1;
    }

    private void generateSphereVertexes(float radius, float[] vertexes, float[] normals, float[] textureCoords, char[] indexes)
    {
        float R = 1f / (float) (RINGS - 1);
        float S = 1f / (float) (SECTIONS - 1);
        int r, s;


        float x, y, z;
        int vertexIndex = 0, normalIndex = 0, textureIndex = 0, indexIndex = 0;

        for (r = 0; r < RINGS; r++)
        {
            for (s = 0; s < SECTIONS; s++)
            {
                y = (float) Math.sin((-Math.PI / 2f) + Math.PI * r * R);
                x = (float) Math.cos(2f * Math.PI * s * S) * (float) Math.sin(Math.PI * r * R);
                z = (float) Math.sin(2f * Math.PI * s * S) * (float) Math.sin(Math.PI * r * R);

                if (textureCoords != null)
                {
                    textureCoords[textureIndex] = s * S;
                    textureCoords[textureIndex + 1] = r * R;

                    textureIndex += 2;
                }

                vertexes[vertexIndex] = x * radius;
                vertexes[vertexIndex + 1] = y * radius;
                vertexes[vertexIndex + 2] = z * radius;

                vertexIndex += 3;

                normals[normalIndex] = x;
                normals[normalIndex + 1] = y;
                normals[normalIndex + 2] = z;

                normalIndex += 3;
            }
        }


        int r1, s1;
        for (r = 0; r < RINGS; r++)
        {
            for (s = 0; s < SECTIONS; s++)
            {
                r1 = (r + 1 == RINGS) ? 0 : r + 1;
                s1 = (s + 1 == SECTIONS) ? 0 : s + 1;

                indexes[indexIndex] = (char) (r * SECTIONS + s);
                indexes[indexIndex + 1] = (char) (r * SECTIONS + (s1));
                indexes[indexIndex + 2] = (char) ((r1) * SECTIONS + (s1));

                indexes[indexIndex + 3] = (char) ((r1) * SECTIONS + s);
                indexes[indexIndex + 4] = (char) ((r1) * SECTIONS + (s1));
                indexes[indexIndex + 5] = (char) (r * SECTIONS + s);
                indexIndex += 6;
            }
        }
    }

    public PointF getPosition()
    {
        return mPosition;
    }

    public float getScale()
    {
        return mScale;
    }

    public void setScale(float scale)
    {
        mScale = scale;
    }


    public float getRotation()
    {
        return mRotation;
    }

    public void setRotation(float rotation)
    {
        mRotation = rotation;
    }

    public float getAlpha()
    {
        return mAlpha;
    }

    public void setAlpha(float alpha)
    {
        mAlpha = alpha;
    }

    public boolean isVisible()
    {
        return mVisible;
    }

    public void setVisible(boolean visible)
    {
        mVisible = visible;
    }

    public float getZ()
    {
        return mZ;
    }

    public void setZ(float z)
    {
        mZ = z;
    }

    public void draw(GL10 gl)
    {
        if (mAlpha == 0 || !mVisible)
            return;

        gl.glPushMatrix();

        gl.glTranslatef(mPosition.x, mPosition.y, mZ);

        gl.glRotatef(-SphereQuiltRenderer.SPHERE_FIELD_ROTATION * 1.25f, 1, 0, 0);
        gl.glRotatef(90, 0, 1, 0);
        gl.glRotatef(180, 0, 0, 1);

        float angle = 270 - mRotation * 180f / (float)(Math.PI);

        gl.glRotatef(-angle, 0, 1, 0f);

        gl.glScalef(mScale, mScale, mScale);

        gl.glColor4f(mAlpha, mAlpha, mAlpha, mAlpha);


        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glFrontFace(GL10.GL_CW);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, kTextures[mTextureId]);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);

        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexes.length, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

        gl.glColor4f(1, 1, 1, 1);

        gl.glPopMatrix();

    }
}
