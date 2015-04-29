package com.fc.zxing;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import fc.com.zxing.core.encode.QRCodeEncoder;
import fc.com.zxing.core.view.FcScanView;

import com.google.zxing.WriterException;


public class MainActivity extends ActionBarActivity {
    private FcScanView fcScanView;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fcScanView = (FcScanView) findViewById(R.id.fc_scan_view);
        fcScanView.initOnActivity(this);
        imageView = (ImageView) findViewById(R.id.iv_image);

        findViewById(R.id.bt_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder("QRCodeEncoder", 600);
                try {
                    imageView.setImageBitmap(qrCodeEncoder.encodeAsBitmap());
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
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
