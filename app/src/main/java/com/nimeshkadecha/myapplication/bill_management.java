package com.nimeshkadecha.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_info);

//        Finding Layout for Spinner (DropDown MEnu) ===============================================
        cl = findViewById(R.id.contactlayout);
        dl = findViewById(R.id.datelayout);
        edl = findViewById(R.id.rangedate);
        bl = findViewById(R.id.billIDlayout);
        CN = findViewById(R.id.namelayout);
// =================================================================================================

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
// =================================================================================================

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
// =================================================================================================

//        Finding Edittext =========================================================================
        nameedt = findViewById(R.id.name);
//        Adding Autocomplete Text view LIST
        String[] NameSuggestion;
        String[] Names;
        Cursor Name_Sugg = DB.cusInfo(sellertxt);
        Name_Sugg.moveToFirst();
        if (Name_Sugg.getCount() > 0) {
            int i = 0;
            boolean insert = true;
            Log.d("ENimesh", "Count = " + Name_Sugg.getCount());
            NameSuggestion = new String[Name_Sugg.getCount()];
            do {
                if (i != 0) {
                    for (int j = 0; j < i; j++) {
                        if (NameSuggestion[j].equals(Name_Sugg.getString(1))) {
                            insert = false;
                            break;
                        } else {
                            insert = true;
                        }
                    }
                }

                if (insert) {
                    NameSuggestion[i] = Name_Sugg.getString(1);
                    i++;
                }
            } while (Name_Sugg.moveToNext());

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
        Cursor Num_Sugg = DB.cusInfo(sellertxt);
        Num_Sugg.moveToFirst();
        if (Num_Sugg.getCount() > 0) {
            int i = 0;
            boolean insert = true;
            numberSugg = new String[Num_Sugg.getCount()];
            do {
                if (i != 0) {
                    for (int j = 0; j < i; j++) {
                        if (numberSugg[j].equals(Num_Sugg.getString(2))) {
                            insert = false;
                            break;
                        }
                    }
                }

                if (insert) {
                    numberSugg[i] = Num_Sugg.getString(2);
                    i++;
                }
            } while (Num_Sugg.moveToNext());

            NUmber = new String[i];
            for (int j = 0; j < i; j++) {
                NUmber[j] = numberSugg[j];
            }
        } else {
            NUmber = new String[]{"No DAta"};
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
// =================================================================================================

//        Calculating And Formatting DATE ==========================================================
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
// =================================================================================================

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
// =================================================================================================

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
// =================================================================================================

//      Search Button ==============================================================================
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nametxt, datetxt, billIDtxt, contactTXT, ToDate;

                nametxt = nameedt.getText().toString();
                datetxt = dateedt.getText().toString();
                billIDtxt = billidedt.getText().toString();
                contactTXT = contactedt.getText().toString();
                ToDate = todateedt.getText().toString();


                if (nametxt.isEmpty() && datetxt.isEmpty() && ToDate.isEmpty()) {
                    Toast.makeText(bill_management.this, "Fill at least one information to search", Toast.LENGTH_SHORT).show();
                } else if (datetxt.isEmpty() && !ToDate.isEmpty()) {
                    Toast.makeText(bill_management.this, "Enter Starting Date", Toast.LENGTH_SHORT).show();
                } else {
                    Cursor res;
                    res = DB.cusInfo(sellertxt);
                    if (!nametxt.isEmpty()) {
//                        Toast.makeText(customer_Info.this, "Search by name", Toast.LENGTH_SHORT).show();
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
                                contactTXT = nametxt;
                                contactedt.setText(contactTXT);
                                res = DB.Customernumberbill(contactTXT, sellertxt);
                            } else {
                                Integer billID;
                                billID = Integer.parseInt(nametxt);
//                                billidedt.setText(billID);
                                res = DB.CustomerBillID(billID, sellertxt);
                            }
                        } else {
                            res = DB.CustomerNameBill(nametxt, sellertxt);
                        }
                    } else if (!datetxt.isEmpty()) {
                        if (!ToDate.isEmpty()) {
                            res = DB.rangeSearch(datetxt, ToDate, sellertxt);
                        } else {
                            res = DB.CustomerDateBill(datetxt, sellertxt);
                        }
                    } else {
                        res = DB.cusInfo(sellertxt);
                        Toast.makeText(bill_management.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    if (res.getCount() == 0) {
                        Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int total = 0;
                    StringBuffer buffer = new StringBuffer();
                    while (res.moveToNext()) {
//                    DATE | name | number | Total |
                        buffer.append("Bill ID = " + res.getString(8) + "\n");
                        buffer.append("Customer Name = " + res.getString(5) + "\n");
                        buffer.append("Customer Number = " + res.getString(6) + "\n");
                        buffer.append("Date = " + res.getString(7) + "\n");
                        buffer.append("Product Name = " + res.getString(1) + "\n");
                        buffer.append("Price = " + res.getString(2) + "\n");
                        buffer.append("Quantity = " + res.getString(3) + "\n");
                        buffer.append("Sub Total = " + res.getString(4) + "\n\n");
                        total += Integer.parseInt(res.getString(4));
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

                    builder.setNegativeButton("Delete Listing", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

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

            private void createPDF() throws FileNotFoundException {
                String nametxt, datetxt, billIDtxt, contactTXT, ToDate;

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
//                    String id = String.valueOf(billId);
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
//                            table1.addCell(new Cell().add(new Paragraph("Seller Name").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(0) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph("Seller Name").setFontSize(14)));
                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(0)+"").setFontSize(32)).setBorder(Border.NO_BORDER));
// =================================================================================================
//                            table1.addCell(new Cell().add(new Paragraph("Address").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(5) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph("Address").setFontSize(14)));
                            table1.addCell(new Cell().add(new Paragraph("Address: "+selerDATA.getString(5)+"").setFontSize(14)).setBorder(Border.NO_BORDER));
// =================================================================================================
//                            table1.addCell(new Cell().add(new Paragraph("Seller Email").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(1) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph("Seller Email").setFontSize(14)));
                            table1.addCell(new Cell().add(new Paragraph("E=mail: "+selerDATA.getString(1)+"").setFontSize(14)).setBorder(Border.NO_BORDER));
// =================================================================================================
//                            table1.addCell(new Cell().add(new Paragraph("Seller Number").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(4) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph("Seller Number").setFontSize(14)));
                            table1.addCell(new Cell().add(new Paragraph("Mo: "+selerDATA.getString(4)+"").setFontSize(14)).setBorder(Border.NO_BORDER));
// =================================================================================================
//                            table1.addCell(new Cell().add(new Paragraph("Seller GST").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph(selerDATA.getString(3) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
//                            table1.addCell(new Cell().add(new Paragraph("Seller GST").setFontSize(14)));
                            if(!selerDATA.getString(3).equals("no")) {
                                table1.addCell(new Cell().add(new Paragraph("GST: " + selerDATA.getString(3) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            }
// =================================================================================================
                            table1.addCell(new Cell());

                        } while (selerDATA.moveToNext());
                    }


                    //        Table 2 do this    FROM BILLID
//        Want display ||||||  customerName=5 customerNumber=6 date=7

                    float cWidth3[] = {142, 142, 142, 142};
                    Table table3 = new Table(cWidth3);

                    float cWidth5[] = {142, 142, 142, 142};
                    Table table5 = new Table(cWidth5);

                    float cWidth2[] = {270, 100, 100, 100};
                    Table table2 = new Table(cWidth2);

                    float cWidth6[] = {560};
                    Table END = new Table(cWidth6);


                    Cursor customerDetail;
                    Cursor list;
                    customerDetail = DB.cusInfo(sellertxt);
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
                                Log.d("ENimesh" , "Contact = "+ contactTXT);
//                                contactedt.setText(contactTXT);
                                customerDetail = DB.Customernumberbill(contactTXT, sellertxt);
                                list = DB.Customernumberbill(contactTXT, sellertxt);
                                Log.d("ENimesh" , "Contact length = "+ list.getCount());
                            } else {
                                checker = 5;
                                Integer billID;
                                billID = Integer.parseInt(nametxt);
//                                billidedt.setText(billID);
                                customerDetail = DB.CustomerBillID(billID, sellertxt);
                                list = DB.CustomerBillID(billID, sellertxt);
                            }
                        } else {
                            checker = 1;
                            customerDetail = DB.CustomerNameBill(nametxt, sellertxt);
                            list = DB.CustomerNameBill(nametxt, sellertxt);
                        }


//                        customerDetail = DB.CustomerNameBill(nametxt, sellertxt);
//                        list = DB.CustomerNameBill(nametxt, sellertxt);
                    } else if (!contactTXT.isEmpty()) {
                        checker = 2;
                        customerDetail = DB.Customernumberbill(contactTXT, sellertxt);
                        list = DB.Customernumberbill(contactTXT, sellertxt);
                    } else if (!datetxt.isEmpty()) {
                        if (!ToDate.isEmpty()) {
                            checker = 4;
                            customerDetail = DB.rangeSearch(datetxt, ToDate, sellertxt);
                            list = DB.rangeSearch(datetxt, ToDate, sellertxt);
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
                        customerDetail = DB.cusInfo(sellertxt);
                        list = DB.cusInfo(sellertxt);
                        Toast.makeText(bill_management.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    Log.d("ENimesh","checker = " + checker);


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
                        if (checker == 3 || checker == 4) {
//                            Date & range search
                            customerDetail.moveToFirst();
                            do {
                                table5.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(5) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(6) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

                                table5.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(7) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));

                                table5.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(8) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));

//Just Printing headings -----------------------------------------------------------------------
                                table5.addCell(new Cell().add(new Paragraph("Product Name")));
                                table5.addCell(new Cell().add(new Paragraph("Product Price")));
                                table5.addCell(new Cell().add(new Paragraph("Product Quantity")));
                                table5.addCell(new Cell().add(new Paragraph("Sub Total")));

//                                this index help to privent repit table printing
                                int index = 0;
                                int total = 0;
                                if (list.getCount() == 0) {
                                    Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    list.moveToFirst();
                                    do {
//                                        Index 8 is bill ID
                                        if (customerDetail.getString(8).equals(list.getString(8))) {
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(1) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(2) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(3) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(4) + "")));
                                            total += list.getInt(4);
                                            index++;
                                        }
                                    } while (list.moveToNext());
                                }

                                int ix = customerDetail.getPosition() + index - 1;
                                customerDetail.moveToPosition(ix);

                                table5.addCell(new Cell(1, 3).add(new Paragraph("Total")));
                                table5.addCell(new Cell().add(new Paragraph(total + "")));
                                table5.addCell(new Cell(1, 4).setBorder(Border.NO_BORDER));

                            } while (customerDetail.moveToNext());
