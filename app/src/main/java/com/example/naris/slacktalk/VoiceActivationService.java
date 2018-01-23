package com.example.naris.slacktalk;

/**
 * Created by Lee on 11/10/2016.
 */
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.voice.AlwaysOnHotwordDetector;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.AlwaysOnHotwordDetector.EventPayload;
import android.service.voice.VoiceInteractionService;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VoiceActivationService extends VoiceInteractionService {
    static final String TAG = "MainInteractionService";
    private final Callback mHotwordCallback = new Callback() {
        @Override
        public void onAvailabilityChanged(int status) {
            Log.i(TAG, "onAvailabilityChanged(" + status + ")");
            hotwordAvailabilityChangeHelper(status);
        }

        @Override
        public void onDetected(EventPayload eventPayload) {
            Log.i(TAG, "onDetected");
        }

        @Override
        public void onError() {
            Log.i(TAG, "onError");
        }

        @Override
        public void onRecognitionPaused() {
            Log.i(TAG, "onRecognitionPaused");
        }

        @Override
        public void onRecognitionResumed() {
            Log.i(TAG, "onRecognitionResumed");
        }
    };
    private AlwaysOnHotwordDetector mHotwordDetector;


    @Override
    public void onReady() {
        super.onReady();
        Log.i(TAG, "Creating " + this);
        try {
            mHotwordDetector = createAlwaysOnHotwordDetector("Hi", Locale.forLanguageTag("en-US"), mHotwordCallback);
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this,"Service is ready",Toast.LENGTH_LONG).show();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void hotwordAvailabilityChangeHelper(int availability) {
        Log.i(TAG, "Hotword availability = " + availability);
        switch (availability) {
            case AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE:
                Log.i(TAG, "STATE_HARDWARE_UNAVAILABLE");
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED:
                Log.i(TAG, "STATE_KEYPHRASE_UNSUPPORTED");
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED:
                Log.i(TAG, "STATE_KEYPHRASE_UNENROLLED");
                Intent enroll = mHotwordDetector.createEnrollIntent();
                Log.i(TAG, "Need to enroll with " + enroll);
                break;
            case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED:
                Log.i(TAG, "STATE_KEYPHRASE_ENROLLED - starting recognition");
                if (mHotwordDetector.startRecognition(AlwaysOnHotwordDetector.RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO)) {
                    Intent speechInput = new Intent(VoiceActivationService.this, SpeechInput.class);
                    speechInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(speechInput);
                    Log.i(TAG, "startRecognition succeeded");
                } else {
                    Log.i(TAG, "startRecognition failed");
                }
                break;
        }
    }
}


