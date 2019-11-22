package com.doscope.kalei;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class kaleidoscopeActivity extends AppCompatActivity {
    private KaleidoscopeView kaleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setStatusBarHide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaleidoscope);
        kaleView = findViewById(R.id.activity_kaleidoscope_kaleView);


        initData();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras().getBundle("params");
//        bundle.putFloat("Or", Or);
//        bundle.putFloat("OR", OR);
//        bundle.putFloat("l", l);
        float or = bundle.getFloat("Or");
        float oR = bundle.getFloat("OR");
        float l = bundle.getFloat("l");

        kaleView.setOrRadius(or);
        kaleView.setoRRadius(oR);
        kaleView.setOrl(l);
//        kaleView.setRunning(true);
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

    private void stop() {
        kaleView.setRunning(false);
    }

    private void start() {
        kaleView.setRunning(true);
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
