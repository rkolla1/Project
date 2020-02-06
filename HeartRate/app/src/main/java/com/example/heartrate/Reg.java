package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Reg extends AppCompatActivity {
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    private String status;
    Fragment fragment;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Toolbar mToolbar;
    private TabLayout mTablayout;
    private ViewPager mViewPager;
    HashSet<Character> special_set;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();
        //firebase related code
        mAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");



        //mToolbar = findViewById(R.id.toolbar);
        mTablayout = findViewById(R.id.tableLayout);
        mViewPager = findViewById(R.id.viewPager);

        //mToolbar.setTitle("Tab Layout");

        setupViewpager(mViewPager);
        mTablayout.setupWithViewPager(mViewPager);

        Character[] c1={'@','#','$','[',']','.'};
        special_set=new HashSet<>(Arrays.asList(c1));


    }

    private void setupViewpager(ViewPager viewPager){

        viewPagerAdapter adapter = new viewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new patient_signup(), "patient");
        adapter.addFragment(new Doctor_signup(),"Doctor");

        viewPager.setAdapter(adapter);

    }

    class viewPagerAdapter extends FragmentPagerAdapter{

        private final List<Fragment> mFragmentList = new ArrayList <>();
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
    public void navigate(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();

    }
    public String getStatus(){
        return status;
    }
    public void Signup(String email, final String username, final String password, final String type){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"task successful",Toast.LENGTH_LONG).show();
                    FirebaseUser currentuser=mAuth.getCurrentUser();

                    String user_id=currentuser.getUid();
                    final String email=currentuser.getEmail();
                    long userid_code=email.hashCode();
                    final Map<String,String> attributes = new HashMap<>();
                    attributes.put("email",email);
                    databaseReference.child("usermapping").child(String.valueOf(userid_code)).setValue(user_id);
                    databaseReference.child(currentuser.getUid()).child("personal_details").child("username").setValue(username);
                    databaseReference.child(user_id).child("personal_details").child("type").setValue(type);
                    AWSMobileClient.getInstance().signUp(email, password, attributes, null, new Callback<SignUpResult>() {
                        @Override
                        public void onResult(SignUpResult result) {
                            Intent intent=new Intent(Reg.this,confirmSignUp.class);
                            intent.putExtra("email",email);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("came",e.getMessage());
                        }
                    });


                }
                else{
                    Toast.makeText(getApplicationContext(),"task not successful",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
