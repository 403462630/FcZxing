package com.fc.zxing;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.fc.zxing.view.FcScanView;


public class MainActivity extends ActionBarActivity {
    private FcScanView fcScanView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fcScanView = (FcScanView) findViewById(R.id.fc_scan_view);
        fcScanView.initOnActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fcScanView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fcScanView.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fcScanView.release();
    }
}
