package com.example.naris.slacktalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;

public class StartActivity extends AppCompatActivity {

    protected boolean _active = true;
    protected int _splashTime = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final TextView textViewQuote = (TextView) findViewById(R.id.textViewQuote);

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {
                            waited += 100;
                        }
                    }
                } catch (Exception e) {
                } finally {
                    startActivity(new Intent(StartActivity.this,
                            MainActivity.class));
                    finish();
                }
            };
        };
        splashTread.start();
    }
}
