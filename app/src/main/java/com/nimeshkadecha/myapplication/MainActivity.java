package com.nimeshkadecha.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private int StoragePermisionCode = 1;
    //    initlizing varablwe
    private EditText email, password;
    private Button login, permisions;

    //    Creating object
    private DBManager DBM;

    private ImageView menuclick;

    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        WORKING WITH TOOLBAR Starts-------------------------------------------------------------
//        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        FINDING menu
        menuclick = findViewById(R.id.Menu);

//        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//        WORKING WITH TOOLBAR Ends-------------------------------------------------------------

//        assign variable;
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

//        assigning DBM
        DBM = new DBManager(this);

        //        Working with Permision -------------------------------------------------------
        permisions = findViewById(R.id.permisions);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Storage permision is Granted", Toast.LENGTH_SHORT).show();
            permisions.setVisibility(View.INVISIBLE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
            permisions.setVisibility(View.VISIBLE);
        }

        permisions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermisions();
            }
        });

//        END Working with Permision -------------------------------------------------------

//        Checking if user is already loged in or not
        alreadyLogin();
    }

    //    Working on requesting STORAGE permision ----------------------------------------
    private void requestStoragePermisions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission is needed for")
                    .setMessage("Permission is needed for Creating Bill PDF")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == StoragePermisionCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                permisions.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(this, "Permission not Granted, Allow it to create PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //    Working on requesting STORAGE permision /----------------------------------------

    //    Code for validating email starts------------------------------------------------------------
    public boolean EmailValidation(String email) {
        String emailinput = email;
        if (!emailinput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailinput).matches()) {
            return true;
        } else {

            return false;
        }
    }
    //    Code for verify email Ends------------------------------------------------------------

    //    Code for validation Password starts------------------------------------------------------------
    public boolean passwordValidation(String password) {
        String passwordInput = password;
        if (!passwordInput.isEmpty() && passwordInput.length() > 6) {
            return true;
        } else {
            return false;
        }
    }
    //    Code for verify Password Ends ------------------------------------------------------------

    //    Going to register page
    public void register(View view) {
        Intent register = new Intent(this, register.class);
        startActivity(register);
    }

    //    Going to HOME Page if ID Password Is correct----------------------------------------------
    public void login(View view) {
        Intent SucessfullyLogin = new Intent(this, home.class);
        boolean EV = EmailValidation(email.getText().toString());
        boolean EP = passwordValidation(password.getText().toString());
        if (EV && EP) {
            boolean verify;
            String emailTXT = email.getText().toString();
            String passwordTXT = password.getText().toString();
            verify = DBM.loginUser(emailTXT, passwordTXT);
            if (verify) {
                SharedPreferences sp = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("Login","true");
                editor.putString("UserName",emailTXT);
//                Log.d("ENimesh","Putting Email = " + emailTXT);
                editor.apply();

                SucessfullyLogin.putExtra("Email", emailTXT);
                SucessfullyLogin.putExtra("Origin", "Login");
                startActivity(SucessfullyLogin);
                finish();
            } else {
                Toast.makeText(this, "wrong email OR password", Toast.LENGTH_SHORT).show();
            }
        } else if (!EV) {
            if (email.getText().length() == 0) {
                email.setError("Enter E-mail here");
                Toast.makeText(this, "Please Enter E-Mail", Toast.LENGTH_SHORT).show();
            } else {
                email.setError("Enter Valid E-mail");
                Toast.makeText(this, "Invalid E-Mail", Toast.LENGTH_SHORT).show();
            }
        } else if (!EP) {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    //    ON Click Forgot Password----------------------------------------------------------------
    public void Forgot_Password(View view) {
        Intent forgotpassword = new Intent(this, ForgotPassword.class);

        startActivity(forgotpassword);
        finish();
    }

    private void alreadyLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String checkLogin = sharedPreferences.getString("Login", "");
        String username = sharedPreferences.getString("UserName","");
        if(Objects.equals(checkLogin, "true")) {

            Intent SucessfullyLogin = new Intent(this, home.class);
            SucessfullyLogin.putExtra("Email", username);
            SucessfullyLogin.putExtra("Origin", "Login");
            startActivity(SucessfullyLogin);
//            Log.d("ENimesh","Activity started");

            finish();
        }
    }

}