package com.example.thesisitfinal;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

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
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
public class map extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, LocationRecyclerViewAdapter.ItemClickListener
{

    //evacuationcenter.000webhostapp.com <----- website name
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private LocationComponent locationComponent;
    private NavigationMapRoute navigationMapRoute;
    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private static final String PERSON_ICON_ID = "PERSON_ICON_ID";
    private static final String PERSON_SOURCE_ID = "PERSON_SOURCE_ID";
    private static final String PERSON_LAYER_ID = "PERSON_LAYER_ID";
    private static final String DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID = "DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID";
    private static final String DASHED_DIRECTIONS_LINE_LAYER_ID = "DASHED_DIRECTIONS_LINE_LAYER_ID";
    private Button button;
    private URL url;
    private HttpURLConnection httpURLConnection;
    private String data = "";
    private Double lat;
    private Double lon;
    private String readLine = "";
    private String ne;
    private LatLng[] possibleLocations;
    public static String[] evacNames;
    private Point originPoint;
    private JSONArray JA;
    List<Feature> features = new ArrayList<>();
    ProgressDialog progressDialog;
    private FeatureCollection dashedFeatureCollection;;
    List<DirectionsRoute> directionsRouteList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        backgroundTask BT = new backgroundTask();
        try {
            BT.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        map.this.mapboxMap = mapboxMap;
        map.this.mapboxMap.setMinZoomPreference(15);
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day),
                new Style.OnStyleLoaded()
                {
                    @Override
                    public void onStyleLoaded(@NonNull Style style)
                    {
                        enableLocationComponent(style);
                        originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                                locationComponent.getLastKnownLocation().getLatitude());
                        initDestinationLocation();
                        initDestinationName();
                        initDestinationFeatureCollection();
                        initRecyclerView();

                        originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                                locationComponent.getLastKnownLocation().getLatitude());
                        style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                                map.this.getResources(), R.drawable.mapbox_marker_icon_default));
                        style.addSource(new GeoJsonSource(MARKER_SOURCE, initDestinationFeatureCollection()));
                        style.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE).withProperties(
                                iconImage(MARKER_IMAGE),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)));

                        style.addSource(new GeoJsonSource(PERSON_SOURCE_ID, Feature.fromGeometry(originPoint)));
                        style.addLayer(new SymbolLayer(PERSON_LAYER_ID, PERSON_SOURCE_ID).withProperties(
                                iconImage(PERSON_ICON_ID),
                                iconSize(2f),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)));

                        style.addSource(new GeoJsonSource(DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID));
                        style.addLayerBelow(new LineLayer(DASHED_DIRECTIONS_LINE_LAYER_ID, DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID)
                                .withProperties(
                                        lineWidth(7f),
                                        lineJoin(LINE_JOIN_ROUND),
                                        lineColor(Color.parseColor("#2096F3"))
                                ), PERSON_LAYER_ID);

                        button = findViewById(R.id.navButton);
                        /*
                        button.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v) {
                                boolean simulateRoute = false;
                                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                        .directionsRoute(currentRoute)
                                        .shouldSimulateRoute(simulateRoute)
                                        .build();
                                // Call this method with Context from within an Activity
                                NavigationLauncher.startNavigation(map.this, options);
                            }
                        });

                         */
                        Toast.makeText(map.this, "" + features.get(0).getStringProperty("name"), Toast.LENGTH_SHORT).show();
                        getNearest();
                    }
                });
    }

    public void getRoute(Point destination)
    {
        MapboxDirections client = MapboxDirections.builder()
                .origin(originPoint)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString((R.string.access_token)))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set hte right user and access token");
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                }
                DirectionsRoute currentRoute;
                currentRoute = response.body().routes().get(0);
                drawPolyline(currentRoute);
            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t)
            {
                Timber.e("Error: " + t.getMessage());
            }
        });
    }

    public void initDestinationLocation()
    {
        try
        {
            JSONArray JA = new JSONArray(data);
            possibleLocations = new LatLng[JA.length()];
            evacNames = new String[JA.length()];
            //features.add(Feature.fromGeometry(Point.fromLngLat(125.605769, 7.064497)));

            for (int i = 0; i < JA.length(); i++)
            {
                JSONObject JO = JA.getJSONObject(i);
                lat = Double.parseDouble(JO.getString("Lat"));
                lon = Double.parseDouble(JO.getString("Lon"));
                possibleLocations[i] = new LatLng(lat,lon);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void initDestinationName()
    {
        try
        {
            JSONArray JA = new JSONArray(data);
            evacNames = new String[JA.length()];

            for (int i = 0; i < JA.length(); i++)
            {
                JSONObject JO = JA.getJSONObject(i);
                evacNames[i] = JO.getString("locationName");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private FeatureCollection initDestinationFeatureCollection()
    {
        try
        {
            JSONArray JA = new JSONArray(data);
            for (int i = 0; i < JA.length(); i++)
            {
                JSONObject JO = JA.getJSONObject(i);
                lat = Double.parseDouble(JO.getString("Lat"));
                lon = Double.parseDouble(JO.getString("Lon"));
                features.add(Feature.fromGeometry(Point.fromLngLat(lon, lat)));
                features.get(i).addStringProperty("name", evacNames[i]);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {}
        return FeatureCollection.fromFeatures(features);
    }

    private void getNearest()
    {
        Double closestEvac = Double.MAX_VALUE;
        int indexOfNearest = 0;

        for(int i = 0; i < possibleLocations.length; i++)
        {
            Double dist = Math.pow(Math.abs(
                    locationComponent.getLastKnownLocation().getLatitude() -
                    possibleLocations[i].getLatitude()), 2) +
                    Math.pow(Math.abs(locationComponent.getLastKnownLocation().getLongitude() -
                    possibleLocations[i].getLongitude()), 2);

            if(dist < closestEvac)
            {
                closestEvac = dist;
                indexOfNearest = i;
            }
        }

        getRoute((Point) features.get(indexOfNearest).geometry());
    }

    public void drawPolyline(final DirectionsRoute route)
    {
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs(DASHED_DIRECTIONS_LINE_LAYER_SOURCE_ID);
        if (source != null)
        {
            source.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
                    LineString.fromPolyline(route.geometry(), PRECISION_6))));
        }
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_on_top_of_map);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new LocationRecyclerViewAdapter(this,
                createRecyclerViewLocations(), mapboxMap));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
    }

    private List<SingleRecyclerViewLocation> createRecyclerViewLocations() {
        ArrayList<SingleRecyclerViewLocation> locationList = new ArrayList<>();
        for (int x = 0; x < possibleLocations.length; x++) {
            SingleRecyclerViewLocation singleLocation = new SingleRecyclerViewLocation();
            singleLocation.setName(evacNames[x]);
            locationList.add(singleLocation);
        }
        return locationList;
    }

    @Override
    public void onItemClick(int position)
    {
        try
        {
            getRoute((Point) features.get(position).geometry());
        }
        catch (Exception e)
        {
            Toast.makeText(this, "" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle)
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {

    }

    @Override
    public void onPermissionResult(boolean granted)
    {
        if (granted)
        {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressLint("StaticFieldLeak")
    class backgroundTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(map.this);
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            try
            {
                url = new URL("https://evacuationcenter.000webhostapp.com/getData.php");
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

            try
            {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.connect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                InputStream inputStream = httpURLConnection.getInputStream(); // <--- read the data from the connection
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // <-- read the data from the stream
                StringBuilder stringBuilder = new StringBuilder();

                while ((readLine = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(readLine);
                }
                inputStream.close();
                bufferedReader.close();

                data = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpURLConnection.disconnect();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String features)
        {
            progressDialog.dismiss();
            super.onPostExecute(features);
        }
    }
}