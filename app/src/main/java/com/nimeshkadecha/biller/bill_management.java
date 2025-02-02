package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class bill_management extends AppCompatActivity {

	private final String[] shorting = {"Name", "Date"};
	private final DBManager DB = new DBManager(this);
	private TextInputEditText date_edt, toDate_edt;
	private AutoCompleteTextView name_edt;
	private String seller_txt;

	private boolean date_picker_is_called = false;

	private ImageView PlodingView;
	private LinearLayout loadingBlur;

	private Animation alpha;

	// Converting scientific notation to normal =======================================================
	public static String convertScientificToNormal(double scientificNotation) {
		BigDecimal bd = new BigDecimal(scientificNotation);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}
	// OnCreate End ===================================================================================

	private AlertDialog.Builder getBuilder(StringBuilder billData) {
		AlertDialog.Builder builder_dialog = new AlertDialog.Builder(bill_management.this);
		builder_dialog.setCancelable(true);
		builder_dialog.setTitle("Bills");
		builder_dialog.setMessage(billData.toString());

		// download pdf btn ---------------------------------------------------------------------------
		builder_dialog.setPositiveButton("Download PDF", (dialog, which) -> {
			PlodingView.setVisibility(View.VISIBLE);
			PlodingView.startAnimation(alpha);
			loadingBlur.setVisibility(View.VISIBLE);
			dialog.dismiss();
			Toast.makeText(this, "Started Generating PDF", Toast.LENGTH_SHORT).show();

			new Handler().postDelayed(() -> {
				try {
					createPDF(); // to create PDF
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}, 200); // Delay in milliseconds
		});
		return builder_dialog;
	}

	@SuppressLint("DefaultLocale")
	private String getRandom() {
		Random rnd = new Random();
		int otp = rnd.nextInt(999999999);
		return String.format("%09d", otp);
	}

	@SuppressLint({"Range", "ClickableViewAccessibility"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill_management);

//      WORKING WITH TOOLBAR Starts ================================================================
		// Removing Support bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
		// Keeping MENU Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
// =================================================================================================

		// Finding progressbar
		PlodingView = findViewById(R.id.PLoading_bm);
		alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);

		loadingBlur = findViewById(R.id.LoadingBlur_bm);
		PlodingView.setVisibility(View.GONE);
		PlodingView.clearAnimation();
		loadingBlur.setVisibility(View.GONE);

// notifying user to select from dropdown menu =====================================================
		TextInputLayout dl = findViewById(R.id.datelayout);
		TextInputLayout edl = findViewById(R.id.rangedate);
		TextInputLayout CN = findViewById(R.id.nameLayout_bm);

		// CLick Listener to inform user to select Respective Layout -------------------------------------
		CN.setOnClickListener(v -> Toast.makeText(bill_management.this, "Select Name from dropdown menu", Toast.LENGTH_SHORT).show());
		dl.setOnClickListener(v -> Toast.makeText(bill_management.this, "Select date from dropdown menu", Toast.LENGTH_SHORT).show());
		edl.setOnClickListener(v -> Toast.makeText(bill_management.this, "Select date from dropdown menu", Toast.LENGTH_SHORT).show());
		// -----------------------------------------------------------------------------------------------

// =================================================================================================

// Getting User from INTENT ========================================================================
		Bundle seller = getIntent().getExtras();
		assert seller != null;
		seller_txt = seller.getString("seller");
// =================================================================================================

// Adding Autocomplete Text view LIST ==============================================================
		name_edt = findViewById(R.id.name);

		// adding name in to Autocomplete Text view ------------------------------------------------------
		String[] NameSuggestion;
		String[] Names;
		Cursor customerNameSuggestionCursor = DB.CustomerInformation(seller_txt);
		customerNameSuggestionCursor.moveToFirst();
		if (customerNameSuggestionCursor.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			NameSuggestion = new String[customerNameSuggestionCursor.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (NameSuggestion[j].equals(customerNameSuggestionCursor.getString(customerNameSuggestionCursor.getColumnIndex("customerName")))) {
							insert = false;
							break;
						} else {
							insert = true;
						}
					}
				}

				if (insert) {
					NameSuggestion[i] = customerNameSuggestionCursor.getString(customerNameSuggestionCursor.getColumnIndex("customerName"));
					i++;
				}
			} while (customerNameSuggestionCursor.moveToNext());

			Names = new String[i];
			System.arraycopy(NameSuggestion, 0, Names, 0, i);
		} else {
			Names = new String[]{"No Data"};
		}
		name_edt.setAdapter(new ArrayAdapter<>(bill_management.this, android.R.layout.simple_list_item_1, Names));
		// -----------------------------------------------------------------------------------------------


		// adding Contact number in to Autocomplete Text view --------------------------------------------
		String[] numberSuggestion;
		String[] number;
		Cursor customerNumberSuggestionCursor = DB.CustomerInformation(seller_txt);
		customerNumberSuggestionCursor.moveToFirst();
		if (customerNumberSuggestionCursor.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			numberSuggestion = new String[customerNumberSuggestionCursor.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (numberSuggestion[j].equals(customerNumberSuggestionCursor.getString(customerNumberSuggestionCursor.getColumnIndex("customerNumber")))) {
							insert = false;
							break;
						}
					} // end of for loop
				}
				if (insert) {
					numberSuggestion[i] = customerNumberSuggestionCursor.getString(customerNumberSuggestionCursor.getColumnIndex("customerNumber"));
					i++;
				}
			} while (customerNumberSuggestionCursor.moveToNext());

			number = new String[i];
			System.arraycopy(numberSuggestion, 0, number, 0, i);
		} else {
			number = new String[]{"No Data"};
		}
		// -----------------------------------------------------------------------------------------------

		// merging both Autocomplete Text view LIST ------------------------------------------------------
		String[] mergedString = new String[Names.length + number.length];

		int len = 0;
		for (int i = 0; i < Names.length; i++) {
			mergedString[i] = Names[i];
			len++;
		}

		for (String s : number) {
			mergedString[len] = s;
			len++;
		}
		// -----------------------------------------------------------------------------------------------

		// setting Autocomplete Text view LIST
		name_edt.setAdapter(new ArrayAdapter<>(bill_management.this, android.R.layout.simple_list_item_1, mergedString));
