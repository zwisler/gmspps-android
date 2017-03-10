package com.citaurus.gmspps;

import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;

//import android.location.LocationListener;

/**
 * Created by rzwisler on 05.10.2015.
 */
public class StateService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //PowerManager.WakeLock wakeLock;
    public static final String TAG = StateService.class.getSimpleName();

    private LocationManager lm;
    //private Thread triggerService;
    private GMSPPSClient Client;// = new GMSPPSClient(this, "http://gmspps.azurewebsites.net");
    public static final String STATUS = "Status";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String NOTIFICATION = "com.citaurus.doit4you";
    private SharedPreferences mPrefs;


    /* Client for accessing Google APIs */
    //private GoogleApiClient mGoogleApiClient;


    private static Timer timer = new Timer();
   // private Context ctx;
    private int mystatus;
    private Location myLastLocation;
    // GPSTracker class
   // private GPSTracker gps;

    private double latitude;
    private double longitude;

    private double Nextlatitude;
    private double Nextlongitude;

    private String Lat = "";
    private String Lon = "";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    private boolean mInProgress;

    private LocationRequest mLocationRequest;



    public void onCreate(){
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3600 * 1000)        // 3 seconds, in milliseconds
                .setFastestInterval(60 * 1000); // 1/2 second, in milliseconds


    }

    private void RestoreState() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        Nextlatitude = mPrefs.getFloat("Nextlatitude", 13);
        Nextlongitude = mPrefs.getFloat("Nextlongitude", 48);
        mystatus = mPrefs.getInt(MainActivity.KEY_STATUS, 1);

    }
    public StateService() {
        // TODO Auto-generated constructor stub
        //this.ctx = context;
    }
    //runs without a timer by reposting this handler at the end of the runnable
    /*
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            setUpLocationClientIfNeeded();

            sendStateandPosition();
           // publishState(Lat, Lon, mystatus);
            timerHandler.postDelayed(this, 5000);

        }
    };
    */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try
        {
        RestoreState();
        Client = new GMSPPSClient(this, "http://gmspps.azurewebsites.net");
        String AuthHeader = intent.getStringExtra("gnspps_auth");
        mystatus = intent.getIntExtra(STATUS, 1);


        switch (mystatus) {
            case 1:
                // Status1

                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(300 * 1000)        // 30 seconds, in milliseconds
                        .setFastestInterval(60 * 1000)
                        .setSmallestDisplacement(200);
                         // 1 second, in milliseconds

                break;

            case 2:
                // Status2

                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(300 * 1000)        // 30 seconds, in milliseconds
                        .setFastestInterval(10 * 1000) // 1 second, in milliseconds
                .setSmallestDisplacement(50);

                break;
            case 3:
                // Status3
                // Create the LocationRequest object

                //Nextlatitude = intent.getDoubleExtra("Nextlatitude", 13);
                //Nextlongitude = intent.getDoubleExtra("Nextlongitude", 48);
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(3 * 1000)        // 3 seconds, in milliseconds
                        .setFastestInterval(1 * 500) // 1/2 second, in milliseconds
                .setSmallestDisplacement(5);
                break;
            case 4:
                // Status3
                //Nextlatitude = intent.getDoubleExtra("Nextlatitude", 13);
                //Nextlongitude = intent.getDoubleExtra("Nextlongitude", 48);

                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(300 * 1000)        // 30 seconds, in milliseconds
                        .setFastestInterval(60 * 1000)
                .setSmallestDisplacement(5);

                break;
            case 6:
                // Status6
                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(3600 * 1000)        // 3 seconds, in milliseconds
                        .setFastestInterval(3600 * 1000); // 1/2 second, in milliseconds
                break;

        }

/*
        if(mystatus >= 3 ) {
            timerHandler.postDelayed(timerRunnable, 0);
        }else {
            timerHandler.removeCallbacks(timerRunnable);
        }
        */

        Client.setAuthorizationHeader(AuthHeader);

        sendStateandPosition();
        // GOOGLE
        if(mGoogleApiClient.isConnected() || mInProgress) {
           // setUpLocationClientIfNeeded();
           setUpLocationClientIfNeeded();

            return START_STICKY;
        }
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress) {
            mInProgress = true;
            mGoogleApiClient.connect();
        }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }


    private void  sendStateandPosition() {

        new SetStateAsyncTask().execute(String.valueOf(mystatus), Lat, Lon);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    private void publishMsg(String Msg) {
        try {

            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra("msg", Msg);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    private void publishDist(double dist) {
        try {
            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra("dist", dist);
            intent.putExtra("mystatus", mystatus);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void publishState() {
        try {
            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra("mystatus", mystatus);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNewLocation(Location location) {
        try {
        Log.d(TAG, location.toString());
        int c=0;
        /*
        for(int i=0;i<alarms.size();i++)
        {
            if(alarms.get(i).getActive())
            {
                break;
            }
            else{
                c++;
            }
        }
        */
       // if(c==alarms.size())          stopSelf();

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Lat = String.valueOf(latitude);
        Lon = String.valueOf(longitude);
            if (mystatus == 3)
            {
                // Distanz ermitteln

                Location loc = new Location("dummyprovider");
                loc.setLatitude(Nextlatitude);
                loc.setLongitude(Nextlongitude);
                double distanz = location.distanceTo(loc);
                publishDist(distanz);
            }
        publishMsg("Positon geänder! LAT: " + Lat );
        //LatLng latLng = new LatLng(latitude, latitude);
            if(mystatus != 6){
                sendStateandPosition();
                publishState();

            }

        /*
        for(int i=0;i<alarms.size();i++)
        {
            if(alarms.get(i).getActive() && (distanceCal(latLng, alarms.get(i).getLatlng())<=alarms.get(i).getDistance()))
            {
                setOffAlarm();
            }
        }
        */
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    private void setUpLocationClientIfNeeded() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {
            Log.d("Error Dialog ", e.getLocalizedMessage());
        }


        //Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
       // if (location == null) {

        //}
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
        setUpLocationClientIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
        myLastLocation = location;
        handleNewLocation(myLastLocation);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
        // * Google Play services can resolve some errors it detects.
        // * If the error has a resolution, try sending an Intent to
        // * start a Google Play services activity that can resolve
        //* error.

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(null, CONNECTION_FAILURE_RESOLUTION_REQUEST);

                // * Thrown if Google Play services canceled the original
                // * PendingIntent

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {

            //* If no resolution is available, display a dialog to the
            // * user with the error.
            publishMsg("Location services connection failed with code " + connectionResult.getErrorCode());

            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }




    private class SetStateAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Client.SetState(params[0], params[1], params[2]);
            } catch (Exception e) {
                e.printStackTrace();
                //return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Status wurde gesendet

        }
    };

}
