package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class inventory_insights extends AppCompatActivity {

	DBManager dbLocal = new DBManager(this);

	private final String blockCharacterSet = " (){}[]:;'//.,-<>?+₹`@~#^|$%&*!=";

	private final InputFilter filter = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			if (source != null && blockCharacterSet.contains(("" + source))) {
				return "";
			}
			return null;
		}
	};

	@SuppressLint("Range")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inventory_insights);

//        Google ads code --------------------------------------------------------------------------
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//  ================================================================================================

		Bundle bundle = getIntent().getExtras();
		String Seller_email = bundle.getString("seller");

		//        Removing Suport bar / top line containing name--------------------------------------------
		Objects.requireNonNull(getSupportActionBar()).hide();

		Cursor getinfo = dbLocal.GetInventory(Seller_email);

		if (getinfo.getCount() <= 0) {
			Toast.makeText(this, "You don't have any stock available", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "GoTo manage stock to enter Stock", Toast.LENGTH_LONG).show();
			Intent i = new Intent(inventory_insights.this, home.class);
			i.putExtra("Email", Seller_email);
			i.putExtra("Origin", "test");
			startActivity(i);
		}

		//        Finding edittext product name
		AutoCompleteTextView itemName = findViewById(R.id.report_product_name);

		itemName.setFilters(new InputFilter[]{filter}); // Adding Filter
//        Adding Suggestion [Autocomplete textview]
		String[] products;

		Cursor productsC = dbLocal.GetInventory(Seller_email);

		productsC.moveToFirst();
		if (productsC.getCount() > 0) {
			products = new String[productsC.getCount()];
			int i = 0;
			do {
				products[i] = productsC.getString(productsC.getColumnIndex("productName"));
				i++;

			} while (productsC.moveToNext());
		} else {
			products = new String[]{"NO Suggestion Available"};
		}

		itemName.setAdapter(new ArrayAdapter<>(inventory_insights.this, android.R.layout.simple_list_item_1, products));

		AutoCompleteTextView catagory = findViewById(R.id.report_Catagory_name);

		//        Adding Suggestion [Autocomplete textview]
		String[] NameSuggestion;
		String[] Names;

		Cursor categoryNameSuggestionCursor = dbLocal.GetCategory(Seller_email);
		categoryNameSuggestionCursor.moveToFirst();
		if (categoryNameSuggestionCursor.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			NameSuggestion = new String[categoryNameSuggestionCursor.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (NameSuggestion[j].equals(categoryNameSuggestionCursor.getString(categoryNameSuggestionCursor.getColumnIndex("category")))) {
							insert = false;
							break;
						} else {
							insert = true;
						}
					}
				}
				if (insert) {
					NameSuggestion[i] = categoryNameSuggestionCursor.getString(categoryNameSuggestionCursor.getColumnIndex("category"));
					i++;
				}
			} while (categoryNameSuggestionCursor.moveToNext());
			Names = new String[i];
			System.arraycopy(NameSuggestion, 0, Names, 0, i);
		} else {
			Names = new String[]{"No Data"};
		}

		catagory.setAdapter(new ArrayAdapter<>(inventory_insights.this, android.R.layout.simple_list_item_1, Names));


		//        working with switches
		// search switch
		Switch searchSwitch = findViewById(R.id.switchSearch);

		if (searchSwitch.isChecked()) {
			itemName.setText("");
			itemName.setVisibility(View.INVISIBLE);
			catagory.setVisibility(View.VISIBLE);
		} else {
			catagory.setVisibility(View.INVISIBLE);
			itemName.setVisibility(View.VISIBLE);
		}

		searchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					itemName.setText("");
					itemName.setVisibility(View.INVISIBLE);
					catagory.setVisibility(View.VISIBLE);

					catagory.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

				} else {
					catagory.setVisibility(View.INVISIBLE);
					catagory.setText("");
					itemName.setVisibility(View.VISIBLE);

					itemName.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				}
			}
		});

//        Switch record switch
		Switch switchRecords = findViewById(R.id.switchRecord);

		TextView heading = findViewById(R.id.displayName);

		if (switchRecords.isChecked()) {

			heading.setText("Sales Record");
		} else {

			heading.setText("Stock Record");
		}

		switchRecords.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (switchRecords.isChecked()) {
					Toast.makeText(inventory_insights.this, "Sales records search", Toast.LENGTH_SHORT).show();
					heading.setText("Sales Record");
				} else {
					Toast.makeText(inventory_insights.this, "Inventory records search", Toast.LENGTH_SHORT).show();
					heading.setText("Stock Record");
				}
			}
		});

//        notifying user to change switch

		TextInputLayout namelayout = findViewById(R.id.nameLayout_IM);


		namelayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itemName.getVisibility() == View.INVISIBLE) {
					Toast.makeText(inventory_insights.this, "Change switch to assess Category", Toast.LENGTH_SHORT).show();
				}
			}
		});

		TextInputLayout catagoryTextInputLayout = findViewById(R.id.catagoryTextInputLayout);

		catagoryTextInputLayout.setOnClickListener(v -> {
			if (catagory.getVisibility() == View.INVISIBLE) {
				Toast.makeText(inventory_insights.this, "Change switch to assess Product", Toast.LENGTH_SHORT).show();
			}
		});


