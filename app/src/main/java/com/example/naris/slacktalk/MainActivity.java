package com.example.naris.slacktalk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import static android.app.PendingIntent.getActivity;
import static com.example.naris.slacktalk.R.id.container;
import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {
    static TextView loadingTextView,loadingContactsTextView;
    View Apptabs;
    FloatingActionButton fab;
    static List<String> allSMS = new ArrayList<>();
    static List<SMS> initialmessage = new ArrayList<SMS>();
    static List<SMS> message = new ArrayList<SMS>();
    public static ListView lv;
    static ContactAdapter contactAdapter;
    public static String choosen_sms;
    public static String choosen_contact;
    static List<String> contacts = new ArrayList<>();
    public static ListView clv;
    static SMSAdapter smsAdapter;
    public ListView searchlv;
    ArrayList<String> result;
    SearchView searchView;
    SearchAdapter searchAdapter;

    ImageButton VoiceControlButton;

    MediaPlayer receivemp;

    static Boolean refreshRequired=false;
    static Boolean refreshContactRequired=false;
    static boolean makecall = false;
    static boolean endVoiceControl=false;
    static String callnumber="";
    static boolean makeMessage = false;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static final String PREF_NAME = "nextage_quiz";
    private static final int PRIVATE_MODE = 0;

    SharedPreferences getPrefs,nightModePrefs,voiceControlSystemPrefs;

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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Code for the floating icon on the main screen.
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSfhDbsggrBezAZ5xWUJy4E-5OM2Ha9Jq8o8VDBumzPQ0F1JcA/viewform"));
                startActivity(intent);
                Snackbar.make(view, "Please rate and comment about this app. ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new LoadingTask().execute();

        receivemp = MediaPlayer.create(this, R.raw.income);

        ImageButton AddConImageButton;
        AddConImageButton = (ImageButton) findViewById(R.id.AddConversationImageButton);
        AddConImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                CharSequence addBtnSelection[] = new CharSequence[]{"Send new message", "Add contact"};
                builder.setItems(addBtnSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosen_contact = null;
                                choosen_sms = null;
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Please enter the receiver's number:");

// Set up the input
                                final EditText input = new EditText(MainActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);

// Set up the buttons
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String receiverNum = input.getText().toString().trim();

                                        Pattern pattern = Pattern.compile("^[\\d() +-]+$");
                                        Matcher matcher = pattern.matcher(receiverNum);

                                        if (matcher.matches()) {
                                            choosen_contact = receiverNum;
                                            Cursor c = MainActivity.this.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
                                            int totalSMS = c.getCount();
                                            if (c.moveToFirst()) {
                                                for (int i = 0; i < totalSMS; i++) {
                                                    String compare_num = c.getString(c.getColumnIndexOrThrow("address")).trim();
                                                    if (compare_num.replaceAll("[()\\s-]+", "").equals(receiverNum.replaceAll("[()\\s-]+", ""))) {
                                                        choosen_sms = c.getString(c.getColumnIndexOrThrow("thread_id"));
                                                        break;
                                                    }
                                                    c.moveToNext();
                                                }
                                            }
                                            c.close();
                                            startActivity(new Intent(MainActivity.this, ChatActivity.class));
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Invalid number", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                                break;
                            case 1:
                                refreshContactRequired = true;
                                Intent intent = new Intent(Intent.ACTION_INSERT,
                                        ContactsContract.Contacts.CONTENT_URI);
                                startActivity(intent);
                                break;
                        }

                    }
                });
                builder.show();
            }
        });

        VoiceControlButton = (ImageButton) findViewById(R.id.VoiceControlButton);
        VoiceControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveMicrophonePermissions()){
                    Intent speechInput = new Intent(MainActivity.this, VoiceControlInput.class);
                    speechInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(speechInput);
                }
                else {
                    getMicrophonePermissions();
                }
            }
        });

        //Method to change background wallpaper

        getPrefs = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        nightModePrefs = this.getSharedPreferences("nightModeSetting",PRIVATE_MODE);
        voiceControlSystemPrefs = this.getSharedPreferences("voiceControlSystemSetting", PRIVATE_MODE);

        if(nightModePrefs.getString("nightMode", "").equals("on")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (voiceControlSystemPrefs.getString("voiceControlSystem", "").equals("on")) {
            VoiceControlButton.setVisibility(View.VISIBLE);
        } else {
            VoiceControlButton.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(!haveAllPermissions()){
            startActivity(new Intent(MainActivity.this, getPermissionActivity.class));
            finish();
        }

        if(nightModePrefs.getString("nightMode", null).equals("on")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        receivemp = MediaPlayer.create(this, R.raw.income);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(IncomingSMS, filter);

        if (refreshRequired) {
            refreshSMSList();
        }

        if (refreshContactRequired) {
            refreshContactList();
        }

        if (voiceControlSystemPrefs.getString("voiceControlSystem", "").equals("on")) {
            VoiceControlButton.setVisibility(View.VISIBLE);
        } else {
            VoiceControlButton.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onPause(){
        super.onPause();

        unregisterReceiver(IncomingSMS);
        receivemp.release();
        receivemp=null;
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    public ArrayList<SMS> fetchSms() {
        ArrayList<SMS> sms = new ArrayList<>();
        try {
            Cursor c = this.getContentResolver().query(Uri.parse("content://sms/conversations/"), null, null, null, "date DESC");
            int totalSMS = c.getCount();

            if (c.moveToFirst()) {
                for (int i = 0; i < totalSMS; i++) {
                    SMS objSms = new SMS();
                    String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                    objSms.setThread_Id(thread_id);

                    String selection = "thread_id = " + thread_id;
                    Uri uri = Uri.parse("content://sms");
                    Cursor cr = this.getContentResolver().query(uri, null, selection, null, null);
                    if (cr.moveToFirst()) {
                        objSms.setId(cr.getString(cr.getColumnIndexOrThrow("_id")));
                        objSms.setNumber(cr.getString(cr.getColumnIndexOrThrow("address")));
                        String name = getContactName(this, cr.getString(cr.getColumnIndexOrThrow("address")));
                        objSms.setName(name);
                        objSms.setBody(cr.getString(cr.getColumnIndex("body")));
                        if (cr.getString(cr.getColumnIndexOrThrow("type")).contains("1")) {
                            objSms.setType("inbox");
                        }
                        if (cr.getString(cr.getColumnIndexOrThrow("type")).contains("2")) {
                            objSms.setType("sent");
                        }
                        if (cr.getString(cr.getColumnIndexOrThrow("type")).contains("3")) {
                            objSms.setType("draft");
                        }
                        Date smsDayTime = new Date(Long.valueOf(cr.getString(cr.getColumnIndex("date"))));
                        objSms.setDate(smsDayTime);
                        objSms.setReadState(cr.getString(cr.getColumnIndexOrThrow("read")));
                    }
                    cr.close();

                    sms.add(objSms);
                    c.moveToNext();
                }
            }
            c.close();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ERROR", e.getMessage());
        }
        return sms;
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    private static long getContactID(ContentResolver contactHelper,String number) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = { ContactsContract.PhoneLookup._ID };
        Cursor cursor = null;
        try {
            cursor = contactHelper.query(contactUri, projection, null, null,null);
            if (cursor.moveToFirst()) {
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return -1;
    }

    public void fetchContact() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone._ID}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC");
        if(cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number=contactNumber.replaceAll("[()\\s-]+", "");
                contacts.add(contactName + "\n" + number);
                cursor.moveToNext();
            }
        }
        cursor.close();

        Set<String> hs = new LinkedHashSet<>();
        hs.addAll(contacts);
        contacts.clear();
        contacts.addAll(hs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        //search engine
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // collapse the view ?
                Log.e("queryText", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty()) {
                    result = new ArrayList<String>();
                    for (int i = 0; i < allSMS.size(); i++) {
                        String current = allSMS.get(i);
                        if (current.toLowerCase().contains(newText.toLowerCase())) {
                            result.add(current);
                        }
                    }
                    for (int i = 0; i < contacts.size(); i++) {
                        String current = contacts.get(i);
                        if (current.toLowerCase().contains(newText.toLowerCase())) {
                            result.add(current);
                        }
                    }
                    searchAdapter = new SearchAdapter(MainActivity.this, result);
                    searchlv.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();
                }
                else{
                    ArrayList<String> empty = new ArrayList<String>();
                    searchAdapter = new SearchAdapter(MainActivity.this, empty);
                    searchlv.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();
                }
                return false;
            }


        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                new LoadingAllSMSTask().execute();
                fab.setVisibility(View.INVISIBLE);
                mViewPager.setVisibility(View.INVISIBLE);
                searchlv = (ListView) findViewById(R.id.listSearchResult);
                searchlv.setVisibility(View.VISIBLE);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                //DO SOMETHING WHEN THE SEARCHVIEW IS CLOSING
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
                appBarLayout.addView(Apptabs);

                searchlv.setVisibility(View.INVISIBLE);
                mViewPager.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
            Apptabs = appBarLayout.findViewById(R.id.tabs);
            appBarLayout.removeViewAt(1);
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_faq) {
            startActivity(new Intent(MainActivity.this, FAQActivity.class));
            return true;
        } else if (id == R.id.action_hiw) {
            startActivity(new Intent(MainActivity.this, HowItWorksActivity.class));
            return true;
        } else if (id == R.id.action_credit) {
            startActivity(new Intent(MainActivity.this, CreditActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                final View rootView = inflater.inflate(R.layout.activity_contact, container, false);

                loadingContactsTextView = (TextView) rootView.findViewById(R.id.LoadingContactsTextView);
                loadingContactsTextView.setVisibility(View.VISIBLE);
                try {
                    clv = (ListView) rootView.findViewById(R.id.listViewContactList);
                    contactAdapter = new ContactAdapter(getContext(),contacts);
                    clv.setAdapter(contactAdapter);

                    clv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            CharSequence addBtnSelection[] = new CharSequence[]{"Call", "Delete"};
                            builder.setItems(addBtnSelection, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            String selected_con = (String) parent.getItemAtPosition(position);
                                            String selected_num = selected_con.substring(selected_con.indexOf("\n", 0), selected_con.length()).trim();
                                            PackageManager pm = getContext().getPackageManager();
                                            int hasPerm = pm.checkPermission(
                                                    Manifest.permission.CALL_PHONE,
                                                    getContext().getPackageName());
                                            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                                                // do stuff
                                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + selected_num));
                                                startActivity(intent);
                                            }
                                            break;
                                        case 1:
                                            String selected = (String) parent.getItemAtPosition(position);
                                            String selected_contact = selected.substring(0,selected.indexOf("\n", 0)).trim();
                                            final String selected_number=selected.substring(selected.indexOf("\n", 0),selected.length()).trim();
                                            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                                            alertDialogBuilder.setMessage("Delete " + selected_contact +" in your contacts?\nThis action cannot be undo.");

                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    ContentResolver contactHelper=getContext().getContentResolver();
                                                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                                                    String[] args = new String[] { String.valueOf(getContactID(contactHelper, selected_number)) };
                                                    ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
                                                    try {
                                                        contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
                                                    } catch (RemoteException e) {
                                                        e.printStackTrace();
                                                    } catch (OperationApplicationException e) {
                                                        e.printStackTrace();
                                                    }
                                                    refreshContactRequired = true;
                                                    startActivity(new Intent(getActivity(), RefreshingActivity.class));
                                                }
                                            });

                                            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                            break;
                                    }

                                }
                            });
                            builder.show();

                            return true;
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return rootView;
            } else {
                final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                loadingTextView = (TextView) rootView.findViewById(R.id.LoadingTextView);
                loadingTextView.setVisibility(View.VISIBLE);
                try {
                    lv = (ListView) rootView.findViewById(R.id.listViewMessage);
                    smsAdapter = new SMSAdapter(getContext(),message);
                    lv.setAdapter(smsAdapter);
                    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
                            final SMS currentSMS = (SMS) smsAdapter.getItem(position);
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                            CharSequence addBtnSelection[] = new CharSequence[]{"Delete Conversation"};
                            builder.setItems(addBtnSelection, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            if(isDefaultSmsApp(getContext())) {
                                                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                                                alertDialogBuilder.setMessage("Delete this conversation?\nThis action cannot be undo.");

                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        String selection = "thread_id = " + currentSMS.getThread_id();
                                                        String uri = "content://sms/";
                                                        getContext().getContentResolver().delete(Uri.parse(uri), selection, null);
                                                        refreshRequired = true;
                                                        startActivity(new Intent(getActivity(), RefreshingActivity.class));
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
                                                    android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                                                    alertDialogBuilder.setMessage("Set Slacktalk as default sms app to enable delete conversation.");

                                                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            Intent intent =
                                                                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,getContext().getPackageName());
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
                                                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
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
                } catch (Exception e) {
                    Toast.makeText(getContext(), "No data in the list.", Toast.LENGTH_LONG).show();
                }
                return rootView;
            }
        }
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
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
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
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Messages";
                case 1:
                    return "Contacts";
            }
            return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {

            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Exit");
            alert.setIcon(R.drawable.exiticon);
            alert.setMessage("Do you really want to exit?");
            alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(getApplicationContext(), "See You Soon!", Toast.LENGTH_LONG);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    System.exit(0);
                }
            });
            alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(getApplicationContext(), "Continue enjoying the Virtual Interaction!", Toast.LENGTH_LONG);
                }
            });
            alert.show();

        }
        return false;

    }

    private class LoadingTask extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            try {
                message.clear();
                message.addAll(fetchSms());
            } catch (Exception e) {
                if (!haveAllPermissions()) {
                    startActivity(new Intent(MainActivity.this, getPermissionActivity.class));
                    finish();
                }
            }
            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(message.isEmpty() || message.size()==0 || message==null){
                loadingTextView.setText("NO SMS");
            }else {
                loadingTextView.setVisibility(View.INVISIBLE);
                smsAdapter.notifyDataSetChanged();
            }
            new LoadingContactTask().execute();
        }
        // Do things like hide the progress bar or change a TextView
    }

    private class LoadingContactTask extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            fetchContact();
            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(contacts.isEmpty() || contacts.size()==0 || contacts==null) {
                loadingContactsTextView.setText("NO CONTACTS");
            }
            else{
                loadingContactsTextView.setVisibility(View.INVISIBLE);
                contactAdapter.notifyDataSetChanged();
            }
        }
        // Do things like hide the progress bar or change a TextView
    }

    private class LoadingAllSMSTask extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            allSMS.clear();
            try {
                Cursor c = MainActivity.this.getContentResolver().query(Uri.parse("content://sms"), null, null, null, "date DESC");
                int totalSMS = c.getCount();

                if (c.moveToFirst()) {
                    for (int i = 0; i < totalSMS; i++) {
                        String address=c.getString(c.getColumnIndexOrThrow("address"));
                        String name = getContactName(MainActivity.this, address);
                        String full_body=c.getString(c.getColumnIndexOrThrow("body"));
                        Date smsDayTime = new Date(Long.valueOf(c.getString(c.getColumnIndex("date"))));
                        String sms_date = smsDayTime.toString();
                        StringBuffer body = new StringBuffer(full_body);

                        if (c.getString(c.getColumnIndexOrThrow("type")).contains("2")) {
                            body.insert(0, "You: ");
                        }

                        if(name!=null){
                            allSMS.add(name + "\n" + body + "\n" + sms_date.substring(4,10) + " (" + sms_date.substring(0,3)+")" );
                        }
                        else{
                            allSMS.add(address + "\n" + body + "\n" + sms_date.substring(4,10) + " (" + sms_date.substring(0,3)+")" );
                        }
                        c.moveToNext();
                    }
                }
                c.close();
            } catch (Exception e) {
                if (!haveAllPermissions()) {
                    startActivity(new Intent(MainActivity.this, getPermissionActivity.class));
                    finish();
                }
            }

            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
        // Do things like hide the progress bar or change a TextView
    }

    public void savedReceivingSMS(String phonenumber, String message){
        ContentValues values = new ContentValues();
        values.put("address", phonenumber);
        values.put("body", message);
        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
        refreshSMSList();
        if (receivemp.isPlaying()) { receivemp.stop();}
        receivemp.start();
    }

    public void refreshSMSList(){
        message.clear();
        message.addAll(fetchSms());
        smsAdapter.notifyDataSetChanged();
        refreshRequired = false;
    }

    public void refreshContactList(){
        contacts.clear();
        fetchContact();
        contactAdapter.notifyDataSetChanged();
        refreshContactRequired=false;
    }
}