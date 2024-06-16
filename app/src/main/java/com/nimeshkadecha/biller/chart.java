package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class chart extends AppCompatActivity {

	private final DBManager local_db = new DBManager(this);

	@SuppressLint("Range")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart);

//        Getting intent data
		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		String Seller_email = bundle.getString("seller");
		String intent = bundle.getString("intent");

		//        Removing Support bar / top line containing name--------------------------------------------
		Objects.requireNonNull(getSupportActionBar()).hide();

//        using switch to switch between quantity chart and if we add another in future
		switch (Objects.requireNonNull(intent)) {
			case "qty_chart":
				ArrayList<String> a_input = new ArrayList<>();
				ArrayList<String> a_quantity = new ArrayList<>();
				ArrayList<String> a_r = new ArrayList<>();
				ArrayList<String> a_g = new ArrayList<>();
				ArrayList<String> a_b = new ArrayList<>();
				ArrayList<String> a_name = new ArrayList<>();

				RecyclerView qty_rec = findViewById(R.id.QTY_REC);
				adapter_stockQuantity s_Q_adapter = new adapter_stockQuantity(chart.this, a_input, a_quantity);
				qty_rec.setAdapter(s_Q_adapter);

				qty_rec.setLayoutManager(new LinearLayoutManager(chart.this));

				RecyclerView Stock_list = findViewById(R.id.list_REC);
				adapter_stockList s_L_adapter = new adapter_stockList(chart.this, a_r, a_g, a_b, a_name);
				Stock_list.setAdapter(s_L_adapter);
				Stock_list.setLayoutManager(new LinearLayoutManager(chart.this));

				Cursor get_Qty = local_db.GetInventory(Seller_email);

				get_Qty.moveToFirst();
				Random rnd = new Random();
				PieChart pieChart = findViewById(R.id.piechart);

				do {
					int r = rnd.nextInt(256);
					int g = rnd.nextInt(256);
					int b = rnd.nextInt(256);

					a_input.add(get_Qty.getString(get_Qty.getColumnIndex("productName")));
					a_quantity.add(get_Qty.getString(get_Qty.getColumnIndex("quantity")));

					a_r.add(String.valueOf(r));
					a_g.add(String.valueOf(g));
					a_b.add(String.valueOf(b));
					a_name.add(get_Qty.getString(get_Qty.getColumnIndex("productName")));

					int qty = Integer.parseInt(get_Qty.getString(get_Qty.getColumnIndex("quantity")));
					if (qty < 0) {
						qty *= -1;
					}
					pieChart.addPieSlice(
													new PieModel(
																					get_Qty.getString(get_Qty.getColumnIndex("productName")),
																					qty,
																					Color.argb(255, r, g, b)));
				} while (get_Qty.moveToNext());
				break;

			default:
				Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
				Intent goTOReport = new Intent(chart.this, inventory_insights.class);
				goTOReport.putExtra("seller", Seller_email);
				startActivity(goTOReport);
				break;
		}
	}
}