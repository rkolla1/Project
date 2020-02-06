package com.example.heartrate;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Patient implements Serializable {
    String patient_id;
    long todays_average_heartbeat;
    long weeks_average_heartbeat;
    String name;
    ArrayList<Long> current_day;
    ArrayList<Long> current_week;
    public Patient(){
        current_day=new ArrayList<>();
        current_week=new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return "name : "+name+" today_average : "+todays_average_heartbeat+" weeks_average : "+weeks_average_heartbeat+
                " current_day : "+current_day+" current_week : "+current_week;
    }
}
