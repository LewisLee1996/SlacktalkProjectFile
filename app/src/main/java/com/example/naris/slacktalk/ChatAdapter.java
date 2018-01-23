package com.example.naris.slacktalk;

/**Created by Lee on 9/23/2016.
 */
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.print.PrintAttributes;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends BaseAdapter{
    Context context;
    List<SMS> chat=new ArrayList<SMS>();
    private static LayoutInflater inflater=null;

    public ChatAdapter(ChatActivity chatActivity, List<SMS> messages) {
        // TODO Auto-generated constructor stub
        chat=messages;
        context=chatActivity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return chat.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return chat.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        TextView datetv;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final SMS currentSMS=chat.get(position);
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.chat_list, null);
        holder.tv=(TextView) rowView.findViewById(R.id.chat_message);
        holder.tv.setText(currentSMS.getBody());
        holder.tv.setGravity(Gravity.CENTER);
        holder.tv.setTextColor(Color.BLACK);
        holder.datetv=(TextView) rowView.findViewById(R.id.chat_date);
        holder.datetv.setText(getSpecifyDate(currentSMS.getDate()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(currentSMS.getType().equals("inbox")) {
            params.gravity=Gravity.LEFT;
            holder.tv.setBackgroundResource(R.drawable.bubble_other);
            holder.tv.setLayoutParams(params);
            holder.datetv.setLayoutParams(params);
            holder.datetv.setPadding(40,0,0,0);
        }
        if(currentSMS.getType().equals("sent")){
            params.gravity=Gravity.RIGHT;
            holder.tv.setBackgroundResource(R.drawable.bubble_me);
            holder.tv.setLayoutParams(params);
            holder.datetv.setLayoutParams(params);
            holder.datetv.setPadding(0,0,40,0);
        }

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
                return convertTime(date_string.substring(11, 16));
            }
            else{
                return date_string.substring(0,3) + " " + convertTime(date_string.substring(11, 16));
            }
        }
        else if(diffInDays>0 && diffInDays<7){
            return date_string.substring(0,3) + " " + convertTime(date_string.substring(11, 16));
        }
        else{
            return date_string.substring(4,10)+ ", " + convertTime(date_string.substring(11, 16));
        }
    }

    public String convertTime(String time){
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