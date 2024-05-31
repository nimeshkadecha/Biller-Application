package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class profile_editing extends AppCompatActivity {

    private ImageView menuclick;

    private DBManager DBM;
    private Button show, update, delete;
    private EditText name, password, gst, contact, address;
    private TextView header;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editing);

//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================

//        Creating object DBM for database activity
        DBM = new DBManager(this);

//      Displaying name and filling edittext of that user data -------------------------------------
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("Email");
        String number = "";

        header = findViewById(R.id.Header);
        header.setText("Update data of E-Mail : " + email);
        name = findViewById(R.id.rName);
        password = findViewById(R.id.password);
        gst = findViewById(R.id.gst);
        contact = findViewById(R.id.contactNumber);
        address = findViewById(R.id.address);

        Cursor getuserinfo = DBM.GetUser(email);
        if (getuserinfo.getCount() > 0) {
            getuserinfo.moveToFirst();
            do {
                name.setText(getuserinfo.getString(getuserinfo.getColumnIndex("name")));
                password.setText(getuserinfo.getString(getuserinfo.getColumnIndex("password")));
                if(getuserinfo.getString(3).equals("no")){
                    gst.setText("");
                }else{
                    gst.setText(getuserinfo.getString(getuserinfo.getColumnIndex("gst")));
                }
                contact.setText(getuserinfo.getString(getuserinfo.getColumnIndex("contact")));
                address.setText(getuserinfo.getString(getuserinfo.getColumnIndex("address")));
            } while (getuserinfo.moveToNext());
        }

//  ================================================================================================

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//          menu Button ----------------------------------------------------------------------------
        menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//  ================================================================================================

//        SHOW Btn Works starts --------------------------------------------------------------------
        show = findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = DBM.GetUser(email);
                if (res.getCount() == 0) {
                    Toast.makeText(profile_editing.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(profile_editing.this);
                builder.setCancelable(true);
                builder.setTitle("Users");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
//  ================================================================================================

//       UPDATE btn Code Starts --------------------------------------------------------------------
        update = findViewById(R.id.update);
        String finalNumber = number;
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameTXT = name.getText().toString();
                String passwordTXT = password.getText().toString();
                String gstTXT = gst.getText().toString();
                if(gstTXT.isEmpty()){
                    gst.setText("no");
                    gstTXT = gst.getText().toString();
                }
                String contactNumberTXT = contact.getText().toString();
                String addressTXT = address.getText().toString();

//                VALIDATING password
                login_Screen ma;
                ma = new login_Screen();
                boolean passwordCheck;
                passwordCheck = ma.passwordValidation(passwordTXT);
                int PasswordChecking = 0;
                if (passwordCheck) {
                    PasswordChecking = 1;
                } else {
                    PasswordChecking = 0;
                    password = findViewById(R.id.password);
                    passwordTXT = name.getText().toString();
                }
                if (nameTXT.isEmpty() || passwordTXT.isEmpty() || gstTXT.isEmpty() || contactNumberTXT.isEmpty() || addressTXT.isEmpty()) {
                    Toast.makeText(profile_editing.this, "Fill up complete Form", Toast.LENGTH_SHORT).show();
                } else {
                    if (PasswordChecking == 1) {
                        boolean check;
                        check = DBM.UpdateUser(nameTXT, email, passwordTXT, gstTXT, contactNumberTXT, addressTXT);
                        if(check){
                            Toast.makeText(profile_editing.this, "Data Updated", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(profile_editing.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
//  ================================================================================================

//        DELETE BUTTON ----------------------------------------------------------------------------
        delete = findViewById(R.id.Delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(profile_editing.this);
                alert.setTitle("Delete user");
                alert.setMessage("Confirm deleting Account permanently ");
                alert.setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean check;
                        check = DBM.DeleteUser(email);
                        if (check) {
//                        Deleting all users data

//                        Going back to login after deleting user
                            Intent intent = new Intent(profile_editing.this, login_Screen.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(profile_editing.this, "User DELETED ...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(profile_editing.this, "Unable to delete user at this Time", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(profile_editing.this, "Deletion Cancel", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
        });
    }
//  ================================================================================================

//  AUTO BACKUP ====================================================================================

    //    On pause
    @Override
    protected void onPause() {
        super.onPause();
        final String SHARED_PREFS = "sharedPrefs";
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        DBManager dbManager = new DBManager(getApplicationContext());
        boolean check = dbManager.AutoLocalBackup(getApplicationContext());
        if (check) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = df.format(c);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("AutoUpload", formattedDate);
            editor.apply();
        }
    }

    // on stop
    @Override
    protected void onStop() {
        super.onStop();
        final String SHARED_PREFS = "sharedPrefs";
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        DBManager dbManager = new DBManager(getApplicationContext());
        boolean check = dbManager.AutoLocalBackup(getApplicationContext());
        if (check) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = df.format(c);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("AutoUpload", formattedDate);
            editor.apply();
        }
    }

//  ================================================================================================

    @Override
    public void onBackPressed() {
        finish();
    }



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