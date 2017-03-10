package com.citaurus.gmspps;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;

public class AlarmController {
    Context context;
    MediaPlayer mp;
    AudioManager mAudioManager;
    int userVolume;
    public AlarmController(Context c) { // constructor for my alarm controller class
        this.context = c;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        //remeber what the user's volume was set to before we change it.
        userVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        mp = new MediaPlayer();
    }
    public void playSound(String soundURI){

        Uri alarmSound = null;
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);


        try{
            alarmSound = Uri.parse(soundURI);
        }catch(Exception e){
            alarmSound = ringtoneUri;
        }
        finally{
            if(alarmSound == null){
                alarmSound = ringtoneUri;
            }
        }



        try {

            if(!mp.isPlaying()){
                mp.setDataSource(context, alarmSound);
                mp.setAudioStreamType(AudioManager.STREAM_ALARM);
                mp.setLooping(true);
                mp.prepare();
                mp.start();
            }


        } catch (IOException e) {
            Toast.makeText(context, "Your alarm sound was unavailable.", Toast.LENGTH_LONG).show();

        }
        // set the volume to what we want it to be.  In this case it's max volume for the alarm stream.
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);

    }

    public void stopSound(){
// reset the volume to what it was before we changed it.
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);
        mp.stop();
        mp.reset();

    }
    public void releasePlayer(){
        mp.release();
    }
}
