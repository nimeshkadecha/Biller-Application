package com.nimeshkadecha.biller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adapter_stockQuantity extends RecyclerView.Adapter<adapter_stockQuantity.MyViewHolder> {
	private final Context context;
	private final ArrayList item;
	private final ArrayList quantity;

	public adapter_stockQuantity(Context context, ArrayList item, ArrayList quantity) {

		this.context = context;
		this.item = item;
		this.quantity = quantity;

	}

	@NonNull
	@Override
	public adapter_stockQuantity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.qty_list, parent, false);
		return new MyViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull adapter_stockQuantity.MyViewHolder holder, int position) {
		holder.item.setText(String.valueOf(item.get(position)));
		holder.quantity.setText(String.valueOf(quantity.get(position)));
	}

	@Override
	public int getItemCount() {
		return item.size();
	}

	public static class MyViewHolder extends RecyclerView.ViewHolder {

		TextView item, quantity;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);
			item = itemView.findViewById(R.id.stock_qty_name);
			quantity = itemView.findViewById(R.id.stock_qty);

		}
	}
}
