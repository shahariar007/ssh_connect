package com.ms.timerforedtl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity {
    private Camera camera = null;
    private boolean inPreview = false;
    SurfaceView preview;
    SurfaceHolder previewHolder;
    Bitmap bmp, itembmp;
    static Bitmap mutableBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
        preview = (SurfaceView) findViewById(R.id.surface);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        previewHolder.setFixedSize(getWindow().getWindowManager()
                .getDefaultDisplay().getWidth(), getWindow().getWindowManager()
                .getDefaultDisplay().getHeight());

    }
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("PreviewDemo",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(MainActivity3.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(i1, i2,
                    parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview = true;
            }
        }
        private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
            Camera.Size result = null;
            for (Camera.Size size: parameters.getSupportedPreviewSizes()) {
                if (size.width <= width && size.height <= height) {
                    if (result == null) {
                        result = size;
                    } else {
                        int resultArea = result.width * result.height;
                        int newArea = size.width * size.height;
                        if (newArea > resultArea) {
                            result = size;
                        }
                    }
                }
            }
            return (result);
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

        }
    };
    public void onPictureTake(byte[] data, Camera camera) {

        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera = null;
        inPreview = false;
        super.onPause();
    }
}