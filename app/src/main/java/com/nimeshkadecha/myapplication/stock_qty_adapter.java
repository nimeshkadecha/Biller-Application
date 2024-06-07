package com.nimeshkadecha.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class stock_qty_adapter extends RecyclerView.Adapter<stock_qty_adapter.MyViewHolder> {
	private final Context context;
	private final ArrayList item;
	private final ArrayList quantity;

	public stock_qty_adapter(Context context, ArrayList item, ArrayList quantity) {

		this.context = context;
		this.item = item;
		this.quantity = quantity;

	}

	@NonNull
	@Override
	public stock_qty_adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.qty_list, parent, false);
		return new stock_qty_adapter.MyViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull stock_qty_adapter.MyViewHolder holder, int position) {
		holder.item.setText(String.valueOf(item.get(position)));
		holder.quantity.setText(String.valueOf(quantity.get(position)));
	}

	@Override
	public int getItemCount() {
		return item.size();
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {

		TextView item, quantity;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);
			item = itemView.findViewById(R.id.stock_qty_name);
			quantity = itemView.findViewById(R.id.stock_qty);

		}
	}
}
