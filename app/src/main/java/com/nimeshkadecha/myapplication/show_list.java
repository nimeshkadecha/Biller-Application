package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class show_list extends AppCompatActivity {
	final int[] save_CLicked = {0};
	String filename;
	private ArrayList<String> ainput, aprice, aquantity, asubtotal, aindex, aGST;
	private RecyclerView recyclerView;
	private MyAdapter adapter;
	private final DBManager DB = new DBManager(this);
	private StorageVolume storageVolume;
	private Button back, save, display, pdf, addmore, checkPrice;

	@SuppressLint({"MissingInflatedId", "WrongViewCast", "SuspiciousIndentation", "Range"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_list);

//        Google ads code --------------------------------------------------------------------------
//        AdView mAdView;
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
//  ================================================================================================

//        Getting data from intent -----------------------------------------------------------------
		Bundle seller = getIntent().getExtras();
		String sellertxt = seller.getString("seller");
//--------------------------------------------------------------------------------------------------

//      Finding ------------------------------------------------------------------------------------
		// Display Button
		display = findViewById(R.id.display);
		display.setVisibility(View.INVISIBLE);
		// PDF Button
		pdf = findViewById(R.id.pdf);
		pdf.setVisibility(View.INVISIBLE);
		//    Back button;
		back = findViewById(R.id.Back);
		back.setVisibility(View.INVISIBLE);
		checkPrice = findViewById(R.id.CheckTotalBtn);
//--------------------------------------------------------------------------------------------------

//        Getting all data -------------------------------------------------------------------------
		String cName, cNumber, date;
		int billId;

		//        GETTING INTENT DATA
		Bundle bundle = getIntent().getExtras();
		cName = bundle.getString("cName");

		cNumber = bundle.getString("cNumber");

		date = bundle.getString("date");

		billId = bundle.getInt("billId");

//--------------------------------------------------------------------------------------------------

		// add more button
		addmore = findViewById(R.id.addMore);


//      SAVE button --------------------------------------------------------------------------------
		save = findViewById(R.id.print);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save_CLicked[0]++;

//                Confirm sale Check it there is products in there or not
				boolean confirm_sale = DB.ConfirmSale(billId); // this will check that there is at least 1 item in list

				if (confirm_sale) {
					//                INSERTING data in customer table
					boolean check;

					check = DB.InsertCustomer(billId, cName, cNumber, date, sellertxt, 0);

					if (check) {

						Boolean update;

						update = DB.RemoveSell(billId, sellertxt);

						if (update) {
							save.setVisibility(View.INVISIBLE);
							checkPrice.setVisibility(View.INVISIBLE);
							pdf.setVisibility(View.VISIBLE);
							back.setVisibility(View.VISIBLE);
							display.setVisibility(View.VISIBLE);
							addmore.setVisibility(View.INVISIBLE);
							Toast.makeText(show_list.this, "Saved ", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(show_list.this, "Failed to remove stock", Toast.LENGTH_SHORT).show();
						}


					} else {
						Toast.makeText(show_list.this, "Error ", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(show_list.this, "Please Add some item to proceed", Toast.LENGTH_SHORT).show();
				}
			}
		});
//--------------------------------------------------------------------------------------------------

//    Back button ----------------------------------------------------------------------------------
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent2 = new Intent(show_list.this, home.class);
				intent2.putExtra("Email", sellertxt);
				startActivity(intent2);
				finish();
			}
		});
//--------------------------------------------------------------------------------------------------

//        Addmore button ---------------------------------------------------------------------------
		addmore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent3 = new Intent(show_list.this, add_product.class);
				intent3.putExtra("seller", sellertxt);
				intent3.putExtra("cName", cName);
				intent3.putExtra("cNumber", cNumber);
				intent3.putExtra("date", date);
				intent3.putExtra("billId", billId);
				intent3.putExtra("origin", "addmore");
				startActivity(intent3);
				finish();
			}
		});
//--------------------------------------------------------------------------------------------------

//        Display Button ---------------------------------------------------------------------------
		display.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Cursor res = DB.BillTotal(billId);
				if (res.getCount() == 0) {
					Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
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

				AlertDialog.Builder builder = new AlertDialog.Builder(show_list.this);
				builder.setCancelable(true);
				builder.setTitle("Bill");
				builder.setMessage(buffer.toString());
				builder.show();

			}
		});
//--------------------------------------------------------------------------------------------------

