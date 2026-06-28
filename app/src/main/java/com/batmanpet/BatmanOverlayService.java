package com.batmanpet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

import java.util.Random;

public class BatmanOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    private int screenWidth, screenHeight;
    private float targetX, targetY;
    private float currentX, currentY;

    private int dragOffsetX, dragOffsetY;
    private float bobAngle = 0f;

    private static final String CHANNEL_ID = "batman_pet_channel";

    private final String[] BATMAN_QUOTES = {
        "I am Batman.",
        "I'm the night.",
        "Focus on your work.",
        "The Dark Knight watches.",
        "Criminals are cowardly.",
        "Why so serious?",
        "Gotham needs me.",
        "Justice never sleeps.",
        "I don't need sleep.",
        "Go finish your assignment!",
        "With great power...\nwait, wrong hero.",
        "Na na na na... Batman!",
        "I've prepared for this.",
        "Stay focused.",
        "Even heroes study."
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
