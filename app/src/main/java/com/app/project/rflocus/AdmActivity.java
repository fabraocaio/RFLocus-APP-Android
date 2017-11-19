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

import com.android.volley.Request;
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


public class AdmActivity extends AppCompatActivity {

    private String tag = "AdmActivity";

    private String url = "http://192.168.100.18:5500/";

    private PeriodicScan periodicScan;
    WifiManager wifiMgr;
    private boolean wifiInitStt;

    private Ap ap1;
    private Ap ap2;
    private Ap ap3;

    private String SSID1, SSID2, SSID3;
    private String MAC1, MAC2, MAC3;
    private Integer RSS1, RSS2, RSS3;

    private EditText etDistAp1, etDistAp2, etDistAp3;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
            tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3,
            tvDist1, tvDist2, tvDist3;
    private Button btnSend;

    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    //RequestQueue requestQueue = Volley.newRequestQueue(this);
    //ArrayList<String> listMAC = new ArrayList<>();

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
        wifiMgr.addNetwork(conf);
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
    }

    /**
     * Function that returns a list of MACs for testing
     *
     * @return ArrayList with the default MAC address
     */
    private ArrayList setListMAC() {
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
        //listMacs.add("2c:55:d3:b0:1c:c4");
        // ---------------------------------- //

        return listMacs;
    }

    /**
     * Function that returns a list of SSIDs for tests
     *
     * @return ArrayList with the default SSID
     */
    private ArrayList serListSSID() {
        ArrayList<String> listSSID = new ArrayList<>();
        listSSID.add("RFLocus 01");
        listSSID.add("RFLocus 02");
        listSSID.add("RFLocus 03");
        return listSSID;
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

        etDistAp1 = (EditText) findViewById(R.id.etDistAp1);
        etDistAp2 = (EditText) findViewById(R.id.etDistAp2);
        etDistAp3 = (EditText) findViewById(R.id.etDistAp3);

        tvSSID1 = (TextView) findViewById(R.id.tvSSID1);
        tvMAC1 = (TextView) findViewById(R.id.tvMAC1);
        tvRSS1 = (TextView) findViewById(R.id.tvRSS1);

        tvSSID2 = (TextView) findViewById(R.id.tvSSID2);
        tvMAC2 = (TextView) findViewById(R.id.tvMAC2);
        tvRSS2 = (TextView) findViewById(R.id.tvRSS2);

        tvSSID3 = (TextView) findViewById(R.id.tvSSID3);
        tvMAC3 = (TextView) findViewById(R.id.tvMAC3);
        tvRSS3 = (TextView) findViewById(R.id.tvRSS3);

        tvDist1 = (TextView) findViewById(R.id.tvDist1);
        tvDist2 = (TextView) findViewById(R.id.tvDist2);
        tvDist3 = (TextView) findViewById(R.id.tvDist3);

        btnSend = (Button) findViewById(R.id.btnSend);

        //autoConnectOPEN("UTFPRWEB");
        //autoConnectWAP("narsil","1119072205");
        //autoConnectWAP("FUNBOX-BOARDGAME-CAFE","Fb-4130400780");
        //autoConnectWEP("RFLocus","oficina3");
        setAps();
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

    private void setAps() {
        SSID1 = "RFLocus 01";
        MAC1 = "FF:FF:FF:FF:FF:01";
        RSS1 = 0;

        SSID2 = "RFLocus 02";
        MAC2 = "FF:FF:FF:FF:FF:02";
        RSS2 = 0;

        SSID3 = "RFLocus 03";
        MAC3 = "FF:FF:FF:FF:FF:03";
        RSS3 = 0;
    }

    /**
     * Function to update the information displayed in the user interface
     */
    public void updateUI() {
        tvSSID1.setText(SSID1);
        tvMAC1.setText(MAC1);
        tvRSS1.setText(String.format("%s dbm", Integer.toString(RSS1)));

        tvSSID2.setText(SSID2);
        tvMAC2.setText(MAC2);
        tvRSS2.setText(String.format("%s dbm", Integer.toString(RSS2)));

        tvSSID3.setText(SSID3);
        tvMAC3.setText(MAC3);
        tvRSS3.setText(String.format("%s dbm", Integer.toString(RSS3)));

        tvDist1.setText("Distância à (" + MAC1.subSequence(12, 17) + ")");
        tvDist2.setText("Distância à (" + MAC2.subSequence(12, 17) + ")");
        tvDist3.setText("Distância à (" + MAC3.subSequence(12, 17) + ")");
    }

    /**
     * Function to update global variables for APs
     *
     * @param results a list of ScanResult
     * @param list    an ArrayList with the MAC Address or SSID
     * @param opc     a Integer witch determinate if the ArrayList contains MAC or SSID
     */
    public void updateInfo(List<ScanResult> results, ArrayList list, int opc) {
        RSS1 = RSS2 = RSS3 = 0;

        switch (opc) {

            case 0: {
                for (ScanResult result : results) {
                    if (result.BSSID.equals(list.get(0))) {
                        SSID1 = result.SSID;
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (result.BSSID.equals(list.get(1))) {
                        SSID2 = result.SSID;
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (result.BSSID.equals(list.get(2))) {
                        SSID3 = result.SSID;
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                }
            }
            break;
            case 1: {
                for (ScanResult result : results) {
                    if (result.SSID.equals(list.get(0))) {
                        SSID1 = result.SSID;
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (result.SSID.equals(list.get(1))) {
                        SSID2 = result.SSID;
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (result.SSID.equals(list.get(2))) {
                        SSID3 = result.SSID;
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                }
            }
            break;
            case 2: {
                int i = 0;
                for (ScanResult result : results) {
                    if (i == 0) {
                        SSID1 = result.SSID;
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (i == 1) {
                        SSID2 = result.SSID;
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (i == 2) {
                        SSID3 = result.SSID;
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                    i++;
                }
            }
            break;
        }

        updateUI();
    }

    /**
     * Event function generated by clicking the Send button
     *
     * @param v reference to view
     */
    public void sendRasp(View v) {
        if ((etDistAp1.getText().toString().matches("")) || (etDistAp2.getText().toString().matches("")) || (etDistAp3.getText().toString().matches(""))) {
            Toast.makeText(this, "Campo vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        //Toast.makeText(this, "Enviando Dados", Toast.LENGTH_SHORT).show();

        //Comunicação REST PUT
        Log.d(tag, "Send Pressed");
        //restPUT(createJSON());
        requestPUT(url,createJSON());
    }

    /**
     * Function to generate the JSON object containing the mac, rssi and coordinates X Y Z
     *
     * @return JSONObject
     */
    private JSONObject createJSON(){
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        ArrayList<String> macs = new ArrayList<>();
        macs.add(MAC1);
        macs.add(MAC2);
        macs.add(MAC3);
        ArrayList<String> rssi = new ArrayList<>();
        rssi.add(Integer.toString(RSS1));
        rssi.add(Integer.toString(RSS2));
        rssi.add(Integer.toString(RSS3));
        try {
            jsonObj.put("type","real");
            jsonObj.put("type","real");

            for (int i = 0; i < macs.size(); i++) {
                JSONObject aux = new JSONObject();
                aux.put("apid",macs.get(i));
                aux.put("rssi",rssi.get(i));
                aux.put("posx",etDistAp1.getText().toString());
                aux.put("posy",etDistAp2.getText().toString());
                aux.put("posz",etDistAp3.getText().toString());
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
        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
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
                    Log.i("json", jsonObject.toString());
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
        ArrayList macs = setListMAC();
        //ArrayList ssids = serListSSID();
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiMgr.isWifiEnabled()) wifiMgr.setWifiEnabled(true);
        wifiMgr.startScan();
        List<ScanResult> results= wifiMgr.getScanResults();
        updateInfo(results,macs,0);
        //updateInfo(results,ssids,1);
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