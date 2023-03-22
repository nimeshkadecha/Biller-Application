package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.signatures.CertificateUtil;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class chart extends AppCompatActivity {

    private ArrayList<String> ainput, aquantity,Ar,Ag,Ab,ANAME;
    private stock_qty_adapter S_Q_adapter;
    private stock_List_adapter S_L_adapter;

    // Create the object of TextView and PieChart class
    TextView tvR, tvPython, tvCPP, tvJava;
    PieChart pieChart;

    DBManager local_db = new DBManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Bundle bundle = getIntent().getExtras();
        String Seller_email = bundle.getString("seller");
        String intentt = bundle.getString("intent");

        //        Removing Suport bar / top line containing name--------------------------------------------
        Objects.requireNonNull(getSupportActionBar()).hide();

        switch (intentt){
            case "qty_chart":
                ainput = new ArrayList<>();
                aquantity = new ArrayList<>();

                Ar = new ArrayList<>();
                Ag = new ArrayList<>();
                Ab = new ArrayList<>();
                ANAME = new ArrayList<>();

                RecyclerView qty_rec = findViewById(R.id.QTY_REC);
                S_Q_adapter = new stock_qty_adapter(chart.this, ainput, aquantity);
                qty_rec.setAdapter(S_Q_adapter);

                qty_rec.setLayoutManager(new LinearLayoutManager(chart.this));

                RecyclerView Stock_list = findViewById(R.id.list_REC);
                S_L_adapter = new stock_List_adapter(chart.this,Ar,Ag,Ab,ANAME);
                Stock_list.setAdapter(S_L_adapter);
                Stock_list.setLayoutManager(new LinearLayoutManager(chart.this));

                Cursor get_Qty = local_db.getInventory(Seller_email);

                get_Qty.moveToFirst();
                Random rnd = new Random();
//                paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                pieChart = findViewById(R.id.piechart);

                do{
                    int r =rnd.nextInt(256);
                    int g =rnd.nextInt(256);
                    int b=rnd.nextInt(256);

                    ainput.add(get_Qty.getString(0));
                    aquantity.add(get_Qty.getString(1));

                    Ar.add(String.valueOf(r));
                    Ag.add(String.valueOf(g));
                    Ab.add(String.valueOf(b));
                    ANAME.add(get_Qty.getString(0));

                    int qty = Integer.parseInt(get_Qty.getString(1));
                    if(qty<0){
                        qty *= -1;
                    }
                    pieChart.addPieSlice(
                            new PieModel(
                                    get_Qty.getString(0),
                                    qty,
                                    Color.argb(255,r,g,b)));
                }while (get_Qty.moveToNext());




                // Set the percentage of language used
//                tvR.setText(Integer.toString(40));
//                tvPython.setText(Integer.toString(30));
//                tvCPP.setText(Integer.toString(5));
//                tvJava.setText(Integer.toString(25));

                // Set the data and color to the pie chart
//                pieChart.addPieSlice(
//                        new PieModel(
//                                "R",
//                                40,
//                                Color.parseColor(s)));
//                pieChart.addPieSlice(
//                        new PieModel(
//                                "Python",
//                                30,
//                                Color.parseColor("#66BB6A")));
//                pieChart.addPieSlice(
//                        new PieModel(
//                                "C++",
//                                10,
//                                Color.parseColor("#EF5350")));
//                pieChart.addPieSlice(
//                        new PieModel(
//                                "R+",
//                                25,
//                                Color.parseColor("#FFA726")));
//                pieChart.addPieSlice(
//                        new PieModel(
//                                "Java",
//                                33,
//                                Color.parseColor("#29B6F6")));
                break;

            default:
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                Intent goTOReport = new Intent(chart.this, Report.class);
                goTOReport.putExtra("seller", Seller_email);
                startActivity(goTOReport);
                break;
        }



    }
}