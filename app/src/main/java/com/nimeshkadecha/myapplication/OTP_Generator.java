package com.nimeshkadecha.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OTP_Generator extends AppCompatActivity {

    private EditText otp;

    private Button verifyy;

    private ImageView menuclick;

    private String b = "Biller";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DBManager DB_local = new DBManager(this);

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String SHARED_PREFS = "sharedPrefs";

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
                } else {
                    Toast.makeText(OTP_Generator.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
//--------------------------------------------------------------------------------------------------


    //    If Origin is cloud then Downloading Data From CLoud ------------------------------------------
    private void downloadData(String num) {
        db.collection(num)
                .document("Seller")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            String name = String.valueOf(document.get("Name"));
                            String Contact = String.valueOf(document.get("Contact"));
                            String Enail = String.valueOf(document.get("Enail"));
                            String GST = String.valueOf(document.get("GST"));
                            String Password = String.valueOf(document.get("Password"));
                            String Address = String.valueOf(document.get("Address"));

                            if (document.get("Contact") == null) {
                                AlertDialog.Builder ad = new AlertDialog.Builder(OTP_Generator.this);
                                ad.setMessage("Based on our search in our cloud system, we were unable to locate any records associated with this contact number.");
                                ad.setTitle("Warning");
                                ad.setCancelable(false);
                                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                ad.show();
                            } else {
                                boolean reg = DB_local.registerUser(name, Enail, Password, GST, Contact, Address);
                                if (reg) {
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

                                                    boolean ins = DB_local.InsertCustomerCloud(Integer.parseInt(bID), cNmae, cNum, date, Seller, 1, Total);
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

                                                                    String P_Name = String.valueOf(qd.get("P_Name"));
                                                                    String P_Price = String.valueOf(qd.get("P_Price"));
                                                                    String P_Qty = String.valueOf(qd.get("P_Qty"));
                                                                    String Seller = String.valueOf(qd.get("Seller"));

                                                                    boolean ins_DIS = DB_local.Insert_List(P_Name, P_Price, P_Qty, cNmae, cNum, date, bID, Seller, 1);
                                                                    if (ins_DIS) {
                                                                        Toast.makeText(OTP_Generator.this, "Adding Bills.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                    DB_local.createTable();
                                                    db.collection(num)
                                                            .document("Business")
                                                            .collection("Stock").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot qd : task.getResult()) {
                                                                            Map<String, Object> data = new HashMap<>(Objects.requireNonNull(qd.getData()));

                                                                            String productID = String.valueOf(data.get("productID"));
                                                                            String productName = String.valueOf(data.get("productName"));
                                                                            String catagory = String.valueOf(data.get("catagory"));
                                                                            String purchesPrice = String.valueOf(data.get("purchesPrice"));
                                                                            String sellingPrice = String.valueOf(data.get("sellingPrice"));
                                                                            String date = String.valueOf(data.get("date"));
                                                                            String quentity = String.valueOf(data.get("quentity"));
                                                                            String seller = String.valueOf(data.get("seller"));

                                                                            boolean ins = DB_local.downloadStock(productName, catagory, purchesPrice, sellingPrice, date, quentity, seller);

                                                                            if (ins) {
                                                                                Toast.makeText(OTP_Generator.this, "Stock added", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Toast.makeText(OTP_Generator.this, "Error while adding Stock", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                    db.collection(num)
                                                            .document("Business")
                                                            .collection("stockQuentity")
                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                    if (error != null) {
                                                                        Log.d("ENimesh", "ERROR issss = " + error);
                                                                    } else {
                                                                        for (DocumentSnapshot dc : value) {
                                                                            Map<String, Object> data = new HashMap<>(Objects.requireNonNull(dc.getData()));

                                                                            String name = String.valueOf(data.get("productName"));
                                                                            String quentity = String.valueOf(data.get("quentity"));
                                                                            String price = String.valueOf(data.get("price"));
                                                                            String seller = String.valueOf(data.get("seller"));

                                                                            boolean insertt = DB_local.addStockQty(name, quentity, price, seller);
                                                                            if (insertt) {
                                                                                Toast.makeText(OTP_Generator.this, "StockQuentity added", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Toast.makeText(OTP_Generator.this, "ERROR while StockQuentity added", Toast.LENGTH_SHORT).show();
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
                                    SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("Login", "true");
                                    editor.putString("UserName", Enail);
                                    editor.apply();

                                    Intent SucessfullyLogin = new Intent(OTP_Generator.this, home.class);
                                    SucessfullyLogin.putExtra("Email", Enail);
                                    SucessfullyLogin.putExtra("Origin", "Login");
                                    startActivity(SucessfullyLogin);
                                    finish();
                                } else {
                                    // IF User is already in SQLite then it create error so ---
                                    Toast.makeText(OTP_Generator.this, "Already have data in", Toast.LENGTH_SHORT).show();
                                }
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

}