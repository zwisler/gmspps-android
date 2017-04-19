package com.citaurus.gmspps;

/**
 * Created by rzwisler on 03.09.2015.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.microsoft.windowsazure.notifications.NotificationsHandler;




public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private AudioManager mAudioManager;
    private  int TypId;
    private GMSPPSClient Client;// = new GMSPPSClient(this, "http://gmspps.azurewebsites.net");
    NotificationCompat.Builder builder;
    Context ctx;
    private SharedPreferences mPrefs;
    private String AuthorizationHeader;

    //static public MissionActivity missionActivity;
    static public MainActivity mainActivity;
    public static final String NOTIFICATION = "com.citaurus.doit4you";






    @Override
    public void onReceive(Context context, Bundle bundle) {
        try {
            ctx = context;
            String nhMessage = bundle.getString("message");
            String uri = bundle.getString("url");
            String id = bundle.getString("ID");
            TypId = bundle.getInt("Typ", 0);
            sendNotification(nhMessage, uri, id);
            //mainActivity.refreshToken();
            publishMsg("onReceive on MyHandle of a Push msg");

            Client = new GMSPPSClient(context, "http://gmspps.azurewebsites.net");
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            //mPrefs = getSharedPreferences("userdetails", MODE_PRIVATE);
            AuthorizationHeader = mPrefs.getString("gnspps_auth", "default"); // Gespeichete Inhalte lesen

            Client.setAuthorizationHeader(AuthorizationHeader);
            sendAkk("1", id);
            mainActivity.NotifyMission(nhMessage, uri, Integer.parseInt(id));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void publishMsg(String Msg) {
        try {

            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra("msg", Msg);
            ctx.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    private void sendNotification(String msg, String missionUrl, String id) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
        AlarmController ac = new AlarmController(ctx);
        String uri = "android.resource://com.citaurus.gmspps/" +  "/" + R.raw.sound_file_1;
        // das funzt muss aber erst gestopt werden
        //ac.playSound(uri);
        //MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.sound_file_1);



        mAudioManager.setRingerMode(AudioManager.FLAG_ALLOW_RINGER_MODES);
        int sb2value = mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_ALARM);
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, sb2value, AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND);
        //WebView myWebView = (WebView) findViewById(R.id.webview);

        //myWebView.loadUrl("http://www.example.com");
        try {
            //TODO askommentier ply wieder aktiviren
           // Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
           // Ringtone r = RingtoneManager.getRingtone(ctx, notification);
           // r.play();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        Intent MissionIntent =  new Intent(ctx, MainActivity.class);
        MissionIntent.putExtra("url", missionUrl);
        MissionIntent.putExtra("message", msg);
        MissionIntent.putExtra("id", id);
        stackBuilder.addNextIntent(MissionIntent);



            PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, MissionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //new Intent(ctx, MainActivity.class), 0);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.gmspps)
                        .setContentTitle(ctx.getResources().getString(R.string.tile_content_title))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void  sendAkk(String Status, String MissioonId) {

        new SetAkkAsyncTask().execute(Status, MissioonId);
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
            String temp = result;
            //Status wurde gesendet

        }
    };
}
