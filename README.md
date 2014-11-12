ResoCamera
==========

Function：
==========    
   *you can set preview size 16:9 or 4:3  
   *Flash Control  
   *Touch Focus  
   *Front and rear camera toggle  
   *Two-finger zoom control camera preview  
   *After taking a picture of the filter(Based on GPUImage)  


## Simple
* Flash Mode : FLASH_AUTO (`default`),FLASH_OPEN,FLASH_CLOSE;    
* Camera Preview Proportion : MODE4T3(`default`),MODE16T9;    
 ####you can use:    
 mCamera.changeFlash();    
 mCamera.changeCamera();    
 mCamera.takePicture();    
 mCamera.changeFlash();     

````java
         public interface OnCameraSelectListener {
            public void onTakePicture(boolean success, String filePath);
            public void onChangeFlashMode(int flashMode);
            public void onChangeCameraPosition(int camera_position);
        }
````
    
## Quick Setup
#### 1. Android Manifest
``` xml
<manifest>
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
</manifest>
```
#### 2. Activity Class
````xml
   <activity android:screenOrientation="portrait"/>
````
    

``` java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        try {
            cameraView = new CameraView(this);
            cameraView.setFocusView((FocusView) findViewById(R.id.sf_focus));
            /**
             *  setCameraView(SurfaceView surfaceView, int screenWidth, int cameraMode)
             */
            cameraView.setCameraView((SurfaceView) findViewById(R.id.sf_camera), getScreenWidth(this), CameraView.MODE4T3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            cameraView.onResume();//must
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.onPause(); //must
    }
````

## License
    Copyright 2011-2014 valuesFeng

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
