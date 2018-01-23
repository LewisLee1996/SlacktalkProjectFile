package com.example.naris.slacktalk;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lee on 9/21/2016.
 */
public class ChatActivity extends AppCompatActivity{
    String thread_id = MainActivity.choosen_sms;
    String call_number = MainActivity.choosen_contact;
    String chat_name;
    List<SMS> chat=new ArrayList<SMS>();
    ListView chatlv;
    ChatAdapter adapter;
    ChatActivity activity=this;

    static boolean messageReady=false;
    static boolean sendMessage=false;
    static boolean cancelMessage=false;
    static String VoiceMessage="";
    MediaPlayer sendmp,receivemp;

    String draft_id;
    String draft_sms="";
    EditText messageTxt;

    private static final String PREF_NAME = "nextage_quiz";
    private static final int PRIVATE_MODE = 0;
    SharedPreferences getPrefs;

    int modeType;

    BroadcastReceiver IncomingSMS =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final Bundle bundle = intent.getExtras();

                if (bundle != null) {
                    final Object[] pduObjects = (Object[]) bundle.get("pdus");

                    for (int i = 0; i < pduObjects.length; ++i) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pduObjects[i]);

                        String number = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();
                        savedReceivingSMS(number, message);
                    }
                    //—retrieve the SMS message received—
                }

            } catch (Exception e) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatlv = (ListView) findViewById(R.id.ChatlistView);

        chat_name=getContactName(this,call_number);
        chat = getConversation();

        TextView nameTextView;
        nameTextView=(TextView)findViewById(R.id.ChatNametextView);


        if(chat_name!=null) {
            nameTextView.setText(chat_name);
        }else{
            nameTextView.setText(call_number);
        }

        sendmp = MediaPlayer.create(this, R.raw.send);
        receivemp = MediaPlayer.create(this, R.raw.receive);

        ImageView background;
        background = (ImageView) findViewById(R.id.chat_background);

        getPrefs = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

        if (getPrefs.getInt("id", 0) == 1) {
            background.setBackgroundResource(R.drawable.wallpaper1);
        } else if (getPrefs.getInt("id", 0) == 2) {
            background.setBackgroundResource(R.drawable.wallpaper2);
        } else if (getPrefs.getInt("id", 0) == 3) {
            background.setBackgroundResource(R.drawable.wallpaper3);
        } else if (getPrefs.getInt("id", 0) == 4) {
            background.setBackgroundResource(R.drawable.wallpaper4);
        }
        else{
            if (!getPrefs.getString("path", "").equalsIgnoreCase("")) {
                BitmapDrawable ob = new BitmapDrawable(getPrefs.getString("path", ""));
                background.setBackgroundDrawable(ob);
            }
        }

        modeType = AppCompatDelegate.getDefaultNightMode();
        if (modeType == AppCompatDelegate.MODE_NIGHT_YES) {
            background.setColorFilter(Color.argb(0, 0, 0, 0));
        }

        Uri uri = Uri.parse("content://sms");
        String selection = "thread_id = '" + thread_id + "' OR address = '"+ call_number + "'";

        ContentValues values = new ContentValues();
        values.put("read", true);

        getContentResolver().update(uri, values, selection, null);

        ImageButton backButton;
        backButton=(ImageButton)findViewById(R.id.chatBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageTxt.getText().toString();
                //if(message.trim()!="" && !message.trim().isEmpty() && message!=null) {
                if(!message.trim().isEmpty()) {
                    ContentValues values = new ContentValues();
                    values.put("address", call_number);
                    values.put("body", message);
                    values.put("date", String.valueOf(System.currentTimeMillis()));
                    values.put("type", "3");
                    values.put("thread_id", thread_id);
                    getContentResolver().insert(Uri.parse("content://sms/draft"), values);
                }
                else{
                    try{
                        getContentResolver().delete(Uri.parse("content://sms/" + draft_id), null, null);
                    }catch (Exception e){
                        Log.e("Error","No draft_id found");
                    }
                }
                MainActivity.refreshRequired=true;
                finish();
            }
        });

        ImageButton deleteButton;
        deleteButton=(ImageButton)findViewById(R.id.deleteImageButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDefaultSmsApp(getApplicationContext())) {

                    android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ChatActivity.this);
                    alertDialogBuilder.setMessage("Delete this conversation?\nThis action cannot be undo.");

                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            String selection = "thread_id = " + thread_id;
                            String uri = "content://sms/";
                            getContentResolver().delete(Uri.parse(uri), selection, null);
                            MainActivity.refreshRequired = true;
                            finish();
                        }
                    });

                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else{
                    try {
                        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ChatActivity.this);
                        alertDialogBuilder.setMessage("Set Slacktalk as default sms app to enable delete conversation.");

                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent =
                                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,getApplicationContext().getPackageName());
                                startActivity(intent);
                            }
                        });

                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        ImageButton callButton;
        callButton=(ImageButton)findViewById(R.id.CallimageButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });

        adapter=new ChatAdapter(this,chat);
        chatlv.setAdapter(adapter);
        chatlv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final SMS currentSMS = (SMS) adapter.getItem(position);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                CharSequence addBtnSelection[] = new CharSequence[]{"Delete"};
                builder.setItems(addBtnSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if(isDefaultSmsApp(activity)) {
                                    String uri = "content://sms/"+currentSMS.getId();
                                    getContentResolver().delete(Uri.parse(uri), null, null);
                                    refreshChatlist();
                                }
                                else{
                                    try {
                                        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
                                        alertDialogBuilder.setMessage("Set Slacktalk as default sms app to enable delete message.");

                                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                Intent intent =
                                                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,getPackageName());
                                                startActivity(intent);
                                            }
                                        });

                                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                    catch (Exception e){
                                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                                break;
                        }

                    }
                });
                builder.show();
                return false;
            }
        });

        messageTxt=(EditText)findViewById(R.id.MessageEditText);
        messageTxt.setText(draft_sms);

        ImageButton btnSendSMS = (ImageButton) findViewById(R.id.sendImageButton);
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isDefaultSmsApp(getApplicationContext())) {
                    String message = messageTxt.getText().toString();
                    if (message.trim()!="" && !message.trim().isEmpty() && message!=null) {
                        sendSMS(call_number, message);
                        ContentValues values = new ContentValues();
                        values.put("address", call_number);
                        values.put("body", message);
                        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        messageTxt.getText().clear();
                        refreshChatlist();

                        if (sendmp.isPlaying()) { sendmp.stop();}
                        sendmp.start();
                    }
                }
                else{
                    try {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                        alertDialogBuilder.setMessage("Set Slacktalk as default sms app to enable send message.");

                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent =
                                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                        getApplicationContext().getPackageName());
                                startActivity(intent);
                            }
                        });

                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(VoiceMessage!=""){
            messageTxt.setText(VoiceMessage);
        }

        if(sendMessage==true){
            String message = messageTxt.getText().toString();
            sendSMS(call_number, message);
            ContentValues values = new ContentValues();
            values.put("address", call_number);
            values.put("body", message);
            getContentResolver().insert(Uri.parse("content://sms/sent"), values);
            messageTxt.getText().clear();
            refreshChatlist();
        }

        if(cancelMessage==true){
            messageTxt.getText().clear();
            MainActivity.choosen_contact="";
            MainActivity.choosen_sms="";
            finish();
        }

        if(!haveAllPermissions()){
            startActivity(new Intent(ChatActivity.this, getPermissionActivity.class));
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(IncomingSMS, filter);

    }

    @Override
    public void onBackPressed() {
        String message = messageTxt.getText().toString();
        if(!message.trim().isEmpty()) {
            ContentValues values = new ContentValues();
            values.put("address", call_number);
            values.put("body", message);
            values.put("date", String.valueOf(System.currentTimeMillis()));
            values.put("type", "3");
            values.put("thread_id", thread_id);
            getContentResolver().insert(Uri.parse("content://sms/draft"), values);
        }
        else{
            try{
                getContentResolver().delete(Uri.parse("content://sms/" + draft_id), null, null);
            }catch (Exception e){
                Log.e("Error","No draft_id found");
            }
        }
        MainActivity.refreshRequired=true;
        finish();
    }

    public void refreshChatlist(){
        chat=getConversation();
        adapter=new ChatAdapter(activity,chat);
        chatlv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        MainActivity.refreshRequired=true;
    }

    public List<SMS> getConversation() {
        List<SMS> chat = new ArrayList<SMS>();
        String selection = "thread_id = '" + thread_id + "' OR address = '"+ call_number + "'";
        try{
            Cursor c = this.getContentResolver().query(Uri.parse("content://sms/"), null, selection, null, null);
            this.startManagingCursor(c);

            int totalSMS = c.getCount();
            if (c.moveToLast()) {

                for (int i = 0; i < totalSMS; i++) {
                    SMS objSms = new SMS();
                    objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                    objSms.setBody(c.getString(c.getColumnIndexOrThrow("body")));
                    objSms.setReadState(c.getString(c.getColumnIndex("read")));
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        objSms.setType("inbox");
                    }
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("2")) {
                        objSms.setType("sent");
                    }
                    Date smsDayTime = new Date(Long.valueOf(c.getString(c.getColumnIndex("date"))));
                    objSms.setDate(smsDayTime);
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("3")) {
                        draft_id=c.getString(c.getColumnIndexOrThrow("_id"));
                        draft_sms=(c.getString(c.getColumnIndexOrThrow("body")));
                    }else {
                        chat.add(objSms);
                    }
                    c.moveToPrevious();
                }
            }
            c.close();

        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return chat;
    }

    public boolean haveAllPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS);
            int permissionReadContacts = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            int permissionCallPhone = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
            if (permissionSendMessage == PackageManager.PERMISSION_GRANTED && permissionReadContacts == PackageManager.PERMISSION_GRANTED && permissionCallPhone == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    public void call(){
        PackageManager pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.CALL_PHONE,
                this.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            // do stuff
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + call_number));
            startActivity(intent);
        }
    }

    public void savedReceivingSMS(String phonenumber, String message){
        ContentValues values = new ContentValues();
        values.put("address", phonenumber);
        values.put("body", message);
        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
        refreshChatlist();

        if(phonenumber.equals(call_number)){
            receivemp = MediaPlayer.create(this, R.raw.receive);
        }else{
            receivemp = MediaPlayer.create(this, R.raw.income);
        }
        if (receivemp.isPlaying()) { receivemp.stop();}
        receivemp.start();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
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
