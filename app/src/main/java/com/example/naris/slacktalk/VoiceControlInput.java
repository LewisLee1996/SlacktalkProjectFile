package com.example.naris.slacktalk;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lee on 10/13/2016.
 */

public class VoiceControlInput extends AppCompatActivity {
    SharedPreferences voiceControlSystemPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voiceControlSystemPrefs= getSharedPreferences("voiceControlSystemSetting", 0);
        if (voiceControlSystemPrefs.getString("voiceControlSystem", "").equals("on")) {
            voiceInput();
        }else{
            Toast.makeText(this,"Please enable voice control in settings.",Toast.LENGTH_LONG).show();
            finish();
        }

        MainActivity.choosen_contact="";
        MainActivity.choosen_sms="";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1  && resultCode==RESULT_OK) {
            ArrayList<String> command = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (command.isEmpty()) {
                Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Sorry, what did you said?");
                this.startService(speechIntent);
                finish();
            } else {
                String result = command.get(0);
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                if (MainActivity.makeMessage == false) {
                    if (result.contains("send") || result.contains("text")){
                        if(isDefaultSmsApp(this)==false){
                            Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                            speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Please set Slacktalk as default SMS application in order to send a message.");
                            this.startService(speechIntent);
                            MainActivity.makecall = false;
                            MainActivity.endVoiceControl = true;
                            finish();
                        }
                        else {
                            String contact;
                            if (result.contains("send") || result.contains("text") && result.contains("to")) {
                                contact = result.substring(result.indexOf("to") + 3);
                            } else {
                                contact = result.substring(result.indexOf("text") + 5);
                            }
                            Pattern pattern = Pattern.compile("^[\\d() +-]+$");
                            Matcher matcher = pattern.matcher(contact);
                            if (matcher.matches()) {
                                MainActivity.choosen_contact = contact;
                            } else {
                                MainActivity.choosen_contact = getPhoneNumber(contact, VoiceControlInput.this);
                            }

                            if (MainActivity.choosen_contact == "") {
                                Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                                speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Sorry, invalid contact number.\nPlease speak again.");
                                this.startService(speechIntent);
                                finish();
                            } else {
                                Cursor c = this.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
                                int totalSMS = c.getCount();
                                if (c.moveToFirst()) {
                                    for (int i = 0; i < totalSMS; i++) {
                                        String compare_num = c.getString(c.getColumnIndexOrThrow("address")).trim();
                                        if (compare_num.replaceAll("[()\\s-]+", "").equals(MainActivity.choosen_contact.replaceAll("[()\\s-]+", ""))) {
                                            MainActivity.choosen_sms = c.getString(c.getColumnIndexOrThrow("thread_id"));
                                            break;
                                        }
                                        c.moveToNext();
                                    }
                                }
                                c.close();
                                Intent intent = new Intent(this, ChatActivity.class);
                                startActivity(intent);
                                Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                                speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nWhat is the message?");
                                this.startService(speechIntent);
                                MainActivity.makeMessage = true;
                                MainActivity.makecall=false;
                                MainActivity.endVoiceControl=false;
                                finish();
                            }
                        }
                    } else if (result.contains("call") || result.contains("phone")) {
                        String contact;
                        if (result.contains("to") ) {
                            contact = result.substring(result.indexOf("to") + 3);
                        } else {
                            contact="";
                            if(result.contains("call")) {
                                contact = result.substring(result.indexOf("call") + 5);
                            }
                            if (result.contains("phone")) {
                                    contact = result.substring(result.indexOf("phone") + 6);
                            }
                        }
                        Pattern pattern = Pattern.compile("^[\\d() +-]+$");
                        Matcher matcher = pattern.matcher(contact);
                        if (matcher.matches()) {
                            MainActivity.callnumber = contact;
                        } else {
                            MainActivity.callnumber = getPhoneNumber(contact, VoiceControlInput.this);
                        }
                        if (MainActivity.callnumber == "") {
                            Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                            speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Sorry, invalid contact number.\nPlease speak again.");
                            this.startService(speechIntent);
                            MainActivity.makecall = false;
                            MainActivity.endVoiceControl = false;
                            finish();
                        } else {
                            Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                            speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nCalling to" + contact);
                            this.startService(speechIntent);
                            MainActivity.makecall = true;
                            MainActivity.endVoiceControl = true;
                            finish();
                        }
                    }else {
                        Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                        speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Sorry, what did you said?");
                        this.startService(speechIntent);
                        MainActivity.makecall = false;
                        MainActivity.endVoiceControl = false;
                        finish();
                    }
                }
                else{
                    if(ChatActivity.messageReady==false) {
                        Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                        speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nMessage is ready\nDo you want to send it?\nOr you want to change the message?");
                        this.startService(speechIntent);
                        ChatActivity.VoiceMessage=result;
                        ChatActivity.messageReady=true;
                        finish();
                    }else {
                        if(result.contains("rewrite") || result.contains("change")){
                            Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                            speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nWhat is the message?");
                            this.startService(speechIntent);
                            ChatActivity.messageReady=false;
                            finish();
                        }else if (result.contains("yes") || result.contains("send") && !result.contains("unsend") && !result.contains("don't send") && !result.contains("do not send")) {
                            Intent speechIntent2 = new Intent(this, VoiceControlOutput.class);
                            speechIntent2.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nMessage is sent.");
                            this.startService(speechIntent2);
                            ChatActivity.sendMessage=true;
                            MainActivity.endVoiceControl = true;
                            MainActivity.makecall = false;
                            MainActivity.makeMessage = false;
                            ChatActivity.messageReady = false;
                            ChatActivity.VoiceMessage="";
                            finish();
                        } else if (result.contains("no") || result.contains("cancel") || result.contains("unsend") || result.contains("don't send") || result.contains("do not send")) {
                            Intent speechIntent2 = new Intent(this, VoiceControlOutput.class);
                            speechIntent2.putExtra(VoiceControlOutput.EXTRA_WORD, "Ok\nMessage is canceled.");
                            this.startService(speechIntent2);
                            ChatActivity.cancelMessage=true;
                            MainActivity.endVoiceControl = true;
                            MainActivity.makecall = false;
                            MainActivity.makeMessage = false;
                            ChatActivity.messageReady = false;
                            ChatActivity.VoiceMessage="";
                            finish();
                        }else{
                            Intent speechIntent = new Intent(this, VoiceControlOutput.class);
                            speechIntent.putExtra(VoiceControlOutput.EXTRA_WORD, "Sorry, I don't understand.\nDo you want me to send the message?\nOr you want to change the message?");
                            this.startService(speechIntent);
                            finish();
                        }
                    }
                }
            }
        }
        else{
            MainActivity.makecall = false;
            MainActivity.endVoiceControl = false;
            MainActivity.makeMessage = false;
            ChatActivity.messageReady = false;
            ChatActivity.sendMessage=false;
            ChatActivity.VoiceMessage="";
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

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "";
        return ret;
    }

    public static boolean isDefaultSmsApp(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName());
        }
        else{
            return true;
        }
    }
}
