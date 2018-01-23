package com.example.naris.slacktalk;

/**
 * Created by Lee on 10/4/2016.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HeadlessSmsSendService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

}