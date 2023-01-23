package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class home extends AppCompatActivity {

    private ImageView menu, backBtn;

    private Button product;

    private Button customerInfo, editInfo, backup, logout,report;

    private View navagationDrawer;

    private DBManager DB = new DBManager(this);

    public static final String SHARED_PREFS = "sharedPrefs";

    private EditText name, number, date;

    private FirebaseAuth mAuth;

    private ProgressBar lodingPB;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCAllbacks;
    private int[] billIdtxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        int billIdtxt[] = new int[5] ;

//        PB
        lodingPB = findViewById(R.id.Ploding);

//      Finding edit texts -------------------------------------------------------------------------
        name = findViewById(R.id.name);
        number = findViewById(R.id.contact);
        date = findViewById(R.id.date);
//  ------------------------------------------------------------------------------------------------

//        backup.setVisibility(View.INVISIBLE);

//        Generating and formating Date ------------------------------------------------------------
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        date.setText(formattedDate);
//  ------------------------------------------------------------------------------------------------

//        Adding seller email from INTENT-----------------------------------------------------------
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("Email");
        String origin = "Test";
        origin = bundle.getString("Origin");
//  ------------------------------------------------------------------------------------------------

//        Working with TOOLBAR STARTS --------------------------------------------------------------

//        Removing Suport bar / top line containing name--------------------------------------------
        Objects.requireNonNull(getSupportActionBar()).hide();

//  ------------------------------------------------------------------------------------------------

//        Finding and hinding navagation drawer ----------------------------------------------------
        navagationDrawer = findViewById(R.id.navigation);
        navagationDrawer.setVisibility(View.INVISIBLE);

//  ------------------------------------------------------------------------------------------------

//      Menu btn work ------------------------------------------------------------------------------
        menu = findViewById(R.id.Menu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.VISIBLE);
                product.setVisibility(View.INVISIBLE);

            }
        });
// -------------------------------------------------------------------------------------------------

//      BackBtn in drawer --------------------------------------------------------------------------
        backBtn = findViewById(R.id.btnBack);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.INVISIBLE);
                product.setVisibility(View.VISIBLE);
            }
        });
// -------------------------------------------------------------------------------------------------

//      Customer Info Button -----------------------------------------------------------------------
        customerInfo = findViewById(R.id.customerinfo);

        customerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(home.this, "Customer Info Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(home.this, customer_Info.class);
                intent.putExtra("seller", email);
                startActivity(intent);
            }
        });
//  ------------------------------------------------------------------------------------------------

//      Edit Info btn ------------------------------------------------------------------------------
        editInfo = findViewById(R.id.editInfo);
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(home.this, "Edit info btn CLICKED", Toast.LENGTH_SHORT).show();

//                This intent go to editinformation.class
                Intent intent = new Intent(home.this, editInformation.class);
                intent.putExtra("Email", email);
                startActivity(intent);

            }
        });
//  ------------------------------------------------------------------------------------------------

//      Backup Btn ---------------------------------------------------------------------------------

        backup = findViewById(R.id.backup);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                String username = sharedPreferences.getString("UserName","");
//                Intent backup = new Intent(home.this, Firestore_Backup.class);
//                backup.putExtra("user", username);
//                startActivity(backup);

//                TODO : remove comment in getOTP so that user have to pass OTP and verify before accessing backup facility And Change INtent on backup to go to firestore if firestore is working;
                getOTP(username);
            }
        });
//  ------------------------------------------------------------------------------------------------

//        Report button ----------------------------------------------------------------------------
        report = findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTOReport = new Intent(home.this,Report.class);
                goTOReport.putExtra("seller",email);
                startActivity(goTOReport);
            }
        });

//  ------------------------------------------------------------------------------------------------

//      Log Out btn --------------------------------------------------------------------------------
        logout = findViewById(R.id.logOutButton);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("Login", "false");
                editor.putString("UserName", "");
                editor.apply();

                Intent logOUT = new Intent(home.this, MainActivity.class);
                startActivity(logOUT);
                finish();
            }
        });
//  ------------------------------------------------------------------------------------------------

//        WORKING IN NAVAGATION DRAWER Ends  -------------------------------------------------------

//        Enter Product BTN ------------------------------------------------------------------------

//        Products enter intent add customer and go to next INTENT for adding product this will add customer information

