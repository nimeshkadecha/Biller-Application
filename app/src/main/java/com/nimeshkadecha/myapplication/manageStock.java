package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

public class manageStock extends AppCompatActivity {

    private ImageView menu ;

    DBManager DB = new DBManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stock);

        //        FINDING menu
        menu = findViewById(R.id.Menu);
        menu.setVisibility(View.INVISIBLE);
        //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();



    }
}