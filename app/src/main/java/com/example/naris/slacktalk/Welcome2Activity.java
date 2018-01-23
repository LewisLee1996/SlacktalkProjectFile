package com.example.naris.slacktalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class Welcome2Activity extends AppCompatActivity {

    protected boolean _active = true;
    protected int _splashTime = 1000;


    Random runRandom = new Random();
    int randomnumber = (int) (11 * Math.random())+1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        final TextView textViewQuote = (TextView) findViewById(R.id.textViewQuote);

        if(randomnumber == 0){
            textViewQuote.setText("The most important thing in communication is hearing what isn't said.\n Peter Drucker");
        }
        else if(randomnumber == 1){
            textViewQuote.setText("Speak when you are angry -- and you'll make the best speech you'll ever regret.\n Laurence Peters");
        }
        else if(randomnumber == 2){
            textViewQuote.setText("People who have nothing to say are never at a loss in talking.\n Josh Billings");
        }
        else if(randomnumber == 3){
            textViewQuote.setText("If you have nothing to say, say nothing.\n Mark Twain");
        }
        else if(randomnumber == 4){
            textViewQuote.setText("The single biggest problem in communication is the illusion that it has taken place.\n  George Bernard Shaw");
        }
        else if(randomnumber == 5){
            textViewQuote.setText("I do not agree with what you have to say, but I'll defend to the death your right to say it.\n Evelyn Beatrice Hall");
        }
        else if(randomnumber == 6){
            textViewQuote.setText("Kind words can be short and easy to speak, but their echoes are truly endless.\n Mother Theresa");
        }
        else if(randomnumber == 7){
            textViewQuote.setText("The art of communication is the language of leadership.\n James Humes");
        }
        else if(randomnumber == 8){
            textViewQuote.setText("Make sure to communicate your idea quickly and keep it straight to the point.\n Paul Bailey");
        }
        else if(randomnumber == 9){
            textViewQuote.setText("Be silent, or say something better than silence.\n Pythagoras");
        }
        else if(randomnumber == 10){
            textViewQuote.setText("Deliver your words not by number but by weight.\n Proverb");
        }
        else if(randomnumber == 11){
            textViewQuote.setText("Good, the more communicated, more abundant grows.\n John Milton");
        }

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
                    startActivity(new Intent(Welcome2Activity.this,
                            MainActivity.class));
                    finish();
                }
            };
        };
        splashTread.start();

    }
}
