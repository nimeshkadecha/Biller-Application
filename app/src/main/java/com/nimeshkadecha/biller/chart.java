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

	PieChart pieChart;
	DBManager local_db = new DBManager(this);
	private ArrayList<String> ainput, aquantity, Ar, Ag, Ab, ANAME;
	private adapter_stockQuantity S_Q_adapter;
	private adapter_stockList S_L_adapter;

	@SuppressLint("Range")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart);

//        Google ads code --------------------------------------------------------------------------
//        AdView mAdView;
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
//  ================================================================================================

//        Getting intent data
		Bundle bundle = getIntent().getExtras();
		String Seller_email = bundle.getString("seller");
		String intentt = bundle.getString("intent");

		//        Removing Support bar / top line containing name--------------------------------------------
		Objects.requireNonNull(getSupportActionBar()).hide();

//        using switch to switch between quantity chart and if we add another in future
		switch (intentt) {
			case "qty_chart":
				ainput = new ArrayList<>();
				aquantity = new ArrayList<>();

				Ar = new ArrayList<>();
				Ag = new ArrayList<>();
				Ab = new ArrayList<>();
				ANAME = new ArrayList<>();

				RecyclerView qty_rec = findViewById(R.id.QTY_REC);
				S_Q_adapter = new adapter_stockQuantity(chart.this, ainput, aquantity);
				qty_rec.setAdapter(S_Q_adapter);

				qty_rec.setLayoutManager(new LinearLayoutManager(chart.this));

				RecyclerView Stock_list = findViewById(R.id.list_REC);
				S_L_adapter = new adapter_stockList(chart.this, Ar, Ag, Ab, ANAME);
				Stock_list.setAdapter(S_L_adapter);
				Stock_list.setLayoutManager(new LinearLayoutManager(chart.this));

				Cursor get_Qty = local_db.GetInventory(Seller_email);

				get_Qty.moveToFirst();
				Random rnd = new Random();
				pieChart = findViewById(R.id.piechart);

				do {
					int r = rnd.nextInt(256);
					int g = rnd.nextInt(256);
					int b = rnd.nextInt(256);

					ainput.add(get_Qty.getString(get_Qty.getColumnIndex("productName")));
					aquantity.add(get_Qty.getString(get_Qty.getColumnIndex("quantity")));

					Ar.add(String.valueOf(r));
					Ag.add(String.valueOf(g));
					Ab.add(String.valueOf(b));
					ANAME.add(get_Qty.getString(get_Qty.getColumnIndex("productName")));

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


//    @Override
//    protected void onStart() {
//        super.onStart();
//        //        Google ads code --------------------------------------------------------------------------
//        AdView mAdView;
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
////  ================================================================================================
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
////        Google ads code --------------------------------------------------------------------------
//        AdView mAdView;
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
////  ================================================================================================
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        Google ads code --------------------------------------------------------------------------
//        AdView mAdView;
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
////  ================================================================================================
//    }

}