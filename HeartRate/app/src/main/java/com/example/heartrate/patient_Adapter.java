package com.example.heartrate;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.MapEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class patient_Adapter extends RecyclerView.Adapter<patient_Adapter.ViewHolder> {

    @NonNull

    private Context context;
    private ArrayList<String> list_of_groups;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    static String Data_Key="Data";
    static String Day_Key="DAY";
    Patient patient;
    ArrayList<Patient> patients;

    public patient_Adapter(Context context,ArrayList<String> list_of_groups) {
        super();
        this.context=context;
        this.list_of_groups=list_of_groups;

    }

    public patient_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctor_patient_info, parent, false);
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");
        patients=new ArrayList<>();
        //last_seven_hours=new ArrayList<>();
        //last_seven_days=new ArrayList<>();


        return new patient_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final patient_Adapter.ViewHolder holder, final int position) {
        //holder.textView.setText(String.valueOf(list_of_groups.get(position)));
        //holder.name.setText(String.valueOf(list_of_groups.get(position)));
        String userid=list_of_groups.get(position);
        final Patient p1=new Patient();
        databaseReference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, HashMap> h1 = (HashMap) dataSnapshot.getValue();
                System.out.println("h1 : " + h1);
                if (h1 != null) {
                    Iterator hmiterator = h1.entrySet().iterator();
                    p1.name = (String) h1.get("personal_details").get("username");
                    holder.name.setText(p1.name);
                    HashMap<String, HashMap> heartratedata = (HashMap) h1.get("HeartRateData");
                    if (heartratedata != null) {
                        HashMap<String, Long> daily_data = (HashMap)heartratedata.get("Daily");
                        HashMap<String, Long> days = (HashMap) h1.get("HeartRateData").get("Days_data");
                        if (daily_data != null) {
                            long sum = daily_data.get("sum");
                            long totalentries = daily_data.get("TotalEntries");
                            p1.todays_average_heartbeat = sum / totalentries;
                            for (int i = 1; i <= daily_data.size() - 3; i++) {
                                p1.current_day.add(daily_data.get(Data_Key + i));
                            }
                        }
                        if (days != null) {
                            long sum = days.get("sum");
                            long totalentries = days.get("TotalEntries");
                            for (int i = 1; i <= days.size() - 3; i++) {
                                p1.current_week.add(days.get(Day_Key + i));
                            }
                            p1.weeks_average_heartbeat = sum / totalentries;
                        }

                        if (p1.todays_average_heartbeat != 0L) {
                            holder.textView.setText(String.valueOf(p1.todays_average_heartbeat));
                        }
                        else if(p1.weeks_average_heartbeat!=0L){
                            holder.textView.setText(String.valueOf(p1.weeks_average_heartbeat));
                        }

                    }
                    patients.add(p1);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,doctor_patient_details.class);
                Bundle bundle =new Bundle();
                intent.putExtra("patient",(Serializable)patients.get(position));
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return list_of_groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView name;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            textView = itemView.findViewById(R.id.average_rate);
            textView.setText(" - - ");

        }
    }

}