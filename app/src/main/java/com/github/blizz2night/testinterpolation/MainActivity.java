package com.github.blizz2night.testinterpolation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int UPDATE_COLOR_TEXT = 101;
    private GLSurfaceView mPreview;
    private int mLinearTex;
    private Bitmap mBWBitmap;
    private SeekBar mSeekBar;
    private float mProgress;
    private int mTexProgram;
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
    private int mSamplerProgram;
    private int u_Progress;
    private TextView mTextView;
    private ByteBuffer mPixels;
    private MyHandler mHandler;
    private TextView mColorTextView;
    private int mNearestTex;
    private int a_SamplerPosition;
    private int a_SamplerTexCoord;
    private int u_SamplerTextureUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreview = findViewById(R.id.preview);
        mSeekBar = findViewById(R.id.seek_bar);
        mTextView = findViewById(R.id.text_view);
        mColorTextView = findViewById(R.id.color_text_view);
        mHandler = new MyHandler(getMainLooper());
        mFsh = Utils.getStringFromFileInAssets(this, "frag_shader.glsl");
        mVsh = Utils.getStringFromFileInAssets(this, "vertex_shader.glsl");
        mFsh2 = Utils.getStringFromFileInAssets(this, "frag_shader2.glsl");
        mPreview.setEGLContextClientVersion(2);
        mPreview.setRenderer(new MyRender());
        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.position(0);
        mVertexBuffer.put(mVertexArr);
        mPixels = ByteBuffer.allocateDirect(4);
        mPixels.order(ByteOrder.nativeOrder());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress / (float) seekBar.getMax();
                String progressStr = Float.toString(mProgress);
                Log.i(TAG, "onProgressChanged: " + progressStr);
                mTextView.setText(progressStr);
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
                int width = 4;
                int height = 1;
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                int[] pixels = new int[width * height];
                pixels[0] = Color.WHITE;
                pixels[1] = Color.BLACK;
                pixels[2] = Color.WHITE;
                pixels[3] = Color.BLACK;

                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                mBWBitmap = bitmap;
            }

            int[] tex = new int[2];
            GLES20.glGenTextures(tex.length, tex, 0);
            mLinearTex = tex[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLinearTex);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBWBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

            mNearestTex = tex[1];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNearestTex);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBWBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);

            mTexProgram = Utils.loadProgram(mVsh, mFsh);
            a_Position = GLES20.glGetAttribLocation(mTexProgram, "a_Position");
            a_TexCoord = GLES20.glGetAttribLocation(mTexProgram, "a_TexCoord");
            u_TextureUnit = GLES20.glGetUniformLocation(mTexProgram, "u_TextureUnit");

            mSamplerProgram = Utils.loadProgram(mVsh, mFsh2);
            a_SamplerPosition = GLES20.glGetAttribLocation(mSamplerProgram, "a_Position");
            a_SamplerTexCoord = GLES20.glGetAttribLocation(mSamplerProgram, "a_TexCoord");
            u_Progress = GLES20.glGetUniformLocation(mSamplerProgram, "u_Progress");
            u_SamplerTextureUnit = GLES20.glGetUniformLocation(mSamplerProgram, "u_TextureUnit");
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

            //绘制纹理
            GLES20.glUseProgram(mTexProgram);
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(a_Position, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_Position);
            mVertexBuffer.position(2);
            GLES20.glVertexAttribPointer(a_TexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_TexCoord);
            //绘制线性采样纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLinearTex);
            GLES20.glUniform1i(u_TextureUnit, 0);
            GLES20.glViewport(0, 0, mWidth, mHeight/2);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, /* first= */ 0, /* offset= */ 4);
            //绘制最近邻采样纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNearestTex);
            GLES20.glUniform1i(u_TextureUnit, 0);
            GLES20.glViewport(0, mHeight/2, mWidth, mHeight/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, /* first= */ 0, /* offset= */ 4);

            //根据progress绘制对应位置的颜色采样
            GLES20.glUseProgram(mSamplerProgram);
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(a_SamplerPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_SamplerPosition);
            mVertexBuffer.position(2);
            GLES20.glVertexAttribPointer(a_SamplerTexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(a_SamplerTexCoord);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLinearTex);
            GLES20.glUniform1i(u_SamplerTextureUnit, 0);
            GLES20.glUniform1f(u_Progress, mProgress);
            GLES20.glViewport(0, mHeight * 3 / 4, mWidth, mHeight / 4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, /* first= */ 0, /* offset= */ 4);

            mPixels.position(0);
            GLES20.glReadPixels(mWidth / 2, mHeight * 3 / 4, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mPixels);
            mHandler.obtainMessage(UPDATE_COLOR_TEXT, mPixels.getInt(), 0, null).sendToTarget();
        }
    }

    private class MyHandler extends Handler{
        MyHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==UPDATE_COLOR_TEXT){
                mColorTextView.setText(Integer.toHexString(msg.arg1));
            }
        }
    }
}
