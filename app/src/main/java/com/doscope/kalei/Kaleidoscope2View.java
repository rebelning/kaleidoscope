package com.doscope.kalei;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 * Created by rebelning
 * Created date 2019/1/9 10:57
 * https://stackoverflow.com/questions/31050206/android-rotate-two-objectscircles-ontouch-with-animation
 * https://www.jianshu.com/p/ac1250bccd3b/
 * https://blog.csdn.net/mikasoi/article/details/79590019
 * https://blog.csdn.net/yaodaoji/article/details/81540883
 * <p>
 * 输入条件：
 * 基圆（定圆）半径R
 * 母圆（动圆）半径r
 * 动点距母圆圆心的距离l
 * <p>
 * 结果：
 * 在输入以上条件以后，画面中出现一个半径为R的圆OR和一个半径为r的圆Or，
 * 二者嵌套并相切，用手指在屏幕沿逆时针画圈，Or沿OR无摩擦滚动，此时，
 * 在Or内且距Or圆心为l的动点会扫过轨迹，随着Or滚动，动点的轨迹逐渐延长。
 * <p>
 * 动点轨迹的横纵坐标公式：
 * X=(R-r)cost+lcos(R/r-1)t
 * Y=(R-r)sint-lsin(R/r-1)t
 * t为随着Or转动，Or圆心相对于x轴张开的角度。
 * r可以大于R，l可以大于r
 */
public class Kaleidoscope2View extends SurfaceView implements SurfaceHolder.Callback {
    private static final String LOGTAG = LogUtil.makeLogTag(Kaleidoscope2View.class);
    private boolean isRunning = false;
    private boolean isStart = false;
    private boolean isDrawing = false;
    private SurfaceHolder holder;
    private Paint drawPaint;
    private Paint drawTrack;
    private Paint drawXyPaint;
    private Paint radiusPaint;
    private Paint radiusPaint1;
    private Paint trackPaint;
    private Paint drawTextPaint;
    private int paintColor = 0xFF000000, paintAlpha = 255;
    private int xyPaintColor = 0xFFf54949;
    private int radiusPaintColor = 0xFFf54949;
    private float brushSize, lastBrushSize;
    private float m_prevX, m_prevY;

    private int divisor = 360 * 100;
    //动圆圆心
    private List<TrackRadiusPoint> trackRadiusList = new ArrayList();
    //动点
    private List<TrackPoint> trackPointList = new ArrayList();

    private int index = 0;

    private int screenWidth;
    private int screenHeight;

    private float oR;
    private float ol;

    private float axisX;
    private float axisY;

    class TrackRadiusPoint {
        float cx;
        float cy;
        float t;
        float radius;
        float lr;
    }

    class TrackPoint {
        float x;
        float y;
        float t;
    }

    public Kaleidoscope2View(Context context) {
        super(context);
        init();
    }

    public Kaleidoscope2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Kaleidoscope2View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        drawPaint = initPaint(brushSize, paintColor, 0, 0);
        drawTrack = initPaint(brushSize, radiusPaintColor, 0, 0);

        //
        isRunning = false;
        //x,y轴
        drawXyPaint = new Paint();
        drawXyPaint.setAntiAlias(true);
        drawXyPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        drawXyPaint.setStrokeJoin(Paint.Join.ROUND);
        drawXyPaint.setStrokeCap(Paint.Cap.ROUND);
        drawXyPaint.setColor(xyPaintColor);
        //radius
        radiusPaint = new Paint();
        radiusPaint.setAntiAlias(true);
        radiusPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        radiusPaint.setStrokeJoin(Paint.Join.ROUND);
        radiusPaint.setStrokeCap(Paint.Cap.ROUND);
        radiusPaint.setColor(radiusPaintColor);
        //radius
        trackPaint = new Paint();
        trackPaint.setAntiAlias(true);
        trackPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//        trackPaint.setStrokeJoin(Paint.Join.ROUND);
//        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(radiusPaintColor);
        trackPaint.setTextSize(30);
        trackPaint.setStyle(Paint.Style.FILL);