//        Check Total Button ---------------------------------------------------------------------------

		checkPrice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				int total = DB.CheckTotal(billId);
				AlertDialog.Builder builder = new AlertDialog.Builder(show_list.this);
				builder.setCancelable(true);
				builder.setTitle("Quick Total");
				builder.setMessage("Current total = " + total);
				builder.show();
			}
		});

//--------------------------------------------------------------------------------------------------
		StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
		List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

		storageVolume = storageVolumes.get(0);

//        Creating PDF button ----------------------------------------------------------------------
		pdf.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("IntentReset")
			@RequiresApi(api = Build.VERSION_CODES.Q)
			@Override
			public void onClick(View view) {
				try {
					createPDF c = new createPDF();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			@SuppressLint("DefaultLocale")
			private String getrandom() {
				Random rnd = new Random();
				int otp = rnd.nextInt(999999999);
				return String.format("%09d", otp);
			}
//--------------------------------------------------------------------------------------------------


//         Creating PDF ----------------------------------------------------------------------------

			class createPDF extends Thread {
				@SuppressLint("Range")
				createPDF() throws FileNotFoundException {

					boolean haveGST = false;

					String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//                File name
					String id = String.valueOf(billId);

					String s = getrandom();
//                Create file object
					File file = new File(pdfPath, "biller-" + id + "-" + s + ".pdf");

					filename = "biller-" + id + "-" + s + ".pdf";
					OutputStream outputStream = new FileOutputStream(file);

					PdfWriter writer = new PdfWriter(file);
					PdfDocument pdfDocument = new PdfDocument(writer);
					Document document = new Document(pdfDocument);

					float[] cWidth = {560};
					Table table1 = new Table(cWidth);

//        Table 1 do this
//        Want users||||| NAME EMail GST ADDRESS NUMBER
//                           0  1     3   5       4

					Cursor selerDATA = DB.GetUser(sellertxt);
					if (selerDATA.getCount() == 0) {
						Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					} else {
						selerDATA.moveToFirst();
						do {
							table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(selerDATA.getColumnIndex("name"))).setFontSize(32)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
							table1.addCell(new Cell().add(new Paragraph("Address: " + selerDATA.getString(selerDATA.getColumnIndex("address"))).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
							table1.addCell(new Cell().add(new Paragraph("E-mail: " + selerDATA.getString(selerDATA.getColumnIndex("email"))).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
							table1.addCell(new Cell().add(new Paragraph("Mo: " + selerDATA.getString(selerDATA.getColumnIndex("contact"))).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
							if (!selerDATA.getString(selerDATA.getColumnIndex("gst")).equals("-1")) {
								haveGST = true;
								table1.addCell(new Cell().add(new Paragraph("GSTIN: " + selerDATA.getString(selerDATA.getColumnIndex("gst"))).setFontSize(14)).setBorder(Border.NO_BORDER));
							}
// --------------------------------------------------------------------------------------------------
							table1.addCell(new Cell());
						} while (selerDATA.moveToNext());
					}

//        Table 2 do this    FROM BILLID
//        Want display ||||||  customerName=5 customerNumber=6 date=7

					float[] cWidth3 = {142, 142, 142, 142};
					Table table3 = new Table(cWidth3);

					int total = 0;

					Cursor customerDetail = DB.BillTotal(billId);
					if (customerDetail.getCount() == 0) {
						Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					} else {
						customerDetail.moveToFirst();
						do {
							table3.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerName"))).setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(customerDetail.getColumnIndex("customerNumber"))).setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(date_convertor.convertDateFormat(customerDetail.getString(customerDetail.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy")).setFontSize(14)).setBorder(Border.NO_BORDER));
							total = customerDetail.getInt(customerDetail.getColumnIndex("total"));
							table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
							table3.addCell(new Cell().add(new Paragraph(billId + "").setFontSize(14)).setBorder(Border.NO_BORDER));
						} while (customerDetail.moveToNext());

					}

//        Table 3 do this
//        Want display |||||| product=1 price=2 quantity=3 subtotal=4 TOTAL

					Table table2;
					if (haveGST) {
						float[] cWidth2 = {120, 90, 100, 85, 85, 90};
						table2 = new Table(cWidth2);

					} else {
						float[] cWidth2 = {270, 100, 100, 100};
						table2 = new Table(cWidth2);
					}

					float[] cWidth6 = {560};
					Table END = new Table(cWidth6);

					table2.addCell(new Cell().add(new Paragraph("Product Name")));
					table2.addCell(new Cell().add(new Paragraph("Product Price")));
					table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
					if (haveGST) {
						table2.addCell(new Cell().add(new Paragraph("CGST")));
						table2.addCell(new Cell().add(new Paragraph("SGST")));
					}
					table2.addCell(new Cell().add(new Paragraph("Sub Total")));

					float TotalGST = 0f;

					Cursor displayListCursor = DB.DisplayList(billId);
					if (displayListCursor.getCount() == 0) {
						Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
						return;
					} else {
						displayListCursor.moveToFirst();
						do {
							if (haveGST) {
								String gst;
								if (displayListCursor.getString(displayListCursor.getColumnIndex("Gst")).equals("-1")) {
									gst = "0";
								} else {
									gst = displayListCursor.getString(displayListCursor.getColumnIndex("Gst"));
								}
								float tax = ((Integer.parseInt(String.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))) * (Integer.parseInt(String.valueOf(gst)) / 100f)));
								TotalGST += tax;
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("product")))));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))));
								table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
								table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("subtotal")))));
							} else {
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("product")))));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))));
								table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("subtotal")))));
							}
						} while (displayListCursor.moveToNext());
					}

					if (haveGST) {
						table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
						table2.addCell(new Cell().add(new Paragraph(TotalGST + "")));
						table2.addCell(new Cell().add(new Paragraph(total + "")));
					} else {
						table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
						table2.addCell(new Cell().add(new Paragraph(total + "")));
					}


