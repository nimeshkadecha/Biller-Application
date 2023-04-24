package com.nimeshkadecha.myapplication;

import static com.itextpdf.kernel.pdf.PdfName.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class Report extends AppCompatActivity {

    DBManager dbLocal = new DBManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle bundle = getIntent().getExtras();
        String Seller_email = bundle.getString("seller");

        //        Removing Suport bar / top line containing name--------------------------------------------
        Objects.requireNonNull(getSupportActionBar()).hide();

        Cursor getinfo = dbLocal.getInventory(Seller_email);

        if(getinfo.getCount()>0)
        {

            Intent gotoReport = new Intent(Report.this,chart.class);
            gotoReport.putExtra("seller",Seller_email);
            gotoReport.putExtra("intent","qty_chart");

            startActivity(gotoReport);
        }else{
            Toast.makeText(this, "You don't have any stock available", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(Report.this, home.class);
            startActivity(i);
        }

        Button qty_chart = findViewById(R.id.QTY_Chart_BTN);
        qty_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gotoReport = new Intent(Report.this,chart.class);
                gotoReport.putExtra("seller",Seller_email);
                gotoReport.putExtra("intent","qty_chart");

                startActivity(gotoReport);
            }
        });

    }
}