package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class ShowList extends AppCompatActivity {
    private ArrayList<String> ainput, aprice, aquantity, asubtotal;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private DBManager DB = new DBManager(this);

    String filename;

    private StorageVolume storageVolume;

    private Button back, save, display, pdf, addmore;

    final int[] save_CLicked = {0};

    @SuppressLint({"MissingInflatedId", "WrongViewCast", "SuspiciousIndentation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

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
//--------------------------------------------------------------------------------------------------

//        Getting all data -------------------------------------------------------------------------
        String cName, cNumber, date;
        int billId;

    //        GETTING INTENT DATA
        Bundle name = getIntent().getExtras();
        cName = name.getString("cName");

        Bundle num = getIntent().getExtras();
        cNumber = num.getString("cNumber");

        Bundle dat = getIntent().getExtras();
        date = dat.getString("date");

        Bundle bID = getIntent().getExtras();
        billId = bID.getInt("billId");

//--------------------------------------------------------------------------------------------------

        // add more button
        addmore = findViewById(R.id.addMore);


//      SAVE button --------------------------------------------------------------------------------
        save = findViewById(R.id.print);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_CLicked[0]++;
            //                INSERTING data in customer table
                boolean check;

//                String id = String.valueOf(billId);

                check = DB.InsertCustomer(billId, cName, cNumber, date, sellertxt, 0);

                if (check) {

                    Boolean update;

                    update = DB.removeSell(billId,sellertxt);

                    if(update){
                        save.setVisibility(View.INVISIBLE);
                        pdf.setVisibility(View.VISIBLE);
                        back.setVisibility(View.VISIBLE);
                        display.setVisibility(View.VISIBLE);
                        addmore.setVisibility(View.INVISIBLE);
                        Toast.makeText(ShowList.this, "Saved ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ShowList.this, "Error While managing Stock", Toast.LENGTH_SHORT).show();
                    }



                } else {
                    Toast.makeText(ShowList.this, "Error ", Toast.LENGTH_SHORT).show();
                }
            }
        });
//--------------------------------------------------------------------------------------------------

//    Back button ----------------------------------------------------------------------------------
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ShowList.this, home.class);
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
                Intent intent3 = new Intent(ShowList.this, Additems.class);
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

                Cursor res = DB.billTotal(billId);
                if (res.getCount() == 0) {
                    Toast.makeText(ShowList.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                //                    DATE | name | number | Total |
                    buffer.append("Bill ID = " + res.getString(0) + "\n");
                    buffer.append("Customer Name = " + res.getString(1) + "\n");
                    buffer.append("Customer Number = " + res.getString(2) + "\n");
                    buffer.append("Date = " + res.getString(3) + "\n");
                    buffer.append("Total = " + res.getString(4) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowList.this);
                builder.setCancelable(true);
                builder.setTitle("Bill");
                builder.setMessage(buffer.toString());
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
                createPDF() throws FileNotFoundException {

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

                    float cWidth[] = {120, 220, 120, 100};
                    Table table1 = new Table(cWidth);

//        Table 1 do this
//        Want users||||| NAME EMail GST ADDRESS NUMBER
//                           0  1     3   5       4

                    Cursor selerDATA = DB.GetUser(sellertxt);
                    if (selerDATA.getCount() == 0) {
                        Toast.makeText(ShowList.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        selerDATA.moveToFirst();
                        do {
                            table1.addCell(new Cell().add(new Paragraph("Seller Name").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(0) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph("Seller Email").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(1) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph("Seller Number").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(4) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph("Seller GST").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(3) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph("Address").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(5) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                        } while (selerDATA.moveToNext());
                    }

//        Table 2 do this    FROM BILLID
//        Want display ||||||  customerName=5 customerNumber=6 date=7

                    float cWidth3[] = {142, 142, 142, 142};
                    Table table3 = new Table(cWidth3);

                    int total = 0;

                    Cursor customerDetail = DB.billTotal(billId);
                    if (customerDetail.getCount() == 0) {
                        Toast.makeText(ShowList.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        customerDetail.moveToFirst();
                        do {
                            table3.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(1) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(2) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(3) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            total = customerDetail.getInt(4);
                            table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(billId + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                        } while (customerDetail.moveToNext());

                    }

//        Table 3 do this
//        Want display |||||| product=1 price=2 quantity=3 subtotal=4 TOTAL

                    float cWidth2[] = {270, 100, 100, 100};
                    Table table2 = new Table(cWidth2);

                    table2.addCell(new Cell().add(new Paragraph("Product Name")));
                    table2.addCell(new Cell().add(new Paragraph("Product Price")));
                    table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
                    table2.addCell(new Cell().add(new Paragraph("Sub Total")));

                    Cursor list = DB.displayList(billId);
                    if (list.getCount() == 0) {
                        Toast.makeText(ShowList.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        list.moveToFirst();
                        do {
                            table2.addCell(new Cell().add(new Paragraph(list.getString(1) + "")));
                            table2.addCell(new Cell().add(new Paragraph(list.getString(2) + "")));
                            table2.addCell(new Cell().add(new Paragraph(list.getString(3) + "")));
                            table2.addCell(new Cell().add(new Paragraph(list.getString(4) + "")));
                        } while (list.moveToNext());
                    }

                    table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
                    table2.addCell(new Cell().add(new Paragraph(total + "")));

//                Displaying data
                    document.add(table1);
                    document.add(new Paragraph("\n"));
                    document.add(table3);
                    document.add(new Paragraph("\n"));
                    document.add(table2);
                    document.close();
                    Toast.makeText(ShowList.this, "PDF Created", Toast.LENGTH_SHORT).show();

//                Opening PDf ---------------------------------

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        if (file.exists()) {
//                            Uri uri = Uri.parse(storageVolume.getDirectory()+"/Documents/"+filename);

                            Uri uri = FileProvider.getUriForFile(ShowList.this, getApplicationContext().getPackageName() + ".provider", file);
                            Log.d("pdfPath", "" + uri);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ShowList.this, "File can't be created", Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            }

        });
//--------------------------------------------------------------------------------------------------

//  Recycler View ----------------------------------------------------------------------------------

//        aindex = new ArrayList<>();
        ainput = new ArrayList<>();
        aprice = new ArrayList<>();
        aquantity = new ArrayList<>();
        asubtotal = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new MyAdapter(ShowList.this, ainput, aprice, aquantity, asubtotal);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(ShowList.this));

        Cursor cursor = DB.displayList(billId);
        if (cursor.getCount() == 0) {
            Toast.makeText(ShowList.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
//                aindex.add(cursor.getString(0));
                ainput.add(cursor.getString(1));
                aprice.add(cursor.getString(2));
                aquantity.add(cursor.getString(3));
                asubtotal.add(cursor.getString(4));
            }
        }
    }
//--------------------------------------------------------------------------------------------------

//     Making Sure User Saved Data Before Going Back -----------------------------------------------
    @Override
    public void onBackPressed() {
        if (save_CLicked[0] == 0) {
            Toast.makeText(this, "Click on Save and Then Press Back", Toast.LENGTH_SHORT).show();
        } else {
            //        Intent data
            Bundle seller = getIntent().getExtras();
            String sellertxt = seller.getString("seller");

            Intent intent2 = new Intent(ShowList.this, home.class);
            intent2.putExtra("Email", sellertxt);
            startActivity(intent2);
            finish();
        }
    }
//--------------------------------------------------------------------------------------------------
}