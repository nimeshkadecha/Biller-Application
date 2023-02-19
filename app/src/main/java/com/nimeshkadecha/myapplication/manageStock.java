package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class manageStock extends AppCompatActivity {

    private ImageView menu ;

    private Button VStock;

    DBManager DB = new DBManager(this);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stock);

        //        FINDING menu
        menu = findViewById(R.id.Menu);
        menu.setVisibility(View.INVISIBLE);
        //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        gettting seller email
        String seller;
        Bundle name = getIntent().getExtras();
        seller = name.getString("Email");

//        Finding edittext
        EditText itemName = findViewById(R.id.itemNameedt);
        itemName.setFilters(new InputFilter[] { filter }); // Adding Filter

        EditText catagory = findViewById(R.id.categoryedt);
        EditText porchesPriceEdt = findViewById(R.id.porchesPriceEdt);
        EditText sellPrice = findViewById(R.id.sellPrice);

        EditText PurchesDate = findViewById(R.id.PurchesDate);
//        Generating and formating Date ------------------------------------------------------------
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        PurchesDate.setText(formattedDate);
//  ------------------------------------------------------------------------------------------------
        EditText quantity = findViewById(R.id.quantity);

        Button AItem = findViewById(R.id.AItem);

        AItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nametxt,catagorytxt,pPricetxt,sPricetxt,qtytxt,datetxt;
                if(itemName.getText().toString().trim().equals("") && catagory.getText().toString().trim().equals("") &&
                        porchesPriceEdt.getText().toString().trim().equals("") && sellPrice.getText().toString().trim().equals("") && PurchesDate.getText().toString().trim().equals("") && quantity.getText().toString().trim().equals("")){
                    Toast.makeText(manageStock.this, "Fill Above Detail add Inventory", Toast.LENGTH_SHORT).show();
                }
                else if(itemName.getText().toString().trim().equals("")){
                    itemName.setError("Enter Name of your Product");
                }else if(catagory.getText().toString().trim().equals("")){
                    catagory.setError("Enter Name of your Product Category");
                }else if(porchesPriceEdt.getText().toString().trim().equals("")){
                    porchesPriceEdt.setError("Enter Buying Price [Price you pay to get this product]");
                }else if(sellPrice.getText().toString().trim().equals("")){
                    sellPrice.setError("Enter Price you want to sell this product on ");
                }else if(PurchesDate.getText().toString().trim().equals("")){
                    PurchesDate.setError("Enter date of your porches");
                }else if(quantity.getText().toString().trim().equals("")){
                    quantity.setError("Enter number of your Product you have");
                }
                else {

                    nametxt = itemName.getText().toString();
                    catagorytxt = catagory.getText().toString();
                    pPricetxt = porchesPriceEdt.getText().toString();
                    sPricetxt = sellPrice.getText().toString();
                    qtytxt = quantity.getText().toString();
                    datetxt = PurchesDate.getText().toString();
                    boolean insert = DB.AddStock(nametxt,catagorytxt,pPricetxt,sPricetxt,datetxt,qtytxt,seller);
                    if(insert){
                        Toast.makeText(manageStock.this, "Product Added", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(manageStock.this, "Error while adding Product", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        Button view = findViewById(R.id.VStock);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = DB.viewStock(seller);

                if(data.getCount()>0){
                    StringBuffer buffer = new StringBuffer();
                    while (data.moveToNext()) {
//                    DATE | name | number | Total |
                        buffer.append("product name = " + data.getString(1) + "\n");
                        buffer.append("Category = " + data.getString(2) + "\n");
                        buffer.append("Porches Price = " + data.getString(3) + "\n");
                        buffer.append("Selling Price = " + data.getString(4) + "\n");
                        buffer.append("Date = " + data.getString(5) + "\n");
                        buffer.append("Quantity = " + data.getString(6) + "\n\n");
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(manageStock.this);
                    builder.setCancelable(true);
                    builder.setTitle("Stock");
                    builder.setMessage(buffer.toString());
                    builder.show();
                }else{
                    Toast.makeText(manageStock.this, "No data available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}