// -----------------------------------------------------------------------------------------------------------------------
                        } else if (checker == 1 || checker == 2) {
                            customerDetail.moveToFirst();
                            do {
                                table5.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(5) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(6) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

                                table5.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(7) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));

                                table5.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().add(new Paragraph(customerDetail.getString(8) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));
                                table5.addCell(new Cell().setBorder(Border.NO_BORDER));

//Just Printing headings -----------------------------------------------------------------------
                                table5.addCell(new Cell().add(new Paragraph("Product Name")));
                                table5.addCell(new Cell().add(new Paragraph("Product Price")));
                                table5.addCell(new Cell().add(new Paragraph("Product Quantity")));
                                table5.addCell(new Cell().add(new Paragraph("Sub Total")));

//                                customerDetail.moveToFirst();

//                                this index help to privent repit table printing
                                int index = 0;
                                int total = 0;
                                if (list.getCount() == 0) {
                                    Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    list.moveToFirst();

                                    do {
                                        if (customerDetail.getString(8).equals(list.getString(8))) {
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(1) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(2) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(3) + "")));
                                            table5.addCell(new Cell().add(new Paragraph(list.getString(4) + "")));
                                            total += list.getInt(4);
                                            index++;
                                        }
                                    } while (list.moveToNext());
                                }

                                int ix = customerDetail.getPosition() + index - 1;
                                customerDetail.moveToPosition(ix);

                                table5.addCell(new Cell(1, 3).add(new Paragraph("Total")));
                                table5.addCell(new Cell().add(new Paragraph(total + "")));
                                table5.addCell(new Cell(1, 4).setBorder(Border.NO_BORDER));

                            } while (customerDetail.moveToNext());
                        }
