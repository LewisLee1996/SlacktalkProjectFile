package com.example.naris.slacktalk;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lee on 10/13/2016.
 */

public class SpeechInput extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboveLockScreen();
        voiceInput();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1  && resultCode==RESULT_OK) {
            ArrayList<String> command = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(command.isEmpty()){
                Intent speechIntent = new Intent(this, SpeechService.class);
                speechIntent.putExtra(SpeechService.EXTRA_WORD, "Sorry, I don't understand.\nDo you want me to read the message?");
                this.startService(speechIntent);
                finish();
            }
            else {
                Toast.makeText(this, command.get(0), Toast.LENGTH_LONG).show();
                String result=command.get(0);

                if(SmsBroadcastReceiver.replyMessage==false) {
                    if(result.contains("repeat") || result.contains("again")){
                        SmsBroadcastReceiver.readedMessage=false;
                    }
                    if (result.contains("read") || result.contains("yes") || result.contains("repeat") || result.contains("again") && SmsBroadcastReceiver.readedMessage == false) {

                        Intent speechIntent = new Intent(this, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "It says \n" + SmsBroadcastReceiver.message + "\nDo you want to reply the message or you want to call to the person?\nOr you want me to repeat the message?");
                        this.startService(speechIntent);
                        SmsBroadcastReceiver.readedMessage=true;
                        finish();
                    } else if (result.contains("reply") && SmsBroadcastReceiver.readedMessage==true) {
                        SmsBroadcastReceiver.replyMessage = true;
                        Intent speechIntent = new Intent(this, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "Ok\nWhat is the message that you want to reply?");
                        this.startService(speechIntent);
                        finish();
                    }else if (result.contains("call") && SmsBroadcastReceiver.readedMessage==true){
                        Intent speechIntent = new Intent(this, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "Ok.\nPlease hold on.");
                        this.startService(speechIntent);
                        SmsBroadcastReceiver.messageIgnored = true;
                        SmsBroadcastReceiver.callback = true;
                        finish();
                    } else if (result.contains("ignore") || result.contains("no")) {
                        Intent speechIntent = new Intent(this, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "Ok.\nMessage ignored.");
                        this.startService(speechIntent);
                        SmsBroadcastReceiver.messageIgnored = true;
                        finish();
                    } else {
                        if(SmsBroadcastReceiver.readedMessage == true) {
                            Intent speechIntent = new Intent(this, SpeechService.class);
                            speechIntent.putExtra(SpeechService.EXTRA_WORD, "Sorry, I don't understand.\nDo you want to reply the message or you want to call to the person?\nOr you want me to repeat the message?");
                            this.startService(speechIntent);
                            finish();
                        }
                        else{
                            Intent speechIntent = new Intent(this, SpeechService.class);
                            speechIntent.putExtra(SpeechService.EXTRA_WORD, "Sorry, I don't understand.\nDo you want me to read the message?");
                            this.startService(speechIntent);
                            finish();
                        }
                    }
                }
                else{
                    if(SmsBroadcastReceiver.MessageReady==false) {
                        Intent speechIntent = new Intent(this, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "Ok\nThis is your message\n" + result + "\nDo you want to send it?\nOr you want to change the message?");
                        this.startService(speechIntent);
                        SmsBroadcastReceiver.replyMessageBody=result;
                        SmsBroadcastReceiver.MessageReady=true;
                        finish();
                    }else {
                        if(result.contains("rewrite") || result.contains("change")){
                            SmsBroadcastReceiver.replyMessage = true;
                            Intent speechIntent = new Intent(this, SpeechService.class);
                            speechIntent.putExtra(SpeechService.EXTRA_WORD, "Ok\nWhat is the message that you want to reply?");
                            this.startService(speechIntent);
                            SmsBroadcastReceiver.MessageReady=false;
                            finish();
                        }else if (result.contains("yes") || result.contains("send")) {
                            sendSMS(SmsBroadcastReceiver.number, SmsBroadcastReceiver.replyMessageBody);
                            Intent speechIntent2 = new Intent(this, SpeechService.class);
                            speechIntent2.putExtra(SpeechService.EXTRA_WORD, "Ok\nMessage is sent.");
                            this.startService(speechIntent2);
                            SmsBroadcastReceiver.messageIgnored = true;
                            finish();
                        } else if (result.contains("no") || result.contains("cancel")) {
                            Intent speechIntent2 = new Intent(this, SpeechService.class);
                            speechIntent2.putExtra(SpeechService.EXTRA_WORD, "Ok\nCanceled reply.");
                            this.startService(speechIntent2);
                            SmsBroadcastReceiver.messageIgnored = true;
                            finish();
                        }else{
                            Intent speechIntent = new Intent(this, SpeechService.class);
                            speechIntent.putExtra(SpeechService.EXTRA_WORD, "Sorry, I don't understand.\nDo you want me to send the message?");
                            this.startService(speechIntent);
                            finish();
                        }
                    }
                }
            }
        }
        else{
            SmsBroadcastReceiver.replyMessageBody="";
            SmsBroadcastReceiver.readedMessage=false;
            SmsBroadcastReceiver.messageIgnored=false;
            SmsBroadcastReceiver.replyMessage=false;
            SmsBroadcastReceiver.MessageReady=false;
            SmsBroadcastReceiver.repliedMessage=false;
            SmsBroadcastReceiver.callback=false;
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void voiceInput(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, 1);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
        ContentValues values = new ContentValues();
        values.put("address", phoneNumber);
        values.put("body", message);
        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        SmsBroadcastReceiver.repliedMessage=true;
    }

    public void aboveLockScreen() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
}
