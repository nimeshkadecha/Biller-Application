package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
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

	private final DBManager dbManager = new DBManager(this);
	// inputfilter to remove special characters =======================================================
	private final InputFilter filter = (source, start, end, dest, dstart, dend) -> {
		String blockCharacterSet = " (){}[]:;'//.,-<>?+₹`@~#^|$%&*!=";
		if (source != null && blockCharacterSet.contains(("" + source))) {
			return "";
		}
		return null;
	};
// =================================================================================================

	@SuppressLint({"Range", "SetTextI18n"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inventory_insights);

		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		String Seller_email = bundle.getString("seller");

		//        Hiding Action Bar ======================================================================
		Objects.requireNonNull(getSupportActionBar()).hide();

		// fetching inventory ============================================================================
		Cursor getinfo = dbManager.GetInventory(Seller_email);

		if (getinfo.getCount() <= 0) {
			Toast.makeText(this, "You don't have any stock available", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "GoTo manage stock to enter Stock", Toast.LENGTH_LONG).show();
			Intent i = new Intent(inventory_insights.this, home.class);
			i.putExtra("Email", Seller_email);
			i.putExtra("Origin", "test");
			startActivity(i);
		}
// =================================================================================================

		//        AutoCompleteTextView ===================================================================
		AutoCompleteTextView itemName = findViewById(R.id.report_product_name);

		itemName.setFilters(new InputFilter[]{filter}); // Adding Filter

		String[] products;
		//        Fetching Products ======================================================================
		Cursor productsC = dbManager.GetInventory(Seller_email);

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
// =================================================================================================


		// autocomplete for category =====================================================================
		AutoCompleteTextView catagory = findViewById(R.id.report_Catagory_name);

		//        Adding Suggestion [Autocomplete textview]
		String[] NameSuggestion;
		String[] Names;

		Cursor categoryNameSuggestionCursor = dbManager.GetCategory(Seller_email);
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
// =================================================================================================


		//        working with switches
		// search switch
		@SuppressLint("UseSwitchCompatOrMaterialCode") Switch searchSwitch = findViewById(R.id.switchSearch);

		if (searchSwitch.isChecked()) {
			itemName.setText("");
			itemName.setVisibility(View.INVISIBLE);
			catagory.setVisibility(View.VISIBLE);
		} else {
			catagory.setVisibility(View.INVISIBLE);
			itemName.setVisibility(View.VISIBLE);
		}

		//        Switch search ==========================================================================
		searchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
		});
// =================================================================================================

// Switch record switch ============================================================================
		@SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchRecords = findViewById(R.id.switchRecord);

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
// =================================================================================================

// notifying user to change switch =================================================================
		TextInputLayout namelayout = findViewById(R.id.nameLayout_IM);
		namelayout.setOnClickListener(v -> {
			if (itemName.getVisibility() == View.INVISIBLE)
				Toast.makeText(inventory_insights.this, "Change switch to assess Category", Toast.LENGTH_SHORT).show();
		});

		TextInputLayout catagoryTextInputLayout = findViewById(R.id.catagoryTextInputLayout);
		catagoryTextInputLayout.setOnClickListener(v -> {
			if (catagory.getVisibility() == View.INVISIBLE) {
				Toast.makeText(inventory_insights.this, "Change switch to assess Product", Toast.LENGTH_SHORT).show();
			}
		});
// =================================================================================================

