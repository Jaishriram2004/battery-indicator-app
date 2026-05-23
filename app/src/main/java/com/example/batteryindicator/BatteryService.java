package com.example.batteryindicator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class BatteryService extends Service {

    int lowValue = 20;
    int highValue = 80;

    boolean lowSpoken = false;
    boolean highSpoken = false;

    TextToSpeech tts;

    BroadcastReceiver batteryReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        android.content.Context context,
                        Intent intent
                ) {

                    int level = intent.getIntExtra(
                            BatteryManager.EXTRA_LEVEL,
                            -1
                    );

                    // LOW BATTERY ALERT
                    if (level <= lowValue && !lowSpoken) {

                        speak("Low Battery");

                        lowSpoken = true;
                        highSpoken = false;
                    }

                    // HIGH BATTERY ALERT
                    else if (level >= highValue
                            && !highSpoken) {

                        speak("Battery Full");

                        highSpoken = true;
                        lowSpoken = false;
                    }

                    // RESET LOW ALERT
                    if (level > lowValue) {

                        lowSpoken = false;
                    }

                    // RESET HIGH ALERT
                    if (level < highValue) {

                        highSpoken = false;
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        // LOAD SAVED VALUES
        SharedPreferences prefs =
                getSharedPreferences(
                        "BatteryPrefs",
                        MODE_PRIVATE
                );

        lowValue = prefs.getInt("LOW", 20);
        highValue = prefs.getInt("HIGH", 80);

        // TEXT TO SPEECH
        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                tts.setLanguage(Locale.US);
            }
        });

        // FOREGROUND NOTIFICATION
        createNotification();

        // BATTERY RECEIVER
        registerReceiver(
                batteryReceiver,
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                )
        );
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {

        if (intent != null) {

            lowValue = intent.getIntExtra(
                    "LOW",
                    lowValue
            );

            highValue = intent.getIntExtra(
                    "HIGH",
                    highValue
            );
        }

        return START_STICKY;
    }

    private void speak(String text) {

        if (tts != null) {

            tts.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
            );
        }
    }

    private void createNotification() {

        String channelId = "battery_alert";

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Battery Alert",
                            NotificationManager
                                    .IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(
                    channel
            );
        }

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        channelId
                )
                        .setContentTitle(
                                "Battery Alert Running"
                        )
                        .setContentText(
                                "Monitoring battery level"
                        )
                        .setSmallIcon(
                                android.R.drawable
                                        .ic_lock_idle_charging
                        )
                        .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(batteryReceiver);

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}