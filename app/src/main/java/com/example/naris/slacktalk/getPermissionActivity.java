package com.example.naris.slacktalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 10/4/2016.
 */

public class getPermissionActivity extends AppCompatActivity {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private Button btnExit, btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_permission_screen);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnSet = (Button) findViewById(R.id.btn_set);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllPermissions();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (haveAllPermissions()) {
            startActivity(new Intent(getPermissionActivity.this, Welcome2Activity.class));
            finish();
        }
    }

    public void getAllPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS);
            int permissionReadContacts = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            int permissionCallPhone = ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE);
            int permissionWriteContact = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS);
                List<String> listPermissionsNeeded = new ArrayList<>();
                if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
                }
                if (permissionReadContacts != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
                }
                if (permissionCallPhone != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
                }
                if (permissionWriteContact != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
                }
                if (!listPermissionsNeeded.isEmpty()) {
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                }
        }
    }

    public boolean haveAllPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS);
            int permissionReadContacts = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            int permissionCallPhone = ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE);
            int permissionWriteContact = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS);
            if(permissionSendMessage == PackageManager.PERMISSION_GRANTED && permissionReadContacts == PackageManager.PERMISSION_GRANTED && permissionCallPhone == PackageManager.PERMISSION_GRANTED && permissionWriteContact == PackageManager.PERMISSION_GRANTED){
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
}
