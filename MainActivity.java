package com.citaurus.gmspps;


import android.accounts.Account;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;





import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Minimal activity demonstrating basic Google Sign-In.
 */
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,
        LocationListener{

    public static final String SENDER_ID = "414999757757";
    private GoogleCloudMessaging gcm;
    private NotificationHub hub;
    private String HubName = "gmspps";
    private String HubListenConnectionString = "Endpoint=sb://gmspps-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=Ln0WZ6PG6KnJrdEAikUzedOLm84Rj//QEPRPHeRHpVY=";
    private static Boolean isVisible = false;
    private String name ="Unknown";

    private GMSPPSClient Client;
    public static String AuthorizationHeader;
    public static final String BACKEND_ENDPOINT = "http://gmspps.azurewebsites.net";

    // private static final String SERVER_CLIENT_ID = "414999757757-t0i02p4g2cjlnfpu6bm1valmrj7csfec.apps.googleusercontent.com"; //debug
   // private  String SERVER_CLIENT_ID = "414999757757-meg30nbsf899quqhhubvarf2cjf3guk5.apps.googleusercontent.com"; // für Backend
// is for Provider layout
    private ListView listView1;
    private ListView list;
    private Provider provider[] = null;



    private static final String TAG = "MainActivity";

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 0;
    private static final int RC_GET_TOKEN = 9002;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    public static final String KEY_STATUS = "key_status";
    public static final String KEY_MSTAT = "key_mstat";
    public static final String KEY_MISSION_ID = "key_mission_id";
    public static final String KEY_MISSION_NAME= "key_mission_NAME";
    public static final String KEY_MISSION_URL = "key_mission_url";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";
    private static final String KEY_LAT = "key_lat";
    private static final String KEY_LON = "key_lon";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    private TextView mStatus;
    private TextView InfoStatus;
    private TextView InfoVersion;


    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;


    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    private boolean online = false;


    /* Set true if RegisterClient Ok. */
    private boolean RegisterClient_Ok = false;
    EditText etResponse;
    private String nhMessage;
    private String nhUrl;
    private int nhID;
    public int nhStatus;
    private String nhName;
    private static final String defaultUrl = "http://beta.html5test.com/";
    //private EditText editLocation = null;
    private ProgressBar pb =null;
    private LocationManager locationMangaer=null;
    private LocationListener locationListener=null;
    private Location mLastLocation;

    private LocationRequest mLocationRequest;

    public int userStatus;



    private String Lat = "";
    private String Lon = "";
    private double latitude;
    private double longitude;
    private Intent StateIntent;
    private SharedPreferences mPrefs;

    private int verCode;


    //  Broadcast Recever

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                double distanz = bundle.getDouble("dist", -1);
                int st = bundle.getInt("mystatus", -1);
                String msg = bundle.getString("msg", "NoMsg");
                if(distanz != -1) {
                    mStatus.setText("Distanz zum Ziel: " + distanz );
                }
                if(st != -1) {
                    userStatus = st;
                    UpdateState();
                }
                if(msg != "NoMsg") {
                    Toast.makeText(MainActivity.this,
                            "Info: " + msg,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        RestoreState();
        registerReceiver(receiver, new IntentFilter(StateService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        SaveState();
        unregisterReceiver(receiver);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        RestoreState();

    }

    private void SaveState() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt(KEY_STATUS, userStatus);
        //nur im Einsatz Werte speichern
        if ((userStatus == 3 || userStatus == 4) && nhUrl != "" ) {
            ed.putInt(KEY_MISSION_ID, nhID);
            ed.putString(KEY_MISSION_URL, nhUrl);
            ed.putString(KEY_MISSION_NAME, nhName);
            ed.putLong(KEY_LAT, Double.doubleToRawLongBits(latitude));
            ed.putLong(KEY_LON, Double.doubleToRawLongBits(longitude));
            ed.putInt(KEY_MSTAT, nhStatus);//KEY_MISSION_NAME
        }



        ed.commit();
    }
    private void RestoreState() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        userStatus = mPrefs.getInt(KEY_STATUS, 1); //KEY_MISSION_ID
        if (userStatus == 3 || userStatus == 4 ) {
            nhID = mPrefs.getInt(KEY_MISSION_ID, -1);
            nhUrl  = mPrefs.getString(KEY_MISSION_URL, "");
            nhName  = mPrefs.getString(KEY_MISSION_NAME, "Currend Mission");
            nhStatus = mPrefs.getInt(KEY_MSTAT, -1);
            latitude = Double.longBitsToDouble(mPrefs.getLong(KEY_LAT, Double.doubleToLongBits(0)));
            longitude = Double.longBitsToDouble(mPrefs.getLong(KEY_LON, Double.doubleToLongBits(0)));
        }
       // UpdateState();
    }
    private void getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    private void refreshIdToken() {
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

        if (opr.isDone()) {
            // Users cached credentials are valid, GoogleSignInResult containing ID token
            // is available immediately. This likely means the current ID token is already
            // fresh and can be sent to your server.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently and get a valid
            // ID token. Cross-device single sign on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String idToken = result.getSignInAccount().getIdToken();
            //todo use the Token now
            //mIdTokenTextView.setText(getString(R.string.id_token_fmt, idToken));
            // Signed in successfully, show authenticated UI.
            MainActivity.this.Client.setAuthorizationHeader(idToken);

            AuthorizationHeader = MainActivity.this.Client.getAuthorizationHeader();
            setAuth(AuthorizationHeader,  getApplicationContext());

            mStatus.setText("Get Suscriptions");
            pb.setVisibility(View.VISIBLE);
            new GetSuscriptionAsyncTask().execute();
            GoogleSignInAccount acct = result.getSignInAccount();
            name =  acct.getDisplayName();
            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "signOut:onResult:" + status);
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "revokeAccess:onResult:" + status);
                        updateUI(false);
                    }
                });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyHandler.mainActivity = this;
        online = ConnectionUtils.isConnected(this);
        NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);
        gcm = GoogleCloudMessaging.getInstance(this);
        /*try {
            String regid = gcm.register(SENDER_ID);
            DialogNotify("Registered Successfully","RegId : " +regid);
        } catch (Exception e) {
            DialogNotify("Exception",e.getMessage());

        } */
        hub = new NotificationHub(HubName, HubListenConnectionString, this);
        online = NetzStatus();
        locationMangaer = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationPermission();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        InfoVersion = (TextView) findViewById(R.id.version);
        String version = pInfo.versionName;
        InfoVersion.setText("Version: " + version );
        verCode = pInfo.versionCode;
        //SERVER_CLIENT_ID = "414999757757-meg30nbsf899quqhhubvarf2cjf3guk5.apps.googleusercontent.com"; // für Backend
        try {


/*
        if (BuildConfig.DEBUG) {
            //Debug build
            SERVER_CLIENT_ID = "414999757757-t0i02p4g2cjlnfpu6bm1valmrj7csfec.apps.googleusercontent.com"; //debug
        } else {
            //Release build
            //SERVER_CLIENT_ID = "414999757757-k6q3js158mftl25717qudroj5g6748rr.apps.googleusercontent.com";
            SERVER_CLIENT_ID = "414999757757-t0i02p4g2cjlnfpu6bm1valmrj7csfec.apps.googleusercontent.com";
        }
*/








        //registerWithNotificationHubs();
        // gmspps Client
        Client = new GMSPPSClient(this, BACKEND_ENDPOINT);
        //GPS Position
        new GetVersionAsyncTask().execute();
        StateIntent = new Intent(this, StateService.class);
        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // UI für GPS
        //editLocation = (EditText) findViewById(R.id.editTextLocation);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.VISIBLE);
        //pb.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        pb.getIndeterminateDrawable().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_IN);


        // get State from DB

        //Status an Background übergeben
       // UpdateState();



        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);

            nhMessage= (String) savedInstanceState.getSerializable("message");
            nhUrl= (String) savedInstanceState.getSerializable("url");
           // DialogNotify("DEBUG savedInstanceState != null ", nhUrl);
        }
        else {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                nhMessage= null;
                nhUrl= defaultUrl;
               // DialogNotify("DEBUG extras == null ", nhUrl);
            } else {

                nhMessage= extras.getString("message");

                nhUrl= extras.getString("url");
                nhID =  extras.getInt("id");
                /*
                StringBuilder str = new StringBuilder();
                Set<String> keys = extras.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    str.append(key);
                    str.append(":");
                    str.append(extras.get(key));
                    str.append("\n\r");
                }
                DialogNotify("DEBUG extras != null ",str.toString());
                */
                if(nhUrl != null) {
                    // Only go to a Mission when Url is not Null
                    Intent MissionIntent = new Intent(this, MissionActivity.class);
                    MissionIntent.putExtra("url", nhUrl);
                    MissionIntent.putExtra("message", nhMessage);
                    MissionIntent.putExtra("missionid", nhID);
                    startActivity(MissionIntent);
                }

            }

        }
        // [END restore_saved_instance_state]

        // Set up button click listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        findViewById(R.id.State_1_button).setOnClickListener(this);
        findViewById(R.id.State_2_button).setOnClickListener(this);
        findViewById(R.id.State_3_button).setOnClickListener(this);
        findViewById(R.id.State_4_button).setOnClickListener(this);
        findViewById(R.id.State_6_button).setOnClickListener(this);
        findViewById(R.id.gotomision).setOnClickListener(this);

        // Large sign-in
        ((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);

        // Start with sign-in button disabled until sign-in either succeeds or fails
        findViewById(R.id.sign_in_button).setEnabled(false);

        // Set up view instances
        mStatus = (TextView) findViewById(R.id.status);
        InfoStatus = (TextView) findViewById(R.id.statusInfo);


            // [START configure_signin]
            // Request only the user's ID token, which can be used to identify the
            // user securely to your backend. This will contain the user's basic
            // profile (name, profile picture URL, etc) so you should not need to
            // make an additional call to personalize your application.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.server_client_id))
                    .requestEmail()
                    .build();
            // [END configure_signin]

        // [START create_google_api_client]
        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(3600 * 1000)        // 3 seconds, in milliseconds
                    .setFastestInterval(60 * 1000); // 1/2 second, in milliseconds

        // [END create_google_api_client]
        if(!mGoogleApiClient.isConnected() && online) {

            mStatus.setText(R.string.signing_in);
            // [START sign_in_clicked]
            mShouldResolve = true;
            mGoogleApiClient.connect();
            // [END sign_in_clicked]
        }
        //netzwerk Prüfen!!
        if(!online) {
            //Fehler bei keiner Inrenetverbindung
            DialogNotify(getString(R.string.net_staus), getString(R.string.net_stausError));

            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
            // [END sign_out_clicked]
            updateUI(false);
            pb.setVisibility(View.GONE);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getIdToken();
        RestoreState();

        UpdateState();
   }
    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);


                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver,
                LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /*----Method to Check network Status is enable or disable ----- */
    private Boolean NetzStatus() {
        Boolean _netz = false;
        try {
            NetworkInfo wifiInfo, mobileInfo, lanInfo;
            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mobileInfo = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                // notify user you are online
                _netz = true;
            }
        }
        catch(Exception e){
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
        }
        return _netz;
    }

