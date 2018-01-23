package com.example.naris.slacktalk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Date;

/**
 * Created by Lee on 9/21/2016.
 */
public class SMS {
    private String id;
    private String thread_id;
    private String number;
    private String name;
    private String type;
    private String body;
    private Date date;
    private String readState;

    public SMS(){
        id=null;
        thread_id=null;
        number=null;
        name=null;
        type=null;
        body=null;
        date=null;
        readState=null;
    }

    public SMS(String SMSid,String SMS_thread_id,String SMSnumber,String SMSname, String SMStype,String SMSbody,Date SMSdate, String Status){
        id=SMSid;
        thread_id=SMS_thread_id;
        number=SMSnumber;
        name=SMSname;
        type=SMStype;
        body=SMSbody;
        date=SMSdate;
        readState=Status;
    }

    public void setId(String SMSid){
        id=SMSid;
    }

    public void setThread_Id(String SMS_thread_id){
        thread_id=SMS_thread_id;
    }

    public void setNumber(String SMSnumber){
        number=SMSnumber;
    }

    public void setName(String SMSname){name=SMSname;}

    public void setType(String SMStype){
        type=SMStype;
    }

    public void setBody(String SMSbody){
        body=SMSbody;
    }

    public void setDate(Date SMSdate){
        date=SMSdate;
    }

    public void setReadState(String Status){
        readState=Status;
    }

    public String getId(){
        return id;
    }

    public String getThread_id(){return thread_id;}

    public String getNumber(){
        return number;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public String getBody(){
        return body;
    }

    public Date getDate(){
        return date;
    }

    public String getReadState(){
        return readState;
    }

}