//                    Adding signature
//                    END.addCell(new Cell(2,4).setBorder(Border.NO_BORDER)); // adding space
					END.addCell(new Cell());// adding line
					END.addCell(new Cell().setBorder(Border.NO_BORDER));// adding line
					END.addCell(new Cell().add(new Paragraph("Signature: ")).setBorder(Border.NO_BORDER));

//                Displaying data
					document.add(table1);
					document.add(new Paragraph("\n"));
					document.add(table3);
					document.add(new Paragraph("\n"));
					document.add(table2);
					document.add(new Paragraph("\n"));
					document.add(END);
					document.close();
					Toast.makeText(show_list.this, "PDF Created", Toast.LENGTH_SHORT).show();

//                Opening PDf ---------------------------------

					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
						if (file.exists()) {

							Uri uri = FileProvider.getUriForFile(show_list.this, getApplicationContext().getPackageName() + ".provider", file);


							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(uri, "application/pdf");
							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
							startActivity(intent);
						} else {
							Toast.makeText(show_list.this, "File can't be created", Toast.LENGTH_SHORT).show();
						}

					}

				}
			}

		});
//--------------------------------------------------------------------------------------------------

//  Recycler View ----------------------------------------------------------------------------------

		aindex = new ArrayList<>();
		ainput = new ArrayList<>();
		aprice = new ArrayList<>();
		aquantity = new ArrayList<>();
		asubtotal = new ArrayList<>();
		aGST = new ArrayList<>();
		recyclerView = findViewById(R.id.recyclerview);
		adapter = new MyAdapter(show_list.this, ainput, aprice, aquantity, asubtotal, aindex, aGST);
		recyclerView.setAdapter(adapter);

		recyclerView.setLayoutManager(new LinearLayoutManager(show_list.this));

		Cursor displayCursorRV = DB.DisplayList(billId);
		displayCursorRV.moveToFirst();
		if (displayCursorRV.getCount() == 0) {
			Toast.makeText(show_list.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
		} else {
			do {
				aindex.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("indexs")));
				ainput.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("product")));
				aprice.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("price")));
				aquantity.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("quantity")));
				asubtotal.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("subtotal")));
				if (displayCursorRV.getString(displayCursorRV.getColumnIndex("Gst")).equals("-1")) {
					aGST.add("0");
				} else {
					aGST.add(displayCursorRV.getString(displayCursorRV.getColumnIndex("Gst")));
				}

			} while (displayCursorRV.moveToNext());
		}
	}
//--------------------------------------------------------------------------------------------------

	//     Making Sure User Saved Data Before Going Back -----------------------------------------------
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (save_CLicked[0] == 0) {
			Toast.makeText(this, "Click on Save and Then Press Back", Toast.LENGTH_SHORT).show();
		} else {
			//        Intent data
			Bundle seller = getIntent().getExtras();
			String sellertxt = seller.getString("seller");

			Intent intent2 = new Intent(show_list.this, home.class);
			intent2.putExtra("Email", sellertxt);
			startActivity(intent2);
			finish();
		}
	}
//--------------------------------------------------------------------------------------------------

//
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