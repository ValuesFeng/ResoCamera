package io.values.camera.resocamera;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import io.values.camera.widget.CameraView;
import io.values.camera.widget.FocusView;


public class CameraActivity extends Activity {

    private CameraView cameraView;
    private SurfaceView surfaceView;
    public static DisplayMetrics metric = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        try {
            cameraView = new CameraView(this);
            cameraView.setMode(CameraView.MODE4T3);
            surfaceView = (SurfaceView) findViewById(R.id.sf_camera);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
            layoutParams.width = getScreenWidth(this);
            layoutParams.height = getScreenWidth(this) * 4 / 3;
            surfaceView.setLayoutParams(layoutParams);

            cameraView.setFocusView((FocusView) findViewById(R.id.sf_focus));
            cameraView.setCameraView(surfaceView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            cameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.onPause();
    }

    public static int getScreenHeight(Activity context) {
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    public static int getScreenWidth(Activity context) {
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

}
