package com.jnetai.deviceinfopro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jnetai.deviceinfopro.info.DeviceInfoCollector;
import com.jnetai.deviceinfopro.utils.ErrorHandler;
import com.jnetai.deviceinfopro.utils.DebugLogger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btnHardware;
    private Button btnSystem;
    private Button btnDisplay;
    private Button btnBattery;
    private Button btnStorage;
    private Button btnNetwork;
    private Button btnSensors;
    private Button btnAll;
    private Button btnAbout;
    private TextView tvResults;
    private ProgressBar progressBar;
    private ScrollView scrollResults;

    private DeviceInfoCollector collector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            DebugLogger.log(TAG, "MainActivity onCreate started");

            initViews();
            collector = new DeviceInfoCollector(this);

            DebugLogger.log(TAG, "MainActivity onCreate completed");
        } catch (Exception e) {
            ErrorHandler.handle(TAG, "ERR-MAIN-001", "Failed to initialize MainActivity", e, this);
        }
    }

    private void initViews() {
        try {
            btnHardware = findViewById(R.id.btnHardware);
            btnSystem = findViewById(R.id.btnSystem);
            btnDisplay = findViewById(R.id.btnDisplay);
            btnBattery = findViewById(R.id.btnBattery);
            btnStorage = findViewById(R.id.btnStorage);
            btnNetwork = findViewById(R.id.btnNetwork);
            btnSensors = findViewById(R.id.btnSensors);
            btnAll = findViewById(R.id.btnAll);
            btnAbout = findViewById(R.id.btnAbout);
            tvResults = findViewById(R.id.tvResults);
            progressBar = findViewById(R.id.progressBar);
            scrollResults = findViewById(R.id.scrollResults);

            btnHardware.setOnClickListener(v -> showInfo("hardware"));
            btnSystem.setOnClickListener(v -> showInfo("system"));
            btnDisplay.setOnClickListener(v -> showInfo("display"));
            btnBattery.setOnClickListener(v -> showInfo("battery"));
            btnStorage.setOnClickListener(v -> showInfo("storage"));
            btnNetwork.setOnClickListener(v -> showInfo("network"));
            btnSensors.setOnClickListener(v -> showInfo("sensors"));
            btnAll.setOnClickListener(v -> showInfo("all"));
            btnAbout.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            });

            DebugLogger.log(TAG, "Views initialized successfully");
        } catch (Exception e) {
            ErrorHandler.handle(TAG, "ERR-MAIN-002", "Failed to initialize views", e, this);
        }
    }

    private void showInfo(String category) {
        try {
            setLoading(true);
            DebugLogger.log(TAG, "Collecting info: " + category);

            new Thread(() -> {
                try {
                    String info = collector.collectInfo(category);
                    runOnUiThread(() -> {
                        setLoading(false);
                        tvResults.setText(info);
                        scrollResults.post(() -> scrollResults.fullScroll(View.FOCUS_UP));
                        DebugLogger.log(TAG, "Info displayed for: " + category);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        tvResults.setText("Error: " + e.getMessage());
                    });
                    ErrorHandler.handle(TAG, "ERR-MAIN-003", "Failed to collect info: " + category, e, MainActivity.this);
                }
            }).start();

        } catch (Exception e) {
            setLoading(false);
            ErrorHandler.handle(TAG, "ERR-MAIN-004", "Failed to show info", e, this);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
