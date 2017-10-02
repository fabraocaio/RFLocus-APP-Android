package com.app.project.rflocus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdmActivity extends AppCompatActivity {

    private String SSID1="NodeRFL1", SSID2="NodeRFL2", SSID3="NodeRFL3";
    private String MAC1="FF:FF:FF:FF:FF:A1", MAC2="FF:FF:FF:FF:FF:A2", MAC3="FF:FF:FF:FF:FF:A3";
    private Integer RSS1=-42, RSS2=-60, RSS3=-74;

    private EditText etDistAp1, etDistAp2, etDistAp3;
    private TextView tvSSID1, tvMAC1, tvRSS1, tvSSID2,
                    tvMAC2, tvRSS2, tvSSID3, tvMAC3, tvRSS3,
                    tvDist1, tvDist2, tvDist3;

    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm);

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

        updateUI();
    }

    public void updateUI(){
        tvSSID1.setText(SSID1);
        tvMAC1.setText(MAC1);
        tvRSS1.setText(RSS1+" dbm");

        tvSSID2.setText(SSID2);
        tvMAC2.setText(MAC2);
        tvRSS2.setText(RSS2+" dbm");

        tvSSID3.setText(SSID3);
        tvMAC3.setText(MAC3);
        tvRSS3.setText(RSS3+" dbm");

        tvDist1.setText("Distância ("+MAC1.subSequence(12,17)+")");
        tvDist2.setText("Distância ("+MAC2.subSequence(12,17)+")");
        tvDist3.setText("Distância ("+MAC3.subSequence(12,17)+")");
    }

    public void sendRasp (View v){
        if ( (etDistAp1.getText().toString().matches("")) || (etDistAp2.getText().toString().matches("")) || (etDistAp3.getText().toString().matches("")) ) {
            Toast.makeText(this, "Campo vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Dados enviados", Toast.LENGTH_SHORT).show();
        //tvSoma.setText(etDistAp1.getText().toString()+" "+etDistAp2.getText().toString()+" "+etDistAp3.getText().toString());

    }
}
