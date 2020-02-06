package com.example.heartrate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class doctor_patient_details extends AppCompatActivity implements Patients_week_data.OnFragmentInteractionListener, patient_todays_data.OnFragmentInteractionListener {
    private Toolbar mToolbar;
    private TabLayout mTablayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_patient_details);
        //mToolbar = findViewById(R.id.toolbar);
        mTablayout = findViewById(R.id.tableLayout);
        mViewPager = findViewById(R.id.viewPager);

        //mToolbar.setTitle("Tab Layout");
        Log.d("status","inside doctor details method");
        Intent intent=getIntent();
        Serializable s1 =intent.getSerializableExtra("patient");
        patient_todays_data patientTodaysData=patient_todays_data.newInstance(s1);
        Patients_week_data patients_week_data=Patients_week_data.newInstance(s1);

        setupViewpager(mViewPager,patientTodaysData,patients_week_data);
        mTablayout.setupWithViewPager(mViewPager);
        //getting the patients data from doctor activity
        ;

        //Patients_week_data weeks_data=new Patients_week_data();


    }
    private void setupViewpager(ViewPager viewPager,patient_todays_data p1,Patients_week_data p2){

        viewPagerAdapter adapter = new viewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(p1, "today");
        adapter.addFragment(p2,"week");

        viewPager.setAdapter(adapter);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

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
}
