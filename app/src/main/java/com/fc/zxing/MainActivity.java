package com.fc.zxing;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import fc.com.zxing.core.encode.QRCodeEncoder;
import fc.com.zxing.core.view.FcScanView;

import com.google.zxing.Result;
import com.google.zxing.WriterException;


public class MainActivity extends ActionBarActivity implements FcScanView.FcScanListener {
    private FcScanView fcScanView;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fcScanView = (FcScanView) findViewById(R.id.fc_scan_view);
        fcScanView.setFcScanListener(this);
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

    @Override
    public void onHandleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            Toast.makeText(this, rawResult.getBarcodeFormat() + "; " + rawResult.getText(), Toast.LENGTH_SHORT).show();
            fcScanView.restartPreviewAfterDelay(1000);
        }
    }
}
