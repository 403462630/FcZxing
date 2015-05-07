package com.fc.zxing;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import fc.com.zxing.core.encode.BarcodeEncoder;
import fc.com.zxing.core.view.FcScanView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements FcScanView.FcScanListener {
    private FcScanView fcScanView;
    private ImageView imageView;
    private ImageView imageView2;

    public BarcodeModel getOneDBarcodeModel() {
        if (one > oneD.size() - 1) {
            one = 0;
        }

        return oneD.get(one++);
    }

    public BarcodeModel getTwoDBarcodeModel() {
        if (two > twoD.size() - 1) {
            two = 0;
        }
        return twoD.get(two++);
    }

    private ArrayList<BarcodeModel> oneD = new ArrayList();
    private ArrayList<BarcodeModel> twoD = new ArrayList();
    private int one = 0;
    private int two = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fcScanView = (FcScanView) findViewById(R.id.fc_scan_view);
        fcScanView.setFcScanListener(this);
        fcScanView.initOnActivity(this);
        imageView = (ImageView) findViewById(R.id.iv_image);
        imageView2 = (ImageView) findViewById(R.id.iv_image2);
        initData();
        findViewById(R.id.bt_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    BarcodeModel model = getTwoDBarcodeModel();
                    BarcodeEncoder twoEncoder = new BarcodeEncoder(model.content, model.format, model.width, model.height);
                    imageView.setImageBitmap(twoEncoder.encodeAsBitmap());
                    Toast.makeText(getApplicationContext(), model.format.name(), Toast.LENGTH_SHORT).show();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.bt_encode2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BarcodeModel model = getOneDBarcodeModel();
                    BarcodeEncoder oneEncoder = new BarcodeEncoder(model.content, model.format, model.width, model.height);
                    imageView2.setImageBitmap(oneEncoder.encodeAsBitmap());
                    Toast.makeText(getApplicationContext(), model.format.name(), Toast.LENGTH_SHORT).show();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initData() {
        oneD.add(new BarcodeModel("CODE39", BarcodeFormat.CODE_39, 400, 200));
        oneD.add(new BarcodeModel("code128", BarcodeFormat.CODE_128, 400, 200));
        oneD.add(new BarcodeModel("12345678", BarcodeFormat.EAN_8, 400, 200));
        oneD.add(new BarcodeModel("0000000000000", BarcodeFormat.EAN_13, 400, 200));

        twoD.add(new BarcodeModel("QR_CODE", BarcodeFormat.QR_CODE, 500, 500));
        twoD.add(new BarcodeModel("DATA_MATRIX", BarcodeFormat.DATA_MATRIX, 500, 500));
        twoD.add(new BarcodeModel("AZTEC", BarcodeFormat.AZTEC, 500, 500));
        twoD.add(new BarcodeModel("PDF_417", BarcodeFormat.PDF_417, 500, 500));
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

    public static class BarcodeModel{
        public String content;
        public BarcodeFormat format;
        public int width;
        public int height;

        public BarcodeModel(String content, BarcodeFormat format, int width, int height) {
            this.content = content;
            this.format = format;
            this.width = width;
            this.height = height;
        }
    }
}
