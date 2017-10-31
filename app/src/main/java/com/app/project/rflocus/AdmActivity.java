package com.app.project.rflocus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

    private PeriodicScan periodicScan;
    WifiManager wifiMgr;
    private boolean wifiInitStt;

    private String SSID1, SSID2, SSID3;
    private String MAC1, MAC2, MAC3;
    private Integer RSS1, RSS2, RSS3;

    private EditText etDistAp1, etDistAp2, etDistAp3;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
            tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3,
            tvDist1, tvDist2, tvDist3;

    Button btnSend;
    int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    ArrayList<String> listMAC = new ArrayList<>();

    private ArrayList setListMAC(){
        ArrayList<String> listMacs = new ArrayList<>();
        listMacs.add("64:ae:0c:65:7a:71");
        listMacs.add("64:ae:0c:be:71:03");
        listMacs.add("64:ae:0c:91:76:31");
        return listMacs;
    }

    private ArrayList serListSSID(){
        ArrayList<String> listSSID = new ArrayList<>();
        listSSID.add("RFLocus 01");
        listSSID.add("RFLocus 02");
        listSSID.add("RFLocus 03");
        return listSSID;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

        //Check if Adnroid version is superior then Lollipop
        //Check permission of FINE LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //Send user a resquest for permition
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }

            }
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

        btnSend =(Button) findViewById(R.id.btnSend);

        startRefresh();
    }

    @Override
    protected void onDestroy(){
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
     * Função que gera valores defaults para as variaveis globais dos APs
     */
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

    /**
     * Função para atualizar as informações exibidas na interface do usuário
     */
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

    /**
     * Função para atualizar as variaveis globais referentes aos Aps
     *
     * @param results a list of ScanResult
     * @param list an ArrayList with the MAC Address or SSID
     * @param opc a Integer witch determinate if the ArrayList contains MAC or SSID
     */
    public void updateInfo(List<ScanResult> results, ArrayList list, int opc){

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
            } break;
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
            } break;
        }

        updateUI();
    }

    /**
     * Função da evento gerado ao clicar no botão Enviar
     *
     * @param v reference to view
     */
    public void sendRasp (View v){
        if ( (etDistAp1.getText().toString().matches("")) || (etDistAp2.getText().toString().matches("")) || (etDistAp3.getText().toString().matches("")) ) {
            Toast.makeText(this, "Campo vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Dados enviados", Toast.LENGTH_SHORT).show();
        //tvSoma.setText(etDistAp1.getText().toString()+" "+etDistAp2.getText().toString()+" "+etDistAp3.getText().toString());
    }

    /**
     * Função para parar a thread periódica
     */
    private void stopRefresh() {
        if(periodicScan != null)
            periodicScan.keepRunning = false;
        periodicScan = null;
    }

    /**
     * Função para iniciar a thread preriódica
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
     * Função que realiza o scan das redes WiFi. Ela se sertifica de manter o WiFi ativo
     */
    private void refresh() {
        ArrayList macs = setListMAC();
        ArrayList ssids = serListSSID();
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiMgr.isWifiEnabled()) wifiMgr.setWifiEnabled(true);
        wifiMgr.startScan();
        List<ScanResult> results= wifiMgr.getScanResults();
        updateInfo(results,macs,0);
        //updateInfo(results,ssids,1);

        startRefresh();
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


    private class PeriodicScan extends AsyncTask<Void, Void, Void> {

        boolean keepRunning = true;

        @Override
        protected Void doInBackground(Void... params) {
            while(keepRunning) {
                runOnUiThread(doRefresh);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            }
            return null;
        }

    }
}