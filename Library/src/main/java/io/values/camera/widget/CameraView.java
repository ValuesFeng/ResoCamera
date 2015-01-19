package io.values.camera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by valuesfeng on 14-8-8.
 */
public class CameraView implements SurfaceHolder.Callback, Camera.PictureCallback,
        Camera.AutoFocusCallback, View.OnTouchListener, ShakeListener.OnShakeListener {

    public final static String TAG = CameraView.class.getSimpleName();

    /**
     * camera flash mode
     */
    public final static int FLASH_AUTO = 2;
    public final static int FLASH_OFF = 0;
    public final static int FLASH_ON = 1;

    /**
     * camera prewView size
     */
    public static final int MODE4T3 = 43;
    public static final int MODE16T9 = 169;

    private int currentMODE = MODE4T3;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private FocusView focusView;

    private int flash_type = FLASH_AUTO; // 0 close , 1 open , 2 auto
    private static int camera_position = Camera.CameraInfo.CAMERA_FACING_BACK;// 0 back camera , 1 front camera
    private int takePhotoOrientation = 90;

    /**
     * if you need square picture , you can set true while you take a picture;
     * see {@link #takePicture(boolean)}
     */
    private boolean isSquare;

    private int topDistance;
    private int zoomFlag = 0;

    private SurfaceView surfaceView;
    private String PATH_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ResoCamera/";
    private String PATH_FILE;

    private String dirPath;
    private int screenDpi;
    /**
     * touch focus area size
     */
    private float focusAreaSize = 300;
    private OnCameraSelectListener onCameraSelectListener;

    private int picQuality = 80;

    public void setPicQuality(int picQuality) {
        if (picQuality > 0 && picQuality < 101)
            this.picQuality = picQuality;
    }

    public void setOnCameraSelectListener(OnCameraSelectListener onCameraSelectListener) {
        this.onCameraSelectListener = onCameraSelectListener;
    }

    public void setFocusAreaSize(float focusAreaSize) {
        this.focusAreaSize = focusAreaSize;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    private Context context;

    public CameraView(Context context) {
        this.context = context;
    }

    public void setCameraView(SurfaceView surfaceView) throws NullPointerException, ClassCastException {
        this.setCameraView(surfaceView, MODE4T3);
    }

    /**
     * @param surfaceView the camera view you should give it
     * @param cameraMode  set the camera preview proportion ,default is MODE4T3; {@link #MODE4T3}
     * @throws Exception
     */
    public void setCameraView(SurfaceView surfaceView, int cameraMode) throws NullPointerException, ClassCastException {
        this.surfaceView = surfaceView;
        this.currentMODE = cameraMode;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        if (currentMODE == MODE4T3) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.surfaceView.getLayoutParams();
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 4 / 3;
            this.surfaceView.setLayoutParams(layoutParams);
        } else if (currentMODE == MODE16T9) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.surfaceView.getLayoutParams();
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 16 / 9;
            this.surfaceView.setLayoutParams(layoutParams);
        }
        ShakeListener.newInstance().setOnShakeListener(this);
        screenDpi = context.getResources().getDisplayMetrics().densityDpi;
        mHolder = surfaceView.getHolder();
        surfaceView.setOnTouchListener(this);
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * set camera top distance
     *
     * @param topDistance
     */
    public void setTopDistance(int topDistance) {
        this.topDistance = topDistance;
    }

    public void setFocusView(FocusView focusView) {
        this.focusView = focusView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (screenDpi == DisplayMetrics.DENSITY_HIGH) {
            zoomFlag = 10;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        mHolder.setKeepScreenOn(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    private void openCamera() {
        try {
            closeCamera();
            mCamera = Camera.open(camera_position);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            setCameraPictureSize();
            setCameraPreviewSize();
            changeFlash(flash_type);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        flash_type = FLASH_AUTO;
        mCamera = null;
    }

    private void resetCamera() {
        if (onCameraSelectListener != null) {
            onCameraSelectListener.onChangeCameraPosition(camera_position);
        }
        Log.i(TAG, "camera-camera-position:" + camera_position);
        closeCamera();
        openCamera();
    }

    private void setCameraPreviewSize() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Collections.sort(sizes, sizeComparator);
        for (Camera.Size size : sizes) {
            params.setPreviewSize(size.width, size.height);
            if (size.width * 1.0 / size.height * 1.0 == 4.0 / 3.0 && currentMODE == MODE4T3) {
                break;
            } else if (size.width * 1.0 / size.height * 1.0 == 16.0 / 9.0 && currentMODE == MODE16T9) {
                break;
            }
        }
        mCamera.setParameters(params);
    }

    private void setCameraPictureSize() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Collections.sort(sizes, sizeComparator);
        for (Camera.Size size : sizes) {
            params.setPictureSize(size.width, size.height);
            if (size.width * 1.0 / size.height * 1.0 == 4.0 / 3.0
                    && currentMODE == MODE4T3 && size.height < 2000) {
                break;
            } else if (size.width * 1.0 / size.height * 1.0 == 16.0 / 9.0
                    && currentMODE == MODE16T9 && size.height < 2000) {
                break;
            }
        }
        params.setJpegQuality(picQuality);
        params.setPictureFormat(ImageFormat.JPEG);
        mCamera.setParameters(params);
    }

    /**
     * use with activity or fragment life circle
     */
    public final void onResume() {
        Log.i(TAG, "camera-resume");
        if (surfaceView == null)
            throw new NullPointerException("not init surfaceView for camera view");
        openCamera();
        ShakeListener.newInstance().start(context);
    }

    /**
     * seem to onResume
     * {@link #onResume()}
     */
    public final void onPause() {
        Log.i(TAG, "camera-pause");
        closeCamera();
        ShakeListener.newInstance().stop();
    }

    public final int changeFlash(int flash_type) {
        this.flash_type = flash_type;
        return changeFlash();
    }

    /**
     * change camera flash mode
     */
    public final int changeFlash() {
        if (mCamera == null) {
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null || flashModes.size() <= 1) {
            return 0;
        }
        if (onCameraSelectListener != null) {
            onCameraSelectListener.onChangeFlashMode((flash_type) % 3);
        }
        Log.i(TAG, "camera-flash-type:" + flash_type);
        switch (flash_type % 3) {
            case FLASH_ON:
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    flash_type++;
                    mCamera.setParameters(parameters);
                }
                break;
            case FLASH_OFF:
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flash_type++;
                    mCamera.setParameters(parameters);
                }
                break;
            case FLASH_AUTO:
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    flash_type++;
                    mCamera.setParameters(parameters);
                }
                break;
            default:
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flash_type++;
                    mCamera.setParameters(parameters);
                }
                break;
        }
        return flash_type;
    }

    /**
     * change camera facing
     */
    public final int changeCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (camera_position == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camera_position = Camera.CameraInfo.CAMERA_FACING_FRONT;
                resetCamera();
                return camera_position;
            } else if (camera_position == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camera_position = Camera.CameraInfo.CAMERA_FACING_BACK;
                resetCamera();
                return camera_position;
            }
        }
        return camera_position;
    }

    public final void takePicture(boolean isSquare) {
        if (mCamera != null) {
            this.isSquare = isSquare;
            mCamera.takePicture(null, null, this);
        }
    }

    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {
        try {
            if (dirPath != null && !dirPath.equals("")) {
                PATH_DIR = dirPath;
            }
            PATH_FILE = PATH_DIR + "IMG_" + System.currentTimeMillis() + ".jpg";
            createFolder(PATH_DIR);
            createFile(PATH_FILE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        FileOutputStream fos;
                        ExifInterface exifInterface = new ExifInterface(PATH_FILE);
                        switch (takePhotoOrientation) {
                            case 0:
                                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
                                break;
                            case 90:
                                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                                break;
                            case 180:
                                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                                break;
                            case 270:
                                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                                break;
                        }
                        if (camera_position == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            Matrix matrix = new Matrix();
                            matrix.postScale(-1, 1);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(), matrix,
                                    true);
                        }
                        if (isSquare) {
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getHeight(), bitmap.getHeight(), null,
                                    true);
                        }
                        System.gc();
                        fos = new FileOutputStream(PATH_FILE);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.flush();
                        bos.close();
                        exifInterface.saveAttributes();
                        bitmap.recycle();
                        if (onCameraSelectListener != null) {
                            onCameraSelectListener.onTakePicture(true, PATH_FILE);
                        }
                        openCamera();
                    } catch (Exception e) {
                        if (bitmap != null)
                            bitmap.recycle();
                        e.printStackTrace();
                        if (onCameraSelectListener != null) {
                            onCameraSelectListener.onTakePicture(false, "相机出错啦!");
                        }
                    }
                    System.gc();
                    takePhotoOrientation = 0;
                }
            }).start();
            closeCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (focusView != null) {
            focusView.clearDraw();
        }
    }

    /**
     * Convert touch position x:y in (-1000~1000)
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        x = x / surfaceView.getWidth();
        y = y / surfaceView.getHeight();

        float cameraX = y;
        float cameraY = 1 - x;

        int centerX = (int) (cameraX * 2000 - 1000);
        int centerY = (int) (cameraY * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private float mDist;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (mCamera == null) {
            return false;
        }
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();

        if (event.getPointerCount() > 1) {
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
            if (focusView != null) {
                focusView.clearDraw();
            }
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                if (focusView != null) {
                    focusView.clearDraw();
                    focusView.drawLine(event.getRawX(), event.getRawY() - topDistance);
                }
            }
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist && newDist - mDist > zoomFlag) {
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist && mDist - newDist > zoomFlag) {
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();

            Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY() - topDistance, 1f);
            Rect meteringRect = calculateTapArea(event.getRawX(), event.getRawY() - topDistance, 2f);

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> areaList = new ArrayList<Camera.Area>();
                areaList.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(areaList);
            }

            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringList = new ArrayList<Camera.Area>();
                meteringList.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(meteringList);
            }

            mCamera.setParameters(parameters);
            mCamera.autoFocus(this);
        }
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void createParentFolder(File file) throws Exception {
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new Exception("create parent directory failure!");
            }
        }
    }

    private void createFolder(String path) throws Exception {
        path = separatorReplace(path);
        File folder = new File(path);
        if (folder.isDirectory()) {
            return;
        } else if (folder.isFile()) {
            deleteFile(path);
        }
        folder.mkdirs();
    }

    private File createFile(String path) throws Exception {
        path = separatorReplace(path);
        File file = new File(path);
        if (file.isFile()) {
            return file;
        } else if (file.isDirectory()) {
            deleteFolder(path);
        }
        return createFile(file);
    }

    private File createFile(File file) throws Exception {
        createParentFolder(file);
        if (!file.createNewFile()) {
            throw new Exception("create file failure!");
        }
        return file;
    }

    private void deleteFolder(String path) throws Exception {
        path = separatorReplace(path);
        File folder = getFolder(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolder(file.getAbsolutePath());
            } else if (file.isFile()) {
                deleteFile(file.getAbsolutePath());
            }
        }
        folder.delete();
    }

    private File getFolder(String path) throws FileNotFoundException {
        path = separatorReplace(path);
        File folder = new File(path);
        if (!folder.isDirectory()) {
            throw new FileNotFoundException("folder not found!");
        }
        return folder;
    }

    private String separatorReplace(String path) {
        return path.replace("\\", "/");
    }

    private void deleteFile(String path) throws Exception {
        path = separatorReplace(path);
        File file = getFile(path);
        if (!file.delete()) {
            throw new Exception("delete file failure");
        }
    }

    private File getFile(String path) throws FileNotFoundException {
        path = separatorReplace(path);
        File file = new File(path);
        if (!file.isFile()) {
            throw new FileNotFoundException("file not found!");
        }
        return file;
    }

    @Override
    public void onShake(int orientation) {
        if (onCameraSelectListener != null) {
            onCameraSelectListener.onShake(orientation);
        }
        this.takePhotoOrientation = orientation;
    }

    public interface OnCameraSelectListener {
        public void onTakePicture(boolean success, String filePath);

        public void onChangeFlashMode(int flashMode);

        public void onChangeCameraPosition(int camera_position);

        public void onShake(int orientation);
    }

    public static class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}