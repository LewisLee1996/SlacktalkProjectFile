package com.example.naris.slacktalk;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lee on 10/17/2016.
 */

public class SMSAdapter extends BaseAdapter {
    Context context;
    List<SMS> sms=new ArrayList<SMS>();
    private static LayoutInflater inflater=null;

    public SMSAdapter(Context listcontext, List<SMS> messages) {
        // TODO Auto-generated constructor stub
        sms=messages;
        context=listcontext;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub\
        return sms.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return sms.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView nametv;
        TextView bodytv;
        TextView datetv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        SMSAdapter.Holder holder=new SMSAdapter.Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.sms_list, null);
        holder.nametv=(TextView) rowView.findViewById(R.id.sms_name);
        holder.bodytv=(TextView) rowView.findViewById(R.id.sms_body);
        holder.datetv=(TextView) rowView.findViewById(R.id.sms_date);

        final SMS currentSMS=sms.get(position);
        String name = currentSMS.getName();
        String number = currentSMS.getNumber();
        StringBuffer body = new StringBuffer(currentSMS.getBody());
        Date date=currentSMS.getDate();
        String read_status=currentSMS.getReadState();

        if (currentSMS.getType().equals("sent")) {
            body.insert(0, "You: ");
        }

        if (currentSMS.getType().equals("draft")) {
            body.insert(0, "Draft: ");
        }

        if (name != null) {
            holder.nametv.setText(name);
        }
        else {
            holder.nametv.setText(number);
        }

        holder.bodytv.setText(body);
        holder.datetv.setText(getSpecifyDate(date));

        if(read_status.contains("0")){
            holder.bodytv.setTextColor(Color.BLACK);
            holder.datetv.setTextColor(Color.BLACK);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(currentSMS.getReadState().contains("0")) {
                    Uri uri = Uri.parse("content://sms");
                    String selection = "thread_id = " + currentSMS.getThread_id() + " OR address = " + currentSMS.getNumber();

                    ContentValues values = new ContentValues();
                    values.put("read", true);

                    context.getContentResolver().update(uri, values, selection, null);
                    MainActivity.refreshRequired=true;
                }

                MainActivity.choosen_sms = currentSMS.getThread_id();
                MainActivity.choosen_contact = currentSMS.getNumber();

                context.startActivity(new Intent(context,ChatActivity.class));
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

    public String getSpecifyDate(Date date){
        Date now = new Date();
        int diffInDays = (int)( (now.getTime() - date.getTime())
                / (1000 * 60 * 60 * 24) );
        String date_string=date.toString();
        String now_string=now.toString();
        if(diffInDays==0){
            if(now_string.substring(4,10).equals(date_string.substring(4,10))) {
                return connvertTime(date_string.substring(11, 16));
            }
            else{
                return "Yesterday";
            }
        }
        else if(diffInDays>0 && diffInDays<7){
            return date_string.substring(0,3);
        }
        else{
            return date_string.substring(4,10);
        }
    }

    public String connvertTime(String time){
        String converted_time;
        Integer hour=Integer.parseInt(time.substring(0,2));
        Integer identified_time;
        String amOrpm;
        if (hour>12){
            identified_time=hour-12;
            amOrpm="PM";
        }
        else if(hour==12){
            identified_time=hour;
            amOrpm="PM";
        }
        else{
            identified_time=hour;
            amOrpm="AM";
        }
        converted_time=identified_time.toString()+(time.substring(2))+" "+amOrpm;
        return converted_time;
    }

    @Override
    public void notifyDataSetChanged() // Create this function in your adapter class
    {
        super.notifyDataSetChanged();

    }

}