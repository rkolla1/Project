package com.example.heartrate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import in.mayanknagwanshi.imagepicker.ImageSelectActivity;

public class information extends AppCompatActivity {

    ImageView image,edit;

    EditText name,age,weight,doctor;
    Button save;

    int flag = 0;

    RadioGroup radioSexGroup;
    RadioButton radioSexButton;

    RadioButton male,female;

    Bitmap selectedImage;

    private StorageReference folder ;


    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference,reference;

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;

    FirebaseUser currentuser;


    HashMap <String,String> h1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEdit = mPref.edit();

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();


        image = findViewById(R.id.imageView1);
        edit = findViewById(R.id.edit);

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        weight = findViewById(R.id.weight);
        doctor = findViewById(R.id.doctor);
        save = findViewById(R.id.save);
        radioSexGroup = findViewById(R.id.radiogroup);

        folder = FirebaseStorage.getInstance().getReference().child("ImageFolder");

        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");

        reference=firebaseDatabase.getReference("users").child(currentuser.getUid()).child("personal_details");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot objects : dataSnapshot.getChildren()) {
                    // log will shows your result
                    h1 = (HashMap <String, String>) dataSnapshot.getValue();


                    if (h1 != null) {
                        Log.d("showdetails", "" + h1);

                        name.setBackgroundResource(android.R.color.transparent);
                        if (h1.containsKey("username"))
                            name.setText("NAME : " + h1.get("username"));
                    }
                    image.setClickable(false);
                    if (h1.containsKey("imageurl")) {
                        if (!h1.get("imageurl").equals("")) {
                            Picasso.get().load(h1.get("imageurl")).into(image);
                        }
                    }

                    if(h1.containsKey("type"))
                        doctor.setText(h1.get("type"));
                    }
                    if (h1.containsKey("age")) {
                        age.setBackgroundResource(android.R.color.transparent);
                        age.setText("AGE : " + h1.get("age"));
                    }
                    if (h1.containsKey("weight")) {
                        weight.setBackgroundResource(android.R.color.transparent);
                        weight.setText("WEIGHT : " + h1.get("weight"));
                    }


                    male.setClickable(false);
                    female.setClickable(false);
                    if (h1.containsKey("gender")) {
                        if (h1.get("gender").equals("Male")) {
                            male.toggle();
                        } else {
                            female.toggle();
                            //Log.d("showdetails1",""+h1.get("gender"));

                        }
                    }


                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });






        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(information.this, ImageSelectActivity.class);
                intent.putExtra(ImageSelectActivity.FLAG_COMPRESS, true);
                intent.putExtra(ImageSelectActivity.FLAG_CAMERA, true);
                intent.putExtra(ImageSelectActivity.FLAG_GALLERY, true);
                startActivityForResult(intent, 1213);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(name.getText().toString() == null){
                    Toast.makeText(getApplicationContext(),"enter name",Toast.LENGTH_LONG).show();
                }
                else if(radioSexGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(),"Select Gender",Toast.LENGTH_LONG).show();
                }
                else if(age.getText().toString() == null){
                    Toast.makeText(getApplicationContext(),"enter age",Toast.LENGTH_LONG).show();

                }
                else if(weight.getText().toString() == null){
                    Toast.makeText(getApplicationContext(),"enter age",Toast.LENGTH_LONG).show();
                }
                else if(flag == 0){
                    Toast.makeText(getApplicationContext(),"Select the Image",Toast.LENGTH_LONG).show();
                }
                else{
                    int selectedId = radioSexGroup.getCheckedRadioButtonId();
                    radioSexButton = findViewById(selectedId);
                    upload();

                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.INVISIBLE);
                save.setVisibility(View.VISIBLE);
                name.setEnabled(true);
                weight.setEnabled(true);
                age.setEnabled(true);
                male.setClickable(true);
                female.setClickable(true);
                image.setClickable(true);


                name.setText(h1.get("username"));
                age.setText(h1.get("age"));
                weight.setText(h1.get("weight"));
                if (h1 != null) {
                    if (h1.containsKey("type")) {
                        if (h1.get("type").equals("patient")) {
                            doctor.setVisibility(View.INVISIBLE);
                        } else {
                            doctor.setText(h1.get("type"));
                        }
                    }
                    if (h1.containsKey("gender")) {
                        if (h1.get("gender").equals("Male")) {
                            male.toggle();
                        } else {
                            female.toggle();

                        }
                    }
                }
            }
        });




    }

    private void upload(){

        final StorageReference Imagename = folder.child("Image"+name.getText().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data1 = baos.toByteArray();

        UploadTask uploadTask = Imagename.putBytes(data1);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(),"Image upload Failed",Toast.LENGTH_LONG).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener <Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        System.out.println(uri);
                        Log.d("lol","lol");
                        System.out.println(uri.toString());


                        //FirebaseUser currentuser=mAuth.getCurrentUser();


                        databaseReference.child(currentuser.getUid()).child("personal_details").child("username").setValue(name.getText().toString());
                        databaseReference.child(currentuser.getUid()).child("personal_details").child("age").setValue(age.getText().toString());
                        databaseReference.child(currentuser.getUid()).child("personal_details").child("weight").setValue(weight.getText().toString());
                        databaseReference.child(currentuser.getUid()).child("personal_details").child("gender").setValue(radioSexButton.getText().toString());
                        databaseReference.child(currentuser.getUid()).child("personal_details").child("imageurl").setValue(uri.toString());

                        save.setVisibility(View.INVISIBLE);
                        edit.setVisibility(View.VISIBLE);
                        name.setEnabled(false);
                        weight.setEnabled(false);
                        age.setEnabled(false);
                        male.setClickable(false);
                        female.setClickable(false);
                        image.setClickable(false);

                    }
                });

            }
        });



    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1213 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);
            selectedImage = BitmapFactory.decodeFile(filePath);
            flag = 1;
            image.setImageBitmap(selectedImage);
        }
    }
}