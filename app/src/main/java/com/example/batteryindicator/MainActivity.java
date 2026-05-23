package com.example.batteryindicator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText lowBattery, highBattery;
    Button setButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lowBattery = findViewById(R.id.lowBattery);
        highBattery = findViewById(R.id.highBattery);
        setButton = findViewById(R.id.setButton);

        SharedPreferences prefs =
                getSharedPreferences(
                        "BatteryPrefs",
                        MODE_PRIVATE
                );

        // Load saved values
        int savedLow = prefs.getInt("LOW", 20);
        int savedHigh = prefs.getInt("HIGH", 80);

        boolean serviceEnabled =
                prefs.getBoolean(
                        "SERVICE_ENABLED",
                        true
                );

        lowBattery.setText(String.valueOf(savedLow));
        highBattery.setText(String.valueOf(savedHigh));

        setButton.setText(
                serviceEnabled
                        ? "UNSET"
                        : "SET"
        );

        PowerManager powerManager =
                (PowerManager)
                        getSystemService(
                                POWER_SERVICE
                        );

        if (!powerManager
                .isIgnoringBatteryOptimizations(
                        getPackageName()
                )) {

            Intent intent =
                    new Intent(
                            Settings
                                    .ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    );

            intent.setData(
                    Uri.parse(
                            "package:" + getPackageName()
                    )
            );

            startActivity(intent);
        }

        setButton.setOnClickListener(v -> {

            if (setButton.getText().toString()
                    .equals("UNSET")) {

                Intent stopIntent =
                        new Intent(
                                this,
                                BatteryService.class
                        );

                stopService(stopIntent);

                prefs.edit()
                        .putBoolean(
                                "SERVICE_ENABLED",
                                false
                        )
                        .apply();

                setButton.setText("SET");

                Toast.makeText(
                        this,
                        "Battery alerts disabled",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
            String low = lowBattery.getText().toString();
            String high = highBattery.getText().toString();

            if (low.isEmpty() || high.isEmpty()) {

                Toast.makeText(
                        this,
                        "Enter both values",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            int lowValue = Integer.parseInt(low);
            int highValue = Integer.parseInt(high);

            if (lowValue >= highValue) {

                Toast.makeText(
                        this,
                        "Low battery must be less than High battery",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            if (lowValue < 1 || highValue > 100) {

                Toast.makeText(
                        this,
                        "Values must be between 1 and 100",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // SAVE VALUES
            SharedPreferences.Editor editor =
                    prefs.edit();

            editor.putInt("LOW", lowValue);
            editor.putInt("HIGH", highValue);

            editor.apply();

            prefs.edit()
                    .putBoolean(
                            "SERVICE_ENABLED",
                            true
                    )
                    .apply();

            Intent serviceIntent =
                    new Intent(this, BatteryService.class);

            serviceIntent.putExtra("LOW", lowValue);
            serviceIntent.putExtra("HIGH", highValue);

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O) {

                startForegroundService(serviceIntent);

            } else {

                startService(serviceIntent);
            }

            setButton.setText("UNSET");

            Toast.makeText(
                    this,
                    "Hurray! Alert Set 🎉",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}