//sollte vom Backand gemacht werden
    @SuppressWarnings("unchecked")
    private void registerWithNotificationHubs() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String regid = gcm.register(SENDER_ID);
                    DialogNotify("Registered Successfully","RegId : " +
                            hub.register(regid).getRegistrationId());
                } catch (Exception e) {
                    DialogNotify("Exception",e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void updateUI(boolean isSignedIn) {
        try {
            if (isSignedIn) {
                // Show signed-in user's name

             /* This Line is the key */
                //Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
                findViewById(R.id.state_panel).setVisibility(View.VISIBLE);

                if (!RegisterClient_Ok) {
                    mStatus.setText("Get Data from Server...");
                    pb.setVisibility(View.VISIBLE);
                    //new GetIdTokenTask().execute();
                }
                if (!RegisterClient_Ok) {
                        mStatus.setText("Get Data from Server...");
                        pb.setVisibility(View.VISIBLE);
                        //new GetIdTokenTask().execute();
                    }

                if (name != "Unknown") {
                    mStatus.setText(getString(R.string.signed_in_fmt, name));
                    // Set button visibility
                    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                    findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                } else {
                    mStatus.setText(R.string.signed_out);
                    RegisterClient_Ok = false;
                    // Set button visibility
                    findViewById(R.id.sign_in_button).setEnabled(true);
                    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
                }


            } else {
                // Show signed-out message
                mStatus.setText(R.string.signed_out);
                RegisterClient_Ok = false;

                // Set button visibility
                findViewById(R.id.sign_in_button).setEnabled(true);
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            }
        }catch(Exception e){
            System.out.println("Update UI Exception: " + e.getMessage());
        }
    }

    // [START on_start_on_stop]
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            getIdToken();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }
    // [END on_start_on_stop]

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //schein aber nicht brauchbar für Save Date
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
        super.onSaveInstanceState(outState);
        }
     // [END on_save_instance_state]

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }
            mIsResolving = false;
            mGoogleApiClient.connect();
        }
        if (requestCode == RC_GET_TOKEN) {
            // [START get_id_token]
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

            if (result.isSuccess()) {
                String idToken = result.getSignInAccount().getIdToken();
                // TODO(developer): send token to server and validate
            }
            // [END get_id_token]

            handleSignInResult(result);
        }


    }
    // [END on_activity_result]

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.

        online = NetzStatus();
        if(online) {
            Log.d(TAG, "onConnected:" + bundle);
            // Show the signed-in UI
            RestoreState();
            UpdateState();
            updateUI(true);
        }
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        try {

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
               LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
               // handleNewLocation(location);
            }
           // setUpLocationClientIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    @Override
    public void onLocationChanged(Location location) {
        try {
            mLastLocation = location;
           // handleNewLocation(myLastLocation);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            }
        }
    }

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            if (!locationMangaer.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                showGPSDisabledAlertToUser();
            }
            return true;
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            getLocation();  //Method called if I have permission
    }

    @SuppressWarnings({"MissingPermission"})
    private void getLocation() {
        //Android studio shows warning at this line.
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }




    @Override
    public void onConnectionSuspended ( int i){
            // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void  SendState() {
        String Header = Client.getAuthorizationHeader();
        if(Header != null)StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
        StateIntent.putExtra(StateService.STATUS, userStatus);
        startService(StateIntent);


    }

    //Status an Background Service übergeben
    private void  UpdateState() {


        //ToDO erst mal ohne
        //String Header = Client.getAuthorizationHeader();
        //if(Header != null)StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
        View Bg_Status = findViewById(R.id.status_bar);
        Button mButton=(Button)findViewById(R.id.gotomision);
        mButton.setText(nhName);
        checkPermissions();

        switch (userStatus) {
            case 1:


                    Bg_Status.setBackgroundColor(getResources().getColor((R.color.C1_blue)));
                    InfoStatus.setText(R.string.st1_button);
                    findViewById(R.id.State_4_button).setVisibility(View.GONE);
                    findViewById(R.id.State_3_button).setVisibility(View.GONE);
                    findViewById(R.id.State_1_button).setVisibility(View.GONE);
                    findViewById(R.id.gotomision).setVisibility(View.GONE);
                    findViewById(R.id.State_2_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.State_6_button).setVisibility(View.VISIBLE);

                break;
            case 2:

                Bg_Status.setBackgroundColor(getResources().getColor((R.color.C1_graygreen)));
                InfoStatus.setText(R.string.st2_button);
                findViewById(R.id.State_4_button).setVisibility(View.GONE);
                findViewById(R.id.State_3_button).setVisibility(View.GONE);
                findViewById(R.id.gotomision).setVisibility(View.GONE);
                findViewById(R.id.State_1_button).setVisibility(View.VISIBLE);
                findViewById(R.id.State_2_button).setVisibility(View.GONE);
                findViewById(R.id.State_6_button).setVisibility(View.VISIBLE);

                break;

            case 3:

                InfoStatus.setText(R.string.st3_button);
                Bg_Status.setBackgroundColor(getResources().getColor((R.color.C1_red)));
                findViewById(R.id.State_4_button).setVisibility(View.GONE);
                findViewById(R.id.State_3_button).setVisibility(View.GONE);
                findViewById(R.id.State_1_button).setVisibility(View.GONE);
                findViewById(R.id.State_2_button).setVisibility(View.GONE);
                findViewById(R.id.gotomision).setVisibility(View.VISIBLE);
                findViewById(R.id.State_6_button).setVisibility(View.GONE);

                break;
            case 4:
                InfoStatus.setText(R.string.st4_button);
                Bg_Status.setBackgroundColor(getResources().getColor((R.color.C1_ligtblue)));
                findViewById(R.id.State_4_button).setVisibility(View.GONE);
                findViewById(R.id.State_3_button).setVisibility(View.GONE);
                findViewById(R.id.gotomision).setVisibility(View.VISIBLE);
                findViewById(R.id.State_1_button).setVisibility(View.GONE);
                findViewById(R.id.State_2_button).setVisibility(View.GONE);
                findViewById(R.id.State_6_button).setVisibility(View.GONE);
                break;
            case 6:
                InfoStatus.setText( R.string.st6_button);
                Bg_Status.setBackgroundColor(getResources().getColor((R.color.C1_yellow)));
                findViewById(R.id.State_4_button).setVisibility(View.GONE);
                findViewById(R.id.State_3_button).setVisibility(View.GONE);
                findViewById(R.id.gotomision).setVisibility(View.GONE);
                findViewById(R.id.State_1_button).setVisibility(View.VISIBLE);
                findViewById(R.id.State_2_button).setVisibility(View.VISIBLE);
                findViewById(R.id.State_6_button).setVisibility(View.GONE);
                break;
        }



    }



    public void NotifyMission(final String message, final String uri, int id ) {
        Intent MissionIntent = new Intent(this, MissionActivity.class);
        MissionIntent.putExtra("url", uri);
        MissionIntent.putExtra("message", message);
        MissionIntent.putExtra("missionid", id);

        startActivity(MissionIntent);

    }

    /**
     * A modal AlertDialog for displaying a message on the UI thread
     * when there's an exception or message to report.
     *
     * @param title   Title for the AlertDialog box.
     * @param message The message displayed for the AlertDialog box.
     */
    public void DialogNotify(final String title,final String message )
    {
       // if (isVisible == false) return; immer anzeigen
        final AlertDialog.Builder dlg;
        dlg = new AlertDialog.Builder(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dlgAlert = dlg.create();
                dlgAlert.setTitle(title);
                dlgAlert.setButton(DialogInterface.BUTTON_POSITIVE,
                        (CharSequence) "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dlgAlert.setMessage(message);
                dlgAlert.setCancelable(false);
                dlgAlert.show();
            }
        });
    }

    /**
     * Stores the RegistrationId in local storage
     *
     * @param auth Google Tooken to store
     * @param context        Application Context
     */
    private  void setAuth(String auth, Context context) {
        SharedPreferences mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("gnspps_auth", auth);
        ed.commit();
         mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor ed1 = mPrefs.edit();
        ed1.putString("gnspps_auth", auth);
        ed1.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                online = NetzStatus();
                if(online) {
                    if (mGoogleApiClient.isConnected()) {

                        mGoogleApiClient.disconnect();
                    }
                    pb.setVisibility(View.VISIBLE);
                    mStatus.setText(R.string.signing_in);
                    // [START sign_in_clicked]
                    mShouldResolve = true;
                    mGoogleApiClient.connect();
                    //Todo only for debug
                    getIdToken();
                    // [END sign_in_clicked]
                }else{
                    DialogNotify(getString(R.string.net_staus), getString(R.string.net_stausError));
                }
                break;

            case R.id.disconnect_button:
                // Revoke all granted permissions and clear the default account.  The user will have
                // to pass the consent screen to sign in again.
                // [START disconnect_clicked]
                if (mGoogleApiClient.isConnected()) {

                    mGoogleApiClient.disconnect();
                }
                // [END disconnect_clicked]
                updateUI(false);
                break;
            case R.id.State_1_button:
                // Status 1 clicked
                userStatus = 1;
                SendState();
                UpdateState();
                //StateIntent.putExtra(StateService.STATUS,userStatus );
                //StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
               // startService(StateIntent);
               // timerHandler.removeCallbacks(timerRunnable);
               // sendStateandPosition();




                break;
            case R.id.State_2_button:
                // Status 2 clicked
                userStatus = 2;
                SendState();
                UpdateState();
               // StateIntent.putExtra("Status",userStatus );
                //startService(StateIntent);
               // timerHandler.removeCallbacks(timerRunnable);
               // sendStateandPosition();


                break;
            case R.id.State_3_button:
                // Status 3 clicked
                userStatus = 3;
                SendState();
                UpdateState();
                //StateIntent.putExtra("Status",userStatus );
                //startService(StateIntent);
              //  timerHandler.postDelayed(timerRunnable, 0);
               // sendStateandPosition();




                break;
            case R.id.State_4_button:
                // Status 4 clicked
                userStatus = 4;
                SendState();
                UpdateState();
               // StateIntent.putExtra("Status",userStatus );
                //startService(StateIntent);
              //  timerHandler.postDelayed(timerRunnable, 0);
                //sendStateandPosition();



                break;
            case R.id.State_6_button:
                // Status 4 clicked
                userStatus = 6;
                SendState();
                UpdateState();
                // StateIntent.putExtra("Status",userStatus );
                //startService(StateIntent);
                //  timerHandler.postDelayed(timerRunnable, 0);
                //sendStateandPosition();



                break;
            case R.id.gotomision:
                RestoreState();
                Intent MissionIntent = new Intent(this, MissionActivity.class);
                MissionIntent.putExtra("url", nhUrl);
                //MissionIntent.putExtra("message", nhMessage);
                MissionIntent.putExtra("missionid", nhID);
                MissionIntent.putExtra(KEY_MSTAT,nhStatus);
                startActivity(MissionIntent);




                break;
        }
    }
    /*
    private class GetIdTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (mGoogleApiClient.isConnected()) {
               // String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
               // Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                //String scopes = "oauth2:https://www.googleapis.com/auth/userinfo.profile";   ****************OK'''''''''''''''' ->
                // -> https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=ya29.jQJ1yvRe9UN2sw-0W7oTDvEyZcfCbidHaYOUpO0Hph-p-OIqQe6C8jUeiN5bg0_k6thZ
               // String scopes = "audience:server:client_id:414999757757.apps.googleusercontent.com";
                //"oauth2:server:client_id:9414861317621.apps.googleusercontent.com"


                String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID  ; // Not the app's client ID.

                try {
                    return GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
                } catch (IOException e) {
                    Log.e(TAG, "Error retrieving ID token. ID:"+ SERVER_CLIENT_ID + " ac:" + accountName, e);
                    return null;
                } catch (GoogleAuthException e) {
                    Log.e(TAG, "Error retrieving ID token. ID:"+ SERVER_CLIENT_ID + " ac:" + accountName, e);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "ID token: " + result);
            if (result != null) {
                // Successfully retrieved ID Token
                // call AsynTask to perform network operation on separate thread
                MainActivity.this.Client.setAuthorizationHeader(result);

                AuthorizationHeader = MainActivity.this.Client.getAuthorizationHeader();
                setAuth(AuthorizationHeader,  getApplicationContext());

                mStatus.setText("Get Suscriptions");
                pb.setVisibility(View.VISIBLE);
                new GetSuscriptionAsyncTask().execute();

            } else {
                // There was some error getting the ID Token
                Log.i(TAG, "Keine Daten erhalten bei get TOOKEN");
            }
        }
    }

    */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONObject jObj = null;
           //String dummy = result;
            //Do anything with response..
             /*
            try {

                JSONArray Obj = new JSONArray(result);
                JSONArray Providers = jObj.getJSONArray("RootResults");
                setContentView(R.layout.providers);

                Provider weather_data[] = new Provider[]
                        {

                                new Provider(1, "Cloudy", "cc"),
                                new Provider(2, "Showers", "22"),

                        };


                ProviderAdapter adapter = new ProviderAdapter( MainActivity.this , R.layout.listview_item_row, weather_data);


                listView1 = (ListView)findViewById(R.id.listView1);

                View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row, null);
                listView1.addHeaderView(header);

                listView1.setAdapter(adapter);
                */

                //jObj = new JSONObject(dummy);
               /* JSONObject c = jObj.getJSONObject("GetC1_KEY_WORTResult");
                JSONArray wortlist = c.getJSONArray("RootResults");
                c = jObj.getJSONObject("GetC1_KEY_WORTResult");
                JSONArray Woerter1 = wortlist;

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            */

        }
    }


    private class RegAsyncTask extends AsyncTask<String, Object, Object> {

        @Override
        protected Object doInBackground(String... params){
            try {
                String a =  params[0];
                String b =  params[1];
                if(!RegisterClient_Ok) {
                    String regid = gcm.register(SENDER_ID);
                    //
                    Client.register(regid, new HashSet<String>(),a,b);
                }
            } catch (Exception e) {
                DialogNotify("MainActivity - Failed to register", e.getMessage());
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object result) {
            RegisterClient_Ok = true;
            //sendPush.setEnabled(true);
            //Toast.makeText(context, "Logged in and registered.",
            //        Toast.LENGTH_LONG).show();

        }
    };
// der erste Schritt
    private class GetSuscriptionAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            try {
                String regid = gcm.register(MainActivity.SENDER_ID);

                return Client.GetSuscribetProvider(regid);




            } catch (Exception e) {
                DialogNotify("MainActivity - Failed to getSubsc....Async", e.getMessage());
                return e.toString();
            }
           // null;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                //JSONObject jObj = new JSONObject(result);
                JSONArray Obj = new JSONArray(result);
                mStatus.setText(getString(R.string.signed_in_fmt, name));
                pb.setVisibility(View.GONE);

                if(Obj.length() > 0)
                {
                    // Client hat sich bereits suscribt
                    //ToDo anzeigen
                    String test = "Ok";
                    findViewById(R.id.Logo).setVisibility(View.GONE);
                    findViewById(R.id.ProviderList).setVisibility(View.VISIBLE);
                    provider = new Provider[Obj.length()];
                    for (int i = 0; i < Obj.length(); i++) {
                        Provider item = new Provider();
                        JSONObject row = Obj.getJSONObject(i);
                        item.Url = row.getString("url");
                        item.Name = row.getString("name");
                        item.TypID = row.getInt("typeint");
                        item.Typ = row.getString("type");
                        item.IconUrl = row.getString("icon");
                        item.ID = row.getString("id");
                        provider[i] = item;
                    }
                    ProviderAdapter adapter = new ProviderAdapter(MainActivity.this, 1 , provider );
                    list=(ListView)findViewById(R.id.Plist);
                    list.setAdapter(adapter);
                }
                else {
                    // Client hat noch keine Provider suscribt
                    //ToDo abfragen
                    String test = "NOK";
                    new GetProviderAsyncTask().execute();

                }

            } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        }
    };


