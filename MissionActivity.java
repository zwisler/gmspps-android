package com.citaurus.gmspps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.citaurus.gmspps.util.SystemUiHider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MissionActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private static final String TAG = "MissionActivity";

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private String nhMessage;
    private String nhUrl;
    private String nhName;
    public  WebView myWebView;
    public static final String NOTIFICATION = "com.citaurus.doit4you";
    private static final String defaultUrl = "http://beta.html5test.com/";
    private ProgressDialog dialog; //= new ProgressDialog(MissionActivity.this);
    private SharedPreferences mPrefs;
    private int missionID;
    private int missionState;
    private int UserState;
    private GMSPPSClient Client;// = new GMSPPSClient(this, "http://gmspps.azurewebsites.net");
    private Intent StateIntent;
    static public MainActivity mainActivity;
    private String AuthorizationHeader;

    private double latitude;
    private double longitude;



    private void SaveState() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
//        mainActivity.userStatus = UserState;
        //mainActivity.NotifyMission(nhMessage, uri, Integer.parseInt(id )); KEY_MSTAT
        ed.putInt(MainActivity.KEY_MSTAT, missionState);
        ed.putInt(MainActivity.KEY_STATUS, UserState);
        ed.putInt(MainActivity.KEY_MISSION_ID, missionID);
        ed.putString(MainActivity.KEY_MISSION_URL, nhUrl);
        ed.putString(MainActivity.KEY_MISSION_NAME, nhName);




        // DODO Hier alle zuspeichernenet States angeben

        ed.commit();

    }

    private void SavePos() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
