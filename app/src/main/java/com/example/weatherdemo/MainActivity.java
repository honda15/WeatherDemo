package com.example.weatherdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private EditText cityEditText;
    private EditText areaEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = findViewById(R.id.cityEditText);
        areaEditText = findViewById(R.id.areaEditText);
        resultTextView = findViewById(R.id.resultTextView);
    }

    public void getWeather(View view) {
        String city = cityEditText.getText().toString();
        String area = areaEditText.getText().toString();
        String openDataUrl = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0001-001?Authorization=CWB-494E5A1D-76E3-4831-B20E-E9AB2F13DBD5&format=JSON";

        new WeatherTask().execute(openDataUrl, city, area);
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String openDataUrl = params[0];
            String city = params[1];
            String area = params[2];

            try {
                URL url = new URL(openDataUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject records = jsonObject.getJSONObject("records");
                    JSONArray locationArray = records.getJSONArray("location");

                    for (int i = 0; i < locationArray.length(); i++) {
                        JSONObject locationObject = locationArray.getJSONObject(i);
                  //      String name = locationObject.getString("locationName");
                        String name = locationObject.getJSONArray("parameter").getJSONObject(0).getString("parameterValue");
                        Log.v("lee"," "+city);
                        if (name.equals(city)) {
                            JSONObject cityData = new JSONObject();
                            String cityArea = locationObject.getJSONArray("parameter").getJSONObject(2).getString("parameterValue");
                            String temp = locationObject.getJSONArray("weatherElement").getJSONObject(3).getString("elementValue");
                            float humd = Float.parseFloat(locationObject.getJSONArray("weatherElement").getJSONObject(4).getString("elementValue")) * 100;
                            String weather = locationObject.getJSONArray("weatherElement").getJSONObject(14).getString("elementValue");

                            cityData.put(cityArea, temp + " 度, 相對濕度 " + humd + "%, 天氣 " + weather);

                            if (cityArea.equals(area)) {
                                return cityData.getString(area);
                            }
                        }
                    }
                } else {
                    return "HTTP請求失敗，響應碼：" + responseCode;
                }

                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "發生錯誤：" + e.getMessage();
            }

            return "找不到相關數據";
        }

        @Override
        protected void onPostExecute(String result) {
            resultTextView.setText(result);
        }
    }
}
