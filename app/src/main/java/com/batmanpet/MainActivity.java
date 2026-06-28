package com.batmanpet;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ = 1234;
    private boolean serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnToggle = findViewById(R.id.btnToggle);
        TextView tvPermission = findViewById(R.id.tvPermission);

        btnToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                tvPermission.setText("Grant 'Display over other apps' permission first!");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ);
            } else {
                toggleBatman(btnToggle, tvPermission);
            }
        });
    }

    private void toggleBatman(Button btn, TextView tv) {
        if (!serviceRunning) {
            Intent service = new Intent(this, BatmanOverlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
            btn.setText("DISMISS BATMAN");
            tv.setText("Batman is watching over you 🦇");
            tv.setTextColor(0xFF27AE60);
            serviceRunning = true;
        } else {
            Intent service = new Intent(this, BatmanOverlayService.class);
            stopService(service);
            btn.setText("SUMMON BATMAN");
            tv.setText("");
            serviceRunning = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            Button btn = findViewById(R.id.btnToggle);
            TextView tv = findViewById(R.id.tvPermission);
            if (Settings.canDrawOverlays(this)) {
                tv.setText("");
                toggleBatman(btn, tv);
            } else {
                tv.setText("Permission denied. Batman cannot appear without it.");
            }
        }
    }
}
