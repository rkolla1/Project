package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class heart_rate_details extends AppCompatActivity {

    private BarChart barChart;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myref;
    DatabaseReference ref;
    String userid;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    static String Data_Key="Data";
    static String Day_Key="DAY";
    TextView average;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_details);
        barChart = findViewById(R.id.barchart);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();
        userid = String.valueOf(mPref.getInt("id", 0));
        average = findViewById(R.id.average);

        average.setText("- -");

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        myref = firebaseDatabase.getReference("Users").child("patients").child(userid);


        //Getting the data through intents
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(Patient_details.bundle_key);
        Long rate_average = bundle.getLong(Patient_details.average_key);
        ArrayList<Long> rate_data = (ArrayList) bundle.getSerializable(Patient_details.array_list_key);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        //setting the data in recycler view.
        if (rate_data.size() > 0) {

            data_Adapter adapter = new data_Adapter(rate_data, getApplicationContext());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
        }

        //adding the data for Barchart.
        final ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < rate_data.size(); i++) {

            yVals.add(new BarEntry(i, rate_data.get(i)));
        }
        if (yVals.size() > 0) {
            average.setText(rate_average.toString());
            BarDataSet set = new BarDataSet(yVals, "Data set");
            set.setColors(Color.WHITE);
            set.setDrawValues(true);
            BarData data = new BarData(set);

            data.setBarWidth(0.5f);


            barChart.setData(data);
            barChart.invalidate();
            barChart.animateY(500);


            barChart.setTouchEnabled(true);
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);

            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisLeft().setDrawAxisLine(false);
            barChart.getAxisRight().setDrawAxisLine(false);
            barChart.getAxisRight().setDrawGridLines(false);

            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setEnabled(true);
            barChart.getAxisLeft().setGridColor(Color.WHITE);
            barChart.getAxisLeft().setTextColor(Color.WHITE);
            barChart.getDescription().setEnabled(true);

            barChart.getDescription().setText("Month Data");
            barChart.getDescription().setTextColor(Color.WHITE);

            barChart.setDrawValueAboveBar(true);

            barChart.getXAxis().setDrawAxisLine(false);
            barChart.getXAxis().setTextColor(Color.WHITE);

            barChart.getAxisLeft().setDrawGridLines(false);

            //barChart.getAxisRight().setDrawGridLines(true);
            barChart.getAxisLeft().setDrawGridLines(true);
            barChart.getXAxis().setDrawGridLines(false);

            barChart.animateY(500);


        }
    }

    /*private void settodaysdata(){
        System.out.println("inside todays data");
        final ArrayList<BarEntry> yVals = new ArrayList<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Long> h1=(HashMap)dataSnapshot.getValue();
                if(h1!=null){
                    Long sum=0L;
                    Long total_entries=0L;
                    int k=0;
                        if(h1.size()>7){
                            k=7;
                        }
                        else{
                            k=h1.size();
                        }
                        System.out.println("Id : "+userid);
                        System.out.println("size "+k);
                        for (int i = 0; i < k; i++) {
                            Long data=h1.get(Data_Key + (i + 1));
                            System.out.println("i "+i);
                            yVals.add(new BarEntry(i, (Long) data));
                            sum+=data;
                            total_entries+=1;
                        }
                        sum+=h1.get("sum");
                        total_entries+=h1.get("TotalEntries");
                        Long total_average=sum/total_entries;
                        System.out.println("average : "+average);
                        average.setText(total_average.toString());

                        BarDataSet set = new BarDataSet(yVals, "Data set");
                        set.setColors(Color.WHITE);
                        set.setDrawValues(true);
                        BarData data = new BarData(set);

                        data.setBarWidth(0.5f);


                        barChart.setData(data);
                        barChart.invalidate();
                        barChart.animateY(500);


                        barChart.setTouchEnabled(true);
                        barChart.setDragEnabled(true);
                        barChart.setScaleEnabled(true);

                        barChart.getAxisLeft().setDrawGridLines(false);
                        barChart.getAxisLeft().setDrawAxisLine(false);
                        barChart.getAxisRight().setDrawAxisLine(false);
                        barChart.getAxisRight().setDrawGridLines(false);

                        barChart.getAxisRight().setEnabled(false);
                        barChart.getAxisLeft().setEnabled(true);
                        barChart.getAxisLeft().setGridColor(Color.WHITE);
                        barChart.getAxisLeft().setTextColor(Color.WHITE);
                        barChart.getDescription().setEnabled(true);

                        barChart.getDescription().setText("Month Data");
                        barChart.getDescription().setTextColor(Color.WHITE);

                        barChart.setDrawValueAboveBar(true);

                        barChart.getXAxis().setDrawAxisLine(false);
                        barChart.getXAxis().setTextColor(Color.WHITE);

                        barChart.getAxisLeft().setDrawGridLines(false);

                        //barChart.getAxisRight().setDrawGridLines(true);
                        barChart.getAxisLeft().setDrawGridLines(true);
                        barChart.getXAxis().setDrawGridLines(false);

                        barChart.animateY(500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void setdaysdata(){

        final ArrayList<BarEntry> yVals = new ArrayList<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Long> h1=(HashMap)dataSnapshot.getValue();
                if(h1!=null){
                    Long sum=0L;
                    Long total_entries=0L;
                    int k=0;
                    if(h1.size()>7){
                        k=7;
                    }
                    else{
                        k=h1.size();
                    }
                    System.out.println("Id : "+userid);
                    System.out.println("size "+k);
                    for (int i = 0; i < k; i++) {
                        Long data=h1.get(Data_Key + (i + 1));
                        System.out.println("i "+i);
                        yVals.add(new BarEntry(i, (Long) data));
                        sum+=data;
                        total_entries+=1;
                    }

                    Long total_average=sum/total_entries;
                    average.setText(total_average.toString());

                    BarDataSet set = new BarDataSet(yVals, "Data set");
                    set.setColors(Color.WHITE);
                    set.setDrawValues(true);
                    BarData data = new BarData(set);

                    data.setBarWidth(0.5f);


                    barChart.setData(data);
                    barChart.invalidate();
                    barChart.animateY(500);


                    barChart.setTouchEnabled(true);
                    barChart.setDragEnabled(true);
                    barChart.setScaleEnabled(true);

                    barChart.getAxisLeft().setDrawGridLines(false);
                    barChart.getAxisLeft().setDrawAxisLine(false);
                    barChart.getAxisRight().setDrawAxisLine(false);
                    barChart.getAxisRight().setDrawGridLines(false);

                    barChart.getAxisRight().setEnabled(false);
                    barChart.getAxisLeft().setEnabled(true);
                    barChart.getAxisLeft().setGridColor(Color.WHITE);
                    barChart.getAxisLeft().setTextColor(Color.WHITE);
                    barChart.getDescription().setEnabled(true);

                    barChart.getDescription().setText("Month Data");
                    barChart.getDescription().setTextColor(Color.WHITE);

                    barChart.setDrawValueAboveBar(true);

                    barChart.getXAxis().setDrawAxisLine(false);
                    barChart.getXAxis().setTextColor(Color.WHITE);

                    barChart.getAxisLeft().setDrawGridLines(false);

                    //barChart.getAxisRight().setDrawGridLines(true);
                    barChart.getAxisLeft().setDrawGridLines(true);
                    barChart.getXAxis().setDrawGridLines(false);

                    barChart.animateY(500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }*/

}
