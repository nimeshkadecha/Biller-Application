package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class add_product extends AppCompatActivity {

	private static final double MAX_REAL_VALUE = Double.MAX_VALUE;

	private final DBManager dbManager = new DBManager(this);
	//    In input Filter =============================================================================
	private final InputFilter filter = (source, start, end, dest, d_start, d_end) -> {
		String blockCharacterSet = " =(){}[]:;'//.,-<>?+₹`@~#^|$%&*!";
		if (source != null && blockCharacterSet.contains(("" + source))) return "";

		return null;
	};
	private Button show;
	private String cName_txt, cNumber_txt, date_text, seller_txt, origin_txt;
	private int billId_txt;
	private EditText price, quantity, gstPercentageEDT;
	private AutoCompleteTextView productName;
	private View rootView;

	// To convert really big number to human readable format ==========================================
	public static String convertScientificToNormal(double scientificNotation) {
		BigDecimal bd = new BigDecimal(scientificNotation);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}
// =================================================================================================

	@SuppressLint({"MissingInflatedId", "Range"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_product);
		rootView = findViewById(R.id.add_product_layout);

		show = findViewById(R.id.show); // its a button to navigate to showList page

// Working with TOOLBAR STARTS =====================================================================
		//        Removing Support bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
		// hiding navigation drawer
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE); // remove visibility
// =================================================================================================

//  INSERT OPERATION IN DISPLAY TABLE ==============================================================

		// Getting INTENT data ---------------------------------------------------------------------------
		Bundle bundle = getIntent().getExtras();
		cName_txt = Objects.requireNonNull(bundle).getString("cName");
		cNumber_txt = bundle.getString("cNumber");
		date_text = bundle.getString("date");
		billId_txt = bundle.getInt("billId");
		seller_txt = bundle.getString("seller");
		origin_txt = bundle.getString("origin");

		//  check weather gst is available or not ---------------------------------------------------------
		TextInputLayout gst = findViewById(R.id.layoutitemGST);
		boolean needGST;
		if (dbManager.CheckGstAvailability(seller_txt)) {
			gst.setVisibility(View.VISIBLE);
			needGST = true;
		} else {
			needGST = false;
			gst.setVisibility(View.GONE);
		}
		// -----------------------------------------------------------------------------------------------

		final int[] validator = {0}; //  to change visibility of show button and add button

		quantity = findViewById(R.id.quantity);
		if (quantity.getText().toString().isEmpty()) quantity.setText("1");

		productName = findViewById(R.id.productname);
		productName.setFilters(new InputFilter[]{filter}); // Adding Filter

		// Adding Suggestion [Autocomplete textview] ----------------------------------------------
		String[] products;
		Cursor productsC = dbManager.GetInventory(seller_txt);
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
		// -----------------------------------------------------------------------------------------------

		// working with automate price and gst -----------------------------------------------------------

		price = findViewById(R.id.price);
		gstPercentageEDT = findViewById(R.id.GstPersentage);

		// set adapter(suggestion) in product names
		productName.setAdapter(new ArrayAdapter<>(add_product.this, android.R.layout.simple_list_item_1, products));

		// adding price is we have that product
		productName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) addPrice(needGST, rootView);
		});

		// adding price if we have that product (in case focus is changed and we have that product)
		price.setOnClickListener(v -> addPrice(needGST, rootView));
		// -----------------------------------------------------------------------------------------------

		// Add item button -------------------------------------------------------------------------------
		// this will insert data in to display table or update the quantity in display table

		Button add = findViewById(R.id.button3);

		add.setOnClickListener(v -> {
			String productName_ST, price_ST, quantity_ST, GstPersentageString;
			productName_ST = productName.getText().toString();
			price_ST = price.getText().toString();
			quantity_ST = this.quantity.getText().toString();
			GstPersentageString = gstPercentageEDT.getText().toString();

			// validating user inputs -----------------------------------------------------------------------
			if (productName_ST.isEmpty() || price_ST.isEmpty() || quantity_ST.isEmpty()) {
				if (productName_ST.isEmpty() && price_ST.isEmpty() && quantity_ST.isEmpty()) {
					productName.setError("Enter Item Name here");
					price.setError("Enter Item Price here");
					this.quantity.setError("Enter Quantity here");
					Snackbar.make(rootView, "Entry details", Snackbar.LENGTH_SHORT).show();
				} else if (productName_ST.isEmpty()) {
					productName.setError("Enter Item Name here");
					Snackbar.make(rootView, "Product filed is Empty", Snackbar.LENGTH_SHORT).show();
				} else if (price_ST.isEmpty()) {
					price.setError("Enter Item Price here");
					Snackbar.make(rootView, "Price filed is Empty", Snackbar.LENGTH_SHORT).show();
				} else {
					this.quantity.setError("Enter Quantity here");
					Snackbar.make(rootView, "Quantity filed is Empty", Snackbar.LENGTH_SHORT).show();
				}
			}
			// ----------------------------------------------------------------------------------------------
			else {
				// verifying that we have intent data ----------------------------------------------------------
				if (cName_txt.isEmpty() || cNumber_txt.isEmpty() || date_text.isEmpty()) {
					Snackbar.make(rootView, "Empty intent", Snackbar.LENGTH_SHORT).show();
				} else if (needGST && GstPersentageString.isEmpty()) {
					Snackbar.make(rootView, "GST filed is empty", Snackbar.LENGTH_SHORT).show();
					gstPercentageEDT.setError("Enter how much gst is applicable enter 0 if there is not any");
				}
				// ---------------------------------------------------------------------------------------------
				else {
					// making sure that the user input is not bigger than the range of Real in database
					if (Float.parseFloat(price_ST) * Float.parseFloat(quantity_ST) < MAX_REAL_VALUE) {
						// calling insert function
						boolean check = dbManager.InsertList(productName_ST, price_ST, quantity_ST, cName_txt, cNumber_txt, date_text, billId_txt, seller_txt, 0, GstPersentageString);
						if (check) {
							validator[0]++;
							show.setVisibility(View.VISIBLE);
							Snackbar.make(Objects.requireNonNull(rootView), "Inserted", Snackbar.LENGTH_SHORT).show();
						} else
							Snackbar.make(rootView, "Not Inserted", Snackbar.LENGTH_SHORT).show();
					} else {
						Snackbar.make(rootView, "This number is too big Android can't handle this big number !", Snackbar.LENGTH_SHORT).show();
					}
				}
			}
		});
		// -----------------------------------------------------------------------------------------------

		// if screen is visible after show list page is displayed then making show button visible and if it's after home then making it invisible
		if (validator[0] == 0) show.setVisibility(View.GONE);

		// hide it(show button) if we came here from home
		if (!origin_txt.equalsIgnoreCase("home")) show.setVisibility(View.VISIBLE);
