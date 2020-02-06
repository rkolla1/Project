package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;

public class scan extends AppCompatActivity {

    PolarBleApi api;
    String DEVICE_ID="";

    private ProgressBar progressBar;
    private int pStatus = 65;
    private  Handler handler=new Handler();
    private TextView txtProgress;
    private  ScheduledExecutorService scheduledExecutorService;
    FirebaseAuth mAuth;
    FirebaseUser currentuser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myref;
    int minutes_delay;
    int seconds_delay;
    String time;
    int hours_delay;
    static String Data_Key="Data";
    static String Day_Key="DAY";
    //Handler handler;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;

    private Random random = new Random();



    private LineChart[] mchart = new LineChart[1];

    ArrayList<Entry> yVals = new ArrayList <>();
    int i =0;

    String userid;
    Calendar calendar;
    DatabaseReference minute_data_date_reference;
    DatabaseReference minute_sum_data_reference;
    DatabaseReference minute_data_reference;
    DatabaseReference minute_total_entries_data_reference;
    DatabaseReference hour_data_reference;
    DatabaseReference hour_date_data_reference;
    DatabaseReference hour_sum_data_reference;
    DatabaseReference hour_total_entries_data_reference;
    DatabaseReference day_data_reference;
    //Data storage variable for last hour
    long last_hour_average_data;
    static long default_value=0L;
    //Data storage variable for previous_day
    long previous_day_average;
    Runnable runnable;
    Runnable runnable1;
    Runnable runnable2;
    Button start;
    Button stop;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        userid=currentuser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myref = firebaseDatabase.getReference("users").child(userid);
        final PulsatorLayout pulsator = findViewById(R.id.pulsator);

        pulsator.start();
        pulsator.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtProgress = (TextView) findViewById(R.id.pro);
        stop=findViewById(R.id.stop);
        start=findViewById(R.id.start);
        minutes_delay=2;//get_minutes_delay();
        seconds_delay=60;

        mPref= PreferenceManager.getDefaultSharedPreferences(this);
        mEdit=mPref.edit();

        //Refernces to store data for the particular hour
        minute_data_reference =myref.child("HeartRateData").child("Current_Day");
        minute_data_date_reference = minute_data_reference.child("Date");
        minute_sum_data_reference = minute_data_reference.child("Sum");
        minute_total_entries_data_reference = minute_data_reference.child("TotalEntries");

        //References to store hourly data for particular Day
        hour_data_reference =myref.child("HeartRateData").child("Daily");
        hour_sum_data_reference = hour_data_reference.child("Sum");
        hour_date_data_reference = hour_data_reference.child("Date");
        hour_total_entries_data_reference = hour_data_reference.child("TotalEntries");

        //Referencees to store days data
        day_data_reference=myref.child("HeartRateData").child("Days_data");

