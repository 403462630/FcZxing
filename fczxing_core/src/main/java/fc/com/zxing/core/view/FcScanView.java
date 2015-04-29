package fc.com.zxing.core.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import fc.com.zxing.core.AmbientLightManager;
import fc.com.zxing.core.BeepManager;
import fc.com.zxing.core.FcPreferences;
import fc.com.zxing.core.R;
import fc.com.zxing.core.camera.CameraManager;
import fc.com.zxing.core.camera.FrontLightMode;
import fc.com.zxing.core.decode.DecodeFormatManager;
import fc.com.zxing.core.decode.FcScanHandler;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
    private boolean hasSurface;
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
        if (cameraManager == null) {
            cameraManager = new CameraManager(getContext());
            viewfinderView.setCameraManager(cameraManager);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FcScanView);
        boolean autoFocus = a.getBoolean(R.styleable.FcScanView_autoFocus, true);
        boolean vibrate = a.getBoolean(R.styleable.FcScanView_vibrate, false);
        boolean playBeep = a.getBoolean(R.styleable.FcScanView_playBeep, true);
        boolean invertScan = a.getBoolean(R.styleable.FcScanView_invertScan, false);
        int frontLightMode = a.getInt(R.styleable.FcScanView_frontLightMode, 1);
        int decodeFormats = a.getInt(R.styleable.FcScanView_decodeFormat, 15);
        a.recycle();

        if ((decodeFormats & 0x20) == 32) {
            addDecodeFormats(DecodeFormatManager.PDF417_FORMATS);
        }
        if ((decodeFormats & 0x10) == 16) {
            addDecodeFormats(DecodeFormatManager.AZTEC_FORMATS);
        }
        if ((decodeFormats & 0x08) == 8) {
            addDecodeFormats(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        if ((decodeFormats & 0x04) == 4) {
            addDecodeFormats(DecodeFormatManager.QR_CODE_FORMATS);
        }
        if ((decodeFormats & 0x02) == 2) {
            addDecodeFormats(DecodeFormatManager.INDUSTRIAL_FORMATS);
        }
        if ((decodeFormats & 0x01) == 1) {
            addDecodeFormats(DecodeFormatManager.PRODUCT_FORMATS);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        FcPreferences.setAutoFocus(prefs, autoFocus);
        FcPreferences.setFrontLightMode(prefs, FrontLightMode.values()[frontLightMode]);
        FcPreferences.setInvertScan(prefs, invertScan);
        FcPreferences.setVibrate(prefs, vibrate);
        FcPreferences.setPlayBeep(prefs, playBeep);
    }

    private void addDecodeFormats(Set<BarcodeFormat> formats) {
        if (decodeFormats == null) {
            decodeFormats = new ArrayList<>();
        }
        decodeFormats.addAll(formats);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        Point point = new Point(width, height);
        cameraManager.setPreviewFramingRect(point);
    }

    public void initOnActivity(Activity activity) {
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
        if (!hasSurface) {
            surfaceView.getHolder().addCallback(this);
        } else {
            initCamera(surfaceView.getHolder());
        }
    }

    public void release() {
        beepManager.close();
        ambientLightManager.stop();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
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
            handler.sendEmptyMessageDelayed(FcPreferences.RESTART_PREVIEW, delayMS);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setTorch(boolean isTorch) {
        cameraManager.setTorch(isTorch);
    }

}