//
        ed.putFloat("Nextlatitude", (float) latitude);
        ed.putFloat("Nextlongitude", (float) longitude);



        // DODO Hier alle zuspeichernenet States angeben

        ed.commit();

    }
    private void RestoreState() {
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        UserState = mPrefs.getInt(MainActivity.KEY_STATUS, -1); //KEY_MISSION_ID
        if (UserState == 3 || UserState == 4 ) {
            missionID = mPrefs.getInt(MainActivity.KEY_MISSION_ID, -1);
            nhUrl  = mPrefs.getString(MainActivity.KEY_MISSION_URL, "");
            missionState = mPrefs.getInt(MainActivity.KEY_MSTAT, -1);
        }
        // UpdateState();
    }
    @Override
    public void onBackPressed() {
       // moveTaskToBack(true);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            //do your stuff
           // moveTaskToBack(true);
           // MissionActivity.this.finish();
            publishState();
            MissionActivity.this.finish();

            //return true;
            //moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }
    private void publishState() {
        try {
            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra("mystatus", UserState);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(MissionActivity.this);

        setContentView(R.layout.activity_mission);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        // Cliend Class
        Client =  new GMSPPSClient(this, MainActivity.BACKEND_ENDPOINT);
        mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);

        //mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        AuthorizationHeader = mPrefs.getString("gnspps_auth", "default"); // Gespeichete Inhalte lesen
        Client.setAuthorizationHeader(AuthorizationHeader);

        StateIntent = new Intent(this, StateService.class);
        missionState = 1;
        int m = UserState;


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                nhMessage= null;
                nhUrl= defaultUrl;
            } else {
                nhMessage= extras.getString("message");
                nhUrl= extras.getString("url");
                missionID = extras.getInt("missionid");
                missionState = extras.getInt(MainActivity.KEY_MSTAT);

                new GetMissionsAsyncTask().execute();
            }
        } else {
            nhMessage= (String) savedInstanceState.getSerializable("message");
            nhUrl= (String) savedInstanceState.getSerializable("url");
            missionID = (int) savedInstanceState.getSerializable("missionid");
            missionState = (int) savedInstanceState.getSerializable(MainActivity.KEY_MSTAT);

            new GetMissionsAsyncTask().execute();
        }
        m = UserState;
        if(missionState > 1) RestoreState();
        if(missionState == 0) sendAkk("1", String.valueOf(missionID));
        UpdateUI();

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                view.setVisibility(View.VISIBLE);
            }
        });
        dialog.setMessage("Loading..Please wait.");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        myWebView.setVisibility(View.GONE);
        myWebView.loadUrl(nhUrl);



        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);



        /*
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(nhUrl);


 */
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.webview);



        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }

            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.accept_btn).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.reject_btn).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.start_btn).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.cancel_btn).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.finish_btn).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }



    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            switch (view.getId()) {
                case R.id.accept_btn:
                    if (missionState != 2) {
                        missionState = 2;
                        UserState = 3;
                        sendAkk("2", String.valueOf(missionID));




                    }

                    break;
                case R.id.reject_btn:
                    if (missionState != 3) {
                        missionState = 3;
                        UserState = 1;
                        SaveState();
                        sendAkk("3", String.valueOf(missionID));

                    }

                    break;
                case R.id.cancel_btn:
                    if (missionState != 3) {
                        missionState = 3;
                        UserState = 1;
                        sendAkk("3", String.valueOf(missionID));
                    }

                    break;
                case R.id.start_btn:
                    if (missionState != 4) {
                        missionState = 4;
                        UserState = 4;
                        sendAkk("4", String.valueOf(missionID));
                    }
                    break;
                case R.id.finish_btn:
                    if (missionState != 5) {
                        missionState = 5;
                        UserState = 1;
                        sendAkk("5", String.valueOf(missionID));
                    }
                    break;
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    /**
     * A modal AlertDialog for displaying a message on the UI thread
     * when there's an exception or message to report.
     *
     * @param title   Title for the AlertDialog box.
     * @param message The message displayed for the AlertDialog box.
     */
    public void DialogNotify(final String title,final String message, String url)
    {
        try {
            WebView webview = new WebView(this);
            setContentView(webview);
            // Simplest usage: note that an exception will NOT be thrown
            // if there is an error loading this page (see below).
            webview.loadUrl(url);


            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadUrl("http://beta.html5test.com/");
            //findViewById(R.id.weblayout).setVisibility(View.VISIBLE);


            myWebView = webview;
        }
        catch( Exception e) {
            Log.e(TAG, "Error .", e);

        }





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
    public void createNotification(View view) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, MissionActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle("New mail from " + "test@gmail.com").build();
        //.setContentText("Subject").setSmallIcon(R.drawable.icon)
               // .setContentIntent(pIntent)
               // .addAction(R.drawable.icon, "Call", pIntent)
               // .addAction(R.drawable.icon, "More", pIntent)
               // .addAction(R.drawable.icon, "And more", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

    }
    private void  sendAkk(String Status, String MissioonId) {

        new SetAkkAsyncTask().execute(Status, MissioonId);
    }
    private void  UpdateUI() {

        switch (missionState) {
            case 2:
                findViewById(R.id.accept_btn).setVisibility(View.GONE);
                findViewById(R.id.reject_btn).setVisibility(View.GONE);
                findViewById(R.id.cancel_btn).setVisibility(View.VISIBLE);
                findViewById(R.id.start_btn).setVisibility(View.VISIBLE);
                // Status der Person auch ändern
                StateIntent.putExtra(StateService.STATUS, UserState);
                StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
                //StateIntent.putExtra("Nextlatitude", latitude);
                //StateIntent.putExtra("Nextlongitude", longitude);
                startService(StateIntent);
                SaveState();

                break;
            case 3:
               // MissionActivity.this.finish();
                // Status der Person auch ändern
                UserState = 1;
                StateIntent.putExtra(StateService.STATUS, UserState);
                StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
                StateIntent.putExtra("Nextlatitude", latitude);
                StateIntent.putExtra("Nextlongitude", longitude);
                startService(StateIntent);
                SaveState();



                break;
            case 4:
                findViewById(R.id.accept_btn).setVisibility(View.GONE);
                findViewById(R.id.reject_btn).setVisibility(View.GONE);
                findViewById(R.id.start_btn).setVisibility(View.GONE);
                findViewById(R.id.finish_btn).setVisibility(View.VISIBLE);
                // Status der Person auch ändern
                StateIntent.putExtra(StateService.STATUS, UserState);
                StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
                StateIntent.putExtra("Nextlatitude", latitude);
                StateIntent.putExtra("Nextlongitude", longitude);
                startService(StateIntent);
                SaveState();


                break;
            case 5:
                MissionActivity.this.finish();
                // Status der Person auch ändern
                StateIntent.putExtra(StateService.STATUS, UserState);
                StateIntent.putExtra("gnspps_auth", Client.getAuthorizationHeader());
                startService(StateIntent);
                SaveState();
                break;

        }
    }
    private class SetAkkAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Client.SetAkk(params[0], params[1]);
            } catch (Exception e) {
                e.printStackTrace();
                //return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            UpdateUI();


            //Status wurde gesendet
            //STATUS Speichern


        }
    };

    private class GetMissionsAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            try {
                return Client.GetMissionInfo(String.valueOf(missionID));

            } catch (Exception e) {
                Toast.makeText(MissionActivity.this, "Mission Akt Fail get Mission ", Toast.LENGTH_SHORT).show();

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
                    JSONObject Mission = Obj.getJSONObject(0);
                    nhName = Mission.getString("title");
                    String costumMissionID =  Mission.getString("costumMissionID");
                    latitude = Mission.getDouble("lat");
                    longitude = Mission.getDouble("lon");
                    if(longitude != 0)SavePos();
                    SaveState();




                    //ToDo anzeigen
                    String test = "Ok";
                    // List View



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
}



