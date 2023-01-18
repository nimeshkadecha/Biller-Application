package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity {

    private EditText number;

    private ImageView menuclick;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCAllbacks;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();
//        WORKING WITH TOOLBAR Starts-------------------------------------------------------------
//        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        FINDING menu
        menuclick = findViewById(R.id.Menu);

//        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//        WORKING WITH TOOLBAR Ends-------------------------------------------------------------

//        Finding -----
        number = findViewById(R.id.contactnumber);
    }

    //    Validation on NUMBER
    private boolean numberValidation(EditText number) {
        String numberInput = number.getText().toString().trim();
        if (numberInput.length() == 10) {
            return true;
        } else {
            return false;
        }
    }

    //    Verifying otp and go to OTP_GEN
    public void GetOTP(View view) {
        boolean NV = numberValidation(number);
        if (NV) {
            getOTP();
        } else {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
//    Generating OTP
    private void getOTP() {
//        OTP From Firebase
        String CN = number.getText().toString();
        Log.d("ENimesh","NUmber ="+CN);
        mCAllbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("ENimesh" , "Error is ="+e);
                Toast.makeText(ForgotPassword.this, "Failed to send OTP, Try Again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                Toast.makeText(ForgotPassword.this, "Sending OTP ...", Toast.LENGTH_SHORT).show();
                Intent GETOTP = new Intent(ForgotPassword.this, OTP_Generator.class);
                Log.d("ENImesh","Sussecc OTP ="+s);
                GETOTP.putExtra("number", CN);
                GETOTP.putExtra("OTP", s);
                startActivity(GETOTP);


            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91 "+CN)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCAllbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
//       ENDS Working with NOTIFICATIOn -----------------------------------------------
    }
}