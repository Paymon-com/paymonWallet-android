package ru.paymon.android.activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;

import ru.paymon.android.R;
import ru.paymon.android.components.PointsOverlayView;

public class QrCodeScannerActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {
    public static final int REQUEST_CODE_QR_SCANNER_START = 1;
    public static final String QR_SCAN_RESULT_KEY = "QR_SCAN_RESULT_KEY";
    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;

    private ViewGroup mainLayout;

    private QRCodeReaderView qrCodeReaderView;
    private PointsOverlayView pointsOverlayView;

    private ImageView activateScanner;
    private ImageView activateFlash;

    private boolean flashIsActivated = false;
    private boolean scanIsActivated = true;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qr_scanner);

        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
    }

    @Override protected void onResume() {
        super.onResume();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override protected void onPause() {
        super.onPause();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mainLayout, R.string.qr_scanner_camera_permission_was_granted, Snackbar.LENGTH_SHORT).show();
            initQRCodeReaderView();
        } else {
            Snackbar.make(mainLayout, R.string.qr_scanner_camera_permission_request_was_denied, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override public void onQRCodeRead(String text, PointF[] points) {
        pointsOverlayView.setPoints(points);
        Intent intent = new Intent();
        intent.putExtra(QR_SCAN_RESULT_KEY, text);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(mainLayout, R.string.qr_scanner_camera_access_is_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.other_ok, view -> ActivityCompat.requestPermissions(QrCodeScannerActivity.this, new String[]{
                            Manifest.permission.CAMERA
                    }, MY_PERMISSION_REQUEST_CAMERA)).show();
        } else {
            Snackbar.make(mainLayout, R.string.qr_scanner_permission_is_not_available,
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        }
    }

    private void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.qr_scanner_view, mainLayout, true);

        qrCodeReaderView = (QRCodeReaderView) content.findViewById(R.id.qrdecoderview);
        activateScanner = (ImageView) content.findViewById(R.id.qr_scanner_scanner_on);
        activateScanner.setImageResource(R.drawable.ic_qr_scanner_on);

        activateFlash = (ImageView) content.findViewById(R.id.qr_scanner_flash_on);
        pointsOverlayView = (PointsOverlayView) content.findViewById(R.id.points_overlay_view);

        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();

        activateScanner.setOnClickListener(view -> {

            if (scanIsActivated) {
                scanIsActivated = false;
                activateScanner.setImageResource(R.drawable.ic_qr_scanner_off);
            } else {
                scanIsActivated = true;
                activateScanner.setImageResource(R.drawable.ic_qr_scanner_on);
            }

            qrCodeReaderView.setQRDecodingEnabled(scanIsActivated);

        });

        activateFlash.setOnClickListener(view -> {


            if (flashIsActivated) {
                flashIsActivated = false;
                activateFlash.setImageResource(R.drawable.ic_flash_off);
            } else {
                flashIsActivated = true;
                activateFlash.setImageResource(R.drawable.ic_flash_on);

            }

            qrCodeReaderView.setTorchEnabled(flashIsActivated);


        });

        qrCodeReaderView.startCamera();
    }
}