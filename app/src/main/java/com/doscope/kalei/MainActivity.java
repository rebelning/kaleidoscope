package com.doscope.kalei;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOGTAG = LogUtil.makeLogTag(MainActivity.class);
    private TextView btnKalei1;
    private TextView btnKalei2;
    //定圆半径
    private SeekBar seekBarRadiusOR;
    //动圆半径
    private SeekBar seekBarRadiusOr;
    //点到动圆周距离
    private SeekBar seekBarOrl;

    private SeekBar seekBarR;
    private SeekBar seekBarRadiusl;

    private TextView txtOR;
    private TextView txtOr;
    private TextView txtOrl;
    private TextView txtR;
    private TextView txtRadiusl;

    private int screenWidth;
    private int screenHeight;

    private float OrProgress = 0f;//动圆半径
    private float ORProgress = 0f;//定圆半径
    private float lProgress = 0f;//点到动圆周距离
    //
    private float rProgress = 0f;//圆半径
    private float rlProgress = 0f;//点到圆心距离

    //单位
    private float unit;
    private float lineUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {
        btnKalei1 = findViewById(R.id.activity_main_btn_kaleidoscope1);
        btnKalei2 = findViewById(R.id.activity_main_btn_kaleidoscope2);
        btnKalei1.setOnClickListener(this);
        btnKalei2.setOnClickListener(this);

        txtOR = findViewById(R.id.activity_main_txt_OR);
        txtOr = findViewById(R.id.activity_main_txt_Or);
        txtOrl = findViewById(R.id.activity_main_txt_Orl);
        txtR = findViewById(R.id.activity_main_txt_R);
        txtRadiusl = findViewById(R.id.activity_main_txt_radiusl);


        //以圆为基线-参数配置
        seekBarRadiusOR = findViewById(R.id.activity_main_seekbar_OR);
        seekBarRadiusOr = findViewById(R.id.activity_main_seekbar_Or);
        seekBarOrl = findViewById(R.id.activity_main_seekbar_Orl);

        //以直线为基线-参数配置
        seekBarR = findViewById(R.id.activity_main_seekbar_R);
        seekBarRadiusl = findViewById(R.id.activity_main_seekbar_radiusl);

        //
        seekBarRadiusOR.setOnSeekBarChangeListener(seekBarORListener);
        seekBarRadiusOr.setOnSeekBarChangeListener(seekBarOrListener);
        seekBarOrl.setOnSeekBarChangeListener(seekBarOrlListener);
        //
        seekBarR.setOnSeekBarChangeListener(seekBarRListener);
        seekBarRadiusl.setOnSeekBarChangeListener(seekBarRadiuslListener);


        getDefaultParams();
    }

    private void getDefaultParams() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        DebugLog.i(LOGTAG, "screenWidth:" + screenWidth);
        DebugLog.i(LOGTAG, "screenHeight:" + screenHeight);
        //定圆圆心
        float cx = screenWidth / 2;
        float cy = screenHeight / 2;
        DebugLog.i(LOGTAG, "cx:" + screenWidth);
        DebugLog.i(LOGTAG, "cy:" + screenHeight);
        //动圆圆心所在的圆
        float radiusOT = screenWidth / 6;
        //定圆半径
        float radiusOR = screenWidth / 8;//80-160
        DebugLog.i(LOGTAG, "radiusOR:" + radiusOR);
        //动圆半径
        float radiusOr = radiusOT + radiusOR / 4;//160-240
        DebugLog.i(LOGTAG, "radiusOr:" + radiusOr);
        //动点
        float l = radiusOR / 2;//40-160
        initUnit();
    }


    private void initUnit() {
        //最大半径
        float radius = screenHeight / 2;
        DebugLog.i(LOGTAG, "radius-max:" + radius);
        //360
        //单位
        unit = radius / 120;

        float axisY = screenHeight * 3 / 4;//

        lineUnit = axisY / 200;


    }

    /**
     * 以圆为基线
     */
    private Bundle getRadiusParams() {

        float Or = OrProgress * unit;//动圆半径
        float OR = ORProgress * unit;//定圆半径
        float l = lProgress * unit;//点到动圆周距离


        DebugLog.i(LOGTAG, "OR:" + OR);
        DebugLog.i(LOGTAG, "Or:" + Or);
        DebugLog.i(LOGTAG, "l:" + l);


        Bundle bundle = new Bundle();
        bundle.putFloat("Or", Or);
        bundle.putFloat("OR", OR);
        bundle.putFloat("l", l);


        return bundle;
    }

    /**
     * 以直线为基线
     */
    private Bundle getLineParams() {

        float R = rProgress * lineUnit;//圆半径
        float rl = rlProgress * lineUnit;//点到圆心距离


        Bundle bundle = new Bundle();
        bundle.putFloat("R", R);
        bundle.putFloat("l", rl);


        return bundle;

    }

    /**
     * 定圆半径
     *
     * @param progress
     */
    private void setORRadius(float progress) {
        float radius = progress * unit;
        String value = String.format("%.2f", radius);
        txtOR.setText("定圆半径:" + value);
    }

    /**
     * 动圆半径
     *
     * @param progress
     */
    private void setOrRadius(float progress) {
        float radius = progress * unit;
        String value = String.format("%.2f", radius);
        txtOr.setText("动圆半径:" + value);
    }

    /**
     * 点到动圆周距离
     *
     * @param progress
     */
    private void setOrlRadius(float progress) {
        float radius = progress * unit;
        String value = String.format("%.2f", radius);
        txtOrl.setText("点到动圆周距离:" + value);
    }

    /**
     * 圆半径
     *
     * @param progress
     */
    private void setRRadius(float progress) {
        float radius = progress * unit;
        String value = String.format("%.2f", radius);
        txtR.setText("圆半径:" + value);
    }

    /**
     * 点到圆心距离
     *
     * @param progress
     */
    private void setlRRadius(float progress) {
        float radius = progress * unit;
        String value = String.format("%.2f", radius);
        txtRadiusl.setText("点到圆心距离:" + value);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_btn_kaleidoscope1:
                onIntentKalei1();
                break;
            case R.id.activity_main_btn_kaleidoscope2:
                onIntentKalei2();
                break;
        }
    }

    private void onIntentKalei1() {
        Bundle bundle = getRadiusParams();
        Intent intent = new Intent(this, kaleidoscopeActivity.class);
        intent.putExtra("params", bundle);
        startActivity(intent);
    }

    private void onIntentKalei2() {
        Bundle bundle = getLineParams();
        Intent intent = new Intent(this, kaleidoscope2Activity.class);
        intent.putExtra("params", bundle);
        startActivity(intent);
    }

    /**
     * 定圆半径
     */
    private SeekBar.OnSeekBarChangeListener seekBarORListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ORProgress = progress;
            setORRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**
     * 动圆半径
     */
    private SeekBar.OnSeekBarChangeListener seekBarOrListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            OrProgress = progress;
            setOrRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**
     * 点到动圆周距离
     */
    private SeekBar.OnSeekBarChangeListener seekBarOrlListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            lProgress = progress;
            setOrlRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**
     * 圆的半径
     */
    private SeekBar.OnSeekBarChangeListener seekBarRListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            rProgress = progress;
            setRRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**
     * //点到圆心距离
     */
    private SeekBar.OnSeekBarChangeListener seekBarRadiuslListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            rlProgress = progress;
            setlRRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


}
