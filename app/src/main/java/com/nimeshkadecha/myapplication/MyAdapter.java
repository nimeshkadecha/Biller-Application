package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
	DBManager DBlocal;
	private final Context context;
	private final ArrayList item;
	private final ArrayList price;
	private final ArrayList quantity;
	private final ArrayList subtotal;
	private final ArrayList index;
	private final ArrayList GST;
		final double MAX_REAL_VALUE = Double.MAX_VALUE;

	public MyAdapter(Context context, ArrayList item, ArrayList price, ArrayList quantity, ArrayList subtotal, ArrayList index, ArrayList GST) {

		
		this.context = context;
		this.price = price;
		this.item = item;
		this.index = index;
		this.quantity = quantity;
		this.subtotal = subtotal;
		this.GST = GST;
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.userentry, parent, false);
		return new MyViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

		Boolean checkGST = false;
		if (GST.get(position).equals("0")) {
			holder.gstLayout.setVisibility(View.GONE);
		} else {
			checkGST = true;
			holder.gstLayout.setVisibility(View.VISIBLE);
		}

		holder.item.setText(String.valueOf(item.get(position)));
		holder.price.setText(String.valueOf(convertScientificToNormal(Double.parseDouble(String.valueOf(price.get(position))))));
		holder.quantity.setText(String.valueOf(quantity.get(position)));
		holder.subtotal.setText(String.valueOf(convertScientificToNormal(Double.parseDouble(String.valueOf(subtotal.get(position))))));

		if (checkGST) {
			double tax = ((Double.parseDouble(String.valueOf(price.get(position))) * Integer.parseInt(String.valueOf(quantity.get(position)))) * (Double.parseDouble(String.valueOf(GST.get(position))) / 100d));
			holder.gst.setText(String.valueOf(tax));
		}

		holder.AddBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DBlocal = new DBManager(context);
				String TempItem = String.valueOf(item.get(position));
				double TempPrice = Double.parseDouble(String.valueOf(price.get(position)));
				int TempQuantity = Integer.parseInt(String.valueOf(quantity.get(position)));
				double TempGST = Double.parseDouble(String.valueOf(GST.get(position)));
				double TempSubtotal = 1d;
				TempQuantity += 1;
				double tax = ((TempPrice * TempQuantity) * (TempGST / 100d));
				TempSubtotal = (TempPrice * TempQuantity) + tax;

				if(TempSubtotal < MAX_REAL_VALUE) {

					boolean updateData = DBlocal.UpdateQuantity(TempQuantity, TempSubtotal, Integer.parseInt(String.valueOf(index.get(position))));
					if (updateData) {
						item.remove(position);
						price.remove(position);
						quantity.remove(position);
						subtotal.remove(position);

						item.add(position, TempItem);
						price.add(position, convertScientificToNormal(TempPrice));
						quantity.add(position, TempQuantity);
						subtotal.add(position, convertScientificToNormal(TempSubtotal));

						MyAdapter.this.notifyDataSetChanged();
					} else {
						Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
					}
				}
				else{
					Toast.makeText(context, "Android can't handle this big number !", Toast.LENGTH_SHORT).show();
				}
			}
		});

		holder.RemoveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (String.valueOf(quantity.get(position)).equals("1")) {
					holder.RemoveBtn.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View view) {
							DBlocal = new DBManager(context);
							boolean removeItem = DBlocal.RemoveItem(String.valueOf(index.get(position)));
							if (!removeItem) {
								Toast.makeText(context, "Failed to Removed", Toast.LENGTH_SHORT).show();
								return false;
							} else {
								Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
								item.remove(position);
								price.remove(position);
								quantity.remove(position);
								subtotal.remove(position);
								MyAdapter.this.notifyDataSetChanged();
								return true;
							}
						}
					});
					Toast.makeText(context, "Long press to remove", Toast.LENGTH_SHORT).show();
				} else {
					DBlocal = new DBManager(context);
					String TempItem = String.valueOf(item.get(position));
					double TempPrice = Double.parseDouble(String.valueOf(price.get(position)));
					int TempQuantity = Integer.parseInt(String.valueOf(quantity.get(position)));
					double TempGST = Double.parseDouble(String.valueOf(GST.get(position)));

					double TempSubtotal = 1;
					TempQuantity -= 1d;
					double tax = ((TempPrice * TempQuantity) * (TempGST / 100d));
					TempSubtotal = (TempPrice * TempQuantity) + tax;

					boolean updateData = DBlocal.UpdateQuantity(TempQuantity, TempSubtotal, Integer.parseInt(String.valueOf(index.get(position))));

					if (updateData) {
						item.remove(position);
						price.remove(position);
						quantity.remove(position);
						subtotal.remove(position);

						item.add(position, TempItem);
						price.add(position, convertScientificToNormal(TempPrice));
						quantity.add(position, TempQuantity);
						subtotal.add(position, convertScientificToNormal(TempSubtotal));

						MyAdapter.this.notifyDataSetChanged();
					} else {
						Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		holder.Delete.setOnClickListener(v -> {
			Toast.makeText(context, "Long press on delete button to remove", Toast.LENGTH_SHORT).show();
		});

		holder.Delete.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				DBlocal = new DBManager(context);
				boolean removeItem = DBlocal.RemoveItem(String.valueOf(index.get(position)));
				if (!removeItem) {
					Toast.makeText(context, "Failed to Removed", Toast.LENGTH_SHORT).show();
					return false;
				} else {
					Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
					item.remove(position);
					price.remove(position);
					quantity.remove(position);
					subtotal.remove(position);
					MyAdapter.this.notifyDataSetChanged();
					return true;

				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return subtotal.size();
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {

		TextView item, price, quantity, subtotal, Delete, gst;

		LinearLayout gstLayout;

		ImageView AddBtn, RemoveBtn;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);
			gstLayout = itemView.findViewById(R.id.linearLayoutGSTLAYOUT);

			item = itemView.findViewById(R.id.textitem);
			price = itemView.findViewById(R.id.textprice);
			quantity = itemView.findViewById(R.id.textquantity);
			subtotal = itemView.findViewById(R.id.textsubtotal);
			Delete = itemView.findViewById(R.id.Delete);
			RemoveBtn = itemView.findViewById(R.id.removeQuantityBtn);
			AddBtn = itemView.findViewById(R.id.addQuantityBtn);
			gst = itemView.findViewById(R.id.gstNumber);
		}
	}



	public static String convertScientificToNormal(double scientificNotation) {
		BigDecimal bd = new BigDecimal(scientificNotation);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}

}
