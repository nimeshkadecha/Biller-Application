package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Additems extends AppCompatActivity {

    //    toolbar and navagation drawer starts;
    private ImageView menu, backBtn;
    private View navagationDrawer;
    private TextView customerInfo, editInfo;
    private Button Add;
//    toolbar and navagation drawer ends;

    //    for insert operation and outher stuf
    Button show;
    private EditText price, quantity;

    private AutoCompleteTextView productName;
    DBManager DB = new DBManager(this);

    String cNametxt, cNumbertxt, datetext, sellertxt,origintxt;
    int billIdtxt;

    private String blockCharacterSet = "(){}[]:;'//.,-<>?+₹`@~#^|$%&*! ";

    private final InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additems);

//        Finding buttons
        Add = findViewById(R.id.button3);
        show = findViewById(R.id.show);

//        Working with TOOLBAR STARTS --------------------------------------------------------------

    //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

    //        Hiding navigationgrawer
        navagationDrawer = findViewById(R.id.navigation);
        navagationDrawer.setVisibility(View.INVISIBLE);

    //        FINDING menu
        menu = findViewById(R.id.Menu);
        menu.setVisibility(View.INVISIBLE);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.VISIBLE);
                Add.setVisibility(View.INVISIBLE);
                show.setVisibility(View.INVISIBLE);

            }
        });

    //        FINDING Backbtn
        backBtn = findViewById(R.id.btnBack);

    //        hiding navagation on back btn click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.INVISIBLE);
                Add.setVisibility(View.VISIBLE);
                show.setVisibility(View.VISIBLE);
            }
        });

    //   customer info btn
        customerInfo = findViewById(R.id.customerinfo);


        customerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Additems.this, "Customer Info Clicked", Toast.LENGTH_SHORT).show();
            }
        });

    //  Edit Info Button
        editInfo = findViewById(R.id.editInfo);
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Additems.this, editInformation.class);
                Bundle bundle = getIntent().getExtras();
                String email = bundle.getString("Email");

                intent.putExtra("Email", email);
                startActivity(intent);

            }
        });

//--------------------------------------------------------------------------------------------------

//  INSERT OPERATION IN DISPLAY TABLE --------------------------------------------------------------

    //        Getting INTENT
        Bundle name = getIntent().getExtras();
        cNametxt = name.getString("cName");

        Bundle num = getIntent().getExtras();
        cNumbertxt = num.getString("cNumber");

        Bundle dat = getIntent().getExtras();
        datetext = dat.getString("date");

        Bundle bID = getIntent().getExtras();
        billIdtxt = bID.getInt("billId");

        Bundle seller = getIntent().getExtras();
        sellertxt = seller.getString("seller");

        Bundle origin = getIntent().getExtras();
        origintxt = origin.getString("origin");

        final int[] validator = {0};

//  Add Item Button --------------------------------------------------------------------------------
        quantity = findViewById(R.id.quantity);
        if(quantity.getText().toString().equals("")){
            quantity.setText("1");
        }


        productName = findViewById(R.id.productname);
        productName.setFilters(new InputFilter[] { filter }); // Adding Filter
