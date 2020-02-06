package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashMap;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;


public class MainActivity extends AppCompatActivity {

    TextView signUp_text;
    ImageView hr;
    EditText email;
    EditText password;
    Button signin;
    String baseurl="https://z1cbskq9w2.execute-api.us-east-2.amazonaws.com/default/";
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String token;
    ToggleSwitch toggleSwitch;
    int position;
    String type;
    FirebaseUser currentuser;
    TextView forgot_password;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set_Heartrateanimation();
        boolean isInternetAvailable=isNetworkConnected();
        mPref=PreferenceManager.getDefaultSharedPreferences(this);
        mEdit=mPref.edit();
        //firebase initialization
        mAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        forgot_password=findViewById(R.id.forgot);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,ForgotPassword.class);
                startActivity(intent);
            }
        });


        toggleSwitch=(ToggleSwitch)findViewById(R.id.toggle_choice);

        signin=findViewById(R.id.signin_button);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("status","inside onclick");
                String email_data=email.getText().toString();
                String password_data=password.getText().toString();
                String type="doctor";
                position=toggleSwitch.getCheckedTogglePosition();
                System.out.println("toggle position : "+position);
                if(TextUtils.isEmpty(email_data)||TextUtils.isEmpty(password_data)){
                    if(TextUtils.isEmpty(email_data)){
                    email.setError("Please enter the email");}
                    if(TextUtils.isEmpty(password_data)){
                        password.setError("please enter the password");
                    }
                }
                else{
                    if(position==0){
                        type="patient";
                    }
                    login(email_data,password_data,type);
                }

            }
        });
        signUp_text = findViewById(R.id.signUp_text);
        signUp_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Reg.class));
            }
        });
    }
    public void onStart() {

        super.onStart();
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });

        if(mAuth.getCurrentUser()!=null){
            if(mPref.getString("type","").equals("doctor")){
                Intent intent=new Intent(this,doctor.class);
                startActivity(intent);
                finish();
            }
            else if(mPref.getString("type","").equals("patient")){
                Intent intent=new Intent(this,Main2Activity.class);
                startActivity(intent);
                finish();
            }

        }
    }
    public void set_Heartrateanimation(){
        hr = findViewById(R.id.hr);
        Drawable d = hr.getDrawable();
        if(d instanceof AnimatedVectorDrawableCompat){
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
            avd.start();
        }else if(d instanceof AnimatedVectorDrawable){
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
            avd.start();

        }

    }
    public void login(final String email, final String password, final String type){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                   currentuser=mAuth.getCurrentUser();
                    AWSMobileClient.getInstance().signIn(email, password, null, new Callback<SignInResult>() {
                        @Override
                        public void onResult(SignInResult result) {
                            Toast.makeText(getApplicationContext(),"signin succesful",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                   user_type_validation(type);
                }
            }
        });


    }
    private void user_type_validation(final String type){
        databaseReference.child(currentuser.getUid()).child("personal_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,String> h1=(HashMap)dataSnapshot.getValue();
                System.out.println("h1 "+h1);
                if(h1==null){
                    Toast.makeText(getApplicationContext(),"Please check your type",Toast.LENGTH_LONG).show();
                }
                else if(!h1.get("type").equals(type)){
                    Toast.makeText(getApplicationContext(),"Please check your type",Toast.LENGTH_LONG).show();
                }
                else{
                        new Asynctasks().execute();

                    if(type.equals("doctor")){
                        mEdit.putString("type",type);
                        doctor.doctor_arn = h1.get("arn");
                        Intent intent=new Intent(getApplicationContext(),doctor.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        mEdit.putString("type",type);
                        Intent intent=new Intent(getApplicationContext(),Main2Activity.class);
                        startActivity(intent);
                        finish();
                    }
                    mEdit.commit();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private boolean isNetworkConnected(){
        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo()!=null && cManager.getActiveNetworkInfo().isConnected();
    }

    private class Asynctasks extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            CognitoCachingCredentialsProvider cccp = new CognitoCachingCredentialsProvider(getApplicationContext(),AWSMobileClient.getInstance().getConfiguration());
            AmazonSNSClient sns = new AmazonSNSClient(cccp);
            sns.setRegion(Region.getRegion("us-west-2"));
            CreatePlatformEndpointRequest cr = new CreatePlatformEndpointRequest().withPlatformApplicationArn("arn:aws:sns:us-west-2:272184526412:app/GCM/Project").withToken(FirebaseInstanceId.getInstance().getToken());
            CreatePlatformEndpointResult cres = sns.createPlatformEndpoint(cr);
            System.out.println("Cres : "+cres.getEndpointArn());
            databaseReference.child(mAuth.getUid()).child("personal_details").child("arn").setValue(cres.getEndpointArn());
            //doctor.doctor_arn = cres.getEndpointArn();
            return null;
        }
        }
    }

