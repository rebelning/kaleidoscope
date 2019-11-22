package com.doscope.kalei;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description
 * Created by shaxiaoning
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
 * 圆在圆里滚那个，动圆按圆心横坐标(R-r)*cost，圆心纵坐标(R-r)*sint，半径r画圆，这样圆的滚动就会和曲线同步
 */
public class KaleidoscopeView1 extends SurfaceView implements SurfaceHolder.Callback {
    private static final String LOGTAG = LogUtil.makeLogTag(KaleidoscopeView1.class);
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
    private Path drawPath;
    private Xfermode mClearMode;
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

    private int position;

    private TrackPoint trackPoint;

    class TrackRadiusPoint {
        float cx;
        float cy;
        float t;
    }

    class TrackPoint {
        float x;
        float y;
        float t;
    }

    public KaleidoscopeView1(Context context) {
        super(context);
        init(context);
    }

    public KaleidoscopeView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KaleidoscopeView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context mContext) {
        holder = getHolder();
        holder.addCallback(this);

        drawPath = new Path();
        drawPaint = initPaint(brushSize, paintColor, 0, 0);
        drawTrack = initPaint(brushSize, radiusPaintColor, 0, 0);

        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
//        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //
//        trackRadiusList = new ArrayList<>();
        //
        isRunning = true;
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
        invalidate();
        return true;

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
//                DebugLog.i(LOGTAG, "isRunning....");
                drawBackground();
                float cx = screenWidth / 2;
                float cy = screenHeight / 2;

                //动圆圆心所在的圆
//                float radiusOT = screenWidth / 6;
                float radiusOT = screenWidth / 6;
                //定圆半径
                float radiusOR = screenWidth / 8;
                DebugLog.i(LOGTAG, "radiusOR:" + radiusOR);
                //动圆半径
//                float radiusOr = radiusOT + radiusOR;
//                float radiusOr = radiusOT + radiusOR / 2;
                float radiusOr = radiusOT + radiusOR / 4;
                DebugLog.i(LOGTAG, "radiusOr:" + radiusOr);
                float l = radiusOR / 2;
                DebugLog.i(LOGTAG, "l:" + l);
                //动圆圆心
//                calculateTrack(cx, cy, radiusOT);

                calculateNewTrack(cx, cy, radiusOT, radiusOR, radiusOr, l);
//                for (int i = 0; i < trackRadiusList.size(); i++) {
//                    if (isRunning)
//                        draw(i);
////                    break;
////                    SystemClock.sleep(1/10000);
//                }
                sort();
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
//    private void getTouchXY() {
//        int count=trackPointList.size();
//
//        for (int i=0;i<count;i++){
//
//        }
//    }

    private void sort() {
        Collections.sort(trackPointList, new Comparator<TrackPoint>() {
            @Override
            public int compare(TrackPoint o1, TrackPoint o2) {
                float diff = o2.t - o1.t;
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0; //相等为0
            }
        });
    }

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
                    canvas.drawColor(Color.WHITE);
//                canvas.translate(screenWidth / 2, screenHeight / 2);//坐标系
                    float cx = screenWidth / 2;
                    float cy = screenHeight / 2;

                    //动圆圆心所在的圆
                    float radiusOT = screenWidth / 6;
                    //定圆半径
                    float radiusOR = screenWidth / 8;

                    //动圆半径
                    float radiusOr = radiusOT + radiusOR;
//                    float radiusOr = radiusOT ;

                    //动圆圆心
//                   calculateTrack(cx, cy, radiusOT);
                    //xy
                    drawXY(canvas, cx, cy);
                    //OR 定圆
                    drawORCircle(canvas, cx, cy, radiusOR);

                    //Or 动圆
                    //动圆Or的轨道
                    drawOrTrack(canvas, i, radiusOr);

                    drawMovingTrack(canvas, i);
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
        //x
//        float radius = screenWidth / 2;
        canvas.drawLine(0, cy, screenWidth, cy, drawXyPaint);
        //y

        canvas.drawLine(cx, 0, cx, screenHeight, drawXyPaint);

        String xy = String.format("(x=%s,y=%s)", cx, cy);
        DebugLog.i(LOGTAG, "xy:" + xy);
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

    private float currentValue1 = 0.0f;

    /**
     * 圆在圆里滚那个，动圆按圆心横坐标(R-r)*cost，圆心纵坐标(R-r)*sint，半径r画圆，这样圆的滚动就会和曲线同步
     *
     * @param x
     * @param y
     * @param OT
     * @param OR
     * @param Or
     * @param l
     */
    private void calculateNewTrack(float x, float y, float OT, float OR, float Or, float l) {
        Path path = new Path();
        path.addCircle(x, y, OT, Path.Direction.CCW);
        PathMeasure pathMeasure = new PathMeasure(path, true);
//        int divisor = 4;
        float degrees = 0.1f;
        for (int i = 0; i < divisor; i++) {
            position = i;
            float[] pos = new float[]{0f, 0f};
            float[] tan = new float[]{0f, 0f};
            currentValue1 += 0.005;                                  // 计算当前的位置在总长度上的比例[0,1]
            if (currentValue1 >= 1) {
                currentValue1 = 0;
            }
//            pathMeasure.getPosTan((i) * pathMeasure.getLength() / divisor, pos, tan);
            pathMeasure.getPosTan(pathMeasure.getLength() * currentValue1, pos, tan);

            float cx = pos[0];
            float cy = pos[1];
            degrees += 0.03;
//            DebugLog.i(LOGTAG, "degrees-1:" + degrees);
            float tx = (float) ((OR - Or) * Math.cos(degrees));
            float ty = (float) ((OR - Or) * Math.sin(degrees));

            float X = x - tx;
            float Y = x + ty;
            TrackRadiusPoint trackRadius = new TrackRadiusPoint();
            trackRadius.cx = cx;
            trackRadius.cy = cy;
//            trackRadius.cx = X;
//            trackRadius.cy = Y;
            trackRadius.t = degrees;
            trackRadiusList.add(trackRadius);
            calculateTrackPoint(x, y, OR, Or, l, degrees);

        }
    }


    /**
     * @param axisX 坐标轴
     * @param axisY 坐标轴
     * @param oR    定圆半径
     * @param or    动圆半径
     * @param l     动点距or的距离
     * @param t     角度
     */
//    private void calculateTrackPoint(float axisX, float axisY, float oR, float or, float l, float t) {
    private void calculateTrackPoint(float axisX, float axisY, float oR, float or, float l, float t) {

        float x = (float) ((oR - or) * Math.cos(t) + l * Math.cos((oR / or - 1) * t));
        float y = (float) ((oR - or) * Math.sin(t) - l * Math.sin((oR / or - 1) * t));

//        DebugLog.i(LOGTAG, "x:" + x);
//        DebugLog.i(LOGTAG, "y:" + y);
//        DebugLog.i(LOGTAG, "T:" + t);


        float X = axisX - x;
        float Y = axisY - y;

//        DebugLog.i(LOGTAG, "X:" + X);
//        DebugLog.i(LOGTAG, "Y:" + Y);
//        DebugLog.i(LOGTAG, "==============================");
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
     * @param radiusOr 动圆半径
     */
    private void drawOrTrack(Canvas canvas, int position,
                             float radiusOr) {

        TrackRadiusPoint radius = trackRadiusList.get(position);
        drawOrCircle(canvas, radius.cx, radius.cy, radiusOr);
        //动点
//        radius.t = 30;
//        radius.t = 0;
//        drawMovingTrack(canvas, axisX, axisY, radiusOR, radiusOr, l, radius.t);

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
    private void drawMovingTrack(Canvas canvas, int position) {

//        TrackPoint point = trackPointList.get(position);
//        SystemClock.sleep(10);
//        canvas.drawCircle(point.x, point.y, 10, radiusPaint);
//        int count = trackPointList.size();
        for (int i = 0; i < index; i++) {
            TrackPoint point = trackPointList.get(i);
//            SystemClock.sleep(10);
            canvas.drawCircle(point.x, point.y, 2, radiusPaint);
        }


    }


}
