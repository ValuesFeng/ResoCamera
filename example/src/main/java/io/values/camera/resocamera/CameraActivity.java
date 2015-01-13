package io.values.camera.resocamera;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import io.values.camera.widget.CameraView;
import io.values.camera.widget.FocusView;


public class CameraActivity extends Activity implements CameraView.OnCameraSelectListener,
        View.OnClickListener {

    private CameraView cameraView;

    DisplayImageOptions options = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_8888)
            .imageScaleType(ImageScaleType.EXACTLY).considerExifParams(true)
            .cacheInMemory(false).cacheOnDisk(false).displayer(new FadeInBitmapDisplayer(0)).build();

    private RelativeLayout rlTop;

    private ImageButton ib_camera_change;
    private ImageButton ib_camera_flash;
    private ImageButton ib_camera_grid;

    private ImageButton ibTakePicture;
    private ImageView ibCameraPhotos;

    private ImageView imgGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImaLoader();
        setContentView(R.layout.activity_camera);
        try {
            cameraView = new CameraView(this);
            cameraView.setOnCameraSelectListener(this);
            cameraView.setFocusView((FocusView) findViewById(R.id.sf_focus));
            //  cameraView.setCameraView((SurfaceView) findViewById(R.id.sf_camera));  //default
            cameraView.setCameraView((SurfaceView) findViewById(R.id.sf_camera), CameraView.MODE4T3);
            cameraView.setPicQuality(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initViews();
        showDCIM();
    }

    private void initViews() {
        rlTop = $(R.id.rl_top);
        ib_camera_change = $(R.id.ib_camera_change);
        ib_camera_flash = $(R.id.ib_camera_flash);
        ib_camera_grid = $(R.id.ib_camera_grid);
        ibTakePicture = $(R.id.ib_camera_take_picture);
        ibCameraPhotos = $(R.id.ib_camera_photos);
        imgGrid = $(R.id.img_grid);
    }

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    private void initImaLoader() {
        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration
                        .Builder(getApplicationContext())
                        .diskCacheFileCount(50)
                        .threadPoolSize(Thread.NORM_PRIORITY - 2)
                        .denyCacheImageMultipleSizesInMemory()
                        .memoryCacheSize(30)
                        .memoryCache(new LRULimitedMemoryCache(30))
                        .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * get first picture DCIM
     */
    private void showDCIM() {
        String columns[] = new String[]{
                MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME
        };
        Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
        boolean isOK = false;
        if (cursor != null) {
            cursor.moveToLast();
            String path = "";
            try {
                while (!isOK) {
                    int photoIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(photoIndex);
                    isOK = !(path.indexOf("DCIM/Camera") == -1); //Is thie photo from DCIM folder ?
                    cursor.moveToPrevious(); //Add this so we don't get an infinite loop if the first image from
                    //the cursor is not from DCIM
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
            ImageLoader.getInstance().displayImage("file://" + path, ibCameraPhotos, options);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ib_camera_change.setOnClickListener(this);
        ib_camera_flash.setOnClickListener(this);
        ib_camera_grid.setOnClickListener(this);
        ibTakePicture.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            cameraView.onResume();
            cameraView.setTopDistance(rlTop.getHeight());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_camera_change:
                cameraView.changeCamera();
                break;
            case R.id.ib_camera_flash:
                cameraView.changeFlash();
                break;
            case R.id.ib_camera_grid:
                if (imgGrid.getVisibility() == View.VISIBLE) {
                    imgGrid.setVisibility(View.GONE);
                    ib_camera_grid.setBackgroundResource(R.drawable.camera_grid_normal);
                    break;
                }
                ib_camera_grid.setBackgroundResource(R.drawable.camera_grid_press);
                imgGrid.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_camera_take_picture:
                cameraView.takePicture(false);
                break;
        }
    }

    @Override
    public void onShake(int orientation) {
        // you can rotate views here
    }

    @Override
    public void onTakePicture(final boolean success, String filePath) {
        //sd/ResoCamera/(file)
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success){
                    Toast.makeText(CameraActivity.this,"seccess!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onChangeFlashMode(int flashMode) {
        switch (flashMode) {
            case CameraView.FLASH_AUTO:
                ib_camera_flash.setBackgroundResource(R.drawable.camera_flash_auto);
                break;
            case CameraView.FLASH_OFF:
                ib_camera_flash.setBackgroundResource(R.drawable.camera_flash_off);
                break;
            case CameraView.FLASH_ON:
                ib_camera_flash.setBackgroundResource(R.drawable.camera_flash_on);
                break;
        }
    }

    @Override
    public void onChangeCameraPosition(int camera_position) {

    }
}
