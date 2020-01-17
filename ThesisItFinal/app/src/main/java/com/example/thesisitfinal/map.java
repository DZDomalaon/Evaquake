package com.example.thesisitfinal;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
public class map extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener
{

    //evacuationcenter.000webhostapp.com <----- website name
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private DirectionsRoute currentRoute;
    private LocationComponent locationComponent;
    private NavigationMapRoute navigationMapRoute;
	private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private Button button;
    private URL url;
    private HttpURLConnection httpURLConnection;
    private String data;
    private String lat;
    private String lon;
    private LatLng[] locations = new LatLng[50];
    List<Feature> features = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    //need change
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) 
	{
        map.this.mapboxMap = mapboxMap;
        map.this.mapboxMap.setMinZoomPreference(15);
        mapboxMap.addOnMapClickListener(map.this);
        backgroundExecute();
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day),
                new Style.OnStyleLoaded()
                {
                    @Override
                    public void onStyleLoaded(@NonNull Style style)
                    {
                        style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                                map.this.getResources(), R.drawable.mapbox_marker_icon_default));
                        addMarkers(style);
                        enableLocationComponent(style);

                        button = findViewById(R.id.navButton);
                        button.setOnClickListener(new View.OnClickListener() {
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
                    }
                });
    }
	
	private void addMarkers(@NonNull Style loadedMapStyle) 
	{
        backgroundExecute();
        //features.add(Feature.fromGeometry(Point.fromLngLat(125.6348, 7.1149)));
        //features.add(Feature.fromGeometry(Point.fromLngLat(125.605769, 7.064497)));

        /* Source: A data source specifies the geographic coordinate where the image marker gets placed. */

        loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(features)));

        /* Style layer: A style layer ties together the source and image and specifies how they are displayed on the map. */
        loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE)
                .withProperties(
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconImage(MARKER_IMAGE)
                ));
    }


    //do not change
    @Override
    public boolean onMapClick(@NonNull LatLng point)
    {
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs(MARKER_SOURCE);
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));

        }
        getRoute(originPoint, destinationPoint);
        button.setEnabled(true);
        button.setBackgroundResource(R.color.mapboxBlue);

        return true;
    }

    // do not change
    public void getRoute(Point origin, Point destination)
    {
        NavigationRoute.builder(this)
                .accessToken(getString((R.string.access_token)))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> 
					response) {
                        if(response.body() == null)
                        {
                            Timber.e("No routes found, make sure you set hte right user and access token");
                        }
                        else if (response.body().routes().size() < 1)
                        {
                            Timber.e("No routes found");
                        }
                        currentRoute = response.body().routes().get(0);

                        if(navigationMapRoute != null)
                        {
                            navigationMapRoute.removeRoute();
                        }
                        else
                        {
                            navigationMapRoute = new NavigationMapRoute(null,mapView,mapboxMap, 
							R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Timber.e("Error: " + t.getMessage());
                    }
                });
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    public void backgroundExecute()
    {
        new backgroundTask().execute();
    }

    class backgroundTask extends AsyncTask<Void, Void, List<Feature>>
    {
        @Override
        protected List<Feature> doInBackground(Void... voids)
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
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                int response_code = httpURLConnection.getResponseCode();
                if(response_code == HttpURLConnection.HTTP_OK)
                {
                    InputStream inputStream = httpURLConnection.getInputStream(); // <--- read the data from the connection
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // <-- read the data from the stream
                    String check;

                    while((check = bufferedReader.readLine()) != null)
                    {
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
                        features.add(Feature.fromGeometry(Point.fromLngLat(Double.parseDouble(lat), Double.parseDouble(lon))));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            finally
            {
                httpURLConnection.disconnect();
            }
            return features;
        }
    }
}