        //Polar api variable inititalization
        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);

        //Ble characteristics
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered) {
                super.blePowerStateChanged(powered);
                if(!powered){
                    Toast.makeText(getApplicationContext(),"Bluetooth Disconnected",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
                super.deviceConnected(polarDeviceInfo);
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
                super.deviceConnecting(polarDeviceInfo);
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
                super.deviceDisconnected(polarDeviceInfo);
            }



            @Override
            public void hrFeatureReady(String identifier) {
                super.hrFeatureReady(identifier);
            }

            @Override
            public void disInformationReceived(String identifier, UUID uuid, String value) {
                super.disInformationReceived(identifier, uuid, value);
            }

            @Override
            public void batteryLevelReceived(String identifier, int level) {
                super.batteryLevelReceived(identifier, level);

            }

            @Override
            public void hrNotificationReceived(String identifier, PolarHrData data) {
                super.hrNotificationReceived(identifier, data);
                /*pStatus=data.hr;
                progressBar.setProgress(pStatus);
                txtProgress.setText(String.valueOf(data.hr)+" BPM");*/

            }

            @Override
            public void polarFtpFeatureReady(String identifier) {
                super.polarFtpFeatureReady(identifier);
            }
        });

        //recieving the intent
        /*Intent intent=getIntent();
        if(intent!=null){
            DEVICE_ID=intent.getStringExtra(Patient_details.id_key);
            try {
                api.connectToDevice(DEVICE_ID);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }*/
        scheduledExecutorService= Executors.newScheduledThreadPool(5);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (pStatus <= 100) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(pStatus);
                                    txtProgress.setText(pStatus + " BPM");

                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            pStatus = (random.nextInt(77-66 +1)) + 66;
                        }
                    }
                }).start();
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        seconds_operation();
                    }
                },0, seconds_delay, TimeUnit.SECONDS);
                runnable=new Runnable() {
                    @Override
                    public void run() {
                        hours_operation();
                        minutes_delay=2*1000*60+1000*10;
                        handler.postDelayed(this,2*1000*60+1000*10);
                    }
                };
                handler.postDelayed(runnable,2*1000*60+1000*10);
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
            }


        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                days_operation();
                progressBar.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.INVISIBLE);
                start.setVisibility(View.VISIBLE);
                scheduledExecutorService.shutdown();
                handler.removeCallbacks(runnable);
            }
        });








        //Thread to perform operations every minute


    }



    private int  get_minutes_delay()
    {   calendar=Calendar.getInstance();
        return 60-calendar.get(Calendar.MINUTE);
    }
    private int get_hours_delay(){
        calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        return 24-hour;
    }
    private void seconds_operation(){
        calendar=Calendar.getInstance();
        time=calendar.getTime().toString();
        String[] date_values= LocalDate.now().toString().split("-");
        final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        final String month=date_values[1];
        final String day=(date_values[2]);
        final String year=(date_values[0]);
        minute_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("snapshotvalue : "+dataSnapshot.getValue());
                HashMap<String,Long> h1 = (HashMap<String,Long>)dataSnapshot.getValue();
                //System.out.println(h1.toString());
                if(h1!=null) {
                    System.out.println("inside h1 not null");
                    long sum = h1.get("Sum");
                    System.out.println("Sum : "+sum);
                    Long total_entries=h1.get("TotalEntries");
                    System.out.println("total_entries : "+total_entries);
                    sum += pStatus;
                    minute_total_entries_data_reference.setValue(total_entries+1);
                    minute_sum_data_reference.setValue(sum);
                }
                else{
                    minute_total_entries_data_reference.setValue(1);
                    minute_sum_data_reference.setValue(pStatus);
                }
                minute_data_reference.child("Date").setValue(time);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void hours_operation(){
        System.out.println("inside hours operation");
        calendar=Calendar.getInstance();
        String[] date_values= LocalDate.now().toString().split("-");
        final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        final String month=date_values[1];
        final String day=(date_values[2]);
        final String year=(date_values[0]);
        final String time=calendar.getTime().toString().substring(11,19);
        minute_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Long> h1=(HashMap<String,Long>)dataSnapshot.getValue();
                    last_hour_average_data = h1.get("Sum") / h1.get("TotalEntries");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        hour_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                minute_data_reference.removeValue();
                ArrayList<Long> arrayList=new ArrayList<Long>();
                HashMap<String,Long> h1= (HashMap<String, Long>) dataSnapshot.getValue();
                Long sum=0L;
                if(last_hour_average_data>0L) {
                    if (h1 == null) {
                        hour_data_reference.child(Data_Key + 1).setValue(last_hour_average_data);
                        hour_data_reference.child("sum").setValue(last_hour_average_data);
                        hour_data_reference.child("TotalEntries").setValue(1);
                    } else {
                        int pos = h1.size() - 3;
                        if (h1.size() < 10) {
                            hour_data_reference.child(Data_Key + (pos + 1)).setValue(last_hour_average_data);
                        } else {
                            Toast.makeText(scan.this, h1.toString(), Toast.LENGTH_LONG).show();
                            Iterator hmIterator = h1.entrySet().iterator();
                            for (int i = 1; i <= 7; i++) {
                                arrayList.add(h1.get(Data_Key + i));
                            }
                            for (int i = 1; i < arrayList.size(); i++) {

                                hour_data_reference.child(Data_Key + (i)).setValue(arrayList.get(i));

                            }
                            hour_data_reference.child(Data_Key + 7).setValue(last_hour_average_data);

                        }
                        hour_data_reference.child("sum").setValue(h1.get("sum") + last_hour_average_data);
                        hour_data_reference.child("TotalEntries").setValue(h1.get("TotalEntries") + 1);
                    }

                    hour_data_reference.child("Date").setValue(time);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void days_operation(){
        calendar=Calendar.getInstance();
        String[] date_values= LocalDate.now().toString().split("-");
        final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        final String month=date_values[1];
        final String day=(date_values[2]);
        final String year=(date_values[0]);
        final String time=calendar.getTime().toString();
        hour_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Long> h1=(HashMap<String,Long>)dataSnapshot.getValue();
                if(h1!=null) {
                    long sum = h1.get("sum");
                    long total_entries = h1.get("TotalEntries");
                    previous_day_average=sum/total_entries;
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        day_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hour_data_reference.removeValue();
                HashMap<String, Long> h1 = (HashMap) dataSnapshot.getValue();
                if (previous_day_average > 0L) {
                    if (h1 == null) {
                        day_data_reference.child(Day_Key + 1).setValue(previous_day_average);
                        day_data_reference.child("sum").setValue(previous_day_average);
                        day_data_reference.child("TotalEntries").setValue(1);

                    } else {
                        int pos = h1.size() - 3;
                        if (h1.size() < 10) {
                            day_data_reference.child(Day_Key + (pos + 1)).setValue(previous_day_average);
                            day_data_reference.child("sum").setValue(h1.get("sum") + previous_day_average);
                            day_data_reference.child("TotalEntries").setValue(h1.get("TotalEntries") + 1);

                        } else {
                            Iterator hmiterator = h1.entrySet().iterator();
                            ArrayList<Long> a1 = new ArrayList<>();
                            for (int i = 1; i < pos; i++) {
                                a1.add(h1.get(Day_Key + (i + 1)));
                            }

                            for (int i = 1; i < a1.size(); i++) {
                                day_data_reference.child(Day_Key + i).setValue(a1.get(i));
                            }
                            day_data_reference.child(Day_Key + 7).setValue(previous_day_average);

                            day_data_reference.child("sum").setValue(h1.get("sum") + previous_day_average-h1.get(Day_Key+1));

                        }



                    }
                    day_data_reference.child("Date").setValue(time);
                /*day_data_reference.child("year").setValue(year);
                day_data_reference.child("hour").setValue(hour);
                day_data_reference.child("day").setValue(day);
                day_data_reference.child("month").setValue(month);*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /*@Override
    protected void onPause() {
        super.onPause();
        scheduledExecutorService.shutdown();
        handler.removeCallbacks(runnable);
    }*/
    public void onDestroy() {

        super.onDestroy();
        Toast.makeText(this,"inside on destroy ",Toast.LENGTH_LONG).show();
        System.out.println("ondestroy");
        minute_data_reference.removeValue();
        api.shutDown();
        days_operation();
    }

}