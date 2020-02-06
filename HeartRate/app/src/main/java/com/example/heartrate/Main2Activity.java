package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;
import io.reactivex.functions.Consumer;

import static com.example.heartrate.patient_details_fragment.id_key;

public class Main2Activity extends AppCompatActivity implements patient_details_fragment.OnFragmentInteractionListener, ScanFragment.OnFragmentInteractionListener, PopupMenu.OnMenuItemClickListener   {

    PolarBleApi api;
    String DEVICE_ID="";
    private Toolbar mToolbar;
    private TabLayout mTablayout;
    private ViewPager mViewPager;
    Disposable scanDisposable;

    //Shared preferences
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    HashMap<String,String> data;
    String arn = "";

    //profile

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    patient_details_fragment fragment1;
    ScanFragment fragment2;

    HashMap <String,String> h1;

    FirebaseUser currentuser;

    ImageView image;
    TextView name;
    boolean isconnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //profile
        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();

        image = findViewById(R.id.imageView1);
        name = findViewById(R.id.profilename);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference=firebaseDatabase.getReference("users");
        data = new HashMap<>();
        databaseReference.child(user.getUid()).child("emergency").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = (HashMap) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        databaseReference.child(user.getUid()).child("personal_details").child("doctor_arn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arn = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mTablayout = findViewById(R.id.tableLayout);
        mViewPager = findViewById(R.id.viewPager);
        patient_details_fragment fragment1=new patient_details_fragment();
        fragment2=new ScanFragment();
        setupViewpager(mViewPager,fragment1,fragment2);
        mTablayout.setupWithViewPager(mViewPager);
        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //ActionBar actionBar = getActionBar();
        //actionBar.show();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);



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
                Log.d("heart rate ",String.valueOf(data.hr));
                fragment2.setheartrate(data.hr);
                //Toast.makeText(Main2Activity.this,String.valueOf(data.hr),Toast.LENGTH_LONG).show();
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

        //getSupportFragmentManager().beginTransaction().add(new patient_details_fragment())
    }


    public boolean isconnected(){
        if(isconnected){
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    private void setupViewpager(ViewPager viewPager,patient_details_fragment fragment1,ScanFragment fragment2){

        viewPagerAdapter adapter = new viewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(fragment1, "today");
        adapter.addFragment(fragment2,"week");

        viewPager.setAdapter(adapter);

    }
    class viewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> MFragmenttitleList = new ArrayList <>();

        public viewPagerAdapter(FragmentManager fm){
            super (fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment (Fragment fragment, String title){
            mFragmentList.add(fragment);
            MFragmenttitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return MFragmenttitleList.get(position);
        }
    }
    public void scanning(){
                 if (scanDisposable == null) {
                    scanDisposable = api.searchForDevice().observeOn(AndroidSchedulers.mainThread()).subscribe(
                            new Consumer<PolarDeviceInfo>() {
                                @Override
                                public void accept(PolarDeviceInfo polarDeviceInfo) throws Exception {
                                    //if (polarDeviceInfo.deviceId.equals("51460B23")) {
                                    //Toast.makeText(getApplicationContext(), "inside on click", Toast.LENGTH_LONG).show();
                                    if (!polarDeviceInfo.deviceId.equals("")) {
                                        DEVICE_ID = polarDeviceInfo.deviceId;
                                        //api.connectToDevice(DEVICE_ID);
                                        alertdialog();
                                        Log.d("hurray", "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable);
                                    }
                                }
                            },
                            new Consumer <Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.d("status", "" + throwable.getLocalizedMessage());

                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d("status", "complete");
                                }
                            }
                    );
                } else {
                    scanDisposable.dispose();
                    scanDisposable = null;
                }


}
    private void alertdialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select One Device:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Main2Activity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        try {
                            api.connectToDevice(DEVICE_ID);
                            isconnected=true;
                            Toast.makeText(getApplicationContext(), "Connected to : " + DEVICE_ID, Toast.LENGTH_LONG).show();

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
    public void onDestroy() {

        super.onDestroy();
        api.shutDown();
    }

    public void showpopup(View v){
        PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference=firebaseDatabase.getReference("users");

        switch (menuItem.getItemId()){
            case R.id.notify:
                CognitoCachingCredentialsProvider cccp = new CognitoCachingCredentialsProvider(getApplicationContext(), AWSMobileClient.getInstance().getConfiguration());
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
                    Toast.makeText(getApplicationContext(), "notifcations sent", Toast.LENGTH_SHORT);
                }
                return true;
            case R.id.details:
                Intent intent1=new Intent(this,information.class);
                startActivity(intent1);
                return true;
            case R.id.logout:
                Toast.makeText(this,"logout", Toast.LENGTH_LONG).show();
                mEdit.clear();
                mEdit.commit();
                if(fragment2!=null) {
                    fragment2.Stop();
                }
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.emergency:
                //Toast.makeText(this,"emergency", Toast.LENGTH_LONG).show();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Add Emergency Contact");
                //alert.setMessage("Message");
                //Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (value == null || value.length() < 10){
                            Toast.makeText(getApplicationContext(),"number shouldn't be null and should be a valid number",Toast.LENGTH_SHORT).show();

                        }
                        else{
                                databaseReference
                                        .child(user.getUid())
                                        .child("emergency").push().setValue(value);
                        }

                        // Do something with value!
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alert.show();

                return true;
            default:
                return false;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        reference=firebaseDatabase.getReference("users").child(currentuser.getUid()).child("personal_details");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                h1=(HashMap<String,String>)dataSnapshot.getValue();
                if(h1!=null) {

                    if (h1.containsKey("imageurl") && !h1.get("imageurl").equals("")) {
                        Picasso.get().load(h1.get("imageurl")).into(image);
                    }

                    name.setText(h1.get("username"));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void stop(){
        try {
            api.disconnectFromDevice(DEVICE_ID);
            isconnected=false;
        } catch (PolarInvalidArgument polarInvalidArgument) {
            polarInvalidArgument.printStackTrace();
        }
    }

    }


