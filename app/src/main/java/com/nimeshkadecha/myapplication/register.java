package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class register extends AppCompatActivity {
    private EditText name, email, password, gst, contact, address;
    private MainActivity MA = new MainActivity();
    private DBManager DBM;
    private Button show;
    private ImageView menuclick;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        contact = findViewById(R.id.contactNumber);
        address = findViewById(R.id.address);

        show = findViewById(R.id.show);
        show.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Display all information of users but it's hidden it is for testing purposes --------------
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = DBM.getdata();
                if (res.getCount() == 0) {
                    Toast.makeText(register.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("E-mail: " + res.getString(0) + "\n");
                    buffer.append("Name: " + res.getString(1) + "\n");
                    buffer.append("Password: " + res.getString(2) + "\n");
                    buffer.append("gst: " + res.getString(3) + "\n");
                    buffer.append("Contact: " + res.getString(4) + "\n");
                    buffer.append("Address: " + res.getString(5) + "\n\n");
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
        Intent login = new Intent(this, MainActivity.class);

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
                    CheckOperation = DBM.registerUser(nameTXT, emailTXT, passwordTXT, gstTXT, contactTXT, addressTXT);
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
}