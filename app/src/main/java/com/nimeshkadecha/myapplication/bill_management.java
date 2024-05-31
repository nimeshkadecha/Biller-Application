package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.color.utilities.ColorUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class bill_management extends AppCompatActivity {

	private EditText dateedt, billidedt, todateedt;

	private AutoCompleteTextView nameedt, contactedt;

	private Button searchbtn, showbtn, pdf;

	private ImageView menuclick;

	private DBManager DB = new DBManager(this);

	private Spinner spinner;

	private TextInputLayout cl, dl, edl, bl, CN;
	String[] shorting = {"Name", "Date"};

	@SuppressLint("Range")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill_management);

//        Google ads code --------------------------------------------------------------------------
		AdView mAdView;
		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

//        Finding Layout for Spinner (DropDown MEnu) ===============================================
		cl = findViewById(R.id.contactlayout);
		dl = findViewById(R.id.datelayout);
		edl = findViewById(R.id.rangedate);
		bl = findViewById(R.id.billIDlayout);
		CN = findViewById(R.id.namelayout);


//        CLick Listener to inform user to select Respective Layout ================================
		cl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(bill_management.this, "Select Number from dropdown menu", Toast.LENGTH_SHORT).show();
			}
		});
		dl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(bill_management.this, "Select date from dropdown menu", Toast.LENGTH_SHORT).show();
			}
		});
		edl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(bill_management.this, "Select date from dropdown menu", Toast.LENGTH_SHORT).show();
			}
		});

//        Getting User from INTENT =================================================================
		Bundle seller = getIntent().getExtras();
		String sellertxt = seller.getString("seller");

//      WORKING WITH TOOLBAR Starts ================================================================
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

//          menu Button ============================================================================
		menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
		menuclick.setVisibility(View.INVISIBLE);


//        Finding Edittext =========================================================================
		nameedt = findViewById(R.id.name);
//        Adding Autocomplete Text view LIST
		String[] NameSuggestion;
		String[] Names;
		Cursor customerNameSuggestionCursor = DB.CustomerInformation(sellertxt);
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
			for (int j = 0; j < i; j++) {
				Names[j] = NameSuggestion[j];
			}
		} else {
			Names = new String[]{"No DAta"};
		}
		nameedt.setAdapter(new ArrayAdapter<>(bill_management.this, android.R.layout.simple_list_item_1, Names));

		dateedt = findViewById(R.id.date);
		billidedt = findViewById(R.id.billID);

		contactedt = findViewById(R.id.contact);
		//        Adding Autocomplete Text view LIST
		String[] numberSugg;
		String[] NUmber;
		Cursor customerNumberSuggestionCursor = DB.CustomerInformation(sellertxt);
		customerNumberSuggestionCursor.moveToFirst();
		if (customerNumberSuggestionCursor.getCount() > 0) {
			int i = 0;
			boolean insert = true;
			numberSugg = new String[customerNumberSuggestionCursor.getCount()];
			do {
				if (i != 0) {
					for (int j = 0; j < i; j++) {
						if (numberSugg[j].equals(customerNumberSuggestionCursor.getString(customerNumberSuggestionCursor.getColumnIndex("customerNumber")))) {
							insert = false;
							break;
						}
					}
				}

				if (insert) {
					numberSugg[i] = customerNumberSuggestionCursor.getString(customerNumberSuggestionCursor.getColumnIndex("customerNumber"));
					i++;
				}
			} while (customerNumberSuggestionCursor.moveToNext());

			NUmber = new String[i];
			for (int j = 0; j < i; j++) {
				NUmber[j] = numberSugg[j];
			}
		} else {
			NUmber = new String[]{"No Data"};
			numberSugg = new String[]{"No Data for Suggestion"};
		}

		String[] mergedString = new String[Names.length + NUmber.length];

		int len = 0;
		for (int i = 0; i < Names.length; i++) {
			mergedString[i] = Names[i];
			len++;
		}

		for (int i = 0; i < NUmber.length; i++) {
			mergedString[len] = NUmber[i];
			len++;
		}

		nameedt.setAdapter(new ArrayAdapter<>(bill_management.this, android.R.layout.simple_list_item_1, mergedString));

		todateedt = findViewById(R.id.rangeDatetEDT);


//        Calculating And Formatting DATE ==========================================================
		Date c = Calendar.getInstance().getTime();

		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String formattedDate = df.format(c);


