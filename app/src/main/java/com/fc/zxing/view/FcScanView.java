package com.fc.zxing.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.fc.zxing.AmbientLightManager;
import com.fc.zxing.BeepManager;
import com.fc.zxing.PreferencesActivity;
import com.fc.zxing.R;
import com.fc.zxing.camera.CameraManager;
import com.fc.zxing.decode.CaptureActivityHandler;
import com.fc.zxing.decode.FcScanHandler;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by rjhy on 15-4-24.
 */
public class FcScanView extends FrameLayout implements SurfaceHolder.Callback{
    private static final String TAG = "FcScanView";
    private SurfaceView surfaceView;

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    private ViewfinderView viewfinderView;

    private boolean initialized;
    private boolean hasSuface;
    private CameraManager cameraManager;
    private FcScanHandler handler;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public void setDecodeHints(Map<DecodeHintType, ?> decodeHints) {
        this.decodeHints = decodeHints;
    }

    public void setDecodeFormats(Collection<BarcodeFormat> decodeFormats) {
        this.decodeFormats = decodeFormats;
    }

    public FcScanView(Context context) {
        this(context, null);
    }

    public FcScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FcScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (getChildCount() > 0) {
            throw new IllegalArgumentException("ScanView can not child view");
        }
        LayoutInflater.from(context).inflate(R.layout.capture, this, true);

        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);
    }

    public void initOnActivity(Activity activity) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(getContext());
            viewfinderView.setCameraManager(cameraManager);
        }
        if (beepManager == null) {
            beepManager = new BeepManager(activity);
        }
        if (ambientLightManager == null) {
            ambientLightManager = new AmbientLightManager(getContext());
        }
        initialized = true;
    }

    public void start() {
        if (!initialized) {
            throw new IllegalArgumentException("ScanView should initOnActivity(Activity activity)");
        }
        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);
        if (!hasSuface) {
            surfaceView.getHolder().addCallback(this);
        } else {
            initCamera(surfaceView.getHolder());
        }
    }

    public void release() {
        beepManager.close();
        ambientLightManager.stop();
        handler.quitSynchronously();
        cameraManager.closeDriver();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (surfaceHolder == null) {
                throw new IllegalStateException("No SurfaceHolder provided");
            }
            if (cameraManager.isOpen()) {
                Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
                return ;
            }
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler =  new FcScanHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.msg_camera_framework_bug, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//        inactivityTimer.onActivity();

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            beepManager.playBeepSoundAndVibrate();
            Toast.makeText(getContext(), rawResult.getBarcodeFormat() + "; " + rawResult.getText(), Toast.LENGTH_SHORT).show();
            restartPreviewAfterDelay(1000);
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(PreferencesActivity.RESTART_PREVIEW, delayMS);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSuface) {
            hasSuface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSuface = false;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setTorch(boolean isTorch) {
        cameraManager.setTorch(isTorch);
    }

}
