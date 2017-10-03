package com.app.project.rflocus;

import android.net.wifi.ScanResult;

import java.util.Comparator;


class WifiReading {

    final String mac;
    final String ssid;
    final int signal;
    //public AccessPoint ap = null;

    public WifiReading(String mac, String ssid, int signal) {
        this.mac = mac;
        this.ssid = ssid;
        this.signal = signal;
    }

    WifiReading(ScanResult result) {
        this.mac = result.BSSID;
        this.ssid = result.SSID;
        this.signal = result.level;
    }

    public static Comparator<WifiReading> byMac = new Comparator<WifiReading>() {
        public int compare(WifiReading lhs, WifiReading rhs) {
            return lhs.mac.compareToIgnoreCase(rhs.mac);
        }
    };

    public static Comparator<WifiReading> bySsid = new Comparator<WifiReading>() {
        public int compare(WifiReading lhs, WifiReading rhs) {
            return lhs.ssid.compareToIgnoreCase(rhs.ssid);
        }
    };

    public static Comparator<WifiReading> bySignal = new Comparator<WifiReading>() {
        public int compare(WifiReading lhs, WifiReading rhs) {
            return (lhs.signal < rhs.signal) ? 1 : (lhs.signal > rhs.signal) ? -1 : 0; // descending order
        }
    };

}
