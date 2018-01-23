package com.example.naris.slacktalk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;

public class SmsBroadcastReceiver extends BroadcastReceiver{

    public static final String SMS_CONTENT = "sms_content";
    static String number;
    static String message;
    String name;
    static String replyMessageBody;
    static boolean readedMessage=false;
    static boolean messageIgnored=false;
    static boolean replyMessage=false;
    static boolean MessageReady=false;
    static boolean repliedMessage=false;
    static boolean callback=false;

    private static final String ACTION_SMS_NEW = "android.provider.Telephony.SMS_RECEIVED";

    SharedPreferences notificationPrefs,voiceControlSystemPrefs;
    private static final int PRIVATE_MODE = 0;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onReceive(final Context context, Intent intent) {
        if (!appIsRunnning(context)) {
        final Bundle bundle = intent.getExtras();

            notificationPrefs = context.getSharedPreferences("notificationSetting", PRIVATE_MODE);
            voiceControlSystemPrefs = context.getSharedPreferences("voiceControlSystemSetting", PRIVATE_MODE);

            try {
            if (bundle != null) {
                final Object[] pduObjects = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pduObjects.length; ++i) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pduObjects[i]);

                    number = currentMessage.getDisplayOriginatingAddress();
                    message = currentMessage.getDisplayMessageBody();
                    name = getContactName(context, number);
                    if (name == null) {
                        name = number;
                    }

                    MainActivity.choosen_contact = number;

                    //use thread id if need
                    Cursor c = context.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
                    int totalSMS = c.getCount();
                    String compare_num;
                    if (c.moveToFirst()) {
                        for (int j = 0; i < totalSMS; j++) {
                            compare_num=c.getString(c.getColumnIndexOrThrow("address")).trim();
                            if(compare_num.replaceAll("[()\\s-]+", "").equals(number.replaceAll("[()\\s-]+", ""))) {
                                MainActivity.choosen_sms = c.getString(c.getColumnIndexOrThrow("thread_id"));
                                break;
                            }
                            c.moveToNext();
                        }
                    }
                    c.close();

                    if(notificationPrefs.getString("notification","").equals("on")) {
                        Intent actionIntentOpen = new Intent(context, ChatActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(ChatActivity.class);
                        stackBuilder.addNextIntent(actionIntentOpen);
                        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


                        Notification notify = new Notification.Builder(context)
                                .setTicker("Slacktalk")
                                .setContentTitle(name)
                                .setContentText(message)
                                .setSmallIcon(R.drawable.notificationicon)
                                .setAutoCancel(true)
                                .setContentIntent(notificationPendingIntent).getNotification();
                        notify.defaults |= Notification.DEFAULT_VIBRATE;
                        notify.flags = Notification.FLAG_AUTO_CANCEL;
                        NotificationManager NotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotifyManager.notify(0, notify);
                    }
                }

                final String action = intent.getAction();
                if (ACTION_SMS_NEW.equals(action)) {
                    if(notificationPrefs.getString("notification","").equals("on") && voiceControlSystemPrefs.getString("voiceControlSystem","").equals("on")) {
                        wakeUpScreen(context);
                        Intent speechIntent = new Intent(context, SpeechService.class);
                        speechIntent.putExtra(SpeechService.EXTRA_WORD, "You have a new message from " + name + "\nDo you want me to read the message?");
                        context.startService(speechIntent);
                    }else{
                        if(notificationPrefs.getString("notification","").equals("on")) {
                            wakeUpScreen(context);
                            Intent speechIntent = new Intent(context, SpeechService.class);
                            speechIntent.putExtra(SpeechService.EXTRA_WORD, "You have a new message from " + name);
                            context.startService(speechIntent);
                            messageIgnored=true;
                        }
                    }

                    ContentValues values = new ContentValues();
                    values.put("address", number);//sender name
                    values.put("body", message);
                    context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

    public void wakeUpScreen(Context context){
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = pm.isScreenOn();

        if(isScreenOn==false)
        {

            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");

            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }

public boolean appIsRunnning(Context context){
    ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    // get the info from the currently running task
    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

    ComponentName componentInfo = taskInfo.get(0).topActivity;
    if (componentInfo.getPackageName().equalsIgnoreCase(context.getPackageName())) {
        return true;
    } else {
        return false;
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
}