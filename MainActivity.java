package com.example.aamani.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.reader.karthik.android.IntentIntegrator;
import com.reader.karthik.android.IntentResult;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements OnClickListener  {
    private TextView formatTxt, contentTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scanBtn = (Button) findViewById(R.id.scan_button);
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);
        scanBtn.setOnClickListener(this);
        new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.scan_button) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);
            MqttClient client = null;
            try {
                client = new MqttClient("tcp://10.0.0.94:1883", MqttClient.generateClientId(), null);
            } catch (MqttException e1) {
                //textView3.setText(e1.getMessage());
            }

            MqttConnectOptions options = new MqttConnectOptions();
            try {
                assert client != null;
                client.connect(options);
            } catch (MqttException e) {
            }
            if (client.isConnected()) {
                MqttMessage message = new MqttMessage();
                MqttMessage errormessage = new MqttMessage();
                message.setPayload(scanContent.getBytes());
                errormessage.setPayload("Cart not assigned to user".getBytes());
                try {
                       client.publish("weight", message);
                   }
                catch (MqttException e) {
                    //  textView2.setText(e.getMessage());        }
                    try {
                        client.disconnect();
                    } catch (MqttException e1) {
                        e1.printStackTrace();
                    }
                }


            }
            } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
