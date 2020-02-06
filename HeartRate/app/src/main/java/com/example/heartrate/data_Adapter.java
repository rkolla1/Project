package com.example.heartrate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class data_Adapter extends RecyclerView.Adapter<data_Adapter.ViewHolder> {

    @NonNull

    private Context context;
    private ArrayList<Long> list_of_groups;


    public data_Adapter(ArrayList<Long> list_of_groups,Context context){
        this.list_of_groups = list_of_groups;
        this.context = context;
    }

    public data_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data,parent,false);

        return new data_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull data_Adapter.ViewHolder holder, int position) {
        long average_value=list_of_groups.get(position);
        if(average_value<=60){
            holder.category.setText("Very Bad");
        }
        else if(average_value>=60 && average_value<=67){
            holder.category.setText("Abnormal");
        }
        else if(average_value>85){
            holder.category.setText("Hyper");
        }
        else {
            holder.category.setText("Normal");
        }
        holder.textView.setText(String.valueOf(list_of_groups.get(position)));

    }

    @Override
    public int getItemCount() {
        return list_of_groups.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView category;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.avgrate);
            category=itemView.findViewById(R.id.category);
        }
    }
}