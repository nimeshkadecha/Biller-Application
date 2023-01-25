package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.Random;

public class OTP_Generator extends AppCompatActivity {

    private EditText otp;

    private Button verifyy;

    private ImageView menuclick;

    private String b = "Biller";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DBManager DB_local = new DBManager(this);

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String SHARED_PREFS = "sharedPrefs";


    //    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_generator);

//      Getting Verification ID from INTENT --------------------------------------------------------
        Bundle otpp = getIntent().getExtras();
        String OTP = otpp.getString("OTP");
//--------------------------------------------------------------------------------------------------

//      Getting Origin From Intent -----------------------------------------------------------------
        Bundle bundle = getIntent().getExtras();
        String origin = bundle.getString("Origin");
//--------------------------------------------------------------------------------------------------

//        OLD CODE to Generate Notification --------------------------------------------------------
            //        Caclling notification
    //        BackgroungTask backgroungTask = new BackgroungTask();
    //        backgroungTask.execute(OTP);
//--------------------------------------------------------------------------------------------------

//        WORKING WITH TOOLBAR Starts---------------------------------------------------------------
    //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

    //        FINDING menu
        menuclick = findViewById(R.id.Menu);

    //        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//      Verifying OTP ------------------------------------------------------------------------------
        verifyy = findViewById(R.id.Verify);
        verifyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp = findViewById(R.id.OTP);
                String otpInput = otp.getText().toString().trim();
                boolean OTP_V = OTPValidate(otpInput);
                if (OTP_V) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, otpInput);

                    mAuth.signInWithCredential(credential)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Bundle bundle = getIntent().getExtras();
                                    String number = bundle.getString("number");

                                    if (origin != null && origin.equalsIgnoreCase("Cloud")) {
                                        downloadData(number);
                                    } else {
                                        Intent GoToResetPassword = new Intent(OTP_Generator.this, resetPassword.class);
                                        //                Fowarding number to intent reset password
                                        GoToResetPassword.putExtra("number", number);

                                        startActivity(GoToResetPassword);
                                        finish();
                                    }
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
//--------------------------------------------------------------------------------------------------