        radiusPaint1 = new Paint();
        radiusPaint1.setAntiAlias(true);
        radiusPaint1.setFlags(Paint.ANTI_ALIAS_FLAG);
        radiusPaint1.setStrokeJoin(Paint.Join.ROUND);
        radiusPaint1.setStrokeCap(Paint.Cap.ROUND);
        radiusPaint1.setColor(0xFF000000);
        //test
        drawTextPaint = new Paint();
        drawTextPaint.setAntiAlias(true);
        drawTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        drawTextPaint.setStrokeJoin(Paint.Join.ROUND);
        drawTextPaint.setStrokeCap(Paint.Cap.ROUND);
        drawTextPaint.setColor(paintColor);
        drawTextPaint.setTextSize(20.0f);


    }

    /**
     * init paint
     *
     * @param brushSize
     * @param paintColor
     * @param drawType
     * @return
     */
    private Paint initPaint(float brushSize, int paintColor, int drawType, int paintAlpha) {
        Paint drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        switch (drawType) {
            case 0://drawing pen
//                calculateColor();
                drawPaint.setXfermode(null);
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(brushSize);
                drawPaint.setStyle(Paint.Style.STROKE);
//                setPenAlpha(paintAlpha);
                break;
            case 1://drawing eraser
                drawPaint.setStyle(Paint.Style.FILL);
                drawPaint.setColor(0x0FFFFFFFF);
                drawPaint.setStrokeWidth(brushSize);
                break;
        }

        return drawPaint;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public float getoR() {
        return oR;
    }

    public void setoR(float oR) {
        this.oR = oR;
    }

    public float getOl() {
        return ol;
    }

    public void setOl(float ol) {
        this.ol = ol;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DebugLog.i(LOGTAG, "surfaceCreated...");
        isRunning = true;
        new Thread(runnable).start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        DebugLog.i(LOGTAG, "surfaceChanged...");
        this.screenWidth = width;
        this.screenHeight = height;

        DebugLog.i(LOGTAG, "screenWidth:" + screenWidth);
        DebugLog.i(LOGTAG, "screenHeight:" + screenHeight);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        DebugLog.i(LOGTAG, "surfaceDestroyed...");
        isRunning = false;
    }

    int[] location = new int[2];
    public float downX, downY, preX, preY, curX, curY;
    public int drawDensity = 2;//绘制密度,数值越高图像质量越低、性能越好

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getLocationInWindow(location); //获取在当前窗口内的绝对坐标
        curX = (event.getRawX() - location[0]) / drawDensity;
        curY = (event.getRawY() - location[1]) / drawDensity;
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
//                DebugLog.i(LOGTAG, "ACTION_POINTER_DOWN-touchX:" + touchX);
//                DebugLog.i(LOGTAG, "ACTION_POINTER_DOWN-touchY:" + touchY);
                break;
            case MotionEvent.ACTION_DOWN:
                //start
//                DebugLog.i(LOGTAG, "ACTION_DOWN-touchX:" + touchX);
//                DebugLog.i(LOGTAG, "ACTION_DOWN-touchY:" + touchY);
//                ++index;
                break;
            case MotionEvent.ACTION_MOVE:
                //move
//                DebugLog.i(LOGTAG, "ACTION_MOVE-touchX:" + touchX);
//                DebugLog.i(LOGTAG, "ACTION_MOVE-touchY:" + touchY);
                ++index;
                break;
            case MotionEvent.ACTION_UP:
//                DebugLog.i(LOGTAG, "ACTION_UP-touchX:" + touchX);
//                DebugLog.i(LOGTAG, "ACTION_UP-touchY:" + touchY);
                break;
            default:
                return false;
        }
        preX = curX;
        preY = curY;
        //redraw
