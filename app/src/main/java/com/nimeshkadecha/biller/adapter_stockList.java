package com.nimeshkadecha.biller;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adapter_stockList extends RecyclerView.Adapter<adapter_stockList.MyViewHolder> {
	private final Context context;
	private final ArrayList r;
	private final ArrayList g;
	private final ArrayList b;
	private final ArrayList name;

	public adapter_stockList(Context context, ArrayList r, ArrayList g, ArrayList b, ArrayList name) {

		this.context = context;
		this.r = r;
		this.g = g;
		this.b = b;
		this.name = name;
	}

	@NonNull
	@Override
	public adapter_stockList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.stock_list, parent, false);
		return new MyViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull adapter_stockList.MyViewHolder holder, int position) {
		int color = Color.argb(255, Integer.parseInt(String.valueOf(r.get(position))), Integer.parseInt(String.valueOf(g.get(position))), Integer.parseInt(String.valueOf(b.get(position))));
		holder.v.setBackgroundColor(color);
		holder.Name.setText(String.valueOf(name.get(position)));
	}

	@Override
	public int getItemCount() {
		return name.size();
	}

	public static class MyViewHolder extends RecyclerView.ViewHolder {

		TextView Name;

		View v;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);
			v = itemView.findViewById(R.id.set_color);
			Name = itemView.findViewById(R.id.stock_Name);

		}
	}
}
