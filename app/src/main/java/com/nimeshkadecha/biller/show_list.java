package com.nimeshkadecha.biller;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class show_list extends AppCompatActivity {
	final int[] save_CLicked = {0};
	String filename;
	private final DBManager DB = new DBManager(this);
	private Button back, save, display, pdf, addmore, checkPrice;
	int billId;
	String sellertxt;
	@SuppressLint({"MissingInflatedId", "WrongViewCast", "SuspiciousIndentation", "Range"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_list);

//        Getting data from intent =================================================================
		Bundle seller = getIntent().getExtras();
		assert seller != null;
		sellertxt = seller.getString("seller");
//==================================================================================================

//      WORKING WITH TOOLBAR =======================================================================
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
//          Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//==================================================================================================

//      Finding ====================================================================================
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
//==================================================================================================

//        Getting all data =========================================================================
		String cName, cNumber, date;

		//        GETTING INTENT DATA
		Bundle bundle = getIntent().getExtras();
		cName = bundle.getString("cName");

		cNumber = bundle.getString("cNumber");

		date = bundle.getString("date");

		billId = bundle.getInt("billId");

//==================================================================================================

		// add more button
		addmore = findViewById(R.id.addMore);

//      SAVE button ================================================================================
		save = findViewById(R.id.print);
		save.setOnClickListener(v -> {

			save_CLicked[0]++; // to let user exit

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
		});
//==================================================================================================

//    Back button ==================================================================================
		back.setOnClickListener(v -> {
			Intent intent2 = new Intent(show_list.this, home.class);
			intent2.putExtra("Email", sellertxt);
			startActivity(intent2);
			finish();
		});
//==================================================================================================

//        Addmore button ===========================================================================
		addmore.setOnClickListener(v -> {
			Intent intent3 = new Intent(show_list.this, add_product.class);
			intent3.putExtra("seller", sellertxt);
			intent3.putExtra("cName", cName);
			intent3.putExtra("cNumber", cNumber);
			intent3.putExtra("date", date);
			intent3.putExtra("billId", billId);
			intent3.putExtra("origin", "addmore");
			startActivity(intent3);
			finish();
		});
//==================================================================================================

//        Display Button ===========================================================================
		display.setOnClickListener(v -> {

			Cursor res = DB.BillTotal(billId);
			if (res.getCount() == 0) {
				Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
				return;
			}

			StringBuilder buffer = new StringBuilder();
			while (res.moveToNext()) {

				String formattedDate = date_convertor.convertDateFormat(res.getString(res.getColumnIndex("date")), "yyyy-MM-dd", "dd/MM/yyyy");
				//                    DATE | name | number | Total |
				buffer.append("Bill ID = ").append(res.getString(res.getColumnIndex("billId"))).append("\n");
				buffer.append("Customer Name = ").append(res.getString(res.getColumnIndex("customerName"))).append("\n");
				buffer.append("Customer Number = ").append(res.getString(res.getColumnIndex("customerNumber"))).append("\n");
				buffer.append("Date = ").append(formattedDate).append("\n");
				buffer.append("Total = ").append(convertScientificToNormal(res.getDouble(res.getColumnIndex("total")))).append("\n\n");
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(show_list.this);
			builder.setCancelable(true);
			builder.setTitle("Bill");
			builder.setMessage(buffer.toString());
			builder.show();

		});
//==================================================================================================

//        Check Total Button =======================================================================

		checkPrice.setOnClickListener(view -> {

			String total = DB.CheckTotal(billId);
			AlertDialog.Builder builder = new AlertDialog.Builder(show_list.this);
			builder.setCancelable(true);
			builder.setTitle("Quick Total");
			builder.setMessage("Current total = " + total);
			builder.show();
		});

//==================================================================================================
		StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
		List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

		StorageVolume storageVolume = storageVolumes.get(0);

//        Creating PDF button ======================================================================
		pdf.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("IntentReset")
			@RequiresApi(api = Build.VERSION_CODES.Q)
			@Override
			public void onClick(View view) {
				try {
					createPDF c = new createPDF();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
//==================================================================================================

//  Recycler View ==================================================================================

		ArrayList<String> aindex = new ArrayList<>();
		ArrayList<String> ainput = new ArrayList<>();
		ArrayList<String> aprice = new ArrayList<>();
		ArrayList<String> aquantity = new ArrayList<>();
		ArrayList<String> asubtotal = new ArrayList<>();
		ArrayList<String> aGST = new ArrayList<>();
		RecyclerView recyclerView = findViewById(R.id.recyclerview);
		adapter_showList adapter = new adapter_showList(show_list.this, ainput, aprice, aquantity, asubtotal, aindex, aGST);
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
//==================================================================================================

	//     Making Sure User Saved Data Before Going Back -----------------------------------------------
	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {

		if (save_CLicked[0] == 0) {
			Toast.makeText(this, "Click on Save and Then Press Back", Toast.LENGTH_SHORT).show();
		} else {
			//        Intent data
			Bundle seller = getIntent().getExtras();
			assert seller != null;
			String sellertxt = seller.getString("seller");

			Intent intent2 = new Intent(show_list.this, home.class);
			intent2.putExtra("Email", sellertxt);
			startActivity(intent2);
			finish();
		}
	}
//==================================================================================================

	@SuppressLint("DefaultLocale")
	private String getrandom() {
		Random rnd = new Random();
		int otp = rnd.nextInt(999999999);
		return String.format("%09d", otp);
	}
//==================================================================================================


//         Creating PDF ============================================================================

	class createPDF extends Thread {
		@SuppressLint("Range")
		createPDF() throws IOException {

			boolean haveGST = false;

			String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//                File name
			String id = String.valueOf(billId);

			String s = getrandom();
//                Create file object
			File file = new File(pdfPath, "biller-" + id + "-" + s + ".pdf");

			filename = "biller-" + id + "-" + s + ".pdf";
			OutputStream outputStream = Files.newOutputStream(file.toPath());

			PdfWriter writer = new PdfWriter(file);
			PdfDocument pdfDocument = new PdfDocument(writer);
			Document document = new Document(pdfDocument);

			float[] cWidth = {560};
			Table table1 = new Table(cWidth);

// Table 1 do this adding seller data -----------------------------------------------------------
//        Want users||||| NAME EMail GST ADDRESS NUMBER
//                           0  1     3   5       4

			Cursor sellerDATA = DB.GetUser(sellertxt);
			if (sellerDATA.getCount() == 0) {
				Toast.makeText(show_list.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
				return;
			} else {
				sellerDATA.moveToFirst();
				do {
					table1.addCell(new Cell().add(new Paragraph(sellerDATA.getString(sellerDATA.getColumnIndex("name"))).setFontSize(32)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
					// Formatting the address with indentation for multiple lines
					String address = sellerDATA.getString(sellerDATA.getColumnIndex("address"));
					StringBuilder formattedAddress = getStringBuilder(address);

					table1.addCell(new Cell().add(new Paragraph(formattedAddress.toString()).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
					table1.addCell(new Cell().add(new Paragraph("E-mail: " + sellerDATA.getString(sellerDATA.getColumnIndex("email"))).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
					table1.addCell(new Cell().add(new Paragraph("Mo: " + sellerDATA.getString(sellerDATA.getColumnIndex("contact"))).setFontSize(14)).setBorder(Border.NO_BORDER));
// --------------------------------------------------------------------------------------------------
					if (!sellerDATA.getString(sellerDATA.getColumnIndex("gst")).equals("-1")) {
						haveGST = true;
						table1.addCell(new Cell().add(new Paragraph("GSTIN: " + sellerDATA.getString(sellerDATA.getColumnIndex("gst"))).setFontSize(14)).setBorder(Border.NO_BORDER));
					}
// --------------------------------------------------------------------------------------------------
					table1.addCell(new Cell());
				} while (sellerDATA.moveToNext());
			}

// Table 2 writing customer data ----------------------------------------------------------------
//        Want display ||||||  customerName=5 customerNumber=6 date=7

			float[] cWidth3 = {142, 142, 142, 142};
			Table table3 = new Table(cWidth3);

			double total;

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
					total = customerDetail.getDouble(customerDetail.getColumnIndex("total"));
					table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
					table3.addCell(new Cell().add(new Paragraph(billId + "").setFontSize(14)).setBorder(Border.NO_BORDER));
				} while (customerDetail.moveToNext());

			}

//        Table 3 do this
//        Want display |||||| product=1 price=2 quantity=3 subtotal=4 TOTAL

			Table table2;
			float[] cWidth2_1;
			if (haveGST) {
				cWidth2_1 = new float[]{120, 90, 100, 85, 85, 90};

			} else {
				cWidth2_1 = new float[]{270, 100, 100, 100};
			}
			table2 = new Table(cWidth2_1);

			float[] cWidth6 = {560};
			Table END = new Table(cWidth6);

			// Adding Header --------------------------------------------------------------------------------
			table2.addCell(new Cell().add(new Paragraph("Product Name")));
			table2.addCell(new Cell().add(new Paragraph("Product Price")));
			table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
			if (haveGST) {
				table2.addCell(new Cell().add(new Paragraph("CGST")));
				table2.addCell(new Cell().add(new Paragraph("SGST")));
			}
			table2.addCell(new Cell().add(new Paragraph("Sub Total")));

			double TotalGST = 0d;

			// Adding data ----------------------------------------------------------------------------------
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
						float tax = ((Float.parseFloat(String.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("price")))) * Integer.parseInt(String.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))) * (Float.parseFloat(String.valueOf(gst)) / 100f)));
						TotalGST += tax;
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("product")))));
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))));
						table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
						table2.addCell(new Cell().add(new Paragraph(tax / 2 + "")));
						table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("subtotal")))))));
					} else {
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("product")))));
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
						table2.addCell(new Cell().add(new Paragraph(displayListCursor.getString(displayListCursor.getColumnIndex("quantity")))));
						table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("subtotal")))))));
					}
				} while (displayListCursor.moveToNext());
			}

			if (haveGST) {
				table2.addCell(new Cell(1, 4).add(new Paragraph("Total")));
				table2.addCell(new Cell().add(new Paragraph(TotalGST + "")));
				table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
			} else {
				table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
				table2.addCell(new Cell().add(new Paragraph(convertScientificToNormal(total))));
			}


			// adding line ----------------------------------------------------------------------------------
			END.addCell(new Cell(2, 4).setBorder(Border.NO_BORDER)); // adding space
			END.addCell(new Cell());// adding line
			END.addCell(new Cell().setBorder(Border.NO_BORDER));// adding line

