package com.example.naris.slacktalk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 10/16/2016.
 */

public class SearchAdapter extends BaseAdapter {
    LinearLayout result_layout;
    View Titleview;
    boolean replacedCategoryTitle=false;
    boolean gainedView=false;
    boolean gainedPosition=false;
    int addCategoryTitlePosition;

    Context context;
    List<String> result_list=new ArrayList<String>();
    private static LayoutInflater inflater=null;


    static List<SMS> message=MainActivity.message;

    public SearchAdapter(MainActivity mainActivity, List<String> result) {
        // TODO Auto-generated constructor stub
        result_list=result;
        context=mainActivity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result_list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView category_tv;
        TextView result_tv;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final String current_result=result_list.get(position);
        SearchAdapter.Holder holder=new SearchAdapter.Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.result_list, null);
        result_layout = (LinearLayout) rowView.findViewById(R.id.result_layout);

        holder.category_tv=(TextView) rowView.findViewById(R.id.resultTitle);
        holder.category_tv.setText("MESSAGES");
        holder.result_tv=(TextView) rowView.findViewById(R.id.resultList);
        holder.result_tv.setText(current_result);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        holder.result_tv.setLayoutParams(params);

        if(gainedView==false) {
            Titleview = result_layout.getChildAt(0);
            gainedView=true;
        }
        if(position>=1 && countLines(current_result)>2){
                result_layout.removeViewAt(0);
        }
        if(countLines(current_result)<=2)
        {
            holder.category_tv.setText("CONTACTS");
            if(gainedPosition==false){
                addCategoryTitlePosition=position;
                gainedPosition=true;
            }
            if(position>addCategoryTitlePosition){
                result_layout.removeViewAt(0);
            }
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MainActivity.choosen_contact=null;
                MainActivity.choosen_sms=null;

                if(countLines(current_result)>2){
                    //do like sms to chat activity
                    String contact = lineNumber(current_result,0);

                    for (int i = 0; i < message.size(); i++) {
                        SMS compare_sms = message.get(i);
                        String compare_no = compare_sms.getNumber();
                        String compare_name = compare_sms.getName();
                        if (contact.equals(compare_name) || contact.equals(compare_no)) {
                            MainActivity.choosen_sms = compare_sms.getThread_id();
                            MainActivity.choosen_contact = compare_sms.getNumber();
                            break;
                        }
                    }
                    context.startActivity(new Intent(context,ChatActivity.class));
                }

                else{
                    //do like contact to chat activity
                    String contact = lineNumber(current_result,1);
                    MainActivity.choosen_contact = contact;


                    String compare_num;
                        Cursor c = context.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
                        int totalSMS = c.getCount();
                        if (c.moveToFirst()) {
                            for (int i = 0; i < totalSMS; i++) {
                                compare_num = c.getString(c.getColumnIndexOrThrow("address")).trim();
                                if (compare_num.replaceAll("[()\\s-]+", "").equals(contact.replaceAll("[()\\s-]+", ""))) {
                                    MainActivity.choosen_sms = c.getString(c.getColumnIndexOrThrow("thread_id"));
                                    break;
                                }
                                c.moveToNext();
                            }
                        }
                        c.close();

                    context.startActivity(new Intent(context,ChatActivity.class));
                }
            }
        });
        return rowView;
    }

    public static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    public static String lineNumber(String str,int position){
        String[] lines = str.split("\r\n|\r|\n");
        return lines[position];
    }

    @Override
    public void notifyDataSetChanged() // Create this function in your adapter class
    {
        super.notifyDataSetChanged();
    }
}