//    If Origin is cloud then Downloading Data From CLoud ------------------------------------------
    private void downloadData(String num){
        db.collection(num)
                .document("Seller")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();

                            String name = String.valueOf(document.get("Name"));
                            String Contact = String.valueOf(document.get("Contact"));
                            String Enail = String.valueOf(document.get("Enail"));
                            String GST = String.valueOf(document.get("GST"));
                            String Password = String.valueOf(document.get("Password"));
                            String Address = String.valueOf(document.get("Address"));

                            boolean reg = DB_local.registerUser(name,Enail,Password,GST,Contact,Address);
                            if(reg){
                                Query q = db.collection(num)
                                        .document("Business")
                                        .collection("Customer_Info").orderBy("Bill_ID", Query.Direction.DESCENDING)
                                        .limit(1);

                                Task<QuerySnapshot> querySnapshotTask = q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot dq : task.getResult()) {
                                                String bID = String.valueOf(dq.get("Bill_ID"));
                                                String cNmae = String.valueOf(dq.get("C_Name"));
                                                String cNum = String.valueOf(dq.get("C_Number"));
                                                String date = String.valueOf(dq.get("Date"));
                                                String Seller = String.valueOf(dq.get("Seller"));
                                                String Total = String.valueOf(dq.get("Total"));

                                                boolean ins = DB_local.InsertCustomerCloud(bID, cNmae, cNum, date, Seller, 1, Total);
                                                if (ins) {
                                                    Toast.makeText(OTP_Generator.this, "Customer information Added", Toast.LENGTH_SHORT).show();
                                                }

                                                Query q2 = db.collection(num)
                                                        .document("Business")
                                                        .collection("Bill_Info").whereEqualTo("BillId", bID);

                                                q2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot qd : task.getResult()) {
                                                                int bID = Integer.parseInt(String.valueOf(qd.get("BillId")));
                                                                String cNmae = String.valueOf(qd.get("Customer_Name"));
                                                                String cNum = String.valueOf(qd.get("Customer_Number"));
                                                                String date = String.valueOf(qd.get("Date"));
//                                                                String Index = String.valueOf(qd.get("Index"));
                                                                String P_Name = String.valueOf(qd.get("P_Name"));
                                                                String P_Price = String.valueOf(qd.get("P_Price"));
                                                                String P_Qty = String.valueOf(qd.get("P_Qty"));
                                                                String Seller = String.valueOf(qd.get("Seller"));
//                                                                String Subtotal = String.valueOf(qd.get("Subtotal"));

                                                                boolean ins_DIS = DB_local.Insert_List(P_Name, P_Price, P_Qty, cNmae, cNum, date, bID, Seller, 1);
                                                                if (ins_DIS) {
                                                                    Toast.makeText(OTP_Generator.this, "Adding Bills.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });

                                // Login in user after Successfully Download ---------
                                SharedPreferences sp = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("Login","true");
                                editor.putString("UserName",Enail);
                                editor.apply();

                                Intent SucessfullyLogin = new Intent(OTP_Generator.this, home.class);
                                SucessfullyLogin.putExtra("Email", Enail);
                                SucessfullyLogin.putExtra("Origin", "Login");
                                startActivity(SucessfullyLogin);
                                finish();
                            }
                            else {
                                // IF User is already in SQLite then it create error so ---
                                Toast.makeText(OTP_Generator.this, "Already have data in", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
//--------------------------------------------------------------------------------------------------

//    OTP validation -------------------------------------------------------------------------------
    private boolean OTPValidate(String otpInput) {
        if (otpInput.length() < 6) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------


//    OLD CODE TO SEND NOTIFICATION ----------------------------------------------------------------

    //    Notification code -----------------------------------------------------------------
//    class BackgroungTask extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected Void doInBackground(String... args) {
//            String OTP = args[0];
//            //       Start Working with NOTIFICATIOn -----------------------------------------------
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(OTP_Generator.this, b)
//                    .setSmallIcon(R.drawable.message)
//                    .setContentTitle("Otp for reset password")
//                    .setContentText("Your OTP is " + OTP + " Do not share this OTP with others\"")
//                    .setPriority(NotificationCompat.DEFAULT_VIBRATE)
//                    .setPriority(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
//                    .setPriority(NotificationCompat.PRIORITY_MAX);
//
////        Creating chennel and set importance
////        private void createNotificationChannel () {
//            // Create the NotificationChannel, but only on API 26+ because
//            // the NotificationChannel class is new and not in the support library
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                CharSequence name = getString(R.string.Biller);
//                String description = getString(R.string.description);
//                int importance = NotificationManager.IMPORTANCE_DEFAULT;
//
//                NotificationChannel channel = new NotificationChannel(b, name, importance);
//                channel.setDescription(description);
//                // Register the channel with the system; you can't change the importance
//                // or other notification behaviors after this
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                notificationManager.createNotificationChannel(channel);
//            }
////    }
//
//            // Create an explicit intent for an Activity in your app
//            Intent intent = new Intent(OTP_Generator.this, OTP_Generator.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            PendingIntent pendingIntent = PendingIntent.getActivity(OTP_Generator.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//            NotificationCompat.Builder builderr = new NotificationCompat.Builder(OTP_Generator.this, b)
//                    .setSmallIcon(R.drawable.message)
//                    .setContentTitle("Otp for reset password")
//                    .setContentText("Your OTP is " + OTP + " Do not share this OTP with others")
//                    .setPriority(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
//                    // Set the intent that will fire when the user taps the notification
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true);
//
////        SHOW NOTIFICATION
//            class notify extends Thread {
//                void sleep() {
//                    try {
//                        int time = Integer.parseInt(sleepTime());
//                        Thread.sleep(time);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(OTP_Generator.this);
//                        // notificationId is a unique int for each notification that you must define
////                        notificationManager.notify(1, builder.build());
//                    } catch (Exception e) {
//                        Toast.makeText(OTP_Generator.this, "e", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @SuppressLint("DefaultLocale")
//                private String sleepTime() {
//                    Random rnd = new Random();
//                    int otp = rnd.nextInt(9999);
//                    return String.format("%04d", otp);
//                }
//            }
//            notify n = new notify();
//
//            n.sleep();
//
//            return null;
//        }
//    }

//--------------------------------------------------------------------------------------------------
}