//        Adding current date On click of edit text ================================================
		dateedt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String date = dateedt.getText().toString();
				if (date.isEmpty()) {
					dateedt.setText(formattedDate);
				} else {
					Toast.makeText(bill_management.this, "Long press to open date picker", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Opening calendar on long press ==========================================================
		dateedt.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				final Calendar c = Calendar.getInstance();

				// on below line we are getting
				// our day, month and year.
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog datePickerDialog = new DatePickerDialog(bill_management.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
						dateedt.setText(" ");
						dateedt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
					}
				}, year, month, day);
				datePickerDialog.show();
				return true;
			}
		});
		// displaying toast to long press and set current date is date is not set ==================
		todateedt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String date = todateedt.getText().toString();
				if (date.isEmpty()) {
					todateedt.setText(formattedDate);
				} else {
					Toast.makeText(bill_management.this, "Long press to open date picker", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Opening calendar on long press ==========================================================
		todateedt.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				final Calendar c = Calendar.getInstance();

				// on below line we are getting
				// our day, month and year.
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog datePickerDialog = new DatePickerDialog(bill_management.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
						todateedt.setText(" ");
						todateedt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
					}
				}, year, month, day);
				datePickerDialog.show();
				return true;
			}
		});


		searchbtn = findViewById(R.id.searchbtn);

