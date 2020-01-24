package com.example.thesisitfinal;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.thesisitfinal.test.txtData;


class getData extends AsyncTask<Void, Void, String>
{
    URL url;
    String readLine;
    String data;
    HttpURLConnection httpURLConnection;
    String[] locations;
    Double lat;
    Double lon;

    @Override
    protected String doInBackground(Void... voids) {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String l)
    {
        txtData.setText(l);
    }
}
