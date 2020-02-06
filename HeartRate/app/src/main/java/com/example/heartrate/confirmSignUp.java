package com.example.heartrate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cognitoidentityprovider.model.ExpiredCodeException;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class confirmSignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_up);

        final EditText code = findViewById(R.id.confirmation);
        Button resend = findViewById(R.id.resend);
        Button confirm = findViewById(R.id.confirm);

        final String email = getIntent().getExtras().get("email").toString();

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        System.out.println("cognito"+ userStateDetails.getDetails());
                    case SIGNED_OUT:
                        break;
                    default:
                        System.out.println("default");
                }

            }

            @Override
            public void onError(Exception e) {

            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWSMobileClient.getInstance().resendSignUp(email, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult result) {
                        Log.d("verification","sent");
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (code.getText().toString() != null){
                    AWSMobileClient.getInstance().confirmSignUp(email, code.getText().toString(), new Callback<SignUpResult>() {
                        @Override
                        public void onResult(SignUpResult result) {
                            //Toast.makeText(getApplicationContext(),"Sign-up done",Toast.LENGTH_SHORT)
                            Intent i = new Intent(confirmSignUp.this,MainActivity.class);
                            startActivity(i);

                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("confirm signup",e.getMessage());
                        }
                    });
                }
            }
        });

    }
}