//        ADding Suggestion [Autocompleet textview]
        String [] products;
        String [] p;

        Cursor productsC = DB.getInventory(sellertxt);

        productsC.moveToFirst();
        if (productsC.getCount()>0){
            p = new String[productsC.getCount()];
            int i=0;
            boolean check = true;
            do{
                if(i!=0){
                    for(int j=0;j<i;j++){
                        if(p[j].equals(productsC.getString(1))){
                            check = false;
                            break;
                        }
                    }
                    if(check){
                        p[i] = productsC.getString(1);
                        i++;
                    }
                }else{
                    p[i] = productsC.getString(1);
                    i++;
                }
            }while (productsC.moveToNext());

            products = new String[i];

            System.arraycopy(p, 0, products, 0, i); // this is just a for loop running and copying data

        }else{
            products = new String[]{"NO Suggestion Available"};
        }

        productName.setAdapter(new ArrayAdapter<>(Additems.this, android.R.layout.simple_list_item_1, products));

        price = findViewById(R.id.price);

        price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Additems.this, "clicked", Toast.LENGTH_SHORT).show();
                if(price.getText().toString().equals("") && !productName.getText().toString().equals("")){
                    Cursor getPrice = DB.getProductInfo(productName.getText().toString(),sellertxt);
                    getPrice.moveToFirst();
                    if(getPrice.getCount()>0){
                        Log.d("ENimesh","VAlUE is = " +String.valueOf(getPrice.getInt(4)) );
                        price.setText(String.valueOf(getPrice.getInt(4)));
                        Toast.makeText(Additems.this, "Added", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Additems.this, "Cantfind", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName_ST, price_ST, quantity_ST;
                productName_ST = productName.getText().toString();
                price_ST = price.getText().toString();
                quantity_ST = quantity.getText().toString();
                if (productName_ST.isEmpty() || price_ST.isEmpty() || quantity_ST.isEmpty()) {
                    if (productName_ST.isEmpty() && price_ST.isEmpty() && quantity_ST.isEmpty()) {
                        productName.setError("Enter Item Name here");
                        price.setError("Enter Item Price here");
                        quantity.setError("Enter Quantity here");
                        Toast.makeText(Additems.this, "Entry details", Toast.LENGTH_SHORT).show();
                    } else if (productName_ST.isEmpty()) {
                        productName.setError("Enter Item Name here");
                        Toast.makeText(Additems.this, "Product filed is Empty", Toast.LENGTH_SHORT).show();
                    } else if (price_ST.isEmpty()) {
                        price.setError("Enter Item Price here");
                        Toast.makeText(Additems.this, "Price filed is Empty", Toast.LENGTH_SHORT).show();
                    } else if (quantity_ST.isEmpty()) {
                        quantity.setError("Enter Quantity here");
                        Toast.makeText(Additems.this, "Quantity filed is Empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Additems.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (cNametxt.isEmpty() || cNumbertxt.isEmpty() || datetext.isEmpty()) {
                        Toast.makeText(Additems.this, "Emptity intent", Toast.LENGTH_SHORT).show();
                    } else {

                        boolean check = DB.Insert_List(productName_ST, price_ST, quantity_ST, cNametxt, cNumbertxt, datetext, billIdtxt, sellertxt, 0);
                        if (check) {
                            validator[0]++;
                            show.setVisibility(View.VISIBLE);
                            Toast.makeText(Additems.this, "Inserted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Additems.this, "Not Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
//--------------------------------------------------------------------------------------------------

        if(validator[0] == 0){
            show.setVisibility(View.GONE);
        }

        if( !origintxt.equalsIgnoreCase("home")){
            show.setVisibility(View.VISIBLE);
        }

// Show List Button --------------------------------------------------------------------------------
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(Additems.this, ShowList.class);

                intent2.putExtra("seller", sellertxt);

                intent2.putExtra("cName", cNametxt);

                intent2.putExtra("cNumber", cNumbertxt);

                intent2.putExtra("date", datetext);

                intent2.putExtra("billId", billIdtxt);

                startActivity(intent2);
                finish();
            }
        });
//--------------------------------------------------------------------------------------------------
    }

//    Going TO Home With User DATA ON BAck Button Press --------------------------------------------
    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(Additems.this, home.class);

        intent2.putExtra("Email", sellertxt);
        intent2.putExtra("Origin", "addItem");

        intent2.putExtra("cName", cNametxt);

        intent2.putExtra("cNumber", cNumbertxt);

        intent2.putExtra("date", datetext);

        intent2.putExtra("billId", billIdtxt);

        startActivity(intent2);
        finish();
    }
//--------------------------------------------------------------------------------------------------
}