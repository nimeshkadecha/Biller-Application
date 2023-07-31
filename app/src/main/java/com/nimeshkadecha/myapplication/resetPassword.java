package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class resetPassword extends AppCompatActivity {

    private EditText password, confirmPassword;

    private DBManager DBM;
    private ImageView menuclick;

    private Button confirm;
    public static final String SHARED_PREFS = "sharedPrefs";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
//        finding
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.ConfirmPassword);
        DBM = new DBManager(this);

//        WORKING WITH TOOLBAR Starts---------------------------------------------------------------
    //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

    //        FINDING menu
        menuclick = findViewById(R.id.Menu);

    //        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Confirm Button ---------------------------------------------------------------------------
        confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling Function
                Confirm();
            }
        });
//--------------------------------------------------------------------------------------------------

    }

//    validating Password length >= 6 --------------------------------------------------------------
    private boolean PasswordValidation(EditText password, EditText confirmPassword) {
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();
        if (passwordInput.length() != confirmPasswordInput.length() || passwordInput.length()<6) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------

//    RESETTING PASSWORD ---------------------------------------------------------------------------
    public void Confirm() {
        boolean VP = PasswordValidation(password, confirmPassword);
        if (VP) {
            if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                Bundle bundle = getIntent().getExtras();
                String Email = bundle.getString("Email");
                boolean check;
                check = DBM.resetPassword(Email, confirmPassword.getText().toString().trim());

                if (check) {
                    // Login in user After Reset
                    SharedPreferences sp = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("Login","true");
                    editor.putString("UserName",Email);
                    editor.apply();

                    Intent SucessfullyLogin = new Intent(resetPassword.this, home.class);
                    SucessfullyLogin.putExtra("Email", Email);
                    SucessfullyLogin.putExtra("Origin", "Login");
                    startActivity(SucessfullyLogin);
                    finish();
                    Toast.makeText(resetPassword.this, "Password Updated", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Password NOT reset Successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password Don't Match", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        }
    }
//--------------------------------------------------------------------------------------------------
}