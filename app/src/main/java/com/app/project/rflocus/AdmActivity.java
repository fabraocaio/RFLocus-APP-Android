package com.app.project.rflocus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdmActivity extends AppCompatActivity {

    private WifiManagerThread wifiManagerThread;
    private Refrescame refrescame;
    
    private String SSID1, SSID2, SSID3;
    private String MAC1, MAC2, MAC3;
    private Integer RSS1, RSS2, RSS3;

    private EditText etDistAp1, etDistAp2, etDistAp3;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
                    tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3,
                    tvDist1, tvDist2, tvDist3;

    private Button btnSend;
    int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5;


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
        //switch (requestCode) {
        //    case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
        }

            // other 'case' lines to check for other
            // permissions this app might request
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //Send user a resquest for permition
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        }

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

        btnSend =(Button) findViewById(R.id.btnSend);

        comienzaRefresco();
        //updateUI();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        paraRefresco();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        //wifiManagerThread.interrupt();
        paraRefresco();
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (scanningMode != ScanningMode.OFF) {
        //    wifiManagerThread = new WifiManagerThread(this);
        //    wifiManagerThread.start();
        //}
        comienzaRefresco();
    }

    private void updateUIHardcoded(){
        this.SSID1="NodeRFL1";
        this.SSID2="NodeRFL2";
        this.SSID3="NodeRFL3";
        this.MAC1="FF:FF:FF:FF:FF:A1";
        this.MAC2="FF:FF:FF:FF:FF:A2";
        this.MAC3="FF:FF:FF:FF:FF:A3";
        this.RSS1=-42;
        this.RSS2=-60;
        this.RSS3=-74;
    }

    public void updateUI(){
        tvSSID1.setText(SSID1);
        tvMAC1.setText(MAC1);
        tvRSS1.setText(String.format("%s dbm", Integer.toString(RSS1)));

        tvSSID2.setText(SSID2);
        tvMAC2.setText(MAC2);
        tvRSS2.setText(String.format("%s dbm", Integer.toString(RSS2)));

        tvSSID3.setText(SSID3);
        tvMAC3.setText(MAC3);
        tvRSS3.setText(String.format("%s dbm", Integer.toString(RSS3)));

        tvDist1.setText("Distância à ("+MAC1.subSequence(12,17)+")");
        tvDist2.setText("Distância à ("+MAC2.subSequence(12,17)+")");
        tvDist3.setText("Distância à ("+MAC3.subSequence(12,17)+")");
    }

    public void updateUI(List<ScanResult> results){
        int i =0;
        if (results.size()>3){
            switch (i){
                case 0:{
                    SSID1=results.get(0).SSID;
                    MAC1=results.get(0).BSSID;
                    RSS1=results.get(0).level;
                }
                case 1:{
                    SSID2=results.get(1).SSID;
                    MAC2=results.get(1).BSSID;
                    RSS2=results.get(1).level;
                }
                case 2:{
                    SSID3=results.get(2).SSID;
                    MAC3=results.get(2).BSSID;
                    RSS3=results.get(2).level;
                }
            }

            updateUI();
        }
    }
    public void sendRasp (View v){
        if ( (etDistAp1.getText().toString().matches("")) || (etDistAp2.getText().toString().matches("")) || (etDistAp3.getText().toString().matches("")) ) {
            Toast.makeText(this, "Campo vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Dados enviados", Toast.LENGTH_SHORT).show();
        //tvSoma.setText(etDistAp1.getText().toString()+" "+etDistAp2.getText().toString()+" "+etDistAp3.getText().toString());
    }

    public void showReadings(List<WifiReading> readings){
        for (WifiReading reading : readings){
            SSID1 = reading.ssid;
            MAC1 = reading.mac;
            RSS1 = reading.signal;
        }
        updateUI();
    }

    private void paraRefresco() {
        if(refrescame != null)
            refrescame.keepRunning = false;
        refrescame = null;
    }

    private void comienzaRefresco() {
        //refrescame classe de tarefa assincrona
        if(refrescame == null) {
            refrescame = new Refrescame();
            refrescame.execute();
        }
        if (!refrescame.keepRunning) {
            refrescame = new Refrescame();
            refrescame.execute();
        }
    }

    Runnable doRefresca = new Runnable() {
        @Override
        public void run() {
            refresca();
        }
    };

    private void refresca() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMgr.startScan();
        List<ScanResult> results= wifiMgr.getScanResults();
        updateUI(results);
        //if(tmpresults == null) results = new ArrayList<ScanResult>();
        /*
        else {
            for(ScanResult s : tmpresults) {
                boolean found = false;
                int i = 0;
                while(!found && i < results.size()) {
                    if(results.get(i).BSSID.equals(s.BSSID)) {
                        found = true;
                        results.get(i).level = s.level;
                    }
                    i++;
                }
            }
        }*/
    }


    private class Refrescame extends AsyncTask<Void, Void, Void> {
        public boolean keepRunning = true;
        @Override
        protected Void doInBackground(Void... params) {
            while(keepRunning) {
                runOnUiThread(doRefresca);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            }
            return null;
        }

    }
}
