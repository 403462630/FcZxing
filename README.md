#### FcZxing 是对优秀的Zxing项目进行简化而来的code，去掉了一些不常用的功能，添加了一些自己的实现
使用方法：
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
2.用FcScanView添加FcScanListener即可
