package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
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

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private ArrayList item, price, quantity, subtotal, index,GST;

    DBManager DBlocal ;

    public MyAdapter(Context context, ArrayList item, ArrayList price, ArrayList quantity, ArrayList subtotal, ArrayList index,ArrayList GST) {

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
        if(GST.get(position).equals("0")){
            holder.gstLayout.setVisibility(View.GONE);
        }else{
            checkGST = true;
            holder.gstLayout.setVisibility(View.VISIBLE);
        }

        holder.item.setText(String.valueOf(item.get(position)));
        holder.price.setText(String.valueOf(price.get(position)));
        holder.quantity.setText(String.valueOf(quantity.get(position)));
        holder.subtotal.setText(String.valueOf(subtotal.get(position)));
        if(checkGST){
            float tax = ((Integer.parseInt(String.valueOf(price.get(position))) * Integer.parseInt(String.valueOf(quantity.get(position)))) * (Integer.parseInt(String.valueOf(GST.get(position))) / 100f));
             holder.gst.setText(String.valueOf(tax));
        }
        holder.AddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBlocal = new DBManager(context);
                String TempItem = String.valueOf(item.get(position));
                int TempPrice = Integer.parseInt(String.valueOf(price.get(position)));
                int TempQuantity = Integer.parseInt(String.valueOf(quantity.get(position)));
                int TempGST = Integer.parseInt(String.valueOf(GST.get(position)));
                float TempSubtotal = 1f;
                TempQuantity += 1;
                float tax = ((TempPrice * TempQuantity) * (TempGST / 100f));
                TempSubtotal = (TempPrice * TempQuantity) + tax;

                boolean updateData = DBlocal.updateQuentity(TempQuantity,TempSubtotal,Integer.parseInt(String.valueOf(index.get(position))));

                if(updateData){
                    item.remove(position);
                    price.remove(position);
                    quantity.remove(position);
                    subtotal.remove(position);

                    item.add(position,TempItem);
                    price.add(position,TempPrice);
                    quantity.add(position,TempQuantity);
                    subtotal.add(position,TempSubtotal);

                    MyAdapter.this.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.RemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(String.valueOf(quantity.get(position)).equals("1")){
                    holder.RemoveBtn.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            DBlocal = new DBManager(context);
                            Cursor b = DBlocal.removeItem(String.valueOf(index.get(position)));
                            if(b.getCount()<0){
                                Toast.makeText(context, "Failed to Removed", Toast.LENGTH_SHORT).show();
                                return false;
                            }else{
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
                }else{
                    DBlocal = new DBManager(context);
                    String TempItem = String.valueOf(item.get(position));
                    int TempPrice = Integer.parseInt(String.valueOf(price.get(position)));
                    int TempQuantity = Integer.parseInt(String.valueOf(quantity.get(position)));
                    int TempGST = Integer.parseInt(String.valueOf(GST.get(position)));

                    float TempSubtotal = 1;
                    TempQuantity -= 1;
                    float tax = ((TempPrice * TempQuantity) * (TempGST / 100f));
                    TempSubtotal = (TempPrice * TempQuantity) + tax;

                    boolean updateData = DBlocal.updateQuentity(TempQuantity,TempSubtotal,Integer.parseInt(String.valueOf(index.get(position))));

                    if(updateData) {
                        item.remove(position);
                        price.remove(position);
                        quantity.remove(position);
                        subtotal.remove(position);

                        item.add(position, TempItem);
                        price.add(position, TempPrice);
                        quantity.add(position, TempQuantity);
                        subtotal.add(position, TempSubtotal);

                        MyAdapter.this.notifyDataSetChanged();
                    }else{
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        holder.Delete.setOnClickListener(v-> {
            Toast.makeText(context, "Long press on delete button to remove", Toast.LENGTH_SHORT).show();
        });

        holder.Delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DBlocal = new DBManager(context);
                Cursor b = DBlocal.removeItem(String.valueOf(index.get(position)));
                if(b.getCount()<0){
                    Toast.makeText(context, "Failed to Removed", Toast.LENGTH_SHORT).show();
                    return false;
                }else{
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

        TextView item, price, quantity, subtotal, Delete,gst;

        LinearLayout gstLayout;

        ImageView AddBtn,RemoveBtn;

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
}
