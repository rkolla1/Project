package com.example.heartrate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.reactivex.disposables.Disposable;
import polar.com.sdk.api.PolarBleApi;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link patient_details_fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link patient_details_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class patient_details_fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
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

    private OnFragmentInteractionListener mListener;

    public patient_details_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment patient_details_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static patient_details_fragment newInstance(String param1, String param2) {
        patient_details_fragment fragment = new patient_details_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setHasOptionsMenu(true);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_patient_details_fragment, container, false);
        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        userid=currentuser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myref = firebaseDatabase.getReference("users");
        minutes_data_reference=myref.child(userid).child("HeartRateData").child("Current_Day");
        hours_data_reference = myref.child(userid).child("HeartRateData").child("Daily");
        days_data_reference = myref.child(userid).child("HeartRateData").child("Days_data");
        todays_details = view.findViewById(R.id.todays_data);
        final Button scan_button = view.findViewById(R.id.scan_button);
        weeks_details = view.findViewById(R.id.days_data);

        todays_view = view.findViewById(R.id.todays_average);
        days_view = view.findViewById(R.id.days_average);
        todays_view.setText(" - - ");
        days_view.setText(" - - ");
        todaysdata_topass = new ArrayList <>();
        weeksdata_topass = new ArrayList <>();

        barChart1 = view.findViewById(R.id.today_barchart);
        barChart2 = view.findViewById(R.id.daily_barchart);


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
                Intent intent = new Intent(getActivity(), heart_rate_details.class);
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
                Intent intent = new Intent(getActivity(), heart_rate_details.class);
                Bundle bundle = new Bundle();
                bundle.putLong(average_key, weeks_average_topass);
                bundle.putSerializable(array_list_key, (Serializable) weeksdata_topass);
                intent.putExtra(bundle_key, bundle);
                startActivity(intent);
            }

        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private void set_todaysdata(){
        System.out.println("inside set todays data");
        final ArrayList <BarEntry> yVals = new ArrayList<>();
        hours_data_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Status : ", "Inside on status change method");
                yVals.clear();
                todaysdata_topass.clear();
                HashMap<String, Long> h1 = (HashMap) dataSnapshot.getValue();

                System.out.println("h1 : " + h1);

                int k = 0;
                if (h1 != null) {
                    if (h1.containsKey("sum") && h1.containsKey("TotalEntries")) {
                        k = h1.size() - 3;
                        System.out.println("Id : " + userid);
                        System.out.println("size " + k);
                        for (int i = 1; i <= k; i++) {
                            Long data = h1.get(Data_Key + (i));
                            System.out.println("i " + i);
                            todaysdata_topass.add(data);
                            yVals.add(new BarEntry(i, (Long) data));
                        }
                        long sum = h1.get("sum");
                        long total_entries = h1.get("TotalEntries");
                        todays_average_topass = sum / total_entries;
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
                    if (h1.containsKey("sum") && h1.containsKey("TotalEntries")) {
                        yVals.clear();
                        weeksdata_topass.clear();
                        for (int i = 0; i < h1.size() - 3; i++) {
                            Long data = h1.get(Day_Key + (i + 1));
                            weeksdata_topass.add(data);
                            yVals.add(new BarEntry(i, (Long) data));

                        }
                        long sum = h1.get("sum");
                        long total_entries = h1.get("TotalEntries");
                        System.out.println("sum : " + sum);
                        System.out.println("TOTAL : " + total_entries);
                        weeks_average_topass = sum / total_entries;
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    /*public void showpopup(View v){
        PopupMenu popupMenu = new PopupMenu(getActivity(),v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.details:
                Intent intent1=new Intent(getActivity(),information.class);
                startActivity(intent1);
                return true;
            case R.id.logout:
                Toast.makeText(getActivity(),"logout", Toast.LENGTH_LONG).show();
                mEdit.clear();
                mEdit.commit();
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                //finish();
                return true;
            default:
                return false;
        }
    }*/

}
