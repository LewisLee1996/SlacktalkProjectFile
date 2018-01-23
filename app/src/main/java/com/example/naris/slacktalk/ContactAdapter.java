package com.example.naris.slacktalk;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 11/24/2016.
 */

public class ContactAdapter extends BaseAdapter {
    Context context;
    List<String> contacts = new ArrayList<String>();
    private static LayoutInflater inflater = null;

    public ContactAdapter(Context listcontext, List<String> contacts_list) {
        // TODO Auto-generated constructor stub
        contacts = contacts_list;
        context = listcontext;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub\
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView contacttv;
        ImageButton callbutton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ContactAdapter.Holder holder = new ContactAdapter.Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.contact__list, null);
        holder.contacttv = (TextView) rowView.findViewById(R.id.contact_info);
        holder.callbutton = (ImageButton) rowView.findViewById(R.id.callButton);

        final String contact = contacts.get(position).toString();
        holder.contacttv.setText(contact);
        holder.callbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected_num = contact.substring(contact.indexOf("\n", 0), contact.length()).trim();
                PackageManager pm = context.getPackageManager();
                int hasPerm = pm.checkPermission(
                        Manifest.permission.CALL_PHONE,
                        context.getPackageName());
                if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                    // do stuff
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + selected_num));
                    context.startActivity(intent);
                }
            }
        });

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MainActivity.choosen_sms = null;
                MainActivity.choosen_contact = null;
                String selected_num = contact.substring(contact.indexOf("\n", 0), contact.length()).trim();
                MainActivity.choosen_contact = selected_num;
                String compare_num;
                try {

                    Cursor c = context.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
                    int totalSMS = c.getCount();
                    if (c.moveToFirst()) {
                        for (int i = 0; i < totalSMS; i++) {
                            compare_num = c.getString(c.getColumnIndexOrThrow("address")).trim();
                            if (compare_num.replaceAll("[()\\s-]+", "").equals(selected_num.replaceAll("[()\\s-]+", ""))) {
                                MainActivity.choosen_sms = c.getString(c.getColumnIndexOrThrow("thread_id"));
                                break;
                            }
                            c.moveToNext();
                        }
                    }
                    c.close();

                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    context.startActivity(new Intent(context, ChatActivity.class));
                }
            }
        });

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        return rowView;
    }

    @Override
    public void notifyDataSetChanged() // Create this function in your adapter class
    {
        super.notifyDataSetChanged();

    }
}