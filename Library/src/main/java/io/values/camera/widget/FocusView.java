package io.values.camera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ${valuesFeng} on ${2014.9.1}.
 */
public class FocusView extends SurfaceView implements SurfaceHolder.Callback {

    private Context context;
    protected SurfaceHolder sh;
    private Bitmap bitmap;

    private Paint paint = new Paint();

    private float rectWidth;

    public void setRectWidth(int rectDpiWidth) {
        float scale = context.getResources().getDisplayMetrics().density;
        rectWidth = (int) (rectDpiWidth * scale + 0.5f);
    }

    public FocusView(Context context) {
        super(context);
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        float scale = context.getResources().getDisplayMetrics().density;
        rectWidth = (int) (30 * scale + 0.5f);
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        paint.setColor(Color.TRANSPARENT);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setBitmap(int res) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), res);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
    }

    public void surfaceCreated(SurfaceHolder arg0) {

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {

    }

    synchronized void clearDraw() {
        Canvas canvas = sh.lockCanvas();
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawColor(Color.TRANSPARENT);
        sh.unlockCanvasAndPost(canvas);
    }

    public synchronized void drawLine(float x, float y) {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        if (bitmap != null) {
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(1.5f);
            canvas.drawRect(rectWidth, rectWidth, rectWidth, rectWidth, paint);
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, paint);
        }
        sh.unlockCanvasAndPost(canvas);
    }
}