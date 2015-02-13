package com.example.smsmanager;

/**
 * Created by NIBBLER on 13/02/15.
 */
public class TimeTableElement {
    private boolean enable;
    private int hour_start;
    private int minute_start;
    private int hour_end;
    private int minute_end;

    public static final String[] weekDays = new String[]{
            "monday",
            "tuesday",
            "wednesday",
            "thursday",
            "friday",
            "saturday",
            "sunday"
    };

    public int getHour_start(){
        return this.hour_start;
    }

    public int getHour_end(){
        return this.hour_end;
    }

    public int getMinute_start(){
        return this.minute_start;
    }

    public int getMinute_end(){
        return this.minute_end;
    }

    public boolean getEnable(){
        return this.enable;
    }

    public String getStartString(){
        String strToReturn = "";
        if (this.hour_start > 10) {
            strToReturn += Integer.toString(this.hour_start) + ":";
        } else {
            strToReturn += "0" + Integer.toString(this.hour_start) + ":";
        }
        if (minute_start > 10) {
            strToReturn += Integer.toString(this.minute_start);
        } else {
            strToReturn += "0" + Integer.toString(this.minute_start);
        }
        return strToReturn;
    }

    public String getEndString(){
        String strToReturn = "";
        if (this.hour_end > 10) {
            strToReturn += Integer.toString(this.hour_end) + ":";
        } else {
            strToReturn += "0" + Integer.toString(this.hour_end) + ":";
        }
        if (minute_end > 10) {
            strToReturn += Integer.toString(this.minute_end);
        } else {
            strToReturn += "0" + Integer.toString(this.minute_end);
        }
        return strToReturn;
    }

    public TimeTableElement(){
        enable = false;
        hour_start = 0;
        hour_end = 23;
        minute_start = 0;
        minute_end = 59;
    }

    public void setHour_end(int hour_end) {
        this.hour_end = hour_end;
        if (this.hour_end > 23) this.hour_end = 23;
        if (this.hour_end < 0) this.hour_end = 0;
    }

    public void setHour_start(int hour_start) {
        this.hour_start = hour_start;
        if (this.hour_start > 23) this.hour_start = 23;
        if (this.hour_start < 0) this.hour_start = 0;
    }

    public void setMinute_end(int minute_end) {
        this.minute_end = minute_end;
        if (this.minute_end > 59) this.minute_end = 59;
        if (this.minute_end < 0) this.minute_end = 0;
    }

    public void setMinute_start(int minute_start) {
        this.minute_start = minute_start;
        if (this.minute_start > 59) this.minute_start = 59;
        if (this.minute_start < 0) this.minute_start = 0;
    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }
}