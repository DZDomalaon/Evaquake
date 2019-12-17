package com.example.thesisitfinal;

import android.os.AsyncTask;

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

public class getData extends AsyncTask<Void,Void,Void>
{
    String data = "";
    String lat = "";
    String lon = "";
    String parsedData = "";
    @Override
    protected Void doInBackground(Void... voids)
    {
        try {
            URL url = new URL("https://evacuationcenter.000webhostapp.com/getData.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream(); // <--- read the data from the connection
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // <-- read the data from the stream

            String check = "";
            while (check != null)
            {
                check = bufferedReader.readLine();
                data += check;
            }

            JSONArray JA = new JSONArray(data);

            for(int i=0; i<JA.length();i++)
            {
                JSONObject JO = (JSONObject) JA.get(i);
                lat = "Latitude: " + JO.get("Lat") + "\n" +
                      "Longitude: " + JO.get("Lon") + "\n" ;


                parsedData += lat + " " + lon;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        test.txtData.setText(this.parsedData);
    }
}