//                        _----------------------------------------------------------------------------
                        else {
                            customerDetail.moveToFirst();

                            table3.addCell(new Cell().add(new Paragraph("Customer Name").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(5) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph("Customer Number").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(6) + "").setFontSize(14)).setBorder(Border.NO_BORDER));

                            if (!ToDate.isEmpty() && !datetxt.isEmpty()) {
                                table3.addCell(new Cell().add(new Paragraph("From Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(7) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table3.addCell(new Cell().add(new Paragraph("To Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table3.addCell(new Cell().add(new Paragraph(ToDate + "").setFontSize(14)).setBorder(Border.NO_BORDER));

                            } else {
                                table3.addCell(new Cell().add(new Paragraph("Date").setFontSize(14)).setBorder(Border.NO_BORDER));
                                table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(7) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
                            }

                            table3.addCell(new Cell().add(new Paragraph("Bill ID").setFontSize(14)).setBorder(Border.NO_BORDER));
                            table3.addCell(new Cell().add(new Paragraph(customerDetail.getString(8) + "").setFontSize(14)).setBorder(Border.NO_BORDER));
////Just Printing headings -----------------------------------------------------------------------
                            table2.addCell(new Cell().add(new Paragraph("Product Name")));
                            table2.addCell(new Cell().add(new Paragraph("Product Price")));
                            table2.addCell(new Cell().add(new Paragraph("Product Quantity")));
                            table2.addCell(new Cell().add(new Paragraph("Sub Total")));

                            customerDetail.moveToFirst();

                            int total = 0;

                            if (list.getCount() == 0) {
                                Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                list.moveToFirst();
                                do {
                                    table2.addCell(new Cell().add(new Paragraph(list.getString(1) + "")));
                                    table2.addCell(new Cell().add(new Paragraph(list.getString(2) + "")));
                                    table2.addCell(new Cell().add(new Paragraph(list.getString(3) + "")));
                                    table2.addCell(new Cell().add(new Paragraph(list.getString(4) + "")));
                                    total += list.getInt(4);
                                } while (list.moveToNext());
                            }

                            table2.addCell(new Cell(1, 3).add(new Paragraph("Total")));
                            table2.addCell(new Cell().add(new Paragraph(total + "")));

                        }
//                        Adding SIgnature ---------------------------------------------
                        END.addCell(new Cell().add(new Paragraph("Signature: ")).setBorder(Border.NO_BORDER));
//                        ---------------------------Working------------------------------------------
//                Displaying data
                        document.add(table1);
                        if (checker <= 4) {
                            document.add(table5);
                        } else {
                            document.add(table3);
                            document.add(new Paragraph("\n"));
                            document.add(table2);
                        }
                        document.add(new Paragraph("\n"));
                        document.add(END);
                        document.close();
                        Toast.makeText(bill_management.this, "PDF Created", Toast.LENGTH_SHORT).show();

//                Opening PDf ---------------------------------
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
// =================================================================================================

//      PDF Button =================================================================================
        pdf = findViewById(R.id.pdfC);
        pdf.setVisibility(View.GONE);
// =================================================================================================
//        Show ALl Customer Button =================================================================
        showbtn = findViewById(R.id.showallData);

        showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Cursor res = DB.cusInfo(sellertxt);
                if (res.getCount() == 0) {
                    Toast.makeText(bill_management.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(bill_management.this);
                builder.setCancelable(true);
                builder.setTitle("Bills");
                builder.setMessage(buffer.toString());
                builder.show();

            }
        });
    }

// =================================================================================================

//  AUTO BACKUP ====================================================================================

    //    On pause
    @Override
    protected void onPause() {
        super.onPause();
        final String SHARED_PREFS = "sharedPrefs";
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        DBManager dbManager = new DBManager(getApplicationContext());
        boolean check = dbManager.AutoLocalBackup(getApplicationContext());
        if(check){
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
        if(check){
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = df.format(c);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("AutoUpload", formattedDate);
            editor.apply();
        }
    }

//  ================================================================================================
    @Override
    public void onBackPressed() {
        finish();
    }
}