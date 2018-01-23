package com.example.naris.slacktalk;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    int modeType;
    private static final String PREF_NAME = "nextage_quiz";
    private static final int PRIVATE_MODE = 0;

    private PrefManager prefManager;
    SharedPreferences getPrefs,nightModePrefs,notificationPrefs,voiceControlSystemPrefs;

    private static int RESULT_LOAD_IMAGE = 1;

    static ImageView ImageViewBackground5;

    static ImageView ImageViewBackgroundCheck1;
    static ImageView ImageViewBackgroundCheck2;
    static ImageView ImageViewBackgroundCheck3;
    static ImageView ImageViewBackgroundCheck4;
    static ImageView ImageViewBackgroundCheck5;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            getPrefs.edit().putString("path", picturePath).apply();
            cursor.close();
            getPrefs.edit().putInt("id", 0).apply();

            File imgFile = new File(getPrefs.getString("path", ""));
            if(imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                BitmapDrawable ob = new BitmapDrawable(getCircleBitmap(myBitmap));
                ImageViewBackground5.setImageDrawable(ob);
            }
            hideAllCheckMark();
            ImageViewBackgroundCheck5.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        prefManager = new PrefManager(this);

        getPrefs = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        nightModePrefs = this.getSharedPreferences("nightModeSetting",PRIVATE_MODE);
        notificationPrefs = this.getSharedPreferences("notificationSetting", PRIVATE_MODE);
        voiceControlSystemPrefs = this.getSharedPreferences("voiceControlSystemSetting", PRIVATE_MODE);

        ImageView ImageViewBackground1 = (ImageView) findViewById(R.id.imageViewBackground1);
        ImageView ImageViewBackground2 = (ImageView) findViewById(R.id.imageViewBackground2);
        ImageView ImageViewBackground3 = (ImageView) findViewById(R.id.imageViewBackground3);
        ImageView ImageViewBackground4 = (ImageView) findViewById(R.id.imageViewBackground4);
        ImageViewBackground5 = (ImageView) findViewById(R.id.imageViewBackground5);
        Button ButtonBackgroundDefault = (Button) findViewById(R.id.wallpaperSetDefaultButton);

        ImageViewBackgroundCheck1 = (ImageView) findViewById(R.id.imageViewBackgroundCheck1);
        ImageViewBackgroundCheck2 = (ImageView) findViewById(R.id.imageViewBackgroundCheck2);
        ImageViewBackgroundCheck3 = (ImageView) findViewById(R.id.imageViewBackgroundCheck3);
        ImageViewBackgroundCheck4 = (ImageView) findViewById(R.id.imageViewBackgroundCheck4);
        ImageViewBackgroundCheck5 = (ImageView) findViewById(R.id.imageViewBackgroundCheck5);

        final Switch nightModeSwitch = (Switch) findViewById(R.id.NightModeswitch);
        final Switch notificationSwitch = (Switch) findViewById(R.id.NotificationSwitch);
        final Switch voiceContorlSystemSwitch = (Switch) findViewById(R.id.VoiceControlSwitch);

        if (getPrefs.getInt("id", 0) == 1) {
            ImageViewBackgroundCheck1.setVisibility(View.VISIBLE);
        } else if (getPrefs.getInt("id", 0) == 2) {
            ImageViewBackgroundCheck2.setVisibility(View.VISIBLE);
        } else if (getPrefs.getInt("id", 0) == 3) {
            ImageViewBackgroundCheck3.setVisibility(View.VISIBLE);
        } else if (getPrefs.getInt("id", 0) == 4) {
            ImageViewBackgroundCheck4.setVisibility(View.VISIBLE);
        }
        else{
            if(!getPrefs.getString("path", "").equals("")) {
                if (haveSDcardPermissions()) {
                    File imgFile = new File(getPrefs.getString("path", ""));
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        BitmapDrawable ob = new BitmapDrawable(getCircleBitmap(myBitmap));
                        ImageViewBackground5.setImageDrawable(ob);
                        ImageViewBackgroundCheck5.setVisibility(View.VISIBLE);
                    }
                } else {
                    getSDCardPermissions();
                }
            }
        }



        ImageViewBackground1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPrefs.edit().putInt("id", 1).apply();
                getPrefs.edit().putString("path", "").apply();
                ImageViewBackground5.setImageResource(android.R.color.transparent);
                hideAllCheckMark();
                ImageViewBackgroundCheck1.setVisibility(View.VISIBLE);
            }
        });

        ImageViewBackground2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPrefs.edit().putInt("id", 2).apply();
                getPrefs.edit().putString("path", "").apply();
                ImageViewBackground5.setImageResource(android.R.color.transparent);
                hideAllCheckMark();
                ImageViewBackgroundCheck2.setVisibility(View.VISIBLE);
            }
        });

        ImageViewBackground3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPrefs.edit().putInt("id", 3).apply();
                getPrefs.edit().putString("path", "").apply();
                ImageViewBackground5.setImageResource(android.R.color.transparent);
                hideAllCheckMark();
                ImageViewBackgroundCheck3.setVisibility(View.VISIBLE);
            }
        });

        ImageViewBackground4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPrefs.edit().putInt("id", 4).apply();
                getPrefs.edit().putString("path", "").apply();
                ImageViewBackground5.setImageResource(android.R.color.transparent);
                hideAllCheckMark();
                ImageViewBackgroundCheck4.setVisibility(View.VISIBLE);
            }
        });
        ImageViewBackground5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(haveSDcardPermissions()) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
                else{
                    getSDCardPermissions();
                }
            }
        });

        ButtonBackgroundDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewBackground5.setImageResource(android.R.color.transparent);
                hideAllCheckMark();
                getPrefs.edit().putInt("id", 0).apply();
                getPrefs.edit().putString("path", "").apply();
            }
        });
            if (notificationPrefs.getString("notification", "").equals("on")) {
                notificationSwitch.setChecked(true);
            } else {
                notificationSwitch.setChecked(false);
            }

            if (voiceControlSystemPrefs.getString("voiceControlSystem", "").equals("on")) {
                voiceContorlSystemSwitch.setChecked(true);
            } else {
                voiceContorlSystemSwitch.setChecked(false);
            }

            if(nightModePrefs.getString("nightMode", "").equals("on")){
                nightModeSwitch.setChecked(true);

                View view = (View) findViewById(R.id.line1);
                view.setBackgroundColor(Color.WHITE);
                view = (View) findViewById(R.id.line2);
                view.setBackgroundColor(Color.WHITE);
                view = (View) findViewById(R.id.line3);
                view.setBackgroundColor(Color.WHITE);
                view = (View) findViewById(R.id.line4);
                view.setBackgroundColor(Color.WHITE);
                view = (View) findViewById(R.id.line5);
                view.setBackgroundColor(Color.WHITE);

            }
            else{
                nightModeSwitch.setChecked(false);
            }

        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    nightModePrefs.edit().putString("nightMode", "on").apply();
                    SettingsActivity.this.recreate();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    nightModePrefs.edit().putString("nightMode", "off").apply();
                    SettingsActivity.this.recreate();
                }
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    notificationPrefs.edit().putString("notification", "on").apply();
                    Snackbar.make(notificationSwitch , "Message Notification is now turned on.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Message Notification");
                    alert.setIcon(R.drawable.notif);
                    alert.setMessage("Do you really want to turn off the message notification?");
                    alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            notificationPrefs.edit().putString("notification", "off").apply();
                            Snackbar.make(notificationSwitch , "Message Notification is now turned off.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                    alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            notificationPrefs.edit().putString("notification", "on").apply();
                            notificationSwitch.setChecked(true);
                            Snackbar.make(notificationSwitch , "Message Notification remain turned on.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                    alert.show();

                }
            }
        });

        voiceContorlSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(haveMicrophonePermissions()){
                        voiceControlSystemPrefs.edit().putString("voiceControlSystem", "on").apply();
                        Snackbar.make(notificationSwitch , "Voice control is now turned on.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else {
                        getMicrophonePermissions();
                        voiceContorlSystemSwitch.setChecked(false);
                    }
                }else{

                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Voice Control");
                    alert.setIcon(R.drawable.speechrecog);
                    alert.setMessage("Do you really want to turn off the voice control feature?");
                    alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            voiceControlSystemPrefs.edit().putString("voiceControlSystem", "off").apply();
                            Snackbar.make(notificationSwitch , "Voice control is now turned off.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                    alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            voiceControlSystemPrefs.edit().putString("voiceControlSystem", "on").apply();
                            voiceContorlSystemSwitch.setChecked(true);
                            Snackbar.make(notificationSwitch , "Voice control remain turned on.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                    alert.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!getPrefs.getString("path", "").equals("")) {
            if (haveSDcardPermissions()) {
                File imgFile = new File(getPrefs.getString("path", ""));
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    BitmapDrawable ob = new BitmapDrawable(getCircleBitmap(myBitmap));
                    ImageViewBackground5.setImageDrawable(ob);
                    ImageViewBackgroundCheck5.setVisibility(View.VISIBLE);
                }
            } else {
                getSDCardPermissions();
            }
        }
    }

    public boolean haveSDcardPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadSDCard = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
            if(permissionReadSDCard == PackageManager.PERMISSION_GRANTED){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean haveMicrophonePermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadSDCard = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
            if(permissionReadSDCard == PackageManager.PERMISSION_GRANTED){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public void getMicrophonePermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    public void getSDCardPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    public void hideAllCheckMark(){
        ImageViewBackgroundCheck1.setVisibility(View.INVISIBLE);
        ImageViewBackgroundCheck2.setVisibility(View.INVISIBLE);
        ImageViewBackgroundCheck3.setVisibility(View.INVISIBLE);
        ImageViewBackgroundCheck4.setVisibility(View.INVISIBLE);
        ImageViewBackgroundCheck5.setVisibility(View.INVISIBLE);

    }

    public static Bitmap getCircleBitmap(Bitmap bm) {

        int sice = Math.min((bm.getWidth()), (bm.getHeight()));

        Bitmap bitmap = ThumbnailUtils.extractThumbnail(bm, sice, sice);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xffff0000;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) 4);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == android.view.KeyEvent.KEYCODE_BACK)
        {
            finish();
        }
        return false;
    };
}
