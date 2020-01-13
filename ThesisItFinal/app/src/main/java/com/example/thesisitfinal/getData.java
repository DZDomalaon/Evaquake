package com.example.thesisitfinal;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.mapbox.geojson.Feature;
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
import java.util.ArrayList;
import java.util.List;

public class getData extends AsyncTask<Void,Void,Void>
{
    String data = "";
    String lat = "";
    String lon = "";
    String parsedData = "";
    List<Feature> evacCenter = new ArrayList<>();
    private static final LatLng[] locations = new LatLng[50];
    @Override
    protected Void doInBackground(Void... voids)
    {
        try {
            URL url = new URL("https://evacuationcenter.000webhostapp.com/getData.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream(); // <--- read the data from the connection
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // <-- read the data from the stream

            String check = ""; // <--- variable for checking the bufferedReader
            while (check != null)
            {
                check = bufferedReader.readLine();
                data += check;
            }

            JSONArray JA = new JSONArray(data);
            //evacCenter.add(Feature.fromGeometry(Point.fromLngLat(125.6348, 7.1149)));
            //evacCenter.add(Feature.fromGeometry(Point.fromLngLat(125.605769, 7.064497)));
            for(int i=0; i<JA.length();i++)
            {
                JSONObject JO = (JSONObject) JA.get(i);
                lat = JO.get("Lat").toString();
                lon = JO.get("Lon").toString();
                locations[i] = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                //evacCenter.add(Feature.fromGeometry(Point.fromLngLat(Double.parseDouble(lon), Double.parseDouble(lat))));
                //parsedData += locations[i];
            }
            parsedData += locations[0].toString();



        // TODO Auto-generated catch block
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        test.txtData.setText(this.parsedData);
    }
}