// =================================================================================================

// Show List Button ================================================================================
		// this will take to show list page
		show.setOnClickListener(v -> {
			Intent intent2 = new Intent(add_product.this, show_list.class);
			//passing customer information and billId
			intent2.putExtra("seller", seller_txt);
			intent2.putExtra("cName", cName_txt);
			intent2.putExtra("cNumber", cNumber_txt);
			intent2.putExtra("date", date_text);
			intent2.putExtra("billId", billId_txt);

			startActivity(intent2);
			finish();
		});
// =================================================================================================

	} // OnCreate Ends ================================================================================
// =================================================================================================

	// adding Price and GST from database if we have it ===============================================
	@SuppressLint("Range")
	public void addPrice(boolean needGST, View v) {
		if (price.getText().toString().isEmpty() && !productName.getText().toString().isEmpty()) {
			Cursor getPrice = dbManager.GetProductQuantity(productName.getText().toString(), seller_txt);
			getPrice.moveToFirst();
			if (getPrice.getCount() > 0) {
				price.setText(String.valueOf(convertScientificToNormal(getPrice.getDouble(getPrice.getColumnIndex("price")))));
				if (needGST) {
					gstPercentageEDT.setText(String.valueOf(convertScientificToNormal(getPrice.getDouble(getPrice.getColumnIndex("Gst")))));
				}
				Snackbar.make(v, "Added", Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make(rootView, "Can't find", Snackbar.LENGTH_SHORT).show();
			}
		}
	}
	// ================================================================================================

	//    Going TO Home With User DATA ON Back Button Press ===========================================
	//    if user click back then goto home with all the data
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (!origin_txt.equalsIgnoreCase("home")) {
			// if origin is not home then we are here from show list so first user must save the bill
			Snackbar.make(Objects.requireNonNull(this.getCurrentFocus()), "Please save the bill before exiting", Snackbar.LENGTH_SHORT).show();
		} else {
			Intent intent2 = new Intent(add_product.this, home.class);
			intent2.putExtra("Email", seller_txt);
			intent2.putExtra("Origin", "addItem");
			intent2.putExtra("cName", cName_txt);
			intent2.putExtra("cNumber", cNumber_txt);
			intent2.putExtra("date", date_text);
			intent2.putExtra("billId", billId_txt);

			startActivity(intent2);
			finish();
		}
	}
	// ================================================================================================


}