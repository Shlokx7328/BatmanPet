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
        "I am the night.",
        "Focus on your work.",
        "The Dark Knight watches.",
        "Criminals are cowardly.",
        "Why so serious?",
        "Gotham needs me.",
        "Justice never sleeps.",
        "I don't need sleep.",
        "Go finish your assignment!",
        "Na na na na... Batman!",
        "Stay focused.",
        "Even heroes study."
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        currentX = screenWidth - 200;
        currentY = 200;
        targetX = currentX;
        targetY = currentY;
        setupOverlay();
        startBobbing();
        startWandering();
        scheduleRandomQuote();
    }

    private void setupOverlay() {
        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = (int) currentX;
        params.y = (int) currentY;
        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_batman, null);
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private float lastTouchX, lastTouchY;
            private boolean dragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouchX = event.getRawX();
                        lastTouchY = event.getRawY();
                        dragOffsetX = params.x;
                        dragOffsetY = params.y;
                        dragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - lastTouchX;
                        float dy = event.getRawY() - lastTouchY;
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) dragging = true;
                        if (dragging) {
                            params.x = (int)(dragOffsetX + (event.getRawX() - lastTouchX));
                            params.y = (int)(dragOffsetY + (event.getRawY() - lastTouchY));
                            currentX = params.x;
                            currentY = params.y;
                            targetX = currentX;
                            targetY = currentY;
                            try { windowManager.updateViewLayout(overlayView, params); } catch (Exception e) {}
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragging) showRandomQuote();
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(overlayView, params);
    }

    private void startBobbing() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (overlayView == null) return;
                bobAngle += 0.1f;
                float bobOffset = (float) Math.sin(bobAngle) * 6f;
                try {
                    params.y = (int)(currentY + bobOffset);
                    windowManager.updateViewLayout(overlayView, params);
                } catch (Exception e) {}
                handler.postDelayed(this, 40);
            }
        }, 40);
    }

    private void startWandering() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (overlayView == null) return;
                targetX = 50 + random.nextInt(Math.max(1, screenWidth - 200));
                targetY = 100 + random.nextInt(Math.max(1, screenHeight - 400));
                animateToTarget();
                handler.postDelayed(this, 6000 + random.nextInt(6000));
            }
        }, 4000);
    }

    private void animateToTarget() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (overlayView == null) return;
                float dx = targetX - currentX;
                float dy = targetY - currentY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 4f) {
                    float speed = Math.min(dist * 0.06f, 12f);
                    currentX += (dx / dist) * speed;
                    currentY += (dy / dist) * speed;
                    try {
                        params.x = (int) currentX;
                        params.y = (int) currentY;
                        windowManager.updateViewLayout(overlayView, params);
                    } catch (Exception e) {}
                    handler.postDelayed(this, 30);
                }
            }
        });
    }

    private void scheduleRandomQuote() {
        long delay = 45000 + random.nextInt(45000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showRandomQuote();
                scheduleRandomQuote();
            }
        }, delay);
    }

    private void showRandomQuote() {
        if (overlayView == null) return;
        LinearLayout bubble = overlayView.findViewById(R.id.speechBubble);
        TextView tvSpeech = overlayView.findViewById(R.id.tvSpeech);
        String quote = BATMAN_QUOTES[random.nextInt(BATMAN_QUOTES.length)];
        tvSpeech.setText(quote);
        bubble.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bubble != null) bubble.setVisibility(View.GONE);
            }
        }, 3000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_desc));
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Batman is watching")
                .setContentText("Tap to open Batman Pet")
                .setSmallIcon(android.R.drawable.star_on)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (overlayView != null) {
            try { windowManager.removeView(overlayView); } catch (Exception e) {}
            overlayView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
              }
