package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
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

    private Button customerInfo, editInfo, backup, logout, report, stock;

    private View navagationDrawer, homeLayout;

    private DBManager DB = new DBManager(this);

    public static final String SHARED_PREFS = "sharedPrefs";

    private EditText number, date;

    private AutoCompleteTextView name;

    private FirebaseAuth mAuth;

    private ProgressBar lodingPB;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCAllbacks;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        int billIdtxt[] = new int[5];


//        Adding seller email from INTENT-----------------------------------------------------------
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("Email");
        String origin = "Test";
        origin = bundle.getString("Origin");
//  ------------------------------------------------------------------------------------------------

//        Geting Biomatrix unlock
        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Switch bio_lock_switch = findViewById(R.id.bio_lock_switch);

        bio_lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("bioLock", "true");
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("bioLock", "false");
                    editor.apply();
                }
            }
        });

//        checking for fingerprint verification

        String biomatrixLock = sp.getString("bioLock", "");
        if (biomatrixLock.equals("true")) {
            bio_lock_switch.setChecked(true);

        }

//      Progressbar Finding ------------------------------------------------------------------------
        lodingPB = findViewById(R.id.Ploding);
//--------------------------------------------------------------------------------------------------

//      Finding edit texts -------------------------------------------------------------------------
        name = findViewById(R.id.name);

//        adding auto complete facility
        String[] NameSuggestion;
        String[] Names;

        Cursor Name_Sugg = DB.cusInfo(email);
        Name_Sugg.moveToFirst();
        if (Name_Sugg.getCount() > 0) {
            int i = 0;
            boolean insert = true;

            NameSuggestion = new String[Name_Sugg.getCount()];
            do {
                if (i != 0) {
                    for (int j = 0; j < i; j++) {

                        if (NameSuggestion[j].equals(Name_Sugg.getString(1))) {
                            insert = false;
                            break;
                        } else {
                            insert = true;
                        }
                    }
                }

                if (insert) {
                    NameSuggestion[i] = Name_Sugg.getString(1);

                    i++;
                }
            } while (Name_Sugg.moveToNext());


            Names = new String[i];
            for (int j = 0; j < i; j++) {
                Names[j] = NameSuggestion[j];

            }
        } else {
            Names = new String[]{"No Data"};
        }
        name.setAdapter(new ArrayAdapter<>(home.this, android.R.layout.simple_list_item_1, Names));

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navagationDrawer.getVisibility() == View.VISIBLE) {
                    navagationDrawer.setVisibility(View.INVISIBLE);
                    product.setVisibility(View.VISIBLE);
                }
            }
        });

        number = findViewById(R.id.contact);

        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navagationDrawer.getVisibility() == View.VISIBLE) {
                    navagationDrawer.setVisibility(View.INVISIBLE);
                    product.setVisibility(View.VISIBLE);
                }
                if (number.getText().toString().equals("")) {
                    Cursor numberC = DB.individualCustomerInfo(email, name.getText().toString());
                    numberC.moveToFirst();
                    if (numberC.getCount() > 0) {
                        number.setText(numberC.getString(2));
                    } else {
                        Toast.makeText(home.this, "New Customer", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        date = findViewById(R.id.date);
//  ------------------------------------------------------------------------------------------------

//        Generating and formatting Date ------------------------------------------------------------
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        date.setText(formattedDate);

        date.setOnClickListener(v -> {
            if (date.getText().toString().equals("")) {
                date.setText(formattedDate);
            } else {
                Toast.makeText(this, "Long press to open date picker", Toast.LENGTH_SHORT).show();
            }

        });

        date.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(home.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(" ");
                        date.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                datePickerDialog.show();
                return true;
            }
        });
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
                if (getCurrentFocus() != null) {
                    InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

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
                Intent intent = new Intent(home.this, editInformation.class);
                intent.putExtra("Email", email);
                startActivity(intent);

            }
        });
//  ------------------------------------------------------------------------------------------------

//      Edit Info btn ------------------------------------------------------------------------------
        stock = findViewById(R.id.stock);
        stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, manageStock.class);
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

                AlertDialog.Builder alert = new AlertDialog.Builder(home.this);
                alert.setTitle("Backup");
                alert.setMessage("This would require OTP to Access ");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        String username = sharedPreferences.getString("UserName", "");
                        getOTP(username);
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.show();

// Testing code to bypass OTP
//                Intent backup = new Intent(home.this, Firestore_Backup.class);
//                backup.putExtra("user", username);
//                startActivity(backup);

            }
        });
//  ------------------------------------------------------------------------------------------------

//        Report button ----------------------------------------------------------------------------
        report =

                findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTOReport = new Intent(home.this, Report.class);
                goTOReport.putExtra("seller", email);
                startActivity(goTOReport);
            }
        });

//  ------------------------------------------------------------------------------------------------

//      Log Out btn --------------------------------------------------------------------------------
        logout =

                findViewById(R.id.logOutButton);

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

//        Clossing navagation drawer ---------------------------------------------------------------

        homeLayout =

                findViewById(R.id.homeLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navagationDrawer.getVisibility() == View.VISIBLE) {
                    navagationDrawer.setVisibility(View.INVISIBLE);
                    product.setVisibility(View.VISIBLE);
                }
            }
        });

//  ------------------------------------------------------------------------------------------------

//        Enter Product BTN ------------------------------------------------------------------------

//        Products enter intent add customer and go to next INTENT for adding product this will add customer information

        if (origin != null && origin.equalsIgnoreCase("addItem")) {
            String cNametxt, cNumbertxt, datetext;

            Bundle bundle1 = getIntent().getExtras();
            cNametxt = bundle1.getString("cName");

            cNumbertxt = bundle1.getString("cNumber");

            datetext = bundle1.getString("date");

            billIdtxt[0] = bundle1.getInt("billId");

            if (cNametxt.length() != 0) {
                name.setText(cNametxt);
                number.setText(cNumbertxt);
                date.setText(datetext);
            }
        } else {
            billIdtxt[0] = 0;
        }

        final int[] finalBillIdtxt = billIdtxt;

        product = findViewById(R.id.products);

        product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.createTable();
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

                        if (finalBillIdtxt[0] == 0) {
                            finalBillIdtxt[0] = DB.getbillid();
                        }

                        intent.putExtra("cName", nametxt);
                        intent.putExtra("cNumber", numbertxt);
                        intent.putExtra("date", datetxt);
                        intent.putExtra("billId", finalBillIdtxt[0]);
                        intent.putExtra("seller", email);
                        intent.putExtra("origin", "home");
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
        String CN = number.getString(0);

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
                Toast.makeText(home.this, "Failed to send OTP, Try Again after SOme time | " + e, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                Toast.makeText(home.this, "Sending OTP ...", Toast.LENGTH_SHORT).show();

                Intent GETOTP = new Intent(home.this, Backup.class);
                GETOTP.putExtra("number", CN);
                GETOTP.putExtra("user", email);
                GETOTP.putExtra("OTP", s);
                startActivity(GETOTP);
                lodingPB.setVisibility(View.GONE);

            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91 " + CN)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCAllbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

//  ------------------------------------------------------------------------------------------------
    }


    //  Alert dialog box for Exiting Application -------------------------------------------------------
    @Override
    public void onBackPressed() {
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