package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class stock_control extends AppCompatActivity {

	//    Shared preference to Get User name
	private static final String SHARED_PREFS = "sharedPrefs";
	private final DBManager dbManager = new DBManager(this);

	private final InputFilter filter = (source, start, end, dest, dstart, dend) -> {
		String blockCharacterSet = " (){}[]:;'//.,-<>?+₹`@~#^|$%&*!=";
		if (source != null && blockCharacterSet.contains(("" + source))) {
			return "";
		}
		return null;
	};

	@SuppressLint({"Range", "ClickableViewAccessibility"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stock_control);

		//        Removing Suport bar / top line containing name =========================================
		Objects.requireNonNull(getSupportActionBar()).hide();
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//==================================================================================================

//        Getting seller email from shared preference
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String sellertxt = sharedPreferences.getString("UserName", "");

//        gettting seller email
		String seller;
		Bundle name = getIntent().getExtras();
		assert name != null;
		seller = name.getString("Email");
		EditText GSTEdt = findViewById(R.id.GSTPersentage);

//        GST filed visibility
		boolean GST_availability = false;

		TextInputLayout gst = findViewById(R.id.layoutitemGST);
		if (dbManager.CheckGstAvailability(seller)) {
			GST_availability = true;
			gst.setVisibility(View.VISIBLE);
		} else gst.setVisibility(View.GONE);

// autocomplete textview for product name ==========================================================

		// for product Name Autocomplete textview --------------------------------------------------------
		AutoCompleteTextView itemName = findViewById(R.id.itemNameedt);
		itemName.setFilters(new InputFilter[]{filter}); // Adding Filter

		String[] products;
		Cursor productsC = dbManager.GetInventory(sellertxt);
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

		// for category name autocomplete TV -------------------------------------------------------------
		AutoCompleteTextView catagory = findViewById(R.id.categoryedt);

		String[] NameSuggestion;
		String[] Names;

		Cursor Name_Sugg = dbManager.GetCategory(sellertxt);
		Name_Sugg.moveToFirst();
		if (Name_Sugg.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			NameSuggestion = new String[Name_Sugg.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (NameSuggestion[j].equals(Name_Sugg.getString(Name_Sugg.getColumnIndex("category")))) {
							insert = false;
							break;
						} else {
							insert = true;
						}
					}
				}
				if (insert) {
					NameSuggestion[i] = Name_Sugg.getString(Name_Sugg.getColumnIndex("category"));
					i++;
				}
			} while (Name_Sugg.moveToNext());

			Names = new String[i];
			System.arraycopy(NameSuggestion, 0, Names, 0, i);
		} else {
			Names = new String[]{"No Data"};
		}
		catagory.setAdapter(new ArrayAdapter<>(stock_control.this, android.R.layout.simple_list_item_1, Names));
//==================================================================================================

		EditText porchesPriceEdt = findViewById(R.id.porchesPriceEdt);
		EditText sellPrice = findViewById(R.id.sellPrice);

//        Generating and formating Date ------------------------------------------------------------
		Date c = Calendar.getInstance().getTime();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String formattedDate = df.format(c);

		TextInputEditText PurchesDate = findViewById(R.id.PurchesDate);

		// this to make sure that the keybord never opens
		PurchesDate.setClickable(false);
		PurchesDate.setFocusable(false);
		PurchesDate.setOnEditorActionListener((v, actionId, event) -> false);
		PurchesDate.setFocusableInTouchMode(false);
		PurchesDate.setText(formattedDate);

		// this is here to open date picker on focus also close keybord
		PurchesDate.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				Toast.makeText(this, "Select date from the calender", Toast.LENGTH_LONG).show();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				PurchesDate.performClick();
			}
		});

		// this is to open date picker on click also close the keybord
		PurchesDate.setOnClickListener(v -> {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			final Calendar c1 = Calendar.getInstance();

			// on below line we are getting
			// our day, month and year.
			int year = c1.get(Calendar.YEAR);
			int month = c1.get(Calendar.MONTH);
			int day = c1.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog datePickerDialog = new DatePickerDialog(stock_control.this, new DatePickerDialog.OnDateSetListener() {
				@SuppressLint("SetTextI18n")
				@Override
				public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
					PurchesDate.setText(" ");
					PurchesDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
				}
			}, year, month, day);
			datePickerDialog.show();
		});

//  ================================================================================================
		EditText quantity = findViewById(R.id.quantity);

		// add stock btn =================================================================================
		Button AItem = findViewById(R.id.AItem);

		boolean finalGST_availability = GST_availability;
		AItem.setOnClickListener(v -> {
			String nametxt, catagorytxt, pPricetxt, sPricetxt, qtytxt, datetxt, gstPersentage;
			gstPersentage = GSTEdt.getText().toString();
			if (itemName.getText().toString().trim().isEmpty() && catagory.getText().toString().trim().isEmpty() &&
											porchesPriceEdt.getText().toString().trim().isEmpty() && sellPrice.getText().toString().trim().isEmpty() && Objects.requireNonNull(PurchesDate.getText()).toString().trim().isEmpty() && quantity.getText().toString().trim().isEmpty()) {
				Toast.makeText(stock_control.this, "Fill Above Detail add Inventory", Toast.LENGTH_SHORT).show();
			} else if (itemName.getText().toString().trim().isEmpty()) {
				itemName.setError("Enter Name of your Product");
			} else if (catagory.getText().toString().trim().isEmpty()) {
				catagory.setError("Enter Name of your Product Category");
			} else if (porchesPriceEdt.getText().toString().trim().isEmpty()) {
				porchesPriceEdt.setError("Enter Buying Price [Price you pay to get this product]");
			} else if (sellPrice.getText().toString().trim().isEmpty()) {
				sellPrice.setError("Enter Price you want to sell this product on ");
			} else if (Objects.requireNonNull(PurchesDate.getText()).toString().trim().isEmpty()) {
				PurchesDate.setError("Enter date of your porches");
			} else if (quantity.getText().toString().trim().isEmpty()) {
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
				boolean insert = dbManager.AddStock(nametxt, catagorytxt, pPricetxt, sPricetxt, datetxt, qtytxt, seller, gstPersentage);
				if (insert) {
					Toast.makeText(stock_control.this, "Product Added", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(stock_control.this, "Error while adding Product", Toast.LENGTH_SHORT).show();
				}

			}
		});
//==================================================================================================

		//        View Stock Button ======================================================================
		Button view = findViewById(R.id.VStock);

		view.setOnClickListener(v -> {
			Cursor data = dbManager.ViewStock(seller);

			if (data.getCount() > 0) {
				StringBuilder buffer = new StringBuilder();
				while (data.moveToNext()) {
//                    DATE | name | number | Total |
					buffer.append("product name = ").append(data.getString(data.getColumnIndex("productName"))).append("\n");
					buffer.append("Price = ").append(data.getString(data.getColumnIndex("price"))).append("\n");
					buffer.append("Quantity = ").append(data.getString(data.getColumnIndex("quantity"))).append("\n\n");
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(stock_control.this);
				builder.setCancelable(true);
				builder.setTitle("Stock");
				builder.setMessage(buffer.toString());
				builder.show();
			} else {
				Toast.makeText(stock_control.this, "No data available", Toast.LENGTH_SHORT).show();
			}
		});
	}
}