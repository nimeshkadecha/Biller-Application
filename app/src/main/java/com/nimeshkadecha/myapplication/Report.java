package com.nimeshkadecha.myapplication;

import static com.itextpdf.kernel.pdf.PdfName.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class Report extends AppCompatActivity {

    DBManager dbLocal = new DBManager(this);

    private String blockCharacterSet = " (){}[]:;'//.,-<>?+₹`@~#^|$%&*!=";

    private final InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle bundle = getIntent().getExtras();
        String Seller_email = bundle.getString("seller");

        //        Removing Suport bar / top line containing name--------------------------------------------
        Objects.requireNonNull(getSupportActionBar()).hide();

        Cursor getinfo = dbLocal.getInventory(Seller_email);

        if (getinfo.getCount() <= 0) {
            Toast.makeText(this, "You don't have any stock available", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "GoTo manage stock to enter Stock", Toast.LENGTH_LONG).show();
            Intent i = new Intent(Report.this, home.class);
            startActivity(i);
        }

        //        Finding edittext product name
        AutoCompleteTextView itemName = findViewById(R.id.report_product_name);

        itemName.setFilters(new InputFilter[]{filter}); // Adding Filter
//        Adding Suggestion [Autocomplete textview]
        String[] products;

        Cursor productsC = dbLocal.getInventory(Seller_email);

        productsC.moveToFirst();
        if (productsC.getCount() > 0) {
            products = new String[productsC.getCount()];
            int i = 0;
            do {
                products[i] = productsC.getString(0);
                i++;

            } while (productsC.moveToNext());
        } else {
            products = new String[]{"NO Suggestion Available"};
        }

        itemName.setAdapter(new ArrayAdapter<>(Report.this, android.R.layout.simple_list_item_1, products));

        AutoCompleteTextView catagory = findViewById(R.id.report_Catagory_name);

        //        Adding Suggestion [Autocomplete textview]
        String[] NameSuggestion;
        String[] Names;

        Cursor Name_Sugg = dbLocal.getCategory(Seller_email);
        Name_Sugg.moveToFirst();
        if (Name_Sugg.getCount() > 0) {
            int i = 0;
            boolean insert = true;
//            Log.d("ENimesh", "Count = " + Name_Sugg.getCount());
            NameSuggestion = new String[Name_Sugg.getCount()];
            do {
                if (i != 0) {
                    for (int j = 0; j < i; j++) {
//                        Log.d("ENimesh", "cursor data equality = " + Name_Sugg.getString(1));
                        if (NameSuggestion[j].equals(Name_Sugg.getString(2))) {
                            insert = false;
                            break;
                        }else{
                            insert = true;
                        }
                    }
                }
//                Log.d("ENimesh", "loop i = " + i);
                if (insert) {
                    NameSuggestion[i] = Name_Sugg.getString(2);
//                    Log.d("ENimesh", "NameSuggestion = " + NameSuggestion[i]);
                    i++;
                }
            } while (Name_Sugg.moveToNext());
//            Log.d("ENimesh", "i = " + i);

            Names = new String[i];
            for (int j = 0; j < i; j++) {
                Names[j] = NameSuggestion[j];
//                Log.d("ENimesh", "Names = " + Names[j]);
            }
        } else {
            Names = new String[]{"No Data"};
        }

        catagory.setAdapter(new ArrayAdapter<>(Report.this, android.R.layout.simple_list_item_1, Names));


//        show report button
        Button showReport = findViewById(R.id.showRecord);

        showReport.setOnClickListener(v -> {
            if (!itemName.getText().toString().trim().equals("")) {
                StringBuffer buffer = new StringBuffer();

                Cursor ReportCursor = dbLocal.viewProductHistory(Seller_email, itemName.getText().toString());

                if (ReportCursor.getCount() <= 0) {
                    Toast.makeText(this, "History Doesn't Exists", Toast.LENGTH_SHORT).show();
                } else {
                    ReportCursor.moveToFirst();

                    buffer.append("Product = " + itemName.getText().toString() + "\n\n");
                    do {
                        buffer.append("Date = " + ReportCursor.getString(5) + "\n");
                        buffer.append("Buy Price = " + ReportCursor.getString(3) + "\n");
                        buffer.append("Sell Price = " + ReportCursor.getString(4) + "\n");
                        buffer.append("Quantity = " + ReportCursor.getString(6) + "\n");
                        buffer.append("Category = " + ReportCursor.getString(2) + "\n\n");
                    } while (ReportCursor.moveToNext());

                    AlertDialog.Builder builder = new AlertDialog.Builder(Report.this);
                    builder.setCancelable(true);
                    builder.setTitle("Stock Report");
                    builder.setMessage(buffer.toString());
                    builder.show();
                }
            } else if(!catagory.getText().toString().equals("")) {
                StringBuffer buffer = new StringBuffer();

                Cursor ReportCursor = dbLocal.viewCategoryHistory(Seller_email, catagory.getText().toString());

                if (ReportCursor.getCount() <= 0) {
                    Toast.makeText(this, "History Doesn't Exists", Toast.LENGTH_SHORT).show();
                } else {
                    ReportCursor.moveToFirst();

                    buffer.append("Category = " + catagory.getText().toString() + "\n\n");
                    do {
                        buffer.append("Date = " + ReportCursor.getString(5) + "\n");
                        buffer.append("Buy Price = " + ReportCursor.getString(3) + "\n");
                        buffer.append("Sell Price = " + ReportCursor.getString(4) + "\n");
                        buffer.append("Quantity = " + ReportCursor.getString(6) + "\n");
                        buffer.append("Product = " + ReportCursor.getString(1) + "\n\n");
                    } while (ReportCursor.moveToNext());

                    AlertDialog.Builder builder = new AlertDialog.Builder(Report.this);
                    builder.setCancelable(true);
                    builder.setTitle("Stock Report");
                    builder.setMessage(buffer.toString());
                    builder.show();
                }
            }else{
                Toast.makeText(this, "Fill Any one of the above filed", Toast.LENGTH_SHORT).show();
            }


        });


//Chart Button
        Button qty_chart = findViewById(R.id.QTY_Chart_BTN);
        qty_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoReport = new Intent(Report.this, chart.class);
                gotoReport.putExtra("seller", Seller_email);
                gotoReport.putExtra("intent", "qty_chart");

                startActivity(gotoReport);
            }
        });

    }
}