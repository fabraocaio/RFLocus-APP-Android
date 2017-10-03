/*
 * This file is part of SurvivalGuide 
 * Copyleft 2011 The SurvivalGuide Team 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.app.project.rflocus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

class WifiManagerThread extends Thread {

    private Context context;
    private Timer periodicScanTimer = new Timer();
    private WifiManager wifi;
    private WifiScanReceiver scanReceiver;

    private class WifiScanReceiver extends BroadcastReceiver {
        AdmActivity ui;

        public WifiScanReceiver(AdmActivity ui) {
            super();
            this.ui = ui;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(scanReceiver);
            List<ScanResult> results = wifi.getScanResults();
            List<WifiReading> readings = new ArrayList<>();
            for (ScanResult result : results) {
                readings.add(new WifiReading(result));
            }
            ui.showReadings(readings);
            /*
            try {
                Location locRes = Location.getFromReadings(readings);
                ui.postUpdateLocation(locRes);
            } catch (Exception e) {
                // TODO: Toast to indicate server error 
                e.printStackTrace();
            }
            if (AdmActivity.COLLECT_TEST_DATA) {
                SnapshotCache.storeSnapshot(readings, ui.snapshotName, ui);
            }*/
        }
    }

    /** Called when the service is created for the first time. */
    WifiManagerThread(AdmActivity ui) {
        context = ui.getApplicationContext();
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        scanReceiver = new WifiScanReceiver(ui);
    }

    @Override
    public void run() {
        startPeriodicScan();
        try {
            // since we scheduled the timer, don't do anything until we get killed 
            while (true) {
                sleep(60 * 60 * 1000);
            }
        } catch (InterruptedException e) {
            stopPeriodicScan();
        }
    }

    private void periodicScan() {
        if (isInterrupted()) {
            stopPeriodicScan();
        }
        wifi.startScan();
    }

    private void startPeriodicScan() {
        periodicScanTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        periodicScan();
                    }
                },
                0, // start right now 
                //1 * 60 * 1000 // every minute
                5 * 1000 //every 5 seconds
        );
    }

    private void stopPeriodicScan() {
        periodicScanTimer.cancel();
    }

}