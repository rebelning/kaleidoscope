package com.doscope.kalei;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class kaleidoscope2Activity extends AppCompatActivity {
    private Kaleidoscope2View kale2View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setStatusBarHide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaleidoscope2);

        kale2View = findViewById(R.id.activity_kaleidoscope_kale2View);

        initData();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras().getBundle("params");
//        bundle.putFloat("R", R);
//        bundle.putFloat("l", l);
        float oR = bundle.getFloat("R");
        float l = bundle.getFloat("l");

        kale2View.setoR(oR);
        kale2View.setOl(l);

    }

    private void stop() {
        kale2View.setRunning(false);
    }

    private void start() {
        kale2View.setRunning(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    protected void setStatusBarHide() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//before Jelly Bean Versions{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(Color.TRANSPARENT);

            }
        } else { // Jelly Bean and up
            // Hide the status bar.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        } else {

            return super.onKeyDown(keyCode, event);
        }
    }
}
