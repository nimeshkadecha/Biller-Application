package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.Random;

public class Backup extends AppCompatActivity {

    private EditText otpedt;

    private Button verify;

    private ImageView menuclick;

    private String OTP, usertxt;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        mAuth = FirebaseAuth.getInstance();

//        TOOL BAR ---------------------------------------------------------------------
        //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        FINDING menu
        menuclick = findViewById(R.id.Menu);

//        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);


//        TOOL BAR /---------------------------------------------------------------------

//        Finding
        otpedt = findViewById(R.id.bOTP);
        verify = findViewById(R.id.bVerify);

//        Getting OTP in INTENT
        Bundle bOTP = getIntent().getExtras();
        OTP = bOTP.getString("OTP");

//        Calling Backgroung class
//        BackgroungTask backgroungTask = new BackgroungTask();
//        backgroungTask.execute(OTP);

//        Verify Button
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otptxt = otpedt.getText().toString().trim();

                boolean otpV = OTPValidate(otptxt);
                if (otpV) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP,otptxt);

                    mAuth.signInWithCredential(credential)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
//                                    Intent backup = new Intent(Backup.this, Backup_Working.class);
                                    Intent backup = new Intent(Backup.this, Backup_Working.class);
                                    Bundle user = getIntent().getExtras();
                                    usertxt = user.getString("user");
                                    backup.putExtra("user", usertxt);
                                    startActivity(backup);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ENimesh","Error is = " + e);
                                    Toast.makeText(Backup.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                                }
                            });
//                    if (otptxt.equals(OTP)) {
//                        Intent backup = new Intent(Backup.this, Backup_Working.class);
//                        Bundle user = getIntent().getExtras();
//                        usertxt = user.getString("user");
//                        backup.putExtra("user", usertxt);
//                        startActivity(backup);
//                    } else {
//                        Toast.makeText(Backup.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    Toast.makeText(Backup.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    Validating OTP
    private boolean OTPValidate(String otpInput) {
        if (otpInput.length() == 6) {
            return true;
        } else {
            return false;
        }
    }
}