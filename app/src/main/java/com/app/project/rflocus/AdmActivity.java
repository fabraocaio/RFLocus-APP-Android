package com.app.project.rflocus;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.*;


public class AdmActivity extends AppCompatActivity {

    private static String TAG = "AdmActivity";
    private static String network = "RFLocus";
    private static String password = "oficina3";

    protected String url = "http://192.168.0.1:5500/";

    private PeriodicScan periodicScan;
    WifiManager wifiMgr;
    private boolean wifiInitStt;

    private List<Ap> apList;

    private EditText etPosX, etPosY, etPosZ;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
            tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3;
    public Button btnSend;

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

	/**
     * Function to check if the device still connected to a network
     * @param ssid  Network SSID to be checked
     * @return boolean
     */
    private boolean isConnected(String ssid){
        wifiMgr = (WifiManager) getApplicationContext().getSystemService (Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return wifiInfo.getSSID().equals("\""+ssid+"\"");
    }

	/**
	* Class that manager the log saving to the device's storage directory
	*/
    public static class CreateLog extends Application {
        private static String TAG = "CreateLog";

        /**
         * Function to save the logcat into files on the device's storage directory
         */
        private static void createLog() {
            if (isExternalStorageWritable()) {

                //DateFormat df = new SimpleDateFormat.getDateTimeInstance("ddmmyy","hhmm");
                File appDirectory = new File(Environment.getExternalStorageDirectory() + "/rflocusUser");
                File logDirectory = new File(appDirectory + "/log");
                File logFile1 = new File(logDirectory, "logcat_default" + System.currentTimeMillis() + ".txt");
                File logFile2 = new File(logDirectory, "logcat_debug" + System.currentTimeMillis() + ".txt");

                if (!appDirectory.exists()) {
                    appDirectory.mkdir();
                    Log.d(TAG,"Creating app directory");
                }

                if (!logDirectory.exists()) {
                    logDirectory.mkdir();
                    Log.d(TAG,"Creating log directory");
                }

                try {
                    Process process = Runtime.getRuntime().exec("logcat -c");
                    process = Runtime.getRuntime().exec("logcat -f " + logFile1 + " *:I");
                    process = Runtime.getRuntime().exec("logcat -f " + logFile2 + " *:D");
                    Log.d(TAG,"Log Saved");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (isExternalStorageReadable()) {
                Log.e(TAG, "External Storage only readable");
            } else {
                Log.e(TAG, "External Storage only accessible");
            }
        }

        /***
         * Checks if external storage is available for read and write
         *
         * @return boolean
         */
        public static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }

        /***
         * Checks if external storage is available to at least read
         *
         * @return boolean
         */
        public static boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }
    }
	
    /**
     * Function to automatically connect to a OPEN Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     */
    private void autoConnectOPEN(String networkSSID) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //For OPEN password
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
        Log.d("AutoConnect", "Open");
    }

    /**
     * Function to automatically connect to a WPA Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     * @param networkPass String with the password of the desire network
     */
    private void autoConnectWPA(String networkSSID, String networkPass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //For WPA password
        conf.preSharedKey = "\"" + networkPass + "\"";
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMgr.addNetwork(conf);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
        Log.d("AutoConnect", "WPA");
    }

    /**
     * Function to automatically connect to a WEP Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     * @param networkPass String with the password of the desire network
     */
    private void autoConnectWEP(String networkSSID, String networkPass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //for wep
        conf.wepKeys[0] = "\"" + networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMgr.addNetwork(conf);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
    }

    /**
     * Function that returns a list of MACs for testing
     *
     * @return ArrayList with the default MAC address
     */
    private ArrayList<String> setListMAC() {
        ArrayList<String> listMacs = new ArrayList<>();
        /*
        // -----------MACS UTFPRWEB---------- //
        listMacs.add("64:ae:0c:65:7a:71");
        listMacs.add("64:ae:0c:be:71:03");
        listMacs.add("64:ae:0c:91:76:31");
        // ---------------------------------- //
        */

        // -----------MACS RFLocus---------- //
        listMacs.add("a2:20:a6:14:ea:ec");
        listMacs.add("a2:20:a6:17:37:d8");
        listMacs.add("a2:20:a6:19:10:45");
        //listMacs.add("a2:20:a6:19:0E:30");
        //listMacs.add("b8:27:eb:a3:7d:75");
        // ---------------------------------- //

        return listMacs;
    }