///Providers
    private class GetProviderAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            try {
                return Client.GetAllProvider();

            } catch (Exception e) {
                DialogNotify("MainActivity - Failed to getAllProvider ....Async", e.getMessage());
                return e.toString();
            }
            // null;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                //JSONObject jObj = new JSONObject(result);
                JSONArray Obj = new JSONArray(result);
                if(Obj.length() > 0)
                {
                    // Client hat sich bereits suscribt
                    //ToDo anzeigen
                    String test = "Ok";
                    //activity.startActivity(new Intent(activity, ProviderActivity.class));
                    Intent ProviderIntent = new Intent(MainActivity.this,  ProviderActivity.class);
                    ProviderIntent.putExtra("provider", result);
                    //MissionIntent.putExtra("message", nhMessage);
                    startActivity(ProviderIntent);


                }
                else {
                    // Keinemöglichen Provider zurück geliefert

                    String test = "NOK";

                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            catch (Exception e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
        }
    };

    ///Version
    private class GetVersionAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            try {
                return Client.GetVersion();

            } catch (Exception e) {
                DialogNotify("MainActivity - Failed to the Version ....Async", e.getMessage());
                return e.toString();
            }
            // null;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                //JSONObject jObj = new JSONObject(result);
                JSONObject Obj = new JSONObject(result);
                int a =  verCode;

                if(Obj.getInt("version") > a) //{"device":"android","version":23}
                {


                    // Client hat sich bereits suscribt
                    //ToDo anzeigen
                    String test = "Ok";
                    try {
                        Intent viewIntent =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse("market://details?id=com.citaurus.gmspps"));
                        startActivity(viewIntent);
                    }catch(Exception e) {
                        Toast.makeText(getApplicationContext(),"Unable to Connect Try Again...",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                }
                else {
                    // Keinemöglichen Provider zurück geliefert

                    String test = "NOK";

                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            catch (Exception e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
        }
    };
/*
    private class SetStateAsyncTask extends AsyncTask<String, Void, String> {
        @Override

        protected String doInBackground(String... params) {
            try {
             Client.SetState(params[0], params[1], params[2]);
        } catch (Exception e) {
            DialogNotify("MainActivity - Failed to sendState", e.getMessage());
            //return e;
        }
        return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //RegisterClient_Ok = true;
            //sendPush.setEnabled(true);
            //Toast.makeText(context, "Logged in and registered.",
            //        Toast.LENGTH_LONG).show();

        }
    };
*/




}







