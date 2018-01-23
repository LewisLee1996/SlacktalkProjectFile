package com.example.naris.slacktalk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Lee on 10/6/2016.
 */

public class SetDefaultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.set_default);

            final String myPackageName = getPackageName();

            Button btnSet = (Button) findViewById(R.id.btn_set);
            Button btnSkip = (Button) findViewById(R.id.btn_skip);

            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!haveAllPermissions()) {
                        startActivity(new Intent(SetDefaultActivity.this, getPermissionActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SetDefaultActivity.this, Welcome2Activity.class));
                        finish();
                    }
                }
            });

            btnSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =
                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            myPackageName);
                    startActivity(intent);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDefaultSmsApp(getApplicationContext())) {
            startActivity(new Intent(SetDefaultActivity.this, Welcome2Activity.class));
            finish();
        }
    }

    public boolean haveAllPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS);
            int permissionReadContacts = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            int permissionCallPhone = ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE);
            if(permissionSendMessage == PackageManager.PERMISSION_GRANTED && permissionReadContacts == PackageManager.PERMISSION_GRANTED && permissionCallPhone == PackageManager.PERMISSION_GRANTED){
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

    public static boolean isDefaultSmsApp(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName());
        }
        else{
            return true;
        }
    }

}
