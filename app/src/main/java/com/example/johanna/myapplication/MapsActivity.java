package com.example.johanna.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;

import android.location.LocationManager;
import android.location.LocationProvider;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//FramgmentActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static Double RADIUS = 5000.0;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    public static final String BASE_URL = "http://swlab-ws1617.iap.hs-heilbronn.de/api/v0.1/";

    //public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private LocationProvider mLocProvider;
    private LocationManager mLocManager;

    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleApiClient mGAPIClient;
    private Location mLocation;
    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;
    private LocationRequest mLocationRequest;
    private GoogleApiClient.ConnectionCallbacks callbacks;
    private Gson gson;
    private Retrofit mRetrofit;
    private RetrofitAPI mRetrofitAPI;
    private MyLocationListener mLocationListener;
    private Marker marker;
    private Marker oldMarker;
    private EditText answer;
    private List<NeighbourLocations> allNeigbourLocations;
    private List<Marker> neigbourMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //mLocManager.requestLocationUpdates(mLocProvider,1000, 200.0,locationListener);
        mLocProvider = mLocManager.getProvider(LOCATION_SERVICE);
        mLocationListener = new MyLocationListener(this);


        //mLocation = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //this.addContentView(R.menu.menu_map, ( new ActionMenuView.LayoutParams.MATCH_PARENT);
        mapFragment.setHasOptionsMenu(true);
        mapFragment.setMenuVisibility(true);

        gson = new GsonBuilder().setDateFormat("'lat', 'lon'").create();                //evtl?
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))                 //evtl?
                .build();
        mRetrofitAPI = mRetrofit.create(RetrofitAPI.class);

        allNeigbourLocations = new ArrayList<NeighbourLocations>();
        neigbourMarkers = new ArrayList<Marker>();

        createGoogleApiClient();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public void createGoogleApiClient() {

        mGAPIClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,
                        onConnectionFailedListener)
                .addApi(LocationServices.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        mGAPIClient.registerConnectionCallbacks(this);
        mGAPIClient.registerConnectionFailedListener(this);
        mGAPIClient.connect();
    }

    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }


    /**
     *
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.updaterate) {                                                                //item updaterate
            answer = new EditText(getApplicationContext());
            answer.setTextColor(Color.BLACK);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Please enter an update Interval >1000");
            builder.setTitle("Updaterate");
            builder.setView(answer);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Long interval = Long.parseLong(answer.getText().toString());
                    mLocationRequest.setFastestInterval(interval);
                    mLocationRequest.setInterval(interval);
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGAPIClient, mLocationRequest, mLocationListener);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setView(answer);
            dialog.show();
            return true;
        }


        if (id == R.id.renameUser) {                                                    //item rename user
            answer = new EditText(getApplicationContext());
            answer.setTextColor(Color.BLACK);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Please enter your new Username");
            builder.setTitle("Rename User");
            builder.setView(answer);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newUsername = answer.getText().toString();
                    renameUser(newUsername);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setView(answer);
            dialog.show();
            return true;
        }
        if (id == R.id.searchUser){                                                         //search for user item
            answer = new EditText(getApplicationContext());
            answer.setTextColor(Color.BLACK);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Wich user do you want to search?");
            builder.setTitle("Search User");
            builder.setView(answer);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NeighbourLocations found = searchForUser(answer.getText().toString());
                    if(found == null){
                        Toast.makeText(getApplicationContext(), "Can\'t find user", Toast.LENGTH_LONG).show();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setView(answer);
            dialog.show();
            return true;

        }
        if (id == R.id.switchUser){                                                     //switch user item
            switchToStartActivity();
        }

        if(id == R.id.changeRadius){
            answer = new EditText(getApplicationContext());
            answer.setTextColor(Color.BLACK);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Change Radius");
            builder.setMessage("In what radius do you want to search? (km)");
            builder.setView(answer);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RADIUS = Double.parseDouble(answer.getText().toString());
                    getNeighbourLocations(mLocation);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setView(answer);
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeNeigbourMarkers(){
        for(int i=0; i<neigbourMarkers.size(); i++){
            neigbourMarkers.get(i).remove();
        }
    }


    /**
     * switch to StartActivity
     */
    private void switchToStartActivity() {
        //switch to start layout
        StartActivity startActivity = new StartActivity();
        Intent toStart = new Intent(MapsActivity.this, StartActivity.class);
        MapsActivity.this.startActivity(toStart);
    }


    /**
     * method for current location
     * @param location
     */
    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mLocation = location;
        if(marker!=null){
            oldMarker = marker;
            oldMarker.remove();
        }
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        marker = mMap.addMarker(options);
        sendLastLocation(location);
    }

    /**
     * zoom to my location
     * @param location
     */

    public void zoomTo(Location location){
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }

    /**
     * zoom to other user
     * @param location
     */
    public void zoomTo(LocationContent location){
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }


    /**
     * handle others location
     * @param location
     * @param name
     */
    private void handleNewLocation(LocationContent location, String name) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(name);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        neigbourMarkers.add(mMap.addMarker(options));

        //marker.remove();
    }


    /**
     * connection callback
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationRequest = mLocationListener.createLocationRequest();
        //Location location = LocationServices.FusedLocationApi.getLastLocation(mGAPIClient);
        //handleNewLocation(location);
        //getNeighbourLocations(location);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGAPIClient,mLocationRequest,mLocationListener);
    }

    /**
     * connection suspended
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Sorry, connection suspended", Toast.LENGTH_LONG).show();
        mGAPIClient.disconnect();
    }

    /**
     * connection failed
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Cannot find GoogleAPI", Toast.LENGTH_LONG).show();
    }


    /**
     * server communication
     * @param location
     */
    public void sendLastLocation(Location location) {
        mLocation = location;
        String ldap = getIntent().getExtras().getString("ldap");
        String token = getIntent().getExtras().getString("authToken");
        //ldap = getIntent().getExtras().get("ldap").toString();
        //authToken = getIntent().getExtras().get("authToken").toString();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //String longitude = String.valueOf(location.getLongitude());
        LocationContent callContent = new LocationContent(latitude, longitude);
        Call<ResponseBody> locUpdate = mRetrofitAPI.sendLocationUpdatePost(token, callContent);
        locUpdate.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    //Toast.makeText(getApplicationContext(), "Update erfolgreich", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * get neighbour locations
     * @param location
     */
    public void getNeighbourLocations(final Location location) {
        if(neigbourMarkers!=null){
            removeNeigbourMarkers();
        }
        String authToken = getIntent().getExtras().getString("authToken").toString();
        final String ldap = getIntent().getExtras().getString("ldap").toString();
        final String username = getIntent().getExtras().get("username").toString();
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Call<ArrayList<NeighbourLocations>> neighbourCall = mRetrofitAPI.getUsersLocations(authToken, RADIUS, lat, lon);
        neighbourCall.enqueue(new Callback<ArrayList<NeighbourLocations>>() {
            @Override
            public void onResponse(Call<ArrayList<NeighbourLocations>> call, Response<ArrayList<NeighbourLocations>> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    double lat = 0.0;
                    double lon = 0.0;
                    String name = ":)";

                    for (int i = 0; i < response.body().size(); i++) {

                        NeighbourLocations neighbours = response.body().get(i);
                        lat = neighbours.locations.get(0).getLatitude();
                        lon = neighbours.locations.get(0).getLongitude();
                        name = neighbours.name.toString();
                        LocationContent loc = new LocationContent(lat, lon);
                        if (!name.equals(username)) {
                            handleNewLocation(loc, name);
                        }

                        //speichern der gelieferten daten in ArrayList lastLocations
                        List<LocationContent> lastLocations = new ArrayList<LocationContent>();
                        lastLocations.add(loc);
                        NeighbourLocations currentNeigbour = new NeighbourLocations(name, null,lastLocations);
                        allNeigbourLocations.add(currentNeigbour);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<NeighbourLocations>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Verbindung fehlgeschalagen", Toast.LENGTH_LONG).show();
            }
        });
    }


    //methods for options menu
    /**
     * change username
     * @param newUsername
     */
    private void renameUser(final String newUsername){
        String ldap = getIntent().getExtras().get("ldap").toString();
        String token = getIntent().getExtras().get("authToken").toString();
        CallContent userContent = new CallContent(newUsername, ldap, null);
        Call<ResponseBody> userUpdate = mRetrofitAPI.sendUserUpdatePOST(token, userContent);
        userUpdate.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(getApplicationContext(), "your new Username: " + newUsername, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Not possible, sorry", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void createAlertDialog(){
        EditText answer = new EditText(getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("Please enter");

    }

    /**
     * search for user
     * @param name
     * @return the user searched for
     */
    public NeighbourLocations searchForUser(String name){
        NeighbourLocations searchedUser;
        for(int i=0; i<allNeigbourLocations.size(); i++){
            searchedUser=allNeigbourLocations.get(i);
            if(searchedUser.getName().equals(name)){
                zoomTo(searchedUser.locations.get(0));
                return searchedUser;
            }
        }
        return null;
    }

}
