package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.model.PolarDeviceInfo;

public class Patient_details extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    PolarBleApi api;
    String DEVICE_ID="";
    Disposable scanDisposable;

    private BarChart barChart1, barChart2;

    //database references
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myref;
    DatabaseReference hours_data_reference;
    DatabaseReference days_data_reference;
    DatabaseReference minutes_data_reference;

    //Shared preferences
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;

    String userid;
    static String Data_Key="Data";
    static String Day_Key="DAY";
    TextView todays_view;
    TextView days_view;
    public static String reference_key="REFERENCE";
    public static String hours_reference="HOURS";
    public static String days_reference="DAYS";
    Calendar calendar;
    ImageView todays_details;
    ImageView weeks_details;
    ArrayList<Long> todaysdata_topass;
    ArrayList<Long> weeksdata_topass;
    Long todays_average_topass=0L;
    Long weeks_average_topass=0L;
    static String average_key="AVERAGE";
    static String array_list_key="ARRAYLIST";
    static String bundle_key="BUNDLE";
    String TAG  = "hurray";
    static String id_key="device";
    FirebaseUser currentuser;
    long last_hour_average_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();
        userid = String.valueOf(mPref.getInt("id", 0));

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        userid=currentuser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myref = firebaseDatabase.getReference("users");

        minutes_data_reference=myref.child(userid).child("HeartRateData").child("Current_Day");
        hours_data_reference = myref.child(userid).child("HeartRateData").child("Daily");
        days_data_reference = myref.child(userid).child("HeartRateData").child("Days_data");
        todays_details = findViewById(R.id.todays_data);
        final Button scan_button = findViewById(R.id.scan_button);
        weeks_details = findViewById(R.id.days_data);

        todays_view = findViewById(R.id.todays_average);
        days_view = findViewById(R.id.days_average);
        todays_view.setText(" - - ");
        days_view.setText(" - - ");
        todaysdata_topass = new ArrayList <>();
        weeksdata_topass = new ArrayList <>();



        barChart1 = findViewById(R.id.today_barchart);
        barChart2 = findViewById(R.id.daily_barchart);


        barChart1.getDescription().setEnabled(false);
        System.out.println("calling set todays method");
        set_todaysdata();
        barChart1.setFitBars(true);

        barChart2.getDescription().setEnabled(false);
        set_daysdata();
        barChart1.setFitBars(true);
        todays_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("inside todays_details on click");
                Intent intent = new Intent(Patient_details.this, heart_rate_details.class);
                Bundle bundle = new Bundle();
                bundle.putLong(average_key, todays_average_topass);
                bundle.putSerializable(array_list_key, (Serializable) todaysdata_topass);
                intent.putExtra(bundle_key, bundle);
                startActivity(intent);
            }
        });
        weeks_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Patient_details.this, heart_rate_details.class);
                Bundle bundle = new Bundle();
                bundle.putLong(average_key, weeks_average_topass);
                bundle.putSerializable(array_list_key, (Serializable) weeksdata_topass);
                intent.putExtra(bundle_key, bundle);
                startActivity(intent);
            }

        });
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    System.out.println(snapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (scanDisposable == null) {
                    scanDisposable = api.searchForDevice().observeOn(AndroidSchedulers.mainThread()).subscribe(
                            new Consumer <PolarDeviceInfo>() {
                                @Override
                                public void accept(PolarDeviceInfo polarDeviceInfo) throws Exception {
                                    //if (polarDeviceInfo.deviceId.equals("51460B23")) {
                                    Toast.makeText(getApplicationContext(), "inside on click", Toast.LENGTH_LONG).show();
                                    if (!polarDeviceInfo.deviceId.equals("")) {
                                        DEVICE_ID = polarDeviceInfo.deviceId;
                                        //api.connectToDevice(DEVICE_ID);
                                        alertdialog();
                                        Log.d("hurray", "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable);
                                        Toast.makeText(getApplicationContext(), "Connected to : " + DEVICE_ID, Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Consumer <Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.d(TAG, "" + throwable.getLocalizedMessage());

                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d(TAG, "complete");
                                }
                            }
                    );
                } else {
                    scanDisposable.dispose();
                    scanDisposable = null;
                }*/
                Intent intent=new Intent(getApplicationContext(),scan.class);
                startActivity(intent);
            }
        });
    }




    private void set_todaysdata(){
        System.out.println("inside set todays data");
        final ArrayList <BarEntry> yVals = new ArrayList<>();
        hours_data_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                yVals.clear();
                todaysdata_topass.clear();
                HashMap<String, Long> h1 = (HashMap) dataSnapshot.getValue();
                System.out.println("h1 : "+h1);

                int k=0;
                if (h1 != null) {
                    k=h1.size()-3;
                    System.out.println("Id : "+userid);
                    System.out.println("size "+k);
                    for (int i = 1; i <=k; i++) {
                        Long data=h1.get(Data_Key + (i));
                        System.out.println("i "+i);
                        todaysdata_topass.add(data);
                        yVals.add(new BarEntry(i, (Long) data));
                    }
                    long sum=h1.get("sum");
                    long total_entries=h1.get("TotalEntries");
                    todays_average_topass=sum/total_entries;
                    todays_view.setText(todays_average_topass.toString());


                    BarDataSet set = new BarDataSet(yVals, "Data set");
                    set.setColors(Color.WHITE);
                    set.setDrawValues(true);
                    BarData data = new BarData(set);

                    data.setBarWidth(0.5f);


                    barChart1.setData(data);
                    barChart1.invalidate();
                    barChart1.animateY(500);


                    barChart1.setTouchEnabled(true);
                    barChart1.setDragEnabled(true);
                    barChart1.setScaleEnabled(true);

                    barChart1.getAxisLeft().setDrawGridLines(false);
                    barChart1.getAxisLeft().setDrawAxisLine(false);
                    barChart1.getAxisRight().setDrawAxisLine(false);
                    barChart1.getAxisRight().setDrawGridLines(false);

                    barChart1.getAxisRight().setEnabled(false);
                    barChart1.getAxisLeft().setEnabled(true);
                    barChart1.getAxisLeft().setGridColor(Color.WHITE);
                    barChart1.getAxisLeft().setTextColor(Color.WHITE);
                    barChart1.getDescription().setEnabled(true);

                    barChart1.getDescription().setText("Month Data");
                    barChart1.getDescription().setTextColor(Color.WHITE);

                    barChart1.setDrawValueAboveBar(true);

                    barChart1.getXAxis().setDrawAxisLine(false);
                    barChart1.getXAxis().setTextColor(Color.WHITE);

                    barChart1.getAxisLeft().setDrawGridLines(false);

                    //barChart.getAxisRight().setDrawGridLines(true);
                    barChart1.getAxisLeft().setDrawGridLines(true);
                    barChart1.getXAxis().setDrawGridLines(false);

                    barChart1.animateY(500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void set_daysdata(){

        final ArrayList <BarEntry> yVals = new ArrayList <>();
        final ArrayList<Long> days_data=new ArrayList<>();
        days_data_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Long> h1 = (HashMap) dataSnapshot.getValue();


                if (h1 != null) {
                    yVals.clear();
                    weeksdata_topass.clear();
                    for (int i = 0; i < h1.size()-3; i++) {
                        Long data=h1.get(Day_Key + (i + 1));
                        weeksdata_topass.add(data);
                        yVals.add(new BarEntry(i, (Long)data ));

                    }
                    long sum=h1.get("sum");
                    long total_entries=h1.get("TotalEntries");
                    System.out.println("sum : "+sum);
                    System.out.println("TOTAL : "+total_entries);
                    weeks_average_topass=sum/total_entries;
                    days_view.setText(weeks_average_topass.toString());

                    BarDataSet set = new BarDataSet(yVals, "Data set");
                    set.setColors(Color.WHITE);
                    set.setDrawValues(true);
                    BarData data = new BarData(set);

                    data.setBarWidth(0.5f);


                    barChart2.setData(data);
                    barChart2.invalidate();
                    barChart2.animateY(500);


                    barChart2.setTouchEnabled(true);
                    barChart2.setDragEnabled(true);
                    barChart2.setScaleEnabled(true);

                    barChart2.getAxisLeft().setDrawGridLines(false);
                    barChart2.getAxisLeft().setDrawAxisLine(false);
                    barChart2.getAxisRight().setDrawAxisLine(false);
                    barChart2.getAxisRight().setDrawGridLines(false);

                    barChart2.getAxisRight().setEnabled(false);
                    barChart2.getAxisLeft().setEnabled(true);
                    barChart2.getAxisLeft().setGridColor(Color.WHITE);
                    barChart2.getAxisLeft().setTextColor(Color.WHITE);
                    barChart2.getDescription().setEnabled(true);

                    barChart2.getDescription().setText("Month Data");
                    barChart2.getDescription().setTextColor(Color.WHITE);

                    barChart2.setDrawValueAboveBar(true);

                    barChart2.getXAxis().setDrawAxisLine(false);
                    barChart2.getXAxis().setTextColor(Color.WHITE);

                    barChart2.getAxisLeft().setDrawGridLines(false);

                    //barChart.getAxisRight().setDrawGridLines(true);
                    barChart2.getAxisLeft().setDrawGridLines(true);
                    barChart2.getXAxis().setDrawGridLines(false);

                    barChart2.animateY(500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void showpopup(View v){
        PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.details:
                Intent intent1=new Intent(this,information.class);
                startActivity(intent1);
                return true;
            case R.id.logout:
                Toast.makeText(this,"logout", Toast.LENGTH_LONG).show();
                mEdit.clear();
                mEdit.commit();
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }
    private void alertdialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Patient_details.this);
        builderSingle.setTitle("Select One Device:-");

        final ArrayAdapter <String> arrayAdapter = new ArrayAdapter<String>(Patient_details.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(DEVICE_ID);


        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Patient_details.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        try {
                            Intent intent=new Intent(getApplicationContext(),scan.class);
                            intent.putExtra(id_key,DEVICE_ID);
                            startActivity(intent);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }
    public void onResume() {

        super.onResume();
        //this.recreate();
    }
    public void onDestroy() {

        super.onDestroy();
        api.shutDown();
        Toast.makeText(this,"inside on destroy ",Toast.LENGTH_LONG).show();
        System.out.println("ondestroy");
    }

}