//        invalidate();
        return true;

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                drawBackground();
                //
                axisX = getoR();
                axisY = screenHeight * 3 / 4;

                //动圆圆心所在的圆
                float radiusOr = getoR();
                float l = getOl();
                calculateNewTrack(axisX, axisY, radiusOr, l);


                int count = trackPointList.size();
                for (int i = 0; i < divisor; i++) {
                    if (isRunning) {
//                        SystemClock.sleep(5);
                        if (index > count - 1) {
                            index = 0;
                        }
                        draw(index);
                    }
                }
                SystemClock.sleep(500);
            }
        }
    };


    /**
     *
     */
    private void drawBackground() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas(null);
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
//                drawXY(canvas);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }


    /**
     * draw
     */
    private void draw(int i) {
        Canvas canvas = null;
        try {
            if (holder != null && holder.getSurface().isValid()) {
                canvas = holder.lockCanvas(null);
                if (canvas != null) {
                    //
                    canvas.drawColor(Color.WHITE);
                    //
                    drawXY(canvas, axisX, axisY);
                    //
                    drawOrTrack(canvas, i);
                    //
                    drawMovingTrack(canvas);

                }

            }
        } catch (Exception e) {

        } finally {

            if (holder != null && canvas != null)
                holder.unlockCanvasAndPost(canvas);
        }

    }

    /**
     * x,y
     *
     * @param canvas
     */
    private void drawXY(Canvas canvas, float cx, float cy) {

        float lx = 0;
        float ly = cy - getoR();
        //动圆圆心所在的轨迹
        //radius line
        canvas.drawLine(lx, ly, screenWidth, ly, drawXyPaint);
        //x
        canvas.drawLine(lx, cy, screenWidth, cy, drawXyPaint);
        //y
        canvas.drawLine(cx, lx, cx, screenHeight, drawXyPaint);


//        String xy = String.format("(x=%s,y=%s)", cx, cy);
//        DebugLog.i(LOGTAG, "xy:" + xy);
//        canvas.drawText(xy, cx - 150, cy + 50, drawTextPaint);

//        x += radius;
//        String xy1 = String.format("(x=%s,y=%s)", x, y);
//        canvas.drawCircle(x, y, 5, radiusPaint);
//        canvas.drawText(xy1, x - 20, y + 20, drawTextPaint);
    }


    /**
     * draw Or circle
     * 母圆（动圆） 半径r
     *
     * @param canvas
     */
    private void drawOrCircle(Canvas canvas, float cx, float cy, float radius) {

        Path path = new Path();
        path.addCircle(cx, cy, radius, Path.Direction.CCW);
        canvas.drawPath(path, drawPaint);

    }

    /**
     * draw OR circle
     * 基圆（定圆） 半径r
     *
     * @param canvas
     */
    private void drawORCircle(Canvas canvas, float x, float y, float radius) {

        Path path = new Path();
        path.addCircle(x, y, radius, Path.Direction.CCW);

        canvas.drawPath(path, drawPaint);
        //圆心
        canvas.drawCircle(x, y, 5, radiusPaint);

    }

    /**
     * 计算轨迹
     *
     * @param axisX
     * @param axisY
     * @param OR
     * @param l
     */
    private void calculateNewTrack(float axisX, float axisY, float OR, float l) {

        float cy = axisY - OR;
        float degrees = 0f;
        for (int i = 0; i < divisor; i++) {
            if (degrees > 12.4) break;
            TrackRadiusPoint trackRadius = new TrackRadiusPoint();
            //
            trackRadius.cx = degrees * OR + axisX;

//            trackRadius.cy = cy + OR / 2 + 15;
            trackRadius.cy = cy;
            //
            trackRadius.t = degrees;
            trackRadius.radius = OR;
            trackRadius.lr = l;
            trackRadiusList.add(trackRadius);
            //
            calculateTrackPoint(axisX, axisY, OR, l, degrees);
            degrees += 0.025;
        }


    }


    /**
     * 计算动点轨迹
     *
     * @param axisX 坐标轴
     * @param axisY 坐标轴
     * @param oR    定圆半径
     * @param l     动点距or的距离
     * @param t     角度
     */
    private void calculateTrackPoint(float axisX, float axisY, float oR, float l, float t) {
        //x=Rt-lsint
        //y=R-lcost
        //动点
        float x = (float) (oR * t - l * Math.sin(t));
        float y = (float) (oR - l * Math.cos(t));
        //
        float X = axisX + x;
        float Y = axisY - y;
        DebugLog.i(LOGTAG, "X:" + X);
        DebugLog.i(LOGTAG, "Y:" + Y);
        //(R-r)*cost
        //(R-r)*sint
        TrackPoint point = new TrackPoint();
        point.x = X;
        point.y = Y;
        point.t = t;
        trackPointList.add(point);

    }


    /**
     * 动圆 Or的轨道
     * 动圆圆心在此轨道上
     *
     * @param canvas
     * @param x        轨道圆心x
     * @param y        轨道圆心y
     * @param radius   轨道圆半径
     * @param radiusOR 动圆半径
     */
    /**
     * @param canvas
     */
    private void drawOrTrack(Canvas canvas, int position) {

        TrackRadiusPoint radius = trackRadiusList.get(position);
        //半径R
        drawOrCircle(canvas, radius.cx, radius.cy, radius.radius);
        //同心圆半径l
        drawOrCircle(canvas, radius.cx, radius.cy, radius.lr);
        //
        canvas.drawCircle(radius.cx, radius.cy, 10, drawXyPaint);

    }

    /**
     * 基圆（定圆）半径R
     * 母圆（动圆）半径r
     * 动点轨迹的横纵坐标公式：
     * X=(R-r)cost+lcos(R/r-1)t
     * Y=(R-r)sint-lsin(R/r-1)t
     * t为随着Or转动，Or圆心相对于x轴张开的角度。
     * r可以大于R，l可以大于r
     */
//    /**
//     * @param canvas
//     * @param axisX  坐标轴X
//     * @param axisY  坐标轴Y
//     * @param oR     基圆（定圆）半径R
//     * @param or     母圆（动圆）半径r
//     * @param l      动点距母圆圆心的距离l
//     * @param t      t为随着Or转动
//     */
    private void drawMovingTrack(Canvas canvas) {

        for (int i = 0; i < index; i++) {
            TrackPoint point = trackPointList.get(i);
//            SystemClock.sleep(10);
            canvas.drawCircle(point.x, point.y, 2, radiusPaint);
        }


    }

    private void drawMovingORTrack(Canvas canvas) {

//        TrackPoint point = trackPointList.get(position);
//        SystemClock.sleep(10);
//        canvas.drawCircle(point.x, point.y, 10, radiusPaint);
//        int count = trackPointList.size();
        for (int i = 0; i < trackRadiusList.size(); i++) {
            TrackRadiusPoint point = trackRadiusList.get(i);
//            SystemClock.sleep(10);
            canvas.drawCircle(point.cx, point.cy, 3, radiusPaint);
        }


    }

}