//        show report button
		Button showReport = findViewById(R.id.showRecord);

		showReport.setOnClickListener(v -> {
			if (!itemName.getText().toString().trim().equals("") || !catagory.getText().toString().trim().equals("")) {
				StringBuffer buffer;

				if (heading.getText().toString().equals("Stock Record")) {
					buffer = new StringBuffer();
					if (!itemName.getText().toString().trim().equals("")) {
						Cursor ReportCursor = dbLocal.ViewProductHistory(Seller_email, itemName.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed '" + itemName.getText().toString() + "' product's stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Product = " + itemName.getText().toString() + "\n\n");
							do {
								buffer.append("Date = " + date_convertor.convertDateFormat(ReportCursor.getString(ReportCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy") + "\n");
								buffer.append("Buy Price = " + ReportCursor.getString(ReportCursor.getColumnIndex("purchasePrice")) + "\n");
								buffer.append("Sell Price = " + ReportCursor.getString(ReportCursor.getColumnIndex("sellingPrice")) + "\n");
								buffer.append("Quantity = " + ReportCursor.getString(ReportCursor.getColumnIndex("quantity")) + "\n");
								buffer.append("Category = " + ReportCursor.getString(ReportCursor.getColumnIndex("category")) + "\n\n");
							} while (ReportCursor.moveToNext());

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Stock Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					} else if (!catagory.getText().toString().equals("")) {
						buffer = new StringBuffer();

						Cursor ReportCursor = dbLocal.ViewCategoryHistory(Seller_email, catagory.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed " + catagory.getText().toString() + " categories stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Category = " + catagory.getText().toString() + "\n\n");
							do {
								buffer.append("Date = " + date_convertor.convertDateFormat(ReportCursor.getString(ReportCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy") + "\n");
								buffer.append("Buy Price = " + ReportCursor.getString(ReportCursor.getColumnIndex("purchasePrice")) + "\n");
								buffer.append("Sell Price = " + ReportCursor.getString(ReportCursor.getColumnIndex("sellingPrice")) + "\n");
								buffer.append("Quantity = " + ReportCursor.getString(ReportCursor.getColumnIndex("quantity")) + "\n");
								buffer.append("Product = " + ReportCursor.getString(ReportCursor.getColumnIndex("productName")) + "\n\n");
							} while (ReportCursor.moveToNext());

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Stock Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					}
				} else if (heading.getText().toString().equals("Sales Record")) {
					buffer = new StringBuffer();
					if (!itemName.getText().toString().trim().equals("")) {
						Cursor ReportCursor = dbLocal.ViewSaleProductHistory(Seller_email, itemName.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed '" + itemName.getText().toString() + "' product's stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Product = " + itemName.getText().toString() + "\n");
							if (ReportCursor.getString(0) == null) {
								buffer.append("\nBased on the information available, it appears that no sales have been recorded as of yet." + "\n\n");
							} else {
								do {
									buffer.append("Total number of products sold = " + ReportCursor.getString(0) + "\n");
									buffer.append("The average price per unit sold = " + ReportCursor.getString(2) + "\n");
									buffer.append("The total revenue generated = " + ReportCursor.getString(1) + "\n\n");
								} while (ReportCursor.moveToNext());
							}

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Sales Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					} else if (!catagory.getText().toString().equals("")) {
						buffer = new StringBuffer();

						Cursor ReportCursor = dbLocal.ViewSaleCategoryHistory(Seller_email, catagory.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed " + catagory.getText().toString() + " categories stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();
							buffer.append("Category = " + catagory.getText().toString() + "\n\n");
							do {
								Cursor productCursor = dbLocal.ViewSaleProductHistory(Seller_email, ReportCursor.getString(0));

								if (productCursor.getCount() <= 0) {
									Toast.makeText(this, "You haven't managed '" + ReportCursor.getString(0) + "' product's stocks.", Toast.LENGTH_SHORT).show();
								} else {
									productCursor.moveToFirst();
									buffer.append("Product = " + ReportCursor.getString(0) + "\n");
									if (productCursor.getString(0) == null) {
										buffer.append("Based on the information available, it appears that no sales have been recorded as of yet." + "\n\n");
									} else {
										do {
											buffer.append("Total number of products sold = " + productCursor.getString(0) + "\n");
											buffer.append("The average price per unit sold = " + productCursor.getString(2) + "\n");
											buffer.append("The total revenue generated = " + productCursor.getString(1) + "\n\n");
										} while (productCursor.moveToNext());
									}
								}
							} while (ReportCursor.moveToNext());

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Slase Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					}
				} else {
					Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "Fill Any one of the above filed", Toast.LENGTH_SHORT).show();
			}


		});


//Chart Button
		Button qty_chart = findViewById(R.id.QTY_Chart_BTN);
		qty_chart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent gotoReport = new Intent(inventory_insights.this, chart.class);
				gotoReport.putExtra("seller", Seller_email);
				gotoReport.putExtra("intent", "qty_chart");

				startActivity(gotoReport);
			}
		});

	}

//
//	@Override
//	protected void onStart() {
//		super.onStart();
//		//        Google ads code --------------------------------------------------------------------------
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
////  ================================================================================================
//	}
//
//	@Override
//	protected void onRestart() {
//		super.onRestart();
////        Google ads code --------------------------------------------------------------------------
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
////  ================================================================================================
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
////        Google ads code --------------------------------------------------------------------------
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
////  ================================================================================================
//	}

}