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

public class OTP_Generator extends AppCompatActivity {

    private EditText otp;

    private Button verifyy;

    private ImageView menuclick;

    private String b = "Biller";

    private FirebaseAuth mAuth;

    //    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_generator);
        //        Getting OTP HERE
        Bundle otpp = getIntent().getExtras();
        String OTP = otpp.getString("OTP");

        mAuth = FirebaseAuth.getInstance();
//        Caclling notification
        BackgroungTask backgroungTask = new BackgroungTask();
//        backgroungTask.execute(OTP);

//        WORKING WITH TOOLBAR Starts-------------------------------------------------------------
//        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        FINDING menu
        menuclick = findViewById(R.id.Menu);

//        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//        WORKING WITH TOOLBAR Ends-------------------------------------------------------------

//        VERIFY BUTON
        verifyy = findViewById(R.id.Verify);

//ON CLICK VERIFY
        verifyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ENimesh","OTP form FIrebase ="+OTP);
                otp = findViewById(R.id.OTP);
                String otpInput = otp.getText().toString().trim();
                boolean OTP_V = OTPValidate(otpInput);
                if (OTP_V) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP,otpInput);

                    mAuth.signInWithCredential(credential)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Intent GoToResetPassword = new Intent(OTP_Generator.this, resetPassword.class);

                                    Bundle bundle = getIntent().getExtras();
                                    String number = bundle.getString("number");
//                Fowarding number to intent reset password
                                    GoToResetPassword.putExtra("number", number);

                                    startActivity(GoToResetPassword);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(OTP_Generator.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                                }
                            });
//                    if (otp.getText().toString().equals(OTP)) {
//                        Intent GoToResetPassword = new Intent(OTP_Generator.this, resetPassword.class);
//
//                        Bundle bundle = getIntent().getExtras();
//                        String number = bundle.getString("number");
////                Fowarding number to intent reset password
//                        GoToResetPassword.putExtra("number", number);
//
//                        startActivity(GoToResetPassword);
//                        finish();
//                    } else {
//                        Toast.makeText(OTP_Generator.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    Toast.makeText(OTP_Generator.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    OTP validation
    private boolean OTPValidate(String otpInput) {
        if (otpInput.length() < 6) {
            return false;
        } else {
            return true;
        }
    }

    //    Notification code -----------------------------------------------------------------
    class BackgroungTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            String OTP = args[0];
            //       Start Working with NOTIFICATIOn -----------------------------------------------

            NotificationCompat.Builder builder = new NotificationCompat.Builder(OTP_Generator.this, b)
                    .setSmallIcon(R.drawable.message)
                    .setContentTitle("Otp for reset password")
                    .setContentText("Your OTP is " + OTP + " Do not share this OTP with others\"")
                    .setPriority(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

//        Creating chennel and set importance
//        private void createNotificationChannel () {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.Biller);
                String description = getString(R.string.description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel = new NotificationChannel(b, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
//    }

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(OTP_Generator.this, OTP_Generator.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(OTP_Generator.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builderr = new NotificationCompat.Builder(OTP_Generator.this, b)
                    .setSmallIcon(R.drawable.message)
                    .setContentTitle("Otp for reset password")
                    .setContentText("Your OTP is " + OTP + " Do not share this OTP with others")
                    .setPriority(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

//        SHOW NOTIFICATION
            class notify extends Thread {
                void sleep() {
                    try {
                        int time = Integer.parseInt(sleepTime());
                        Thread.sleep(time);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(OTP_Generator.this);
                        // notificationId is a unique int for each notification that you must define
//                        notificationManager.notify(1, builder.build());
                    } catch (Exception e) {
                        Toast.makeText(OTP_Generator.this, "e", Toast.LENGTH_SHORT).show();
                    }
                }

                @SuppressLint("DefaultLocale")
                private String sleepTime() {
                    Random rnd = new Random();
                    int otp = rnd.nextInt(9999);
                    return String.format("%04d", otp);
                }
            }
            notify n = new notify();

            n.sleep();

            return null;
        }
    }
}