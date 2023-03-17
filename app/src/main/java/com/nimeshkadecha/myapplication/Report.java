package com.nimeshkadecha.myapplication;

import static com.itextpdf.kernel.pdf.PdfName.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class Report extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle bundle = getIntent().getExtras();
        String Seller_email = bundle.getString("seller");

        //        Removing Suport bar / top line containing name--------------------------------------------
        Objects.requireNonNull(getSupportActionBar()).hide();

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