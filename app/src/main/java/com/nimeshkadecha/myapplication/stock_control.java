package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class stock_control extends AppCompatActivity {

	//    Shared preference to Get User name
	public static final String SHARED_PREFS = "sharedPrefs";
	DBManager DB = new DBManager(this);
	private ImageView menu;
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
		setContentView(R.layout.stock_control);

//        Google ads code --------------------------------------------------------------------------
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//  ================================================================================================

//        Getting seller email from shared preference
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String sellertxt = sharedPreferences.getString("UserName", "");

		//        FINDING menu
		menu = findViewById(R.id.Menu);
		menu.setVisibility(View.INVISIBLE);
		//        Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

//        gettting seller email
		String seller;
		Bundle name = getIntent().getExtras();
		seller = name.getString("Email");
		EditText GSTEdt = findViewById(R.id.GSTPersentage);

//        GST filed visibility =====================================================================
		Boolean GST_availability = false;

		TextInputLayout gst = findViewById(R.id.layoutitemGST);
		if (DB.CheckGstAvailability(seller)) {
			GST_availability = true;
			gst.setVisibility(View.VISIBLE);

		} else {
			gst.setVisibility(View.GONE);
		}

//        Finding edittext product name
		AutoCompleteTextView itemName = findViewById(R.id.itemNameedt);

		itemName.setFilters(new InputFilter[]{filter}); // Adding Filter
//        Adding Suggestion [Autocomplete textview]
		String[] products;

		Cursor productsC = DB.GetInventory(sellertxt);

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

		itemName.setAdapter(new ArrayAdapter<>(stock_control.this, android.R.layout.simple_list_item_1, products));

		AutoCompleteTextView catagory = findViewById(R.id.categoryedt);

		//        Adding Suggestion [Autocomplete textview]
		String[] NameSuggestion;
		String[] Names;

		Cursor Name_Sugg = DB.GetCategory(sellertxt);
		Name_Sugg.moveToFirst();
		if (Name_Sugg.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			NameSuggestion = new String[Name_Sugg.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (NameSuggestion[j].equals(Name_Sugg.getString(2))) {
							insert = false;
							break;
						} else {
							insert = true;
						}
					}
				}
				if (insert) {
					NameSuggestion[i] = Name_Sugg.getString(2);
					i++;
				}
			} while (Name_Sugg.moveToNext());

			Names = new String[i];
			System.arraycopy(NameSuggestion, 0, Names, 0, i);
		} else {
			Names = new String[]{"No Data"};
		}

		catagory.setAdapter(new ArrayAdapter<>(stock_control.this, android.R.layout.simple_list_item_1, Names));


		EditText porchesPriceEdt = findViewById(R.id.porchesPriceEdt);
		EditText sellPrice = findViewById(R.id.sellPrice);

		EditText PurchesDate = findViewById(R.id.PurchesDate);
//        Generating and formating Date ------------------------------------------------------------
		Date c = Calendar.getInstance().getTime();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String formattedDate = df.format(c);

		PurchesDate.setText(formattedDate);

		PurchesDate.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				final Calendar c = Calendar.getInstance();

				// on below line we are getting
				// our day, month and year.
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog datePickerDialog = new DatePickerDialog(stock_control.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
						PurchesDate.setText(" ");
						PurchesDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
					}
				}, year, month, day);
				datePickerDialog.show();
				return true;
			}
		});

//  ------------------------------------------------------------------------------------------------
		EditText quantity = findViewById(R.id.quantity);

		Button AItem = findViewById(R.id.AItem);

		Boolean finalGST_availability = GST_availability;
		AItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String nametxt, catagorytxt, pPricetxt, sPricetxt, qtytxt, datetxt, gstPersentage;
				gstPersentage = GSTEdt.getText().toString();
				if (itemName.getText().toString().trim().equals("") && catagory.getText().toString().trim().equals("") &&
												porchesPriceEdt.getText().toString().trim().equals("") && sellPrice.getText().toString().trim().equals("") && PurchesDate.getText().toString().trim().equals("") && quantity.getText().toString().trim().equals("")) {
					Toast.makeText(stock_control.this, "Fill Above Detail add Inventory", Toast.LENGTH_SHORT).show();
				} else if (itemName.getText().toString().trim().equals("")) {
					itemName.setError("Enter Name of your Product");
				} else if (catagory.getText().toString().trim().equals("")) {
					catagory.setError("Enter Name of your Product Category");
				} else if (porchesPriceEdt.getText().toString().trim().equals("")) {
					porchesPriceEdt.setError("Enter Buying Price [Price you pay to get this product]");
				} else if (sellPrice.getText().toString().trim().equals("")) {
					sellPrice.setError("Enter Price you want to sell this product on ");
				} else if (PurchesDate.getText().toString().trim().equals("")) {
					PurchesDate.setError("Enter date of your porches");
				} else if (quantity.getText().toString().trim().equals("")) {
					quantity.setError("Enter number of your Product you have");
				} else if (finalGST_availability && gstPersentage.isEmpty()) {
					Toast.makeText(stock_control.this, "GST filed is empty", Toast.LENGTH_SHORT).show();
					GSTEdt.setError("Enter how much gst is applicable");
				} else {

					nametxt = itemName.getText().toString();
					catagorytxt = catagory.getText().toString();
					pPricetxt = porchesPriceEdt.getText().toString();
					sPricetxt = sellPrice.getText().toString();
					qtytxt = quantity.getText().toString();
					datetxt = PurchesDate.getText().toString();
					boolean insert = DB.AddStock(nametxt, catagorytxt, pPricetxt, sPricetxt, datetxt, qtytxt, seller, gstPersentage);
					if (insert) {
						Toast.makeText(stock_control.this, "Product Added", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(stock_control.this, "Error while adding Product", Toast.LENGTH_SHORT).show();
					}

				}
			}
		});


		Button view = findViewById(R.id.VStock);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Cursor data = DB.ViewStock(seller);

				if (data.getCount() > 0) {
					StringBuffer buffer = new StringBuffer();
					while (data.moveToNext()) {
//                    DATE | name | number | Total |
						buffer.append("product name = " + data.getString(data.getColumnIndex("productName")) + "\n");
						buffer.append("Price = " + data.getString(data.getColumnIndex("price")) + "\n");
						buffer.append("Quantity = " + data.getString(data.getColumnIndex("quantity")) + "\n\n");
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(stock_control.this);
					builder.setCancelable(true);
					builder.setTitle("Stock");
					builder.setMessage(buffer.toString());
					builder.show();
				} else {
					Toast.makeText(stock_control.this, "No data available", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

//  AUTO BACKUP ====================================================================================

	//    On pause
	@Override
	protected void onPause() {
		super.onPause();
		final String SHARED_PREFS = "sharedPrefs";
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		DBManager dbManager = new DBManager(getApplicationContext());
		boolean check = dbManager.AutoLocalBackup(getApplicationContext());
		if (check) {
			Date c = Calendar.getInstance().getTime();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			String formattedDate = df.format(c);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("AutoUpload", formattedDate);
			editor.apply();
		}
	}

	// on stop
	@Override
	protected void onStop() {
		super.onStop();
		final String SHARED_PREFS = "sharedPrefs";
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		DBManager dbManager = new DBManager(getApplicationContext());
		boolean check = dbManager.AutoLocalBackup(getApplicationContext());
		if (check) {
			Date c = Calendar.getInstance().getTime();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			String formattedDate = df.format(c);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("AutoUpload", formattedDate);
			editor.apply();
		}
	}

//  ================================================================================================

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