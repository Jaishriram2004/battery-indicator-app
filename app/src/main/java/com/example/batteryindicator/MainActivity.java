package com.example.batteryindicator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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

        lowBattery.setText(String.valueOf(savedLow));
        highBattery.setText(String.valueOf(savedHigh));

        setButton.setOnClickListener(v -> {

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

            // SAVE VALUES
            SharedPreferences.Editor editor =
                    prefs.edit();

            editor.putInt("LOW", lowValue);
            editor.putInt("HIGH", highValue);

            editor.apply();

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

            Toast.makeText(
                    this,
                    "Hurray! Alert Set 🎉",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}