//                    Adding signature
//			END.addCell(new Cell().add(new Paragraph("Signature: ")).setBorder(Border.NO_BORDER));

//                Displaying data ------------------------------------------------------------------
			document.add(table1);
			document.add(new Paragraph("\n"));
			document.add(table3);
			document.add(new Paragraph("\n"));
			document.add(table2);
			document.add(new Paragraph("\n"));
			document.add(END);
			document.close();

			Toast.makeText(show_list.this, "PDF Created", Toast.LENGTH_SHORT).show();

//                Opening PDf ----------------------------------------------------------------------
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
//==================================================================================================

	// converting scientific notation to normal notation ==============================================
	public static String convertScientificToNormal(double scientificNotation) {
		BigDecimal bd = new BigDecimal(scientificNotation);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}
// =================================================================================================

	// This will Formate address ======================================================================
	private static @NonNull StringBuilder getStringBuilder(String address) {
		Log.d("ENimesh", " address" + address);
		String[] addressLines = address.split("\n");
		StringBuilder formattedAddress = new StringBuilder();

		// Add first line with "Address: "
		formattedAddress.append("Address: ").append(addressLines[0]).append("\n");

		// Add subsequent lines with indentation
		String indentation = "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"; // Adjust the number of non-breaking spaces
		// Add subsequent lines with indentation
		for (int i = 1; i < addressLines.length; i++)
			formattedAddress.append(indentation).append(addressLines[i]).append("\n"); // Adjust the spaces for indentation

		return formattedAddress;
	}
	// ================================================================================================

}