package com.conecta.restserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
    Button pullAlertButton;

    CustomCallback callback = new CustomCallback() {
        @Override
        public void completionHandler(Boolean success, RequestType type, final Object object) {
            Log.d(TAG, "completionJhandler");
            switch (type) {
                case ALERT_PULL:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            alertaList.clear();
                            alertaList.addAll((List<Alert>) object);
                            adapterAlert.notifyDataSetChanged();
                            Log.d(TAG, "alertaLIst" + alertaList.toString());
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
