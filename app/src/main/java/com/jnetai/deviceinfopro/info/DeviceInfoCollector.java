package com.jnetai.deviceinfopro.info;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.jnetai.deviceinfopro.utils.ErrorHandler;
import com.jnetai.deviceinfopro.utils.DebugLogger;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class DeviceInfoCollector {

    private static final String TAG = "DeviceInfoCollector";
    private final Context context;

    public DeviceInfoCollector(Context context) {
        this.context = context;
        DebugLogger.log(TAG, "DeviceInfoCollector initialized");
    }

    public String collectInfo(String category) {
        try {
            switch (category) {
                case "hardware": return collectHardware();
                case "system": return collectSystem();
                case "display": return collectDisplay();
                case "battery": return collectBattery();
                case "storage": return collectStorage();
                case "network": return collectNetwork();
                case "sensors": return collectSensors();
                case "all": return collectAll();
                default: return "Unknown category: " + category;
            }
        } catch (Exception e) {
            ErrorHandler.handle(TAG, "ERR-COLLECT-001", "Failed to collect info: " + category, e, null);
            return "Error collecting " + category + ": " + e.getMessage();
        }
    }

    private String collectAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEVICE INFO PRO ===\n\n");
        sb.append(collectHardware()).append("\n");
        sb.append(collectSystem()).append("\n");
        sb.append(collectDisplay()).append("\n");
        sb.append(collectBattery()).append("\n");
        sb.append(collectStorage()).append("\n");
        sb.append(collectNetwork()).append("\n");
        sb.append(collectSensors());
        return sb.toString();
    }

    private String collectHardware() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HARDWARE ===\n\n");
        sb.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        sb.append("Brand: ").append(Build.BRAND).append("\n");
        sb.append("Model: ").append(Build.MODEL).append("\n");
        sb.append("Device: ").append(Build.DEVICE).append("\n");
        sb.append("Product: ").append(Build.PRODUCT).append("\n");
        sb.append("Board: ").append(Build.BOARD).append("\n");
        sb.append("Hardware: ").append(Build.HARDWARE).append("\n");
        sb.append("Serial: ").append(Build.getSerial()).append("\n");

        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            sb.append("\n--- Memory ---\n");
            sb.append("Total RAM: ").append(formatSize(memInfo.totalMem)).append("\n");
            sb.append("Available RAM: ").append(formatSize(memInfo.availMem)).append("\n");
            sb.append("Low Memory: ").append(memInfo.lowMemory).append("\n");
        } catch (Exception e) {
            sb.append("Memory info: Error - ").append(e.getMessage()).append("\n");
        }

        sb.append("\n--- CPU ---\n");
        sb.append("ABI: ").append(Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "Unknown").append("\n");
        sb.append("Supported ABIs: ").append(String.join(", ", Build.SUPPORTED_ABIS)).append("\n");
        sb.append("CPU Cores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");

        try {
            java.io.RandomAccessFile reader = new java.io.RandomAccessFile("/proc/cpuinfo", "r");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Hardware")) sb.append("CPU HW: ").append(line.split(":")[1].trim()).append("\n");
                if (line.startsWith("Processor")) sb.append("Processor: ").append(line.split(":")[1].trim()).append("\n");
                if (line.startsWith("BogoMIPS")) sb.append("BogoMIPS: ").append(line.split(":")[1].trim()).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            sb.append("CPU details: Not available\n");
        }

        return sb.toString();
    }

    private String collectSystem() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SYSTEM ===\n\n");
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("SDK Level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Build ID: ").append(Build.ID).append("\n");
        sb.append("Build Type: ").append(Build.TYPE).append("\n");
        sb.append("Build Tags: ").append(Build.TAGS).append("\n");
        sb.append("Build Time: ").append(Build.TIME).append("\n");
        sb.append("Fingerprint: ").append(Build.FINGERPRINT).append("\n");
        sb.append("Security Patch: ").append(Build.VERSION.SECURITY_PATCH).append("\n");
        sb.append("Bootloader: ").append(Build.BOOTLOADER).append("\n");
        sb.append("Radio: ").append(Build.getRadioVersion()).append("\n");

        sb.append("\n--- OS Features ---\n");
        sb.append("Is 64-bit: ").append(Build.SUPPORTED_64_BIT_ABIS.length > 0).append("\n");
        sb.append("Is TV: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)).append("\n");
        sb.append("Is Watch: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)).append("\n");
        sb.append("Has NFC: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)).append("\n");
        sb.append("Has Bluetooth: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)).append("\n");
        sb.append("Has Camera: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)).append("\n");
        sb.append("Has Fingerprint: ").append(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)).append("\n");

        sb.append("\n--- Runtime ---\n");
        Runtime rt = Runtime.getRuntime();
        sb.append("Max Memory: ").append(formatSize(rt.maxMemory())).append("\n");
        sb.append("Total Memory: ").append(formatSize(rt.totalMemory())).append("\n");
        sb.append("Free Memory: ").append(formatSize(rt.freeMemory())).append("\n");

        sb.append("\n--- Settings ---\n");
        sb.append("Device Name: ").append(Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME)).append("\n");
        sb.append("Timezone: ").append(java.util.TimeZone.getDefault().getID()).append("\n");

        return sb.toString();
    }

    private String collectDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DISPLAY ===\n\n");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);

        sb.append("Resolution: ").append(metrics.widthPixels).append("x").append(metrics.heightPixels).append("\n");
        sb.append("Real Resolution: ").append(realMetrics.widthPixels).append("x").append(realMetrics.heightPixels).append("\n");
        sb.append("Density: ").append(metrics.densityDpi).append(" dpi\n");
        sb.append("Density Scale: ").append(metrics.density).append("\n");
        sb.append("Scaled Density: ").append(metrics.scaledDensity).append("\n");
        sb.append("Density Bucket: ").append(getDensityBucket(metrics.densityDpi)).append("\n");
        sb.append("Screen Size: ").append(String.format("%.1f\"", getScreenSize(metrics))).append("\n");
        sb.append("Refresh Rate: ").append(String.format("%.1f Hz", wm.getDefaultDisplay().getRefreshRate())).append("\n");

        return sb.toString();
    }

    private String collectBattery() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BATTERY ===\n\n");

        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int percent = (int) (level * 100 / (float) scale);
                sb.append("Level: ").append(percent).append("%\n");

                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                sb.append("Status: ").append(getBatteryStatus(status)).append("\n");

                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                sb.append("Plugged: ").append(getPluggedState(plugged)).append("\n");

                int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                sb.append("Health: ").append(getBatteryHealth(health)).append("\n");

                int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                sb.append("Temperature: ").append(String.format("%.1f°C", temp / 10.0)).append("\n");

                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                sb.append("Voltage: ").append(String.format("%.1f V", voltage / 1000.0)).append("\n");

                String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                sb.append("Technology: ").append(technology != null ? technology : "Unknown").append("\n");

                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bm != null) {
                    sb.append("Capacity: ").append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).append("%\n");
                    sb.append("Charge Counter: ").append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).append(" uAh\n");
                    sb.append("Current Now: ").append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)).append(" uA\n");
                    sb.append("Current Avg: ").append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)).append(" uA\n");
                    sb.append("Energy Counter: ").append(bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)).append(" nWh\n");
                }
            }
        } catch (Exception e) {
            sb.append("Battery info: Error - ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    private String collectStorage() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STORAGE ===\n\n");

        try {
            File dataDir = Environment.getDataDirectory();
            StatFs dataStat = new StatFs(dataDir.getPath());
            long dataTotal = dataStat.getTotalBytes();
            long dataFree = dataStat.getFreeBytes();
            long dataUsed = dataTotal - dataFree;
            sb.append("--- Internal Storage ---\n");
            sb.append("Total: ").append(formatSize(dataTotal)).append("\n");
            sb.append("Used: ").append(formatSize(dataUsed)).append("\n");
            sb.append("Free: ").append(formatSize(dataFree)).append("\n");
            sb.append("Usage: ").append(String.format("%.1f%%", (dataUsed * 100.0 / dataTotal))).append("\n");
        } catch (Exception e) {
            sb.append("Internal storage: Error\n");
        }

        try {
            File externalDir = Environment.getExternalStorageDirectory();
            if (externalDir != null && externalDir.exists()) {
                StatFs extStat = new StatFs(externalDir.getPath());
                long extTotal = extStat.getTotalBytes();
                long extFree = extStat.getFreeBytes();
                long extUsed = extTotal - extFree;
                sb.append("\n--- External Storage ---\n");
                sb.append("Path: ").append(externalDir.getAbsolutePath()).append("\n");
                sb.append("Total: ").append(formatSize(extTotal)).append("\n");
                sb.append("Used: ").append(formatSize(extUsed)).append("\n");
                sb.append("Free: ").append(formatSize(extFree)).append("\n");
                sb.append("Usage: ").append(String.format("%.1f%%", (extUsed * 100.0 / extTotal))).append("\n");
            }
        } catch (Exception e) {
            sb.append("External storage: Not available\n");
        }

        sb.append("\n--- App Storage ---\n");
        File filesDir = context.getFilesDir();
        sb.append("Files Dir: ").append(filesDir.getAbsolutePath()).append("\n");
        File cacheDir = context.getCacheDir();
        sb.append("Cache Dir: ").append(cacheDir.getAbsolutePath()).append("\n");

        return sb.toString();
    }

    private String collectNetwork() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NETWORK ===\n\n");

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                sb.append("--- WiFi ---\n");
                sb.append("WiFi Enabled: ").append(wifiManager.isWifiEnabled()).append("\n");
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    sb.append("SSID: ").append(wifiInfo.getSSID()).append("\n");
                    sb.append("BSSID: ").append(wifiInfo.getBSSID()).append("\n");
                    sb.append("Signal: ").append(wifiInfo.getRssi()).append(" dBm\n");
                    sb.append("Link Speed: ").append(wifiInfo.getLinkSpeed()).append(" Mbps\n");
                    sb.append("Frequency: ").append(wifiInfo.getFrequency()).append(" MHz\n");
                    int ip = wifiInfo.getIpAddress();
                    sb.append("IP: ").append((ip & 0xFF)).append(".").append((ip >> 8 & 0xFF))
                      .append(".").append((ip >> 16 & 0xFF)).append(".").append((ip >> 24 & 0xFF)).append("\n");
                    sb.append("MAC: ").append(wifiInfo.getMacAddress()).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("WiFi info: Error\n");
        }

        sb.append("\n--- Interfaces ---\n");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                sb.append(ni.getDisplayName()).append(": ");
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macStr = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macStr.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    sb.append(macStr);
                }
                sb.append("\n");
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    sb.append("  ").append(addr.getHostAddress()).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Interfaces: Error\n");
        }

        sb.append("\n--- Connectivity ---\n");
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                    if (caps != null) {
                        sb.append("Has Internet: ").append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).append("\n");
                        sb.append("Has WiFi: ").append(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).append("\n");
                        sb.append("Has Cellular: ").append(caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).append("\n");
                        sb.append("Has Ethernet: ").append(caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).append("\n");
                        sb.append("Has VPN: ").append(caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            sb.append("Connectivity: Error\n");
        }

        sb.append("\n--- Telephony ---\n");
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                sb.append("Network Type: ").append(getNetworkType(tm.getNetworkType())).append("\n");
                sb.append("Network Operator: ").append(tm.getNetworkOperatorName()).append("\n");
                sb.append("SIM Operator: ").append(tm.getSimOperatorName()).append("\n");
                sb.append("SIM State: ").append(getSimState(tm.getSimState())).append("\n");
                sb.append("Country ISO: ").append(tm.getNetworkCountryIso()).append("\n");
                sb.append("Is Roaming: ").append(tm.isNetworkRoaming()).append("\n");
            }
        } catch (Exception e) {
            sb.append("Telephony: Not available\n");
        }

        return sb.toString();
    }

    private String collectSensors() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SENSORS ===\n\n");

        try {
            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sm != null) {
                List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
                sb.append("Total sensors: ").append(sensors.size()).append("\n\n");
                for (Sensor sensor : sensors) {
                    sb.append(sensor.getName()).append("\n");
                    sb.append("  Type: ").append(getSensorType(sensor.getType())).append("\n");
                    sb.append("  Vendor: ").append(sensor.getVendor()).append("\n");
                    sb.append("  Version: ").append(sensor.getVersion()).append("\n");
                    sb.append("  Power: ").append(String.format("%.2f mA", sensor.getPower())).append("\n");
                    sb.append("  Resolution: ").append(sensor.getResolution()).append("\n");
                    sb.append("  Max Range: ").append(sensor.getMaximumRange()).append("\n");
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Sensors: Error - ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    private String getDensityBucket(int dpi) {
        if (dpi <= 120) return "ldpi";
        if (dpi <= 160) return "mdpi";
        if (dpi <= 240) return "hdpi";
        if (dpi <= 320) return "xhdpi";
        if (dpi <= 480) return "xxhdpi";
        if (dpi <= 640) return "xxxhdpi";
        return "unknown";
    }

    private double getScreenSize(DisplayMetrics metrics) {
        double widthInches = metrics.widthPixels / (double) metrics.xdpi;
        double heightInches = metrics.heightPixels / (double) metrics.ydpi;
        return Math.sqrt(widthInches * widthInches + heightInches * heightInches);
    }

    private String getBatteryStatus(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING: return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING: return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL: return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "Not Charging";
            default: return "Unknown";
        }
    }

    private String getPluggedState(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC: return "AC";
            case BatteryManager.BATTERY_PLUGGED_USB: return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Wireless";
            default: return "Unplugged";
        }
    }

    private String getBatteryHealth(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: return "Failure";
            case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";
            default: return "Unknown";
        }
    }

    private String getNetworkType(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_NR: return "5G NR";
            default: return "Type " + type;
        }
    }

    private String getSimState(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_READY: return "Ready";
            case TelephonyManager.SIM_STATE_ABSENT: return "Absent";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: return "Network Locked";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED: return "PIN Required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED: return "PUK Required";
            default: return "Unknown";
        }
    }

    private String getSensorType(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return "Accelerometer";
            case Sensor.TYPE_MAGNETIC_FIELD: return "Magnetometer";
            case Sensor.TYPE_GYROSCOPE: return "Gyroscope";
            case Sensor.TYPE_LIGHT: return "Light";
            case Sensor.TYPE_PRESSURE: return "Pressure";
            case Sensor.TYPE_PROXIMITY: return "Proximity";
            case Sensor.TYPE_GRAVITY: return "Gravity";
            case Sensor.TYPE_LINEAR_ACCELERATION: return "Linear Acceleration";
            case Sensor.TYPE_ROTATION_VECTOR: return "Rotation Vector";
            case Sensor.TYPE_AMBIENT_TEMPERATURE: return "Ambient Temperature";
            case Sensor.TYPE_RELATIVE_HUMIDITY: return "Relative Humidity";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: return "Magnetometer Uncalibrated";
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: return "Gyroscope Uncalibrated";
            case Sensor.TYPE_SIGNIFICANT_MOTION: return "Significant Motion";
            case Sensor.TYPE_STEP_DETECTOR: return "Step Detector";
            case Sensor.TYPE_STEP_COUNTER: return "Step Counter";
            case Sensor.TYPE_HEART_RATE: return "Heart Rate";
            case Sensor.TYPE_POSE_6DOF: return "Pose 6DOF";
            case Sensor.TYPE_STATIONARY_DETECT: return "Stationary Detect";
            case Sensor.TYPE_MOTION_DETECT: return "Motion Detect";
            case Sensor.TYPE_HEART_BEAT: return "Heart Beat";
            default: return "Type " + type;
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
