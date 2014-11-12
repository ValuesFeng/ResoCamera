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
   
### Simple
``` java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        try {
            cameraView = new CameraView(this);
            cameraView.setFocusView((FocusView) findViewById(R.id.sf_focus));
            cameraView.setCameraView((SurfaceView) findViewById(R.id.sf_camera), getScreenWidth(this), CameraView.MODE4T3);
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
````

