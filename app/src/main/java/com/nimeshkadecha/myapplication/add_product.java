package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class add_product extends AppCompatActivity {

    //    toolbar and navagation drawer starts;
    private ImageView menu;
    private Button Add;
    //    toolbar and navagation drawer ends;

    //    for insert operation and outher stuf
    Button show;
    private EditText price, quantity;

    private AutoCompleteTextView productName;
    DBManager DB = new DBManager(this);

    String cNametxt, cNumbertxt, datetext, sellertxt, origintxt;
    int billIdtxt;

    //    In input Filter ==========================================================================
    String blockCharacterSet = " =(){}[]:;'//.,-<>?+₹`@~#^|$%&*!";

    private final InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @SuppressLint({"MissingInflatedId", "Range"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        //        Google ads code ==================================================================
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //        Finding buttons ==================================================================
        Add = findViewById(R.id.button3);
        show = findViewById(R.id.show);

        //        Working with TOOLBAR STARTS ======================================================

        //        Removing Support bar / top line containing name ===================================
        Objects.requireNonNull(getSupportActionBar()).hide();

        //        FINDING menu =====================================================================
        menu = findViewById(R.id.Menu);
        menu.setVisibility(View.INVISIBLE); // remove visibility



        //  INSERT OPERATION IN DISPLAY TABLE ======================================================

        //        Getting INTENT data
        Bundle bundle = getIntent().getExtras();
        cNametxt = bundle.getString("cName");
        cNumbertxt = bundle.getString("cNumber");
        datetext = bundle.getString("date");
        billIdtxt = bundle.getInt("billId");
        sellertxt = bundle.getString("seller");
        origintxt = bundle.getString("origin");

        //  GST filed visibility ===================================================================
        TextInputLayout gst = findViewById(R.id.layoutitemGST);
        boolean needGST = false;
        if (DB.CheckGstAvailability(sellertxt)) {
            gst.setVisibility(View.VISIBLE);
            needGST = true;
        } else {
            gst.setVisibility(View.GONE);
        }

        final int[] validator = {0}; //  to change visibility of show button and add button

        //  Add Item Button ========================================================================
        quantity = findViewById(R.id.quantity);
        if (quantity.getText().toString().equals("")) {
            quantity.setText("1");
        }

        productName = findViewById(R.id.productname);
        productName.setFilters(new InputFilter[]{filter}); // Adding Filter

        //        Adding Suggestion [Autocomplete textview]
        String[] products;

        Cursor productsC = DB.GetInventory(sellertxt);

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

        productName.setAdapter(new ArrayAdapter<>(add_product.this, android.R.layout.simple_list_item_1, products));


        // Price edittext clickListener ============================================================
        // this will listen if price edittext is clicked the it add price and GST to its box if available
        price = findViewById(R.id.price);

        EditText GstPersentageEDT = findViewById(R.id.GstPersentage);

        boolean finalNeedGST1 = needGST;
        price.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                if (price.getText().toString().equals("") && !productName.getText().toString().equals("")) {
                    Cursor getPrice = DB.GetProductQuantity(productName.getText().toString(), sellertxt);
                    getPrice.moveToFirst();
                    if (getPrice.getCount() > 0) {
                        price.setText(String.valueOf(getPrice.getInt(getPrice.getColumnIndex("price"))));
                        if (finalNeedGST1) {
                            GstPersentageEDT.setText(String.valueOf(getPrice.getInt(getPrice.getColumnIndex("Gst"))));
                        }

                        Toast.makeText(add_product.this, "Added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(add_product.this, "Can't find", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        
        // Add item button =========================================================================
        // this will insert data in to display table OR update quantity if it's already in list
        final int[] quentity = {0};

        boolean finalNeedGST = needGST;
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName_ST, price_ST, quantity_ST, GstPersentageString;
                productName_ST = productName.getText().toString();
                price_ST = price.getText().toString();
                quantity_ST = quantity.getText().toString();

                GstPersentageString = GstPersentageEDT.getText().toString();
                if (productName_ST.isEmpty() || price_ST.isEmpty() || quantity_ST.isEmpty()) {
                    if (productName_ST.isEmpty() && price_ST.isEmpty() && quantity_ST.isEmpty()) {
                        productName.setError("Enter Item Name here");
                        price.setError("Enter Item Price here");
                        quantity.setError("Enter Quantity here");
                        Toast.makeText(add_product.this, "Entry details", Toast.LENGTH_SHORT).show();
                    } else if (productName_ST.isEmpty()) {
                        productName.setError("Enter Item Name here");
                        Toast.makeText(add_product.this, "Product filed is Empty", Toast.LENGTH_SHORT).show();
                    } else if (price_ST.isEmpty()) {
                        price.setError("Enter Item Price here");
                        Toast.makeText(add_product.this, "Price filed is Empty", Toast.LENGTH_SHORT).show();
                    } else if (quantity_ST.isEmpty()) {
                        quantity.setError("Enter Quantity here");
                        Toast.makeText(add_product.this, "Quantity filed is Empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(add_product.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (cNametxt.isEmpty() || cNumbertxt.isEmpty() || datetext.isEmpty()) {
                        Toast.makeText(add_product.this, "Emptity intent", Toast.LENGTH_SHORT).show();
                    } else if (finalNeedGST && GstPersentageString.isEmpty()) {
                        Toast.makeText(add_product.this, "GST filed is empty", Toast.LENGTH_SHORT).show();
                        GstPersentageEDT.setError("Enter how much gst is applicable enter 0 if there is not any");
                    } else {
                        // calling insert function
                        boolean check = DB.InsertList(productName_ST, price_ST, quantity_ST, cNametxt, cNumbertxt, datetext, billIdtxt, sellertxt, 0, GstPersentageString);
                        if (check) {
                            quentity[0] = quentity[0] + Integer.parseInt(quantity_ST);
                            validator[0]++;
                            show.setVisibility(View.VISIBLE);
                            Toast.makeText(add_product.this, "Inserted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(add_product.this, "Not Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // if screen is visible after show list page is displayed then making show button visible and if it's after home then making it invisible
        if (validator[0] == 0) {
            show.setVisibility(View.GONE);
        }

        if (!origintxt.equalsIgnoreCase("home")) {
            show.setVisibility(View.VISIBLE);
        }

    // Show List Button ============================================================================
        // this will take to show list page 
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(add_product.this, show_list.class);
                //passing customer information and billId
                intent2.putExtra("seller", sellertxt);

                intent2.putExtra("cName", cNametxt);

                intent2.putExtra("cNumber", cNumbertxt);

                intent2.putExtra("date", datetext);

                intent2.putExtra("billId", billIdtxt);

                startActivity(intent2);
                finish();
            }
        });
    }

    //    Going TO Home With User DATA ON Back Button Press ========================================
    //    if user click back then goto home with all the data 
    @Override
    public void onBackPressed() {
        if (!origintxt.equalsIgnoreCase("home")) {
            // if origin is not home then we are here from show list so first user must save the bill 
            Toast.makeText(this, "Please save the bill before exiting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent2 = new Intent(add_product.this, home.class);

            intent2.putExtra("Email", sellertxt);
            intent2.putExtra("Origin", "addItem");

            intent2.putExtra("cName", cNametxt);

            intent2.putExtra("cNumber", cNumbertxt);

            intent2.putExtra("date", datetext);

            intent2.putExtra("billId", billIdtxt);

            startActivity(intent2);
            finish();
        }
    }



    // for google ads ==============================================================================

    @Override
    protected void onStart() {
        super.onStart();
        //        Google ads code ==================================================================
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //        Google ads code ==================================================================
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //        Google ads code ==================================================================
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

}