// show report button ==============================================================================
		Button showReport = findViewById(R.id.showRecord);

		showReport.setOnClickListener(v -> {
			if (!itemName.getText().toString().trim().isEmpty() || !catagory.getText().toString().trim().isEmpty()) {
				StringBuilder buffer;

				if (heading.getText().toString().equals("Stock Record")) {
					buffer = new StringBuilder();
					if (!itemName.getText().toString().trim().isEmpty()) {
						Cursor ReportCursor = dbManager.ViewProductHistory(Seller_email, itemName.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed '" + itemName.getText().toString() + "' product's stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Product = ").append(itemName.getText().toString()).append("\n\n");
							do {
								buffer.append("Date = ").append(date_convertor.convertDateFormat(ReportCursor.getString(ReportCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy")).append("\n");
								buffer.append("Buy Price = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("purchasePrice"))).append("\n");
								buffer.append("Sell Price = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("sellingPrice"))).append("\n");
								buffer.append("Quantity = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("quantity"))).append("\n");
								buffer.append("Category = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("category"))).append("\n\n");
							} while (ReportCursor.moveToNext());

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Stock Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					} else if (!catagory.getText().toString().isEmpty()) {
						buffer = new StringBuilder();

						Cursor ReportCursor = dbManager.ViewCategoryHistory(Seller_email, catagory.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed " + catagory.getText().toString() + " categories stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Category = ").append(catagory.getText().toString()).append("\n\n");
							do {
								buffer.append("Date = ").append(date_convertor.convertDateFormat(ReportCursor.getString(ReportCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy")).append("\n");
								buffer.append("Buy Price = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("purchasePrice"))).append("\n");
								buffer.append("Sell Price = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("sellingPrice"))).append("\n");
								buffer.append("Quantity = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("quantity"))).append("\n");
								buffer.append("Product = ").append(ReportCursor.getString(ReportCursor.getColumnIndex("productName"))).append("\n\n");
							} while (ReportCursor.moveToNext());

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Stock Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					}
				} else if (heading.getText().toString().equals("Sales Record")) {
					buffer = new StringBuilder();
					if (!itemName.getText().toString().trim().isEmpty()) {
						Cursor ReportCursor = dbManager.ViewSaleProductHistory(Seller_email, itemName.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed '" + itemName.getText().toString() + "' product's stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();

							buffer.append("Product = ").append(itemName.getText().toString()).append("\n");
							if (ReportCursor.getString(0) == null) {
								buffer.append("\nBased on the information available, it appears that no sales have been recorded as of yet." + "\n\n");
							} else {
								do {
									buffer.append("Total number of products sold = ").append(ReportCursor.getString(0)).append("\n");
									buffer.append("The average price per unit sold = ").append(ReportCursor.getString(2)).append("\n");
									buffer.append("The total revenue generated = ").append(ReportCursor.getString(1)).append("\n\n");
								} while (ReportCursor.moveToNext());
							}

							AlertDialog.Builder builder = new AlertDialog.Builder(inventory_insights.this);
							builder.setCancelable(true);
							builder.setTitle("Sales Report");
							builder.setMessage(buffer.toString());
							builder.show();
						}
					} else if (!catagory.getText().toString().isEmpty()) {
						buffer = new StringBuilder();

						Cursor ReportCursor = dbManager.ViewSaleCategoryHistory(Seller_email, catagory.getText().toString());

						if (ReportCursor.getCount() <= 0) {
							Toast.makeText(this, "You haven't managed " + catagory.getText().toString() + " categories stocks.", Toast.LENGTH_SHORT).show();
						} else {
							ReportCursor.moveToFirst();
							buffer.append("Category = ").append(catagory.getText().toString()).append("\n\n");
							do {
								Cursor productCursor = dbManager.ViewSaleProductHistory(Seller_email, ReportCursor.getString(0));

								if (productCursor.getCount() <= 0) {
									Toast.makeText(this, "You haven't managed '" + ReportCursor.getString(0) + "' product's stocks.", Toast.LENGTH_SHORT).show();
								} else {
									productCursor.moveToFirst();
									buffer.append("Product = ").append(ReportCursor.getString(0)).append("\n");
									if (productCursor.getString(0) == null) {
										buffer.append("Based on the information available, it appears that no sales have been recorded as of yet." + "\n\n");
									} else {
										do {
											buffer.append("Total number of products sold = ").append(productCursor.getString(0)).append("\n");
											buffer.append("The average price per unit sold = ").append(productCursor.getString(2)).append("\n");
											buffer.append("The total revenue generated = ").append(productCursor.getString(1)).append("\n\n");
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
// =================================================================================================

//Chart Button =====================================================================================
		Button qty_chart = findViewById(R.id.QTY_Chart_BTN);
		qty_chart.setOnClickListener(v -> {
			Intent gotoReport = new Intent(inventory_insights.this, chart.class);
			gotoReport.putExtra("seller", Seller_email);
			gotoReport.putExtra("intent", "qty_chart");
			startActivity(gotoReport);
		});
	}
// =================================================================================================

}