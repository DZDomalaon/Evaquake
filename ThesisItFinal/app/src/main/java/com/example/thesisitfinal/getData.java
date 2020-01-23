package com.example.thesisitfinal;

import android.os.AsyncTask;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.thesisitfinal.test.txtData;


class getData extends AsyncTask<Void, Void, LatLng[]>
{
    URL url;
    String readLine;
    String data;
    HttpURLConnection httpURLConnection;
    LatLng[] locations = new LatLng[50];
    Double lat;
    Double lon;

    @Override
    protected LatLng[] doInBackground(Void... voids) {
        try {
            url = new URL("https://evacuationcenter.000webhostapp.com/getData.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream inputStream = httpURLConnection.getInputStream(); // <--- read the data from the connection
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // <-- read the data from the stream
            StringBuilder stringBuilder = new StringBuilder();

            while ((readLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(readLine);
            }
            inputStream.close();
            bufferedReader.close();

            data = stringBuilder.toString();

            JSONArray JA = new JSONArray(data);

            //features.add(Feature.fromGeometry(Point.fromLngLat(125.605769, 7.064497)));

            for (int i = 0; i < JA.length(); i++) {
                JSONObject JO = JA.getJSONObject(i);
                lat = Double.parseDouble(JO.getString("Lat"));
                lon = Double.parseDouble(JO.getString("Lon"));
                locations[i] = new LatLng(lat, lon);
                //features.add(Feature.fromGeometry(Point.fromLngLat(lon, lat)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locations;
    }

    @Override
    protected void onPostExecute(LatLng[] l)
    {
        txtData.setText(l[0].toString());
    }
}