    /**
     * Function to request the user permission to access Fine location
     */
    private void permissionRequest(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Send user a resquest for permition
                Log.d("PerFinLoc", "request needed");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Log.d("PerFineLoc", "Permission granted");
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.d("PerFineLoc", "Permission denied");
            }
        }
        // other 'case' lines to check for other
        // permissions this app might request
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm);

        //Fix portrait orientation to this activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Check if Android version is superior then Lollipop
            //Check permission of FINE LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            permissionRequest();
        }

        CreateLog.createLog();

        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInitStt = wifiMgr.isWifiEnabled();

        etPosX = (EditText) findViewById(R.id.etPosX);
        etPosY = (EditText) findViewById(R.id.etPosY);
        etPosZ = (EditText) findViewById(R.id.etPosZ);

        tvSSID1 = (TextView) findViewById(R.id.tvSSID1);
        tvMAC1 = (TextView) findViewById(R.id.tvMAC1);
        tvRSS1 = (TextView) findViewById(R.id.tvRSS1);

        tvSSID2 = (TextView) findViewById(R.id.tvSSID2);
        tvMAC2 = (TextView) findViewById(R.id.tvMAC2);
        tvRSS2 = (TextView) findViewById(R.id.tvRSS2);

        tvSSID3 = (TextView) findViewById(R.id.tvSSID3);
        tvMAC3 = (TextView) findViewById(R.id.tvMAC3);
        tvRSS3 = (TextView) findViewById(R.id.tvRSS3);

        btnSend = (Button) findViewById(R.id.btnSend);

        apList = new ArrayList<>();

        autoConnectWPA(network,password);
        requestGET();
        startRefresh();
        Log.i("LogSave","apid1,rssi1,apid2,rssi2,apid3,rssi3,posX,posY,posZ");
        //Log.i("LogSave","apid1,rssi1,apid2,rssi2,apid3,rssi3,apid4,rssi4,posX,posY,posZ");
        //Log.i("LogSave","apid1,rssi1,apid2,rssi2,apid3,rssi3,apid4,rssi4,apid5,rssi5,posX,posY,posZ");
        Log.d(TAG,"onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefresh();
        //if (!wifiInitStt)
        wifiMgr.setWifiEnabled(wifiInitStt);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefresh();
    }

    /**
     * Function to update the information displayed in the user interface
     *
     * @param apList List with the MAC's Address
     */
    private void updateUI(List<Ap> apList){
        tvSSID1.setText(apList.get(0).getSsid());
        tvMAC1.setText(apList.get(0).getMac());
        tvRSS1.setText(String.format("%s dbm", Integer.toString(apList.get(0).getRssi())));

        tvSSID2.setText(apList.get(1).getSsid());
        tvMAC2.setText(apList.get(1).getMac());
        tvRSS2.setText(String.format("%s dbm", Integer.toString(apList.get(1).getRssi())));

        tvSSID3.setText(apList.get(2).getSsid());
        tvMAC3.setText(apList.get(2).getMac());
        tvRSS3.setText(String.format("%s dbm", Integer.toString(apList.get(2).getRssi())));
    }

    /**
     * Function to update AP information
     *
     * @param results a list of ScanResult
     * @param apList  List with the MAC's Address
     */
    private void updateAP(List<ScanResult> results, List<Ap> apList){
        for (Ap ap : apList){
            ap.setRssi(0);
        }

        for (ScanResult result : results) {
            for (Ap ap : apList) {
                if (ap.getMac().equals(result.BSSID)){
                    ap.setSsid(result.SSID);
                    ap.setRssi(result.level);
                }
            }
        }
    }

    /**
     * Event function generated by clicking the Send button
     *
     * @param v View's reference
     */
    public void sendRasp(View v) {
        Log.d(TAG, "Send Pressed");
        if ((etPosX.getText().toString().matches("")) || (etPosY.getText().toString().matches("")) || (etPosZ.getText().toString().matches(""))) {
            Toast.makeText(this, "Campo vázio", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Campo vázio");
            return;
        }
        logSave(apList);
        if (isConnected(network)) {
            requestPUT(url, createJSON(apList));
            Toast.makeText(AdmActivity.this, "Enviado", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Enviando request");
        }
        else {
            Toast.makeText(AdmActivity.this, "Reconectando", Toast.LENGTH_SHORT).show();
            autoConnectWPA(network,password);
            Log.d(TAG,"Reconectando à rede");
        }
    }

	 /**
     * Function to generated the AP information to a log format
     *
     * @param apList List of Ap's
     */
    private void logSave(List<Ap> apList){
        String log = "";
        for (Ap ap : apList){
            log += ap.getMac() +",";
            log += ap.getRssi()+",";
        }
        log += etPosX.getText().toString()+",";
        log += etPosY.getText().toString()+",";
        log += etPosZ.getText().toString();

        Log.i("LogSave",log);
    }

    /**
     * Function to generate the JSON object containing the mac, rssi and coordinates X Y Z
     *
     * @return JSONObject
     */
    private JSONObject createJSON(List<Ap> apList){
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObj.put("type","real");

            for (Ap ap : apList) {
                JSONObject aux = new JSONObject();
                aux.put("apid",ap.getMac());
                aux.put("rssi",ap.getRssi());
                aux.put("posx",etPosX.getText().toString());
                aux.put("posy",etPosY.getText().toString());
                aux.put("posz",etPosZ.getText().toString());
                jsonArray.put(aux);
            }
            Log.d("CreateJSONArray",jsonArray.toString());
            jsonObj.put("data",jsonArray);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    /**
     * Function to create a HTTP PUT request to a server URL
     *
     * @param url        server to be connect
     * @param jsonObject to be send
     */
    private void requestPUT(String url, final JSONObject jsonObject){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest putRequest = new JsonObjectRequest(Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ResponseOK", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ResponseError",error.toString());
                        Toast.makeText(AdmActivity.this, "Erro ao enviar", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    //Log.i("BodyJSON", jsonObject.toString());
                    return jsonObject.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        queue.add(putRequest);
    }

    /**
     * Function to request the MAC address of the APs
     */
    private void requestGET(){
        ArrayList<String> listMacs = setListMAC();
        for (String mac : listMacs) {
            Ap ap = new Ap(mac);
            apList.add(ap);
        }
    }

    /**
     * Function to stop the periodic thread
     */
    private void stopRefresh() {
        if(periodicScan != null)
            periodicScan.keepRunning = false;
        periodicScan = null;
    }

    /**
     * Function to start the periodic thread
     */
    private void startRefresh() {
        //periodicScan classe de tarefa assincrona
        if(periodicScan == null) {
            periodicScan = new PeriodicScan();
            periodicScan.execute();
        }
        if (!periodicScan.keepRunning) {
            periodicScan = new PeriodicScan();
            periodicScan.execute();
        }
    }

    Runnable doRefresh = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    /**
     * Function that scans the Wi-Fi networks. It makes sure to keep Wi-Fi active.
     */
    private void refresh() {
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiMgr.isWifiEnabled())
            wifiMgr.setWifiEnabled(true);
        wifiMgr.startScan();
        List<ScanResult> results = wifiMgr.getScanResults();
        updateAP(results,apList);
        updateUI(apList);
        //Log.d("AutoRefresh","Scan Completed");
    }

    /**
     * Class that implements AsyncTask
     */
    private class PeriodicScan extends AsyncTask<Void, Void, Void> {

        boolean keepRunning = true;

        @Override
        protected Void doInBackground(Void... params) {
            while(keepRunning) {
                runOnUiThread(doRefresh);
                try {
                    Thread.sleep(500); // Refresh every 0,5 second
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            }
            return null;
        }

    }
}