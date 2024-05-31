package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class register extends AppCompatActivity {
    private EditText name, email, password, gst, contact, address;
    private login_Screen MA = new login_Screen();
    private DBManager DBM;
    private Button show;
    private ImageView menuclick;

    private TextInputLayout gstLayout;

    private Switch gstSwitch;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================

//        WORKING WITH TOOLBAR Starts---------------------------------------------------------------
        //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

        //        FINDING menu
        menuclick = findViewById(R.id.Menu);

        //        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Assigning object it's value for SQLite Assess --------------------------------------------
        DBM = new DBManager(this);
//--------------------------------------------------------------------------------------------------

//        Finding editText and Buttons  ------------------------------------------------------------
        name = findViewById(R.id.rName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        gst = findViewById(R.id.gst);
        gstLayout = findViewById(R.id.textInputLayoutGST_Number);
// Making GST button invisible so that user can togel switch
        gst.setVisibility(View.GONE);
        gstLayout.setVisibility(View.GONE);
        gst.setText("no");
        contact = findViewById(R.id.contactNumber);
        address = findViewById(R.id.address);
        gstSwitch = findViewById(R.id.switch1GST);
        gstSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    gst.setText("");
                    gst.setVisibility(View.VISIBLE);
                    gstLayout.setVisibility(View.VISIBLE);

                }else{
                    gst.setVisibility(View.GONE);
                    gstLayout.setVisibility(View.GONE);
                    gst.setText("no");
                }
            }
        });

        show = findViewById(R.id.show);
        show.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Display all information of users but it's hidden it is for testing purposes --------------
        show.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                Cursor res = DBM.getdata();
                if (res.getCount() == 0) {
                    Toast.makeText(register.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("E-mail: " + res.getString(res.getColumnIndex("email")) + "\n");
                    buffer.append("Name: " + res.getString(res.getColumnIndex("name")) + "\n");
                    buffer.append("Password: " + res.getString(res.getColumnIndex("password")) + "\n");
                    buffer.append("gst: " + res.getString(res.getColumnIndex("gst")) + "\n");
                    buffer.append("Contact: " + res.getString(res.getColumnIndex("contact")) + "\n");
                    buffer.append("Address: " + res.getString(res.getColumnIndex("address")) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                builder.setCancelable(true);
                builder.setTitle("Users");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
//--------------------------------------------------------------------------------------------------
    }

    //    Register button ------------------------------------------------------------------------------
    public void register(View view) {
        Intent login = new Intent(this, login_Screen.class);

        String nameTXT = name.getText().toString();
        String emailTXT = email.getText().toString();
        String passwordTXT = password.getText().toString();
        String gstTXT = gst.getText().toString();
        String contactTXT = contact.getText().toString();
        String addressTXT = address.getText().toString();

        if (emailTXT.isEmpty() || passwordTXT.isEmpty() || nameTXT.isEmpty() || gstTXT.isEmpty() || contactTXT.isEmpty() || addressTXT.isEmpty()) {
            if (emailTXT.isEmpty() && passwordTXT.isEmpty() && nameTXT.isEmpty() && gstTXT.isEmpty() && contactTXT.isEmpty() && addressTXT.isEmpty()) {
                Toast.makeText(this, "Fillup FORM", Toast.LENGTH_SHORT).show();
            } else if (emailTXT.isEmpty()) {
                Toast.makeText(this, "Fill up E-mail", Toast.LENGTH_SHORT).show();
            } else if (passwordTXT.isEmpty()) {
                Toast.makeText(this, "Fill up Password", Toast.LENGTH_SHORT).show();
            } else if (nameTXT.isEmpty()) {
                Toast.makeText(this, "Fill up name", Toast.LENGTH_SHORT).show();
            } else if (gstTXT.isEmpty()) {
                Toast.makeText(this, "Fill up GST", Toast.LENGTH_SHORT).show();
            } else if (contactTXT.isEmpty()) {
                Toast.makeText(this, "Fill up Contact", Toast.LENGTH_SHORT).show();
            } else if (addressTXT.isEmpty()) {
                Toast.makeText(this, "Fill up address", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean validEmail = MA.EmailValidation(emailTXT);
            boolean validPassword = MA.passwordValidation(passwordTXT);

            if (contactTXT.length() != 10) {
                Toast.makeText(register.this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
            } else {
                boolean CheckOperation;

                if (validEmail && validPassword) {
                    CheckOperation = DBM.RegisterUser(nameTXT, emailTXT, passwordTXT, gstTXT, contactTXT, addressTXT);
                    if (CheckOperation) {
                        Toast.makeText(this, "User Register Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(login);
                        finish();
                    } else {
                        Toast.makeText(this, "Fail to Register User", Toast.LENGTH_SHORT).show();
                    }
                } else if (!validEmail) {
                    Toast.makeText(this, "Invalid E-Mail", Toast.LENGTH_SHORT).show();
                } else if (!validPassword) {
                    Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
//--------------------------------------------------------------------------------------------------


    @Override
    protected void onStart() {
        super.onStart();
        //        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

}