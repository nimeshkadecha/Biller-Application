package com.nimeshkadecha.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class stock_List_adapter extends RecyclerView.Adapter<stock_List_adapter.MyViewHolder>{
    private Context context;
    private ArrayList r,g,b, name;

    public stock_List_adapter(Context context, ArrayList r,ArrayList g,ArrayList b, ArrayList name) {

        this.context = context;
        this.r = r;
        this.g = g;
        this.b = b;
        this.name = name;

    }

    @NonNull
    @Override
    public stock_List_adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.stock_list, parent, false);
        return new stock_List_adapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull stock_List_adapter.MyViewHolder holder, int position) {
        int color = Color.argb(255,Integer.parseInt(String.valueOf(r.get(position))), Integer.parseInt(String.valueOf(g.get(position))), Integer.parseInt(String.valueOf(b.get(position))));
        holder.v.setBackgroundColor(color);;
        holder.Name.setText(String.valueOf(name.get(position)));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView Name;

        View v;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            index = itemView.findViewById(R.id.textindex);
            v = itemView.findViewById(R.id.set_color);
            Name = itemView.findViewById(R.id.stock_Name);

        }
    }
}
