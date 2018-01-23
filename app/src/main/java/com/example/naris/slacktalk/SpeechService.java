package com.example.naris.slacktalk;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Lee on 10/13/2016.
 */

public class SpeechService extends Service implements TextToSpeech.OnInitListener {

    public static final String EXTRA_WORD = "word";

    private TextToSpeech tts;
    private String word;
    private boolean isInit;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(), this);
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        word = intent.getStringExtra(SpeechService.EXTRA_WORD);

        if (isInit) {
            speak();
        }

        return SpeechService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if(SmsBroadcastReceiver.messageIgnored==false) {
                    Intent speechInput = new Intent(SpeechService.this, SpeechInput.class);
                    speechInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(speechInput);
                    stopSelf();
                }else{
                    if(SmsBroadcastReceiver.callback == true){
                        PackageManager pm = SpeechService.this.getPackageManager();
                        int hasPerm = pm.checkPermission(
                                Manifest.permission.CALL_PHONE,
                                SpeechService.this.getPackageName());
                        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                            // do stuff
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + SmsBroadcastReceiver.number));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                            startActivity(intent);
                            stopSelf();
                        }
                    }
                    else{
                        stopSelf();
                    }
                }
            }
        });

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                speak();
                isInit = true;
            }
        }
    }

    private void speak() {
        if (tts != null) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"messageID");
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, map);

        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}