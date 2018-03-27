package com.conecta.restserver;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 1765 IRON on 26/03/2018.
 */

public class HttpPostAsyncTask extends AsyncTask<String, Void, Void> {
    JSONObject postData;
    RequestType type;
    CustomCallback callback;


    // HttpPostAsyncTask task = new HttpPostAsyncTask(postData, this, RequestType.REQUEST_TYPE_1);
    public HttpPostAsyncTask(Map<String, String> postData,
                             RequestType type, CustomCallback callback) {
        if (postData != null) {
            this.postData = new JSONObject(postData);
            this.type = type;
            this.callback = callback;
        }
    }


   @Override
    protected Void doInBackground(String... params) {
        String TAG = "RestServer - AsyncTask";
        try {
            URL url = new URL(params[0]);
            Log.d(TAG, "url: "+ url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
           //urlConnection.setDoInput(true); urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
/*
            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }
            */
           int statusCode = urlConnection.getResponseCode();
          if (statusCode ==  200) {
              Log.d (TAG, "Statuscode 200" );
              InputStream responseBody = urlConnection.getInputStream();
              InputStreamReader responseBodyReader =
                      new InputStreamReader(responseBody, "UTF-8");
              JsonReader jsonReader = new JsonReader(responseBodyReader);
                 switch (type) {
                     case REFRESH_ALERT:
                         callback.completionHandler(true, type, null);
                         break;
                     case REFRESH_POS:
                         callback.completionHandler(true, type, null);
                         break;
                     case TRAKERPOS_PULL:
                      Log.d (TAG, "TRACKERPOS" );
                      List<TrackerPos> trackerPosList = new ArrayList<>();
                      TrackerPos trackerPos;
                      jsonReader.beginArray();
                      while (jsonReader.hasNext()) {
                          trackerPos = readerTrackerPos(jsonReader);
                          trackerPosList.add(trackerPos);
                      }
                      jsonReader.endArray();
                      callback.completionHandler(true, type, trackerPosList);
                      Log.d (TAG, "trackerPosList: " + trackerPosList);
                      break;
                  case ALERT_PULL:
                      Log.d (TAG, "ALERT" );
                      final List<Alert> alertaList = new ArrayList<>();
                      Alert alert = new Alert("Pos", "Mov", "Giro", "Time");
                      alertaList.add(alert);
                      jsonReader.beginArray();
                      while (jsonReader.hasNext()) {
                          alert = readerAlert(jsonReader);
                          alertaList.add(alert);
                      }
                      jsonReader.endArray();
                      callback.completionHandler(true, type, alertaList);
                      Log.d (TAG, "alertaList: " + alertaList);
                      break;
                     case CONFIG_GIRO_PULL:
                         Log.d (TAG, "CONFIG_PULL" );
                         final List<String> configGiroList = new ArrayList<>();
                         jsonReader.beginArray();
                         while (jsonReader.hasNext()) {
                             configGiroList.add(jsonReader.nextString());
                         }
                         jsonReader.endArray();
                         callback.completionHandler(true, type, configGiroList);
                         Log.d (TAG, "alertaList: " + configGiroList);
                         break;
                     case CONFIG_GPS_PULL:
                         Log.d (TAG, "CONFIG_GPS_PULL" );
                         final List<String> configGpsList = new ArrayList<>();
                         jsonReader.beginArray();
                         while (jsonReader.hasNext()) {
                             configGpsList.add(jsonReader.nextString());
                         }
                         jsonReader.endArray();
                         callback.completionHandler(true, type, configGpsList);
                         Log.d (TAG, "alertaList: " + configGpsList);
                         break;

                  default:
                      break;
              }

              jsonReader.close();
              urlConnection.disconnect();

            } else {
              Log.d(TAG,"Código <> 200");
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return null;

    }

    public Alert readerAlert (JsonReader reader) throws IOException {
        String pos = null;
        String giro = null;
        String mov = null;
        String time = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextString();
            } else if (name.equals("giro")) {
                giro = reader.nextString();
            } else if (name.equals("mov")) {
                mov = reader.nextString();
            } else if (name.equals("time")) {
                time = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Alert(pos, giro, mov, time);
    }


    public TrackerPos readerTrackerPos(JsonReader reader) throws IOException {
        String pos = null;
        String time = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextString();
            } else if (name.equals("time")) {
                time = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new TrackerPos(pos, time);
    }
}