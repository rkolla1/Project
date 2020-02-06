package com.example.heartrate;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class patient_signup extends Fragment {

    EditText username;
    EditText email;
    EditText password;
    Button signup_button;
    public patient_signup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_patient_signup, container, false);
        username=view.findViewById(R.id.username_id);
        password=view.findViewById(R.id.password_id);
        email=view.findViewById(R.id.email_id);
        signup_button=view.findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username_data=username.getText().toString();
                String password_data=password.getText().toString();
                String email_data=email.getText().toString();
                if(TextUtils.isEmpty(username_data)){
                    username.setError("Please enter the username");
                }
                else if(TextUtils.isEmpty(password_data)){
                    password.setError("Please enter the password");
                }
                else if(TextUtils.isEmpty(email_data)){
                    email.setError("please enter the email");
                }
                else{
                    ((Reg)getActivity()).Signup(email_data,username_data,password_data,"patient");
                }
            }
        });


        return view;
    }

}
