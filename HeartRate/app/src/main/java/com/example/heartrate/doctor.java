package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.MapEntry;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class doctor extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference patientdetails_reference;
    FirebaseUser currentuser;
    String userid;
    ArrayList<String> p_id_details;
    RecyclerView recyclerView;
    HashSet<Character> special_set;
    TextView nametag;
    static String doctor_arn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();
        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        userid=currentuser.getUid();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");
        patientdetails_reference=databaseReference.child(userid).child("patients");
        nametag=findViewById(R.id.name_tag);
        p_id_details=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerview);
        patientdetails();
        nameallocation();

    }

    public void doctor_popup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.doctor_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_patient:
                add_patient();
                return true;
            case R.id.details:
                Toast.makeText(this, "logout", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this,information.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.logout:
                mEdit.clear();
                mEdit.commit();
                FirebaseAuth.getInstance().signOut();
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                finish();
                return true;
            default:
                return false;
        }
    }

    private void add_patient() {
        AlertDialog.Builder builder = new AlertDialog.Builder(doctor.this);
        LayoutInflater inflater = doctor.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_patient, null);

        final EditText tl = view.findViewById(R.id.em);


        builder.setView(view)
                .setTitle("Patient Email")
                .setNegativeButton("cancel", null)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String user_email = tl.getText().toString();
                        String email_hash=String.valueOf(user_email.hashCode());

                            databaseReference.child("usermapping").child(email_hash).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String patient_id = (String) dataSnapshot.getValue();
                                    if (patient_id != null) {
                                        long id_hashcode = patient_id.hashCode();
                                        databaseReference.child(userid).child("patients").child(String.valueOf(id_hashcode)).setValue(patient_id);
                                        databaseReference.child(patient_id).child("personal_details").child("doctor_id").setValue(userid);
                                        databaseReference.child(patient_id).child("personal_details").child("doctor_arn").setValue(doctor_arn);
                                    } else {
                                        tl.setError("Not a registered user");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });



        AlertDialog alert = builder.create();
        alert.show();
    }
    public void patientdetails(){
        System.out.println("inside patientdetils method");
        patientdetails_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                p_id_details.clear();
                HashMap<String,String> h1=(HashMap)dataSnapshot.getValue();
                System.out.println("h1 : "+h1);
                if(h1!=null) {
                    Iterator hmiterator=h1.entrySet().iterator();
                    while(hmiterator.hasNext()) {
                        Map.Entry mapEntry=(Map.Entry)hmiterator.next();
                        p_id_details.add((String)mapEntry.getValue());

                    }
                    patient_Adapter adapter = new patient_Adapter(doctor.this, p_id_details);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(doctor.this));
                    recyclerView.setAdapter(adapter);

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("error while retrieving the data");
            }
        });
    }
    private void nameallocation(){
        databaseReference.child(userid).child("personal_details").child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    nametag.setText((String)dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}