//        Adding Spinner (Dropdown menu) ===========================================================
		spinner = findViewById(R.id.spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(bill_management.this, android.R.layout.simple_spinner_item, shorting);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String value = parent.getItemAtPosition(position).toString();

				if (value.equals("Name")) {
					contactedt.setText("");
					dateedt.setText("");
					billidedt.setText("");
					todateedt.setText("");

					nameedt.setVisibility(View.VISIBLE);
					dateedt.setVisibility(View.GONE);
					billidedt.setVisibility(View.GONE);
					contactedt.setVisibility(View.GONE);
					todateedt.setVisibility(View.GONE);
				} else if (value.equals("Date")) {
					nameedt.setText("");
					contactedt.setText("");
					billidedt.setText("");

					nameedt.setVisibility(View.GONE);
					dateedt.setVisibility(View.VISIBLE);
					billidedt.setVisibility(View.GONE);
					contactedt.setVisibility(View.GONE);
					todateedt.setVisibility(View.VISIBLE);
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
		searchbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String nametxt, datetxt, contactTXT, ToDate, what_to_delete = null;

				nametxt = nameedt.getText().toString();
				datetxt = dateedt.getText().toString();
				ToDate = todateedt.getText().toString();

				if (nametxt.isEmpty() && datetxt.isEmpty() && ToDate.isEmpty()) {
					Toast.makeText(bill_management.this, "Fill at least one information to search", Toast.LENGTH_SHORT).show();
				} else if (datetxt.isEmpty() && !ToDate.isEmpty()) {
					Toast.makeText(bill_management.this, "Enter Starting Date", Toast.LENGTH_SHORT).show();
				} else {
					Cursor searchResultCursor;
					searchResultCursor = DB.CustomerInformation(sellertxt);
					if (!nametxt.isEmpty()) {
						char c[] = nametxt.toCharArray();
						boolean contain_digit = false;
						int NumberOfDigits = 0;
						for (char check : c) {
							if (Character.isDigit(check)) {
								contain_digit = true;
								NumberOfDigits++;
							}
						}
						if (contain_digit && NumberOfDigits == nametxt.length()) {
							if (NumberOfDigits == 10) {
								contactTXT = nametxt;
								what_to_delete = "contact";
								searchResultCursor = DB.CustomerNumberBill(contactTXT, sellertxt);
							} else {
								Integer billID;
								billID = Integer.parseInt(nametxt);
								what_to_delete = "billid";
								searchResultCursor = DB.CustomerBillID(billID, sellertxt);
							}
						} else {
							what_to_delete = "name";
							searchResultCursor = DB.CustomerNameBill(nametxt, sellertxt);
						}
					} else if (!datetxt.isEmpty()) {
						if (!ToDate.isEmpty()) {
							what_to_delete = "rangDate";
							searchResultCursor = DB.RangeSearch(datetxt, ToDate, sellertxt);
						} else {
							what_to_delete = "date";
							searchResultCursor = DB.CustomerDateBill(datetxt, sellertxt);
						}
					} else {
						what_to_delete = "error";
						searchResultCursor = DB.CustomerInformation(sellertxt);
						Toast.makeText(bill_management.this, "Error", Toast.LENGTH_SHORT).show();
					}

					if (searchResultCursor.getCount() == 0) {
						what_to_delete = "error";
						Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					}

					int total = 0;
					StringBuffer buffer = new StringBuffer();
					while (searchResultCursor.moveToNext()) {
						String formattedDate = date_convertor.convertDateFormat(searchResultCursor.getString(searchResultCursor.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy");
//                    DATE | name | number | Total |
						buffer.append("Bill ID = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("billId")) + "\n");
						buffer.append("Customer Name = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("customerName")) + "\n");
						buffer.append("Customer Number = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("customerNumber")) + "\n");
						buffer.append("Date = " + formattedDate + "\n");
						buffer.append("Product Name = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("product")) + "\n");
						buffer.append("Price = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("price")) + "\n");
						buffer.append("Quantity = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("quantity")) + "\n");
						buffer.append("Sub Total = " + searchResultCursor.getString(searchResultCursor.getColumnIndex("subtotal")) + "\n\n");
						total += Float.parseFloat(searchResultCursor.getString(searchResultCursor.getColumnIndex("subtotal")));
					}

					buffer.append("Total = " + total);

					AlertDialog.Builder builder = new AlertDialog.Builder(bill_management.this);
					builder.setCancelable(true);
					builder.setTitle("Bills");
					builder.setMessage(buffer.toString());
					builder.setPositiveButton("Download PDF", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								createPDF();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
					});

					String finalWhat_to_delete = what_to_delete;
					Cursor finalRes = searchResultCursor;
					builder.setNegativeButton("Delete Listing", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							AlertDialog.Builder alert = new AlertDialog.Builder(bill_management.this);
							alert.setTitle("Delete listing !");
							switch (finalWhat_to_delete) {
								case "contact":
									alert.setMessage("Delete all data of the contact : " + nametxt);
									break;

								case "billid":
									alert.setMessage("Delete data of the billid : " + String.valueOf(Integer.parseInt(nametxt)));
									break;

								case "name":
									alert.setMessage("Delete all data of the customer : " + nametxt);
									break;

								case "rangDate":
									alert.setMessage("Delete all data from date: " + datetxt + " to : " + ToDate);
									break;

								case "date":
									alert.setMessage("Delete all data of the date: " + datetxt);
									break;

								default:
									alert.setMessage("Error, try again!");
							}

							alert.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {

									switch (finalWhat_to_delete) {
										case "contact":
											boolean confirmDelete = DB.DeleteBillWithCustomerNumber(nametxt, sellertxt);
											if (confirmDelete) {
												Toast.makeText(bill_management.this, "all data of that contact number is deleted successfully", Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(bill_management.this, "Error while deleting bill", Toast.LENGTH_SHORT).show();
											}
											break;

										case "billid":
											confirmDelete = false;
											String billID;
											billID = String.valueOf(Integer.parseInt(nametxt));
											confirmDelete = DB.DeleteBillWithBillID(billID, sellertxt);
											if (confirmDelete) {
												Toast.makeText(bill_management.this, "Bill was deleted successfully", Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(bill_management.this, "Error while deleting bill", Toast.LENGTH_SHORT).show();
											}
											break;

										case "name":
											confirmDelete = false;
											confirmDelete = DB.DeleteBillWithCustomerName(nametxt, sellertxt);
											if (confirmDelete) {
												Toast.makeText(bill_management.this, "all data of that customer is deleted successfully", Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(bill_management.this, "Error while deleting bill", Toast.LENGTH_SHORT).show();
											}
											break;

										case "rangDate":
											confirmDelete = false;
											confirmDelete = DB.DeleteCustomerWithRangeDate(finalRes, sellertxt);
											if (confirmDelete) {
												Toast.makeText(bill_management.this, "bills from that range is deleted successfully", Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(bill_management.this, "Error while deleting bill", Toast.LENGTH_SHORT).show();
											}
											break;

										case "date":
											confirmDelete = false;
											confirmDelete = DB.DeleteBillWithDate(finalRes, sellertxt);
											if (confirmDelete) {
												Toast.makeText(bill_management.this, "bills from that range is deleted successfully", Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(bill_management.this, "Error while deleting bill", Toast.LENGTH_SHORT).show();
											}
											break;

										default:
											Toast.makeText(bill_management.this, "Error while deleting", Toast.LENGTH_SHORT).show();

									}
								}
							});
							alert.setNegativeButton("No, Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									Toast.makeText(bill_management.this, "Cancel", Toast.LENGTH_SHORT).show();
									dialogInterface.dismiss();
								}
							});

							alert.show();
						}
					});
					builder.show();
				}
			}

			private String getrandom() {
				Random rnd = new Random();
				int otp = rnd.nextInt(999999999);
				return String.format("%09d", otp);
			}

			@SuppressLint("Range")
			private void createPDF() throws FileNotFoundException {
				String nametxt, datetxt, billIDtxt, contactTXT, ToDate;

				boolean haveGST = false;

				nametxt = nameedt.getText().toString();
				datetxt = dateedt.getText().toString();
				billIDtxt = nameedt.getText().toString();
				contactTXT = nameedt.getText().toString();
				ToDate = todateedt.getText().toString();

				if (nametxt.isEmpty() && datetxt.isEmpty() && billIDtxt.isEmpty() && contactTXT.isEmpty()) {
					Toast.makeText(bill_management.this, "Fill at least one information to search", Toast.LENGTH_SHORT).show();
				} else {
					String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//                File name
//                Create file object
					String s = getrandom();
					File file = new File(pdfPath, "Renewed BILL" + s + ".pdf");
					OutputStream outputStream = new FileOutputStream(file);

					PdfWriter writer = new PdfWriter(file);
					PdfDocument pdfDocument = new PdfDocument(writer);
					Document document = new Document(pdfDocument);

					float cWidth[] = {560};
					Table table1 = new Table(cWidth);

//        Table 1 do this
//        Want users||||| NAME EMail GST ADDRESS NUMBER
//                           0  1     3   5       4

					Cursor selerDATA = DB.GetUser(sellertxt);
					if (selerDATA.getCount() == 0) {
						Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					} else {
						selerDATA.moveToFirst();
						do {
							table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(selerDATA.getColumnIndex("name")) + "").setFontSize(32)).setBorder(Border.NO_BORDER));

							table1.addCell(new Cell().add(new Paragraph("Address: " + selerDATA.getString(selerDATA.getColumnIndex("address")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

							table1.addCell(new Cell().add(new Paragraph("E=mail: " + selerDATA.getString(selerDATA.getColumnIndex("email")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

							table1.addCell(new Cell().add(new Paragraph("Mo: " + selerDATA.getString(selerDATA.getColumnIndex("contact")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

							if (!selerDATA.getString(3).equals("no")) {
								haveGST = true;
								table1.addCell(new Cell().add(new Paragraph("GSTIN: " + selerDATA.getString(selerDATA.getColumnIndex("gst")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
							}

							table1.addCell(new Cell());

						} while (selerDATA.moveToNext());
					}


					//        Table 2 do this    FROM BILLID
//        Want display ||||||  customerName=5 customerNumber=6 date=7

					float cWidth3[] = {142, 142, 142, 142};
					Table table3 = new Table(cWidth3);

					Table table2;
					if (haveGST) {
						float cWidth2[] = {120, 90, 110, 80, 80, 90};
						table2 = new Table(cWidth2);

					} else {
						float cWidth2[] = {270, 100, 100, 100};
						table2 = new Table(cWidth2);
					}

					float cWidth6[] = {560};
					Table END = new Table(cWidth6);
					Table END_Border = new Table(cWidth6);


					Cursor customerDetail;
					Cursor list;
					customerDetail = DB.CustomerInformation(sellertxt);
					int checker = 0;

					if (!nametxt.isEmpty()) {
						char c[] = nametxt.toCharArray();
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
								contactTXT = nametxt;
								customerDetail = DB.CustomerNumberBill(contactTXT, sellertxt);
								list = DB.CustomerNumberBill(contactTXT, sellertxt);
							} else {
								checker = 5;
								Integer billID;
								billID = Integer.parseInt(nametxt);
								customerDetail = DB.CustomerBillID(billID, sellertxt);
								list = DB.CustomerBillID(billID, sellertxt);
							}
						} else {
							checker = 1;
							customerDetail = DB.CustomerNameBill(nametxt, sellertxt);
							list = DB.CustomerNameBill(nametxt, sellertxt);
						}

					} else if (!contactTXT.isEmpty()) {
						checker = 2;
						customerDetail = DB.CustomerNumberBill(contactTXT, sellertxt);
						list = DB.CustomerNumberBill(contactTXT, sellertxt);
					} else if (!datetxt.isEmpty()) {
						if (!ToDate.isEmpty()) {
							checker = 4;
							customerDetail = DB.RangeSearch(datetxt, ToDate, sellertxt);
							list = DB.RangeSearch(datetxt, ToDate, sellertxt);
						} else {
							checker = 3;
							customerDetail = DB.CustomerDateBill(datetxt, sellertxt);
							list = DB.CustomerDateBill(datetxt, sellertxt);
						}
					} else if (!billIDtxt.isEmpty()) {
						checker = 5;
						Integer billID;
						billID = Integer.parseInt(billIDtxt);
						customerDetail = DB.CustomerBillID(billID, sellertxt);
						list = DB.CustomerBillID(billID, sellertxt);
					} else {
						customerDetail = DB.CustomerInformation(sellertxt);
						list = DB.CustomerInformation(sellertxt);
						Toast.makeText(bill_management.this, "Error", Toast.LENGTH_SHORT).show();
					}

                    /*

                    CHECKERS
                    1 = name
                    2 = contact
                    3 = date
                    4 = toDate
                    5 = billid

                     */

					if (customerDetail.getCount() == 0) {
						Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
					} else {
						if (checker != 5) {
							document.add(table1);
							customerDetail.moveToFirst();
							do {
								//  Printing bill heading ==========================================
								float cWidth5[] = {142, 142, 142, 142};
								Table table5 = new Table(cWidth5);

								table5.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerName")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerNumber")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

								table5.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().add(new Paragraph(date_convertor.convertDateFormat(customerDetail.getString(customerDetail.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy") + "").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().setBorder(Border.NO_BORDER));

								table5.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("billId")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().setBorder(Border.NO_BORDER));
								table5.addCell(new Cell().setBorder(Border.NO_BORDER));
								document.add(table5);
								table5.flushContent();
								//  printing bill details ==========================================
								if (haveGST) {
									float cWidth2[] = {120, 90, 110, 80, 80, 90};
									table2 = new Table(cWidth2);

								} else {
									float cWidth2[] = {270, 100, 100, 100};
									table2 = new Table(cWidth2);
								}

								int index = 0;

								table2.addCell(new Cell().add(new Paragraph("Product Name")));
								table2.addCell(new Cell().add(new Paragraph("Product Price")));
								table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
								if (haveGST) {
									table2.addCell(new Cell().add(new Paragraph("CGST")));
									table2.addCell(new Cell().add(new Paragraph("SGST")));
								}
								table2.addCell(new Cell().add(new Paragraph("Sub Total")));

								float TotalGST = 0f;

								int total = 0;

								if (list.getCount() == 0) {
									Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
									return;
								} else {
									list.moveToFirst();
									do {
										if (customerDetail.getString(customerDetail.getColumnIndex("billId")).equals(list.getString(list.getColumnIndex("billId")))) {
											if (haveGST) {
												String gst;
												if (list.getString(list.getColumnIndex("Gst")).equals("")) {
													gst = "0";
												} else {
													gst = list.getString(list.getColumnIndex("Gst"));
												}
												float tax = ((Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("quantity")))) * (Integer.parseInt(String.valueOf(gst)) / 100f)));
												TotalGST += tax;
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")) + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")) + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")) + "")));
												table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
												table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")) + "")));
											} else {
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")) + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")) + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")) + "")));
												table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")) + "")));
											}
											index++;
											total += list.getInt(list.getColumnIndex("subtotal"));
										}
									} while (list.moveToNext());
								}

								if (haveGST) {
									table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
									table2.addCell(new Cell().add(new Paragraph(TotalGST + "")));
									table2.addCell(new Cell().add(new Paragraph(total + "")));
									table2.addCell(new Cell(2, 6).setBorder(Border.NO_BORDER)); // adding space
									table2.addCell(new Cell(0, 6).setBold());// adding line
								} else {
									table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
									table2.addCell(new Cell().add(new Paragraph(total + "")));
									table2.addCell(new Cell(2, 4).setBorder(Border.NO_BORDER)); // adding space
									table2.addCell(new Cell(0, 4).setBold());// adding line
								}

								int ix = customerDetail.getPosition() + index - 1;

								customerDetail.moveToPosition(ix);

								document.add(table2);
								table2.flushContent();

							} while (customerDetail.moveToNext());
						}
						//  printing bills in different format CHECKER = 5 =========================
						else {
							//  Printing headings ==================================================
							customerDetail.moveToFirst();

							table3.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerName")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerNumber")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

							table3.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(date_convertor.convertDateFormat(customerDetail.getString(customerDetail.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy") + "").setFontSize(14)).setBorder(Border.NO_BORDER));

							table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("billId")) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
							//  Printing bill details ==============================================
							table2.addCell(new Cell().add(new Paragraph("Product Name")));
							table2.addCell(new Cell().add(new Paragraph("Product Price")));
							table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
							if (haveGST) {
								table2.addCell(new Cell().add(new Paragraph("CGST")));
								table2.addCell(new Cell().add(new Paragraph("SGST")));
							}
							table2.addCell(new Cell().add(new Paragraph("Sub Total")));

							float TotalGST = 0f;

							customerDetail.moveToFirst();

							int total = 0;

							if (list.getCount() == 0) {
								Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
								return;
							} else {
								list.moveToFirst();
								do {
									if (haveGST) {
										String gst;
										if (list.getString(list.getColumnIndex("Gst")).equals("")) {
											gst = "0";
										} else {
											gst = list.getString(list.getColumnIndex("Gst"));
										}
										float tax = ((Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(list.getString(list.getColumnIndex("quantity")))) * (Integer.parseInt(String.valueOf(gst)) / 100f)));
										TotalGST += tax;
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")) + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")) + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")) + "")));
										table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
										table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")) + "")));
									} else {
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("product")) + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("price")) + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("quantity")) + "")));
										table2.addCell(new Cell().add(new Paragraph(list.getString(list.getColumnIndex("subtotal")) + "")));
									}
									total += list.getInt(list.getColumnIndex("subtotal"));
								} while (list.moveToNext());
							}

							if (haveGST) {
								table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
								table2.addCell(new Cell().add(new Paragraph(TotalGST + "")));
								table2.addCell(new Cell().add(new Paragraph(total + "")));
							} else {
								table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
								table2.addCell(new Cell().add(new Paragraph(total + "")));
							}

						}

//                        Adding border at the end of each bill !
						END_Border.addCell(new Cell());

//                        Adding Signature =========================================================
						END.addCell(new Cell().add(new Paragraph("Signature: ")).setBorder(Border.NO_BORDER));
//                        ---------------------------Working----------------------------------------
//                Displaying data

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

						//  Opening PDf ============================================================
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
							if (file.exists()) {
								Uri uri = FileProvider.getUriForFile(bill_management.this, getApplicationContext().getPackageName() + ".provider", file);
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setDataAndType(uri, "application/pdf");
								intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
								startActivity(intent);
							} else {
								Toast.makeText(bill_management.this, "File can't be created", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			}
		});

//      PDF Button =================================================================================
		pdf = findViewById(R.id.pdfC);
		pdf.setVisibility(View.GONE);

//        Show ALl Customer Button =================================================================
		showbtn = findViewById(R.id.showallData);

		showbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Cursor res = DB.CustomerInformation(sellertxt);
				if (res.getCount() == 0) {
					Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
					return;
				}

				StringBuffer buffer = new StringBuffer();
				while (res.moveToNext()) {
					String formattedDate = date_convertor.convertDateFormat(res.getString(res.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy");
//                    DATE | name | number | Total |
					buffer.append("Bill ID = " + res.getString(res.getColumnIndex("billId")) + "\n");
					buffer.append("Customer Name = " + res.getString(res.getColumnIndex("customerName")) + "\n");
					buffer.append("Customer Number = " + res.getString(res.getColumnIndex("customerNumber")) + "\n");
					buffer.append("Date = " + formattedDate + "\n");
					buffer.append("Total = " + res.getString(res.getColumnIndex("total")) + "\n\n");
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(bill_management.this);
				builder.setCancelable(true);
				builder.setTitle("Bills");
				builder.setMessage(buffer.toString());
				builder.show();

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


	@Override
	public void onBackPressed() {
		finish();
	}


	@Override
	protected void onStart() {
		super.onStart();
		//        Google ads code --------------------------------------------------------------------------
		AdView mAdView;
		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

	}

	@Override
	protected void onRestart() {
		super.onRestart();
//        Google ads code --------------------------------------------------------------------------
		AdView mAdView;
		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

	}

	@Override
	protected void onResume() {
		super.onResume();
//        Google ads code --------------------------------------------------------------------------
		AdView mAdView;
		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

	}

}