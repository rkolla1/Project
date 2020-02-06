package com.example.heartrate;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    PolarBleApi api;
    String DEVICE_ID="";

    private ProgressBar progressBar;
    private int pStatus = 77;
    private Handler handler=new Handler();
    private TextView txtProgress;
    private ScheduledExecutorService scheduledExecutorService;
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
    DatabaseReference databaseReference;
    String arn="";
    HashMap<String,String> data;
    String doctor_id;
    DatabaseReference doctor_dabasereference;
    Button Scan_button;

    public ScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanFragment newInstance(String param1, String param2) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_scan, container, false);
        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        userid=currentuser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myref = firebaseDatabase.getReference("users").child(userid);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtProgress = (TextView) view.findViewById(R.id.pro);
        stop=view.findViewById(R.id.stop);
        start=view.findViewById(R.id.start);
        Scan_button=view.findViewById(R.id.scan_button);
        minutes_delay=2;//get_minutes_delay();
        seconds_delay=30;
        mPref= PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEdit=mPref.edit();


        //Refernces to store data for the particular hour
        doctor_dabasereference=firebaseDatabase.getReference().child("users");
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
        Scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Main2Activity)getActivity()).scanning();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((Main2Activity) getActivity()).isconnected()) {
                    scheduledExecutorService = Executors.newScheduledThreadPool(5);
                    progressBar.setVisibility(View.VISIBLE);


                /*new Thread(new Runnable() {
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
                }).start();*/
                    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("inside run method");
                            seconds_operation();
                            if (pStatus < 75) {
                                new notify().execute();
                            }

                        }
                    }, 0, seconds_delay, TimeUnit.SECONDS);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Calculating_minute_average();
                            minutes_delay = 2 * 1000 * 60 + 1000 * 10;
                            handler.postDelayed(this, 1* 1000 * 60 + 1000 * 10);
                        }
                    };
                    handler.postDelayed(runnable, 1 * 1000 * 60 + 1000 * 10);
                    start.setVisibility(View.INVISIBLE);
                    stop.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(getActivity(),"device is not connected",Toast.LENGTH_LONG).show();
                }


            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((Main2Activity)getActivity()).isconnected()) {
                    days_operation();
                    ((Main2Activity) getActivity()).stop();
                    minute_data_reference.removeValue();
                    hour_data_reference.removeValue();
                    progressBar.setVisibility(View.INVISIBLE);
                    stop.setVisibility(View.INVISIBLE);
                    start.setVisibility(View.VISIBLE);
                    Stop();
                }
                else{
                    Toast.makeText(getActivity(),"Sorry the device is not connected",Toast.LENGTH_LONG).show();
                }

            }
        });
        myref.child("emergency").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = (HashMap) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        myref.child("personal_details").child("doctor_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                doctor_id=(String)dataSnapshot.getValue();
                if(doctor_id!=null){
                    get_doctor_arn();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }
public void get_doctor_arn(){
        doctor_dabasereference.child(doctor_id).child("personal_details").child("arn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //System.out.println("doctor_id "+doctor_id);
                //System.out.println("Hash map: "+(HashMap)dataSnapshot.getValue());
                arn=(String)dataSnapshot.getValue();
                System.out.println("arn : "+arn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    private void seconds_operation(){
        System.out.println("inside seconds operation");
        calendar=Calendar.getInstance();
        time=calendar.getTime().toString();
        //String[] date_values= LocalDate.now().toString().split("-");
        //final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        //final String month=date_values[1];
        //final String day=(date_values[2]);
        //final String year=(date_values[0]);
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
    private void Calculating_minute_average(){
        Log.d("status","Inside calculating minute average");
        minute_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Long> h1=(HashMap<String,Long>)dataSnapshot.getValue();
                Log.d("status","Calculating the average data");
                last_hour_average_data = h1.get("Sum") / h1.get("TotalEntries");
                Log.d("status","Found the data : "+last_hour_average_data);

                hours_operation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void hours_operation(){
        System.out.println("inside hours operation");
        calendar=Calendar.getInstance();
        //String[] date_values= LocalDate.now().toString().split("-");
        //final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        //final String month=date_values[1];
        //final String day=(date_values[2]);
        //final String year=(date_values[0]);
        final String time=calendar.getTime().toString().substring(11,19);

        hour_data_reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("Data deleted");
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
                            Toast.makeText(getActivity(), h1.toString(), Toast.LENGTH_LONG).show();
                            Iterator hmIterator = h1.entrySet().iterator();
                            for (int i = 1; i <= 7; i++) {
                                arrayList.add(h1.get(Data_Key + i));
                            }
                            for (int i = 1; i < arrayList.size(); i++) {

                                hour_data_reference.child(Data_Key + (i)).setValue(arrayList.get(i));

                            }
                            hour_data_reference.child(Data_Key + 7).setValue(last_hour_average_data);

                        }
                        Log.d("status","about to add the sum");
                        hour_data_reference.child("sum").setValue(h1.get("sum") + last_hour_average_data);
                        hour_data_reference.child("TotalEntries").setValue(h1.get("TotalEntries") + 1);
                        Log.d("Status","Added the sum : "+h1.get("sum"));
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
        //String[] date_values= LocalDate.now().toString().split("-");
        //final String hour=String.valueOf(Calendar.HOUR_OF_DAY);
        //final String month=date_values[1];
        //final String day=(date_values[2]);
        //final String year=(date_values[0]);
        //final String time=calendar.getTime().toString();
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
    public void Stop(){
        scheduledExecutorService.shutdown();
        handler.removeCallbacks(runnable);

    }

    public void notify_doctor(){
            System.out.println("data : ");
            Toast.makeText(getActivity(),"notifying the doctor",Toast.LENGTH_LONG);
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference=firebaseDatabase.getReference("users");
            CognitoCachingCredentialsProvider cccp = new CognitoCachingCredentialsProvider(getActivity(), AWSMobileClient.getInstance().getConfiguration());
            AmazonSNSClient sns = new AmazonSNSClient(cccp);
            sns.setRegion(Region.getRegion("us-west-2"));
            sns.setEndpoint("https://sns." + Region.getRegion("us-west-2") + ".amazonaws.com/");
            String message = user.getDisplayName() + "is in emergency";
            for (String number:data.values()){
                PublishRequest pr = new PublishRequest().withMessage(message).withPhoneNumber(number);
                sns.publish(pr);
            }
            if (!arn.equals("")) {
                System.out.println("arn: " + arn);
                PublishRequest pr = new PublishRequest().withTargetArn(arn).withMessage(message);
                sns.publish(pr);
                Toast.makeText(getActivity(), "notifcations sent", Toast.LENGTH_SHORT);
            }

    }
    private class notify extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference=firebaseDatabase.getReference("users");
            CognitoCachingCredentialsProvider cccp = new CognitoCachingCredentialsProvider(getActivity(), AWSMobileClient.getInstance().getConfiguration());
            AmazonSNSClient sns = new AmazonSNSClient(cccp);
            sns.setRegion(Region.getRegion("us-west-2"));
            sns.setEndpoint("https://sns." + Region.getRegion("us-west-2") + ".amazonaws.com/");
            String message = user.getEmail() + "is in emergency";
            if(data!=null){
            for (String number:data.values()) {
                PublishRequest pr = new PublishRequest().withMessage(message).withPhoneNumber(number);
                sns.publish(pr);
            }
            }
            if (arn!=null && !arn.equals("")) {
                //System.out.println("inside arn equla method : "+arn);
                PublishRequest pr = new PublishRequest().withTargetArn(arn).withMessage(message);
                sns.publish(pr);
            }
            return null;
        }
    }
    public void setheartrate(int data){
        //Toast.makeText(getActivity(),"Inside setheart rate : "+pStatus,Toast.LENGTH_LONG).show();
        pStatus=data;
        progressBar.setProgress(pStatus);
        txtProgress.setText(String.valueOf(pStatus));

    }
}
