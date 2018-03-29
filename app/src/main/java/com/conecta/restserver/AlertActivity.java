package com.conecta.restserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class AlertActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, CustomCallback {

    String TAG = "RestAlerta";
    String baseURL = "";
    private String publishAlertString = "";
    private String pullAlertString = "";
    List<String> alertEntities = new ArrayList<>();
    List<Alert> alertaList = new ArrayList<>();
    AlertAdapter adapterAlert;
    Button pullAlertButton, deleteButton;
    Map<String, String> postData = new HashMap<>();
    private final String alertDeleteString = "alertsdelete";
    private final String entityAlert = "motoalert";

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            Log.d(TAG, "completionJhandler");
            switch (type) {
                case ALERT_PULL:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (object instanceof String) {
                                Toast.makeText(AlertActivity.this, object.toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                alertaList.clear();
                                alertaList.addAll((List<Alert>) object);
                                adapterAlert.notifyDataSetChanged();
                                Log.d(TAG, "alertaLIst" + alertaList.toString());
                                Toast.makeText(AlertActivity.this, "Obtida lista de alertas", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
                case REFRESH_ALERT:

                    runOnUiThread(new Runnable() {
                        public void run() {
                            pullAlertButton.performClick();
                            Toast.makeText(AlertActivity.this, "Alertas deletados", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        pullAlertButton.performClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            baseURL = b.getString("baseURL");
            publishAlertString = b.getString("publishAlertString");
            pullAlertString = b.getString("pullAlertString");
            alertEntities = b.getStringArrayList("alertEntities");

        } else {
            Log.d(TAG, "Não foi possível obter os dados do intent");
        }

        pullAlertButton = findViewById(R.id.atualizaAlerta);
        deleteButton = findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "Deleting alertas");
                new HttpPostAsyncTask(postData, RequestType.REFRESH_ALERT, callback)
                        .execute(baseURL + alertDeleteString + "?entity=" + entityAlert);
            }
        });
        final Spinner spinnerAlert = findViewById(R.id.spinnerAlert);

        ArrayAdapter<String> spinnerAlertAdapter =
                new ArrayAdapter<String> (AlertActivity.this, android.R.layout.simple_spinner_item, alertEntities);
        spinnerAlertAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAlert.setAdapter(spinnerAlertAdapter);
        spinnerAlert.setOnItemSelectedListener(this);
        final ListView listaAlerta = findViewById(R.id.listaAlerta);


        adapterAlert = new AlertAdapter(this,
                R.layout.activity_layout_list_alert, alertaList);
        listaAlerta.setAdapter(adapterAlert);


        listaAlerta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Pos i: " + id);
                Alert selectedAlert = (Alert) listaAlerta.getAdapter().getItem(position);
                String pos = selectedAlert.getPos();
                Log.d(TAG, "pos: " + pos);
                Intent intent = new Intent(AlertActivity.this, MapsActivity.class);
                String positions[] = pos.split(",");
                Bundle b = new Bundle();
                b.putDouble("latitude", Double.parseDouble(positions[0]));
                b.putDouble("longitude", Double.parseDouble(positions[1]));
                intent.putExtras(b);
                startActivity(intent);


            }
        });

        pullAlertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Map<String, String> postData = new HashMap<>();
                postData.put("entity", "");
                new HttpPostAsyncTask(postData, RequestType.ALERT_PULL, callback)
                        .execute(baseURL + pullAlertString + "?entity=" + spinnerAlert.getSelectedItem().toString());

            }
        });
        pullAlertButton.performClick();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pullAlertButton.performClick();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void completionHandler(Boolean success, RequestType type, Object object) {

    }
}
