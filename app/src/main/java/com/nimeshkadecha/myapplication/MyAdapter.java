package com.nimeshkadecha.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private ArrayList item, price, quantity, subtotal, index;

    DBManager DBlocal ;

    public MyAdapter(Context context, ArrayList item, ArrayList price, ArrayList quantity, ArrayList subtotal, ArrayList index) {

        this.context = context;
        this.price = price;
        this.item = item;
        this.index = index;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.userentry, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        holder.index.setText(String.valueOf(index.get(position)));
        holder.item.setText(String.valueOf(item.get(position)));
        holder.price.setText(String.valueOf(price.get(position)));
        holder.quantity.setText(String.valueOf(quantity.get(position)));
        holder.subtotal.setText(String.valueOf(subtotal.get(position)));
        holder.Delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DBlocal = new DBManager(context);
                Cursor b = DBlocal.removeItem(String.valueOf(index.get(position)));
                if(b.getCount()<0){
                    Toast.makeText(context, "Failed to Removed", Toast.LENGTH_SHORT).show();
                    return false;
                }else{
                    Log.d("ENimesh", "onLongClick: POSITION = "+position);
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

        TextView item, price, quantity, subtotal, Delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            index = itemView.findViewById(R.id.textindex);
            item = itemView.findViewById(R.id.textitem);
            price = itemView.findViewById(R.id.textprice);
            quantity = itemView.findViewById(R.id.textquantity);
            subtotal = itemView.findViewById(R.id.textsubtotal);
            Delete = itemView.findViewById(R.id.Delete);
        }
    }
}
