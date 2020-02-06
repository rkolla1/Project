package com.example.heartrate;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.HashSet;


/**
 * A simple {@link Fragment} subclass.
 */
public class Doctor_signup extends Fragment {

    EditText email;
    EditText password;
    EditText username;
    public Doctor_signup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_doctor_signup, container, false);
        email=view.findViewById(R.id.doctor_email);
        password=view.findViewById(R.id.doctor_password);
        username=view.findViewById(R.id.doctor_username);
        Button signup_button=view.findViewById(R.id.doctor_signup);

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Inside signup on click");
                String email_data=email.getText().toString();
                String password_data=email.getText().toString();
                String username_data=username.getText().toString();
                if (TextUtils.isEmpty(username_data)) {
                    username.setError("Please enter the username");
                }
                else if (TextUtils.isEmpty(email_data)) {
                    email.setError("Please enter the email");
                }
                else if (TextUtils.isEmpty(password_data)) {
                    password.setError("Please enter the passoword");
                }

                else {
                    ((Reg) getActivity()).Signup(email.getText().toString(), username.getText().toString(), password.getText().toString(), "doctor");
                }

            }
        });

        return view;
    }


}
