#### 本项目是优秀的Zxing项目一个android的简化版，去掉了不常用的功能，添加了一个自定义View

使用如下：

1. 使用FcScanView
```
<fc.com.zxing.core.view.FcScanView
            android:id="@+id/fc_scan_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            scan:vibrate="false"
            scan:playBeep="true"
            scan:invertScan="false"
            scan:frontLightMode="off"
            scan:autoFocus="true"
            scan:decodeFormat="decode_PDF417|decode_QR"
            scan:frameScale="0.63"/>
```
2.在Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fcScanView = (FcScanView) findViewById(R.id.fc_scan_view);
        fcScanView.setFcScanListener(new FcScanView.FcScanListener() {
            @Override
            public void onHandleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
                //do something...
            }
        });
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
