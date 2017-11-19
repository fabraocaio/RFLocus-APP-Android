package com.app.project.rflocus;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.*;


public class AdmActivity extends AppCompatActivity {

    protected String TAG = "AdmActivity";

    protected String url = "http://192.168.100.18:5500/";

    private PeriodicScan periodicScan;
    WifiManager wifiMgr;
    private boolean wifiInitStt;

    private List<Ap> apList;

    private EditText etPosX, etPosY, etPosZ;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
            tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3;
    protected Button btnSend;

    protected int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

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
        listMacs.add("64:ae:0c:65:7a:71");
        listMacs.add("64:ae:0c:be:71:03");
        listMacs.add("64:ae:0c:91:76:31");
        listMacs.add("2c:55:d3:b0:1c:c4");
        // ---------------------------------- //

        /*
        // ----------MACS SafeHouse--------- //
        listMacs.add("9c:7d:a3:eb:95:28");
        listMacs.add("00:04:df:07:b5:eb");
        listMacs.add("20:10:7a:e0:37:f0");
        // ---------------------------------- //
        */

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

        Ap ap1 = new Ap();
        Ap ap2 = new Ap();
        Ap ap3 = new Ap();
        apList = new ArrayList<>();
        apList.add(ap1);
        apList.add(ap2);
        apList.add(ap3);

        //autoConnectOPEN("UTFPRWEB");
        //autoConnectWAP("narsil","1119072205");
        //autoConnectWAP("FUNBOX-BOARDGAME-CAFE","Fb-4130400780");
        //autoConnectWEP("RFLocus","oficina3");
        requestGET();
        //setAps();
        startRefresh();
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
     * @param v reference to view
     */
    public void sendRasp(View v) {
        if ((etPosX.getText().toString().matches("")) || (etPosY.getText().toString().matches("")) || (etPosZ.getText().toString().matches(""))) {
            Toast.makeText(this, "Campo vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Enviando Dados", Toast.LENGTH_SHORT).show();
        for (int i = 0; i<3; i++){
            refresh();
            requestPUT(url, createJSON(apList));
        }
        Log.d(TAG, "Send Pressed");
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
            //Log.i("JSONArray",jsonArray.toString());
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
                        Log.d("Response", response.toString());
                        Toast.makeText(AdmActivity.this, "Concluido", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(AdmActivity.this, "Erro ao enviar", Toast.LENGTH_SHORT).show();
                        Log.d("Error.Response", error.toString());
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
                    //Log.i("json", jsonObject.toString());
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
        int i = 0;
        ArrayList<String> listMacs = (setListMAC());
        for (Ap ap : apList){
            ap.setMac(listMacs.get(i++));
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
        //ArrayList macs = setListMAC();
        //updateAP(results,macs,0);
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
                    Thread.sleep(1500); // Refresh every 1,5 second
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            }
            return null;
        }

    }
}