// =================================================================================================

// Calculating And Formatting DATE =================================================================
		Date c = Calendar.getInstance().getTime();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String formattedDate = df.format(c);
// =================================================================================================

// Adding current date On click of edit text =======================================================

		// working with date picker (FROM) ---------------------------------------------------------------
		date_edt = findViewById(R.id.date_bm);
		// Hide keyboard on click of edit text
		date_edt.setOnTouchListener((v, event) -> {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			date_edt.performClick();
			return true;
		});

		// Opening Date picker on focus change
		date_edt.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus && !date_picker_is_called) show_date_time_picker(date_edt);
		});
		// Opening calendar on click of edit text
		date_edt.setOnClickListener(v -> {if (!date_picker_is_called) show_date_time_picker(date_edt);});

		// setting today's date on long press
		date_edt.setOnLongClickListener(v -> {
			if (Objects.requireNonNull(date_edt.getText()).toString().isEmpty())
				date_edt.setText(formattedDate);
			else
				Toast.makeText(bill_management.this, "Long press to open date picker", Toast.LENGTH_SHORT).show();
			return true;
		});
		// -----------------------------------------------------------------------------------------------

		// working with date picker (TO) -----------------------------------------------------------------
		// displaying toast to long press and set current date is date is not set
		toDate_edt = findViewById(R.id.rangeDateEDT);
		// Hide keyboard on click of edit text
		toDate_edt.setOnTouchListener((v, event) -> {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			toDate_edt.performClick();
			return true;
		});

		// Opening Date picker on focus change
		toDate_edt.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus && !date_picker_is_called) show_date_time_picker(toDate_edt);
		});
		// Opening calendar on click of edit text
		toDate_edt.setOnClickListener(v -> {
			if (!date_picker_is_called) show_date_time_picker(toDate_edt);
		});

		// setting today's date on long press
		toDate_edt.setOnLongClickListener(v -> {
			if (Objects.requireNonNull(toDate_edt.getText()).toString().isEmpty())
				toDate_edt.setText(formattedDate);
			else
				Toast.makeText(bill_management.this, "Long press to open date picker", Toast.LENGTH_SHORT).show();

			return true;
		});
		// -----------------------------------------------------------------------------------------------


// Adding Spinner (Dropdown menu) ==================================================================
		Spinner spinner = findViewById(R.id.spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(bill_management.this, android.R.layout.simple_spinner_item, shorting);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String value = parent.getItemAtPosition(position).toString();

				if (value.equals("Name")) {
					date_edt.setText("");
					toDate_edt.setText("");
					name_edt.setVisibility(View.VISIBLE);
					date_edt.setVisibility(View.GONE);
					toDate_edt.setVisibility(View.GONE);
				} else if (value.equals("Date")) {
					name_edt.setText("");
					name_edt.setVisibility(View.GONE);
					date_edt.setVisibility(View.VISIBLE);
					toDate_edt.setVisibility(View.VISIBLE);
				} else {
					Toast.makeText(bill_management.this, "Select which type of search you want", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Toast.makeText(bill_management.this, "Select which type of search you want", Toast.LENGTH_SHORT).show();
			}
		});

//      Search Button ==============================================================================
		Button search_btn = findViewById(R.id.searchbtn);
		// end of OnCLick
		search_btn.setOnClickListener(view -> {
			String name_txt, date_txt, contactTXT, ToDate, what_to_delete;

			// getting data from edit text
			name_txt = name_edt.getText().toString();
			date_txt = Objects.requireNonNull(date_edt.getText()).toString();
			ToDate = Objects.requireNonNull(toDate_edt.getText()).toString();

			// checking if edit text is empty
			if (name_txt.isEmpty() && date_txt.isEmpty() && ToDate.isEmpty()) {
				Toast.makeText(bill_management.this, "Fill at least one information to search", Toast.LENGTH_SHORT).show();
			} else if (date_txt.isEmpty() && !ToDate.isEmpty()) {
				Toast.makeText(bill_management.this, "Enter Starting Date", Toast.LENGTH_SHORT).show();
			}
			// searching data from database
			else {
				Cursor searchResultCursor; // this cursor store the result, the user's choice don't matter
				if (!name_txt.isEmpty()) {
					char[] c1 = name_txt.toCharArray();
					boolean contain_digit = false;
					int NumberOfDigits = 0;
					for (char check : c1) {
						if (Character.isDigit(check)) {
							contain_digit = true;
							NumberOfDigits++;
						}
					}
					if (contain_digit && NumberOfDigits == name_txt.length()) {
						if (NumberOfDigits == 10) {
							contactTXT = name_txt;
							what_to_delete = "contact";
							searchResultCursor = DB.CustomerNumberBill(contactTXT, seller_txt);
						} else {
							int billID;
							billID = Integer.parseInt(name_txt);
							what_to_delete = "billid";
							searchResultCursor = DB.CustomerBillID(billID, seller_txt);
						}
					} else {
						what_to_delete = "name";
						searchResultCursor = DB.CustomerNameBill(name_txt, seller_txt);
					}
				} else {
					if (!ToDate.isEmpty()) {
						what_to_delete = "rangDate";
						searchResultCursor = DB.RangeSearch(date_txt, ToDate, seller_txt);
					} else {
						what_to_delete = "date";
						searchResultCursor = DB.CustomerDateBill(date_txt, seller_txt);
					}
				}

				if (searchResultCursor.getCount() == 0) {
					Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
					return;
				}

				// displaying the result ----------------------------------------------------------------------
				double total = 0;
				StringBuilder billData = new StringBuilder();
				while (searchResultCursor.moveToNext()) {
					String formattedDate1 = date_convertor.convertDateFormat(searchResultCursor.getString(searchResultCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy");
					billData.append("Bill ID = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("billId"))).append("\n");
					billData.append("Customer Name = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("customerName"))).append("\n");
					billData.append("Customer Number = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("customerNumber"))).append("\n");
					billData.append("Date = ").append(formattedDate1).append("\n");
					billData.append("Product Name = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("product"))).append("\n");
					billData.append("Price = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("price"))).append("\n");
					billData.append("Quantity = ").append(searchResultCursor.getString(searchResultCursor.getColumnIndex("quantity"))).append("\n");
					billData.append("Sub Total = ").append(convertScientificToNormal(searchResultCursor.getDouble(searchResultCursor.getColumnIndex("subtotal")))).append("\n\n");
					total += Double.parseDouble(searchResultCursor.getString(searchResultCursor.getColumnIndex("subtotal")));
				}

				billData.append("Total = ").append(convertScientificToNormal(total));

				AlertDialog.Builder builder_dialog = getBuilder(billData);
				// --------------------------------------------------------------------------------------------

				// Delete Listing -----------------------------------------------------------------------------
				String finalWhat_to_delete = what_to_delete;
				Cursor finalRes = searchResultCursor;
				builder_dialog.setNegativeButton("Delete Listing", (dialogInterface, i) -> {
					AlertDialog.Builder alert_confirmation = new AlertDialog.Builder(bill_management.this);
					alert_confirmation.setTitle("Delete listing !");
					switch (finalWhat_to_delete) {
						case "contact":
							alert_confirmation.setMessage("Delete all data of the contact : " + name_txt);
							break;

						case "billid":
							alert_confirmation.setMessage("Delete data of the billid : " + Integer.parseInt(name_txt));
							break;

						case "name":
							alert_confirmation.setMessage("Delete all data of the customer : " + name_txt);
							break;

						case "rangDate":
							alert_confirmation.setMessage("Delete all data from date: " + date_txt + " to : " + ToDate);
							break;

						case "date":
							alert_confirmation.setMessage("Delete all data of the date: " + date_txt);
							break;
					}

					// confirmation box
					alert_confirmation.setPositiveButton("Yes, Delete", (dialogInterface12, i12) -> {

						boolean confirmDelete = false; // store the result for any of the bellow

						switch (finalWhat_to_delete) {
							case "contact":
								confirmDelete = DB.DeleteBillWithCustomerNumber(name_txt, seller_txt);
								break;

							case "billid":
								confirmDelete = DB.DeleteBillWithBillID(String.valueOf(Integer.parseInt(name_txt)), seller_txt);
								break;

							case "name":
								confirmDelete = DB.DeleteBillWithCustomerName(name_txt, seller_txt);
								break;

							case "rangDate":
							case "date":
								confirmDelete = DB.DeleteBillWithDate(finalRes, seller_txt);
								break;
						}

						if (confirmDelete)
							Toast.makeText(bill_management.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(bill_management.this, "Error while deleting", Toast.LENGTH_SHORT).show();

						dialogInterface12.dismiss();

					}).setNegativeButton("No, Cancel", (dialogInterface1, i1) -> {
						Toast.makeText(bill_management.this, "Cancel", Toast.LENGTH_SHORT).show();
						dialogInterface1.dismiss();
					});
					alert_confirmation.show(); // showing confirmation box
				});
				// --------------------------------------------------------------------------------------------

				builder_dialog.show(); // displaying all listings
				// --------------------------------------------------------------------------------------------
			} // end of else
		});
// =================================================================================================

//      PDF Button =================================================================================
		Button pdf = findViewById(R.id.pdfC);
		pdf.setVisibility(View.GONE);

//        Show ALl Customer Button =================================================================
		Button show_btn = findViewById(R.id.showallData);

		show_btn.setOnClickListener(view -> {
			Cursor res = DB.CustomerInformation(seller_txt); // getting information from database
			if (res.getCount() == 0) {
				Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
				return;
			}
			StringBuilder buffer = new StringBuilder();
			while (res.moveToNext()) {
				buffer.append("Customer ID = ").append(res.getString(res.getColumnIndex("customerId"))).append("\n");
				buffer.append("Customer Name = ").append(res.getString(res.getColumnIndex("customerName"))).append("\n");
				buffer.append("Customer Number = ").append(res.getString(res.getColumnIndex("customerNumber"))).append("\n");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(bill_management.this);
			builder.setCancelable(true);
			builder.setTitle("Bills");
			builder.setMessage(buffer.toString());
			builder.show();
		});
// =================================================================================================
	}
	// Create PDF End =================================================================================

	// ================================================================================================

	// Showing Date picker and hiding input filed =====================================================
	public void show_date_time_picker(TextInputEditText edt) {
		date_picker_is_called = true;

		// Delay showing the date picker to ensure the keyboard is hidden first

		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(bill_management.this, new DatePickerDialog.OnDateSetListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
				assert edt != null;
				edt.setText("");
				edt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
				date_picker_is_called = false;
			}
		}, year, month, day);
		datePickerDialog.show();
		datePickerDialog.setOnCancelListener(dialog -> date_picker_is_called = false);
		datePickerDialog.setOnDismissListener(dialog -> date_picker_is_called = false);

	}
//  ================================================================================================

	// This will Formate address ----------------------------------------------------------------------
	private static @NonNull StringBuilder getStringBuilder(String address) {
		String[] addressLines = address.split("\n");
		StringBuilder formattedAddress = new StringBuilder();

		// Add first line with "Address: "
		formattedAddress.append("Address: ").append(addressLines[0]).append("\n");

		// Add subsequent lines with indentation
		String indentation = "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"; // Adjust the number of non-breaking spaces
		// Add subsequent lines with indentation
		for (int i = 1; i < addressLines.length; i++) {
			formattedAddress.append(indentation).append(addressLines[i]).append("\n"); // Adjust the spaces for indentation
		}
		return formattedAddress;
	}

	@SuppressLint("Range")
	private void createPDF() throws FileNotFoundException {
		String name_txt, date_txt, billId_txt, contactTXT, ToDate;

		boolean haveGST = false;

		name_txt = name_edt.getText().toString();
		date_txt = Objects.requireNonNull(date_edt.getText()).toString();
		billId_txt = name_edt.getText().toString();
		contactTXT = name_edt.getText().toString();
		ToDate = Objects.requireNonNull(toDate_edt.getText()).toString();

		if (name_txt.isEmpty() && date_txt.isEmpty() && billId_txt.isEmpty() && contactTXT.isEmpty()) {
			Toast.makeText(bill_management.this, "Fill at least one information to search", Toast.LENGTH_SHORT).show();
		} else {
			String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//                File name
//                Create file object
			String s = getRandom();
			File file = new File(pdfPath, "Renewed BILL" + s + ".pdf");

			PdfWriter writer = new PdfWriter(file);
			PdfDocument pdfDocument = new PdfDocument(writer);
			Document document = new Document(pdfDocument);

			float[] cWidth = {560};
			Table table1 = new Table(cWidth);

			// Table 1 do this adding seller data -----------------------------------------------------------
//        Want users||||| NAME EMail GST ADDRESS NUMBER
//                           0  1     3   5       4

			Cursor sellerDATA = DB.GetUser(seller_txt);
			if (sellerDATA.getCount() == 0) {
				Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
				return;
			} else {
				sellerDATA.moveToFirst();
				do {
					table1.addCell(new Cell().add(new Paragraph(sellerDATA.getString(sellerDATA.getColumnIndex("name"))).setFontSize(32)).setBorder(Border.NO_BORDER));

					// Formatting the address with indentation for multiple lines
					String address = sellerDATA.getString(sellerDATA.getColumnIndex("address"));
					StringBuilder formattedAddress = getStringBuilder(address);

					table1.addCell(new Cell().add(new Paragraph(formattedAddress.toString()).setFontSize(14)).setBorder(Border.NO_BORDER));

					table1.addCell(new Cell().add(new Paragraph("E=mail: " + sellerDATA.getString(sellerDATA.getColumnIndex("email"))).setFontSize(14)).setBorder(Border.NO_BORDER));

					table1.addCell(new Cell().add(new Paragraph("Mo: " + sellerDATA.getString(sellerDATA.getColumnIndex("contact"))).setFontSize(14)).setBorder(Border.NO_BORDER));


					if (!sellerDATA.getString(sellerDATA.getColumnIndex("gst")).equals("-1")) {
						haveGST = true;
						table1.addCell(new Cell().add(new Paragraph("GSTIN: " + sellerDATA.getString(sellerDATA.getColumnIndex("gst"))).setFontSize(14)).setBorder(Border.NO_BORDER));
					}

					table1.addCell(new Cell());

				} while (sellerDATA.moveToNext());
			}
			// ----------------------------------------------------------------------------------------------

			// Table 2 writing customer data ----------------------------------------------------------------
//        Want display ||||||  customerName=5 customerNumber=6 date=7

			float[] cWidth3 = {142, 142, 142, 142};
			Table table3 = new Table(cWidth3);

			//checking for GST number
			Table table2;
			float[] c_width;
			if (haveGST) {
				c_width = new float[]{120, 90, 110, 80, 80, 90}; // width space for GST
			} else {
				c_width = new float[]{270, 100, 100, 100}; // width without space for GST
			}
			table2 = new Table(c_width);

			float[] cWidth6 = {560};
			Table END = new Table(cWidth6);
			Table END_Border = new Table(cWidth6);

			// getting customer data from database
			Cursor customerDetail;
			Cursor list;
			int checker;
			/*
							CHECKERS
							1 = name
							2 = contact
							3 = date
							4 = toDate
							5 = billId
   */
			if (!name_txt.isEmpty()) {
				char[] c = name_txt.toCharArray();
				boolean contain_digit = false;
				int NumberOfDigits = 0;
				for (char check : c) {
					if (Character.isDigit(check)) {
						contain_digit = true;
						NumberOfDigits++;
					}
				}
				if (contain_digit) {
					if (NumberOfDigits == 10) {
						checker = 2;
						contactTXT = name_txt;
						customerDetail = DB.CustomerNumberBill(contactTXT, seller_txt);
						list = DB.CustomerNumberBill(contactTXT, seller_txt);
					} else {
						checker = 5;
						int billID;
						billID = Integer.parseInt(name_txt);
						customerDetail = DB.CustomerBillID(billID, seller_txt);
						list = DB.CustomerBillID(billID, seller_txt);
					}
				} else {
					checker = 1;
					customerDetail = DB.CustomerNameBill(name_txt, seller_txt);
					list = DB.CustomerNameBill(name_txt, seller_txt);
				}

			} else if (!contactTXT.isEmpty()) {
				checker = 2;
				customerDetail = DB.CustomerNumberBill(contactTXT, seller_txt);
				list = DB.CustomerNumberBill(contactTXT, seller_txt);
			} else if (!date_txt.isEmpty()) {
				if (!ToDate.isEmpty()) {
					checker = 4;
					customerDetail = DB.RangeSearch(date_txt, ToDate, seller_txt);
					list = DB.RangeSearch(date_txt, ToDate, seller_txt);
				} else {
					checker = 3;
					customerDetail = DB.CustomerDateBill(date_txt, seller_txt);
					list = DB.CustomerDateBill(date_txt, seller_txt);
				}
			} else {
				checker = 5;
				int billID;
				billID = Integer.parseInt(billId_txt);
				customerDetail = DB.CustomerBillID(billID, seller_txt);
				list = DB.CustomerBillID(billID, seller_txt);
			}


			if (customerDetail.getCount() == 0) {
				Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
			} else {
				if (checker != 5) { // for any other search
					document.add(table1);
					customerDetail.moveToFirst();
					do {
						//  Printing bill heading --------------------------------------------------------------------
						float[] cWidth5 = {142, 142, 142, 142};
						Table table5 = new Table(cWidth5);

						table5.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerName"))).setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerNumber"))).setFontSize(14)).setBorder(Border.NO_BORDER));

						table5.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().add(new Paragraph(date_convertor.convertDateFormat(customerDetail.getString(customerDetail.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy")).setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().setBorder(Border.NO_BORDER));

						table5.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("billId"))).setFontSize(14)).setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().setBorder(Border.NO_BORDER));
						table5.addCell(new Cell().setBorder(Border.NO_BORDER));
						document.add(table5);
						table5.flushContent();
						// -------------------------------------------------------------------------------------------

						//  printing bill details --------------------------------------------------------------------

						float[] cWidth2;
						if (haveGST)
							cWidth2 = new float[]{120, 90, 110, 80, 80, 90};
						else
							cWidth2 = new float[]{270, 100, 100, 100};

						table2 = new Table(cWidth2);

						int index = 0;

						// printing table header ---------------------------------------------------------------------
						table2.addCell(new Cell().add(new Paragraph("Product Name")));
						table2.addCell(new Cell().add(new Paragraph("Product Price")));
						table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
						if (haveGST) {
							table2.addCell(new Cell().add(new Paragraph("CGST")));
							table2.addCell(new Cell().add(new Paragraph("SGST")));
						}
						table2.addCell(new Cell().add(new Paragraph("Sub Total")));
						// -------------------------------------------------------------------------------------------

						// printing bill details ---------------------------------------------------------------------
						float TotalGST = 0f;
						double total = 0d;
						if (list.getCount() == 0) {
							Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
							return;
						} else {
							list.moveToFirst();
							do {
								if (customerDetail.getString(customerDetail.getColumnIndex("billId")).equals(list.getString(list.getColumnIndex("billId")))) {
									if (haveGST) {
										String gst;
										if (list.getString(list.getColumnIndex("Gst")).equals("-1")) gst = "0";
										else gst = list.getString(list.getColumnIndex("Gst"));

										float tax = ((Float.parseFloat(String.valueOf(list.getString(list.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("quantity")))) * (Float.parseFloat(String.valueOf(gst)) / 100f)));
										TotalGST += tax;
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")))));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")))));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")))));
										table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
										table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")))));
									} else {
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")))));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")))));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")))));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")))));
									}
									index++;
									total += list.getDouble(list.getColumnIndex("subtotal"));
								}
							} while (list.moveToNext());
						}
						// -------------------------------------------------------------------------------------------

						//  Adding Total & GST (if available ) -------------------------------------------------------
						if (haveGST) {
							table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
							table2.addCell(new Cell().add(new Paragraph(TotalGST + "")));
							table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
							table2.addCell(new Cell(2, 6).setBorder(Border.NO_BORDER)); // adding space
							table2.addCell(new Cell(0, 6).setBold());// adding line
						} else {
							table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
							table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
							table2.addCell(new Cell(2, 4).setBorder(Border.NO_BORDER)); // adding space
							table2.addCell(new Cell(0, 4).setBold());// adding line
						}
						// -------------------------------------------------------------------------------------------

						customerDetail.moveToPosition(customerDetail.getPosition() + index - 1);
						document.add(table2);
						table2.flushContent();
					} while (customerDetail.moveToNext());
				}
				//  printing bills in different format CHECKER = 5 ---------------------------------------------
				else { // for BillId search
					customerDetail.moveToFirst();
					//  Printing headings -------------------------------------------------------------------------
					table3.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerName"))).setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerNumber"))).setFontSize(14)).setBorder(Border.NO_BORDER));

					table3.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph(date_convertor.convertDateFormat(customerDetail.getString(customerDetail.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy")).setFontSize(14)).setBorder(Border.NO_BORDER));

					table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("billId"))).setFontSize(14)).setBorder(Border.NO_BORDER));
					// --------------------------------------------------------------------------------------------

					//  Printing bill headings details ------------------------------------------------------------
					table2.addCell(new Cell().add(new Paragraph("Product Name")));
					table2.addCell(new Cell().add(new Paragraph("Product Price")));
					table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
					if (haveGST) {
						table2.addCell(new Cell().add(new Paragraph("CGST")));
						table2.addCell(new Cell().add(new Paragraph("SGST")));
					}
					table2.addCell(new Cell().add(new Paragraph("Sub Total")));
					// --------------------------------------------------------------------------------------------

					// printing bill details ----------------------------------------------------------------------
					float TotalGST = 0f;
					customerDetail.moveToFirst();
					double total = 0d;
					if (list.getCount() == 0) {
						Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					} else {
						list.moveToFirst();
						do {
							if (haveGST) {
								String gst;
								if (list.getString(list.getColumnIndex("Gst")).equals("-1")) gst = "0";
								else gst = list.getString(list.getColumnIndex("Gst"));

								float tax = ((Float.parseFloat(String.valueOf(list.getString(list.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("quantity")))) * (Float.parseFloat(String.valueOf(gst)) / 100f)));
								TotalGST += tax;
								table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")))));
								table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(Double.parseDouble(list.getString(list.getColumnIndex("price")))))));
								table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")))));
								table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
								table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
								table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(Double.parseDouble(list.getString(list.getColumnIndex("subtotal")))))));
							} else {
								table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")))));
								table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")))));
								table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")))));
								table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(Double.parseDouble(list.getString(list.getColumnIndex("subtotal")))))));
							}
							total += list.getDouble(list.getColumnIndex("subtotal"));
						} while (list.moveToNext());
					}
					// --------------------------------------------------------------------------------------------

					//  Adding Total & GST (if available ) --------------------------------------------------------
					if (haveGST) {
						table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
						table2.addCell(new Cell().add(new Paragraph(String.valueOf(TotalGST))));
						table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
					} else {
						table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
						table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
					}
					// --------------------------------------------------------------------------------------------

				}

				// Adding border at the end of each bill !
				END_Border.addCell(new Cell());

				// Adding Signature ----------------------------------------------------------------------------
				//				END.addCell(new Cell().add(new Paragraph("Signature: ")).setBorder(Border.NO_BORDER));

				// Displaying data -----------------------------------------------------------------------------
				if (checker == 5) {
					document.add(table1);
					document.add(table3);
					document.add(new Paragraph("\n"));
					document.add(table2);
					document.add(new Paragraph("\n"));
					document.add(END_Border);
				}
				document.add(new Paragraph("\n"));

				document.add(END);
				document.close();

				Toast.makeText(bill_management.this, "PDF Created", Toast.LENGTH_SHORT).show();

				//  Opening PDF --------------------------------------------------------------------------------
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
					if (file.exists()) {
						PlodingView.setVisibility(View.GONE);
						PlodingView.clearAnimation();
						loadingBlur.setVisibility(View.GONE);
						Uri uri = FileProvider.getUriForFile(bill_management.this, getApplicationContext().getPackageName() + ".provider", file);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(uri, "application/pdf");
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						startActivity(intent);
					} else {
						PlodingView.setVisibility(View.GONE);
						PlodingView.clearAnimation();
						loadingBlur.setVisibility(View.GONE);
						Toast.makeText(bill_management.this, "File can't be created", Toast.LENGTH_SHORT).show();
					}
				}
				// ---------------------------------------------------------------------------------------------

				// ---------------------------------------------------------------------------------------------
			}
		}
	}
	// ================================================================================================
}