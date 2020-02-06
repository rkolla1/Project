package com.example.heartrate;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Patients_week_data.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Patients_week_data#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Patients_week_data extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Patient p1;
    private BarChart barChart;
    TextView average;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Patients_week_data() {}

    public static Patients_week_data newInstance(Serializable s1) {
        Patients_week_data fragment = new Patients_week_data();
        Bundle args = new Bundle();
        args.putSerializable("patient",s1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            System.out.println("got the arguments");
            p1=(Patient)getArguments().getSerializable("patient");
            System.out.println(p1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_patients_week_data, container, false);
        barChart = view.findViewById(R.id.barchart);
        average = view.findViewById(R.id.average);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);
        ArrayList<BarEntry> yVals=new ArrayList<>();
        for(int i=0;i< p1.current_week.size();i++){
            yVals.add(new BarEntry(i,p1.current_week.get(i)));
        }
        if (yVals.size() > 0) {
            average.setText(String.valueOf(p1.weeks_average_heartbeat));
            BarDataSet set = new BarDataSet(yVals, "Data set");
            set.setColors(Color.WHITE);
            set.setDrawValues(true);
            BarData data = new BarData(set);

            data.setBarWidth(0.5f);


            barChart.setData(data);
            barChart.invalidate();
            barChart.animateY(500);


            barChart.setTouchEnabled(true);
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);

            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisLeft().setDrawAxisLine(false);
            barChart.getAxisRight().setDrawAxisLine(false);
            barChart.getAxisRight().setDrawGridLines(false);

            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setEnabled(true);
            barChart.getAxisLeft().setGridColor(Color.WHITE);
            barChart.getAxisLeft().setTextColor(Color.WHITE);
            barChart.getDescription().setEnabled(true);

            barChart.getDescription().setText("Month Data");
            barChart.getDescription().setTextColor(Color.WHITE);

            barChart.setDrawValueAboveBar(true);

            barChart.getXAxis().setDrawAxisLine(false);
            barChart.getXAxis().setTextColor(Color.WHITE);

            barChart.getAxisLeft().setDrawGridLines(false);

            //barChart.getAxisRight().setDrawGridLines(true);
            barChart.getAxisLeft().setDrawGridLines(true);
            barChart.getXAxis().setDrawGridLines(false);

            barChart.animateY(500);


        }
        if(p1.current_week!=null && p1.current_week.size()>0){

            System.out.println("current week data : "+p1.current_week);
            data_Adapter adapter = new data_Adapter(p1.current_week, getActivity());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        }

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
}