//        Log.d("ENimesh","Origin is="+origin);
        if(origin!= null && origin.equalsIgnoreCase("addItem")){
            String cNametxt, cNumbertxt, datetext, sellertxt;
//            int[] billIdtxt ;
            Bundle name1 = getIntent().getExtras();
            cNametxt = name1.getString("cName");

            Bundle num = getIntent().getExtras();
            cNumbertxt = num.getString("cNumber");

            Bundle dat = getIntent().getExtras();
            datetext = dat.getString("date");

            Bundle bID = getIntent().getExtras();
//            billIdtxt = new int[]{0};
            billIdtxt [0] = bID.getInt("billId");

            Bundle seller = getIntent().getExtras();
            sellertxt = seller.getString("seller");

            if(cNametxt.length() != 0){
                name.setText(cNametxt);
                number.setText(cNumbertxt);
                date.setText(datetext);
            }
        }else{
//            int[] billIdtxt ;
            billIdtxt [0] = 0;
        }
        final int[] finalBillIdtxt = billIdtxt;

        product = findViewById(R.id.products);

        product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                billIDd[0]++;
                Intent intent = new Intent(home.this, Additems.class);

                String nametxt, numbertxt, datetxt;
                nametxt = name.getText().toString();
                numbertxt = number.getText().toString();
                datetxt = date.getText().toString();

                if (nametxt.isEmpty() || numbertxt.isEmpty() || datetxt.isEmpty()) {
                    if (nametxt.isEmpty() && numbertxt.isEmpty() && datetxt.isEmpty()) {
                        name.setError("Enter Name Here");
                        number.setError("Enter Number Here");
                        date.setError("Enter Date Here");
                        Toast.makeText(home.this, "Fill up above detail", Toast.LENGTH_SHORT).show();
                    } else if (nametxt.isEmpty()) {
                        name.setError("Enter Name Here");
                        Toast.makeText(home.this, "Enter Customer Name", Toast.LENGTH_SHORT).show();
                    } else if (numbertxt.isEmpty()) {
                        number.setError("Enter Number Here");
                        Toast.makeText(home.this, "Enter Customer Number", Toast.LENGTH_SHORT).show();
                    } else if (datetxt.isEmpty()) {
                        date.setError("Enter Date Here");
                        Toast.makeText(home.this, "Enter Date", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(home.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (numbertxt.length() != 10) {
                        Toast.makeText(home.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                    } else {
                        datetxt = date.getText().toString();
//                        Log.d("ENimesh","finalBillIdtxt = " + finalBillIdtxt[0]);
                        if(finalBillIdtxt[0] == 0){
                            finalBillIdtxt[0] = DB.getbillid();
                        }
//                        int billIDd = DB.getbillid();
//                        Log.d("ENimesh","billIDd = " + billIDd);

                        intent.putExtra("cName", nametxt);
                        intent.putExtra("cNumber", numbertxt);
                        intent.putExtra("date", datetxt);
                        intent.putExtra("billId", finalBillIdtxt[0]);
                        intent.putExtra("seller", email);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
//  ------------------------------------------------------------------------------------------------
    }
//    Generating OTP -------------------------------------------------------------------------------
    private void getOTP(String email) {

        lodingPB.setVisibility(View.VISIBLE);
//        OTP From Firebase
        Cursor number = DB.Seller_Contact(email);

        number.moveToFirst();
//        Log.d("ENimesh","count ="+number.getCount());
//        Log.d("ENimesh","test2 ="+number.getString(0));
        String  CN = number.getString(0);
//        Log.d("ENimesh","NUmber ="+CN);

        mCAllbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                lodingPB.setVisibility(View.GONE);
                mAuth.signInWithCredential(phoneAuthCredential)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(home.this, "Verified...", Toast.LENGTH_SHORT).show();
                                        Intent backup = new Intent(home.this, Firestore_Backup.class);
                                        backup.putExtra("user", email);
                                        startActivity(backup);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(home.this, "Auto sign in Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                lodingPB.setVisibility(View.GONE);
                Toast.makeText(home.this, "Failed to send OTP, Try Again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                Toast.makeText(home.this, "Sending OTP ...", Toast.LENGTH_SHORT).show();

                Intent GETOTP = new Intent(home.this, Backup.class);
//                Log.d("ENimesh","Sussecc OTP ="+s);
                GETOTP.putExtra("number", CN);
                GETOTP.putExtra("user", email);
                GETOTP.putExtra("OTP", s);
                startActivity(GETOTP);
                lodingPB.setVisibility(View.GONE);

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

//  ------------------------------------------------------------------------------------------------
    }
//  ------------------------------------------------------------------------------------------------

//  Alert dialog box for Exiting Application -------------------------------------------------------
    @Override
    public void onBackPressed() {
//        Log.d("ENimesh", "status == " + String.valueOf(navagationDrawer.getVisibility()));

        if (String.valueOf(navagationDrawer.getVisibility()).equals("0")) {
            navagationDrawer.setVisibility(View.INVISIBLE);
            product.setVisibility(View.VISIBLE);

        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(home.this);
            alert.setTitle("Exit App");
            alert.setMessage("Confirm Exit");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishAffinity();
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            alert.show();
        }
    }
//  ------------------------------------------------------------------------------------------------
}