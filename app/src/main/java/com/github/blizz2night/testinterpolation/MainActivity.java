package com.github.blizz2night.testinterpolation;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GLSurfaceView mPreview;
    private int mTex;
    private Bitmap mBWBitmap;
    private SeekBar mSeekBar;
    private float mProgress;
    private int mProgram;
    private float[] mVertexArr = new float[]{
            -1.f,  1.f, 0.f, 1.f,
            -1.f, -1.f, 0.f, 0.f,
            1.f, -1.f, 1.f, 0.f,
            1.f,  1.f, 1.f, 1.f
    };
    private FloatBuffer mVertexBuffer;
    private int a_Position;
    private int a_TexCoord;
    private String mFsh;
    private String mVsh;
    private int u_TextureUnit;
    private int mHeight;
    private int mWidth;
    private String mFsh2;
    private int mProgram2;
    private int u_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreview = findViewById(R.id.preview);
        mSeekBar = findViewById(R.id.seek_bar);
        mFsh = Utils.getStringFromFileInAssets(this, "frag_shader.glsl");
        mVsh = Utils.getStringFromFileInAssets(this, "vertex_shader.glsl");
        mFsh2 = Utils.getStringFromFileInAssets(this, "frag_shader2.glsl");

        mPreview.setEGLContextClientVersion(2);
        mPreview.setRenderer(new MyRender());
        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.position(0);
        mVertexBuffer.put(mVertexArr);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress/100.f;
                Log.i(TAG, "onProgressChanged: " + mProgress);

                if (mPreview != null) {
                    mPreview.requestRender();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class MyRender implements GLSurfaceView.Renderer {


        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            if (mBWBitmap == null) {
                Bitmap bitmap = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888);
                int[] pixels = new int[2];
                pixels[0] = Color.WHITE;
                pixels[1] = Color.BLACK;
//                pixels[2] = Color.BLACK;
//                pixels[3] = Color.WHITE;

                bitmap.setPixels(pixels, 0, 2, 0, 0, 2, 1);
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                mBWBitmap = bitmap;
            }

            int[] tex = new int[1];
            GLES20.glGenTextures(tex.length, tex, 0);
            mTex = tex[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTex);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBWBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

            mProgram = Utils.loadProgram(mVsh, mFsh);
            a_Position = GLES20.glGetAttribLocation(mProgram, "a_Position");
            a_TexCoord = GLES20.glGetAttribLocation(mProgram, "a_TexCoord");
            u_TextureUnit = GLES20.glGetUniformLocation(mProgram, "u_TextureUnit");

            mProgram2 = Utils.loadProgram(mVsh, mFsh2);
            a_Position = GLES20.glGetAttribLocation(mProgram, "a_Position");
            a_TexCoord = GLES20.glGetAttribLocation(mProgram, "a_TexCoord");
            u_Progress = GLES20.glGetUniformLocation(mProgram, "u_Progress");
            u_TextureUnit = GLES20.glGetUniformLocation(mProgram, "u_TextureUnit");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            GLES20.glViewport(0, height/2, width, height/2);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram2);
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(a_Position, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_Position);

            mVertexBuffer.position(2);
            GLES20.glVertexAttribPointer(a_TexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_TexCoord);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTex);
            GLES20.glUniform1i(u_TextureUnit, 0);

            GLES20.glUniform1f(u_Progress, mProgress);

            GLES20.glViewport(0, 0, mWidth, mHeight);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, /* first= */ 0, /* offset= */ 4);

            GLES20.glUseProgram(mProgram);

            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(a_Position, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_Position);

            mVertexBuffer.position(2);
            GLES20.glVertexAttribPointer(a_TexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_TexCoord);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTex);
            GLES20.glUniform1i(u_TextureUnit, 0);

            GLES20.glViewport(0, 0, mWidth, mHeight/2);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, /* first= */ 0, /* offset= */ 4);




        }
    }
}
