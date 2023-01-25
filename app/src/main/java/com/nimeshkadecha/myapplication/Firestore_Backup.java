package com.nimeshkadecha.myapplication;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Firestore_Backup extends AppCompatActivity {

    private  FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DBManager local_db = new DBManager(this);

    public static final String SHARED_PREFS = "sharedPrefs";

    private TextView uploadDate, Download;

    private Button upload, Downloadbtn;

    private ProgressBar lodingPB;

    private ImageView menuclick;


    //    Verifying internet is ON -----------------------------------------------------------------
    boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo net = manager.getActiveNetworkInfo();

        if (net == null) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_backup);

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//          menu Button ----------------------------------------------------------------------------
        menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Progressbar ------------------------------------------------------------------------------
        lodingPB = findViewById(R.id.Ploding);
        lodingPB.setVisibility(View.GONE);
//--------------------------------------------------------------------------------------------------

//      Getting Current Date to put ----------------------------------------------------------------
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
//--------------------------------------------------------------------------------------------------

//        Getting User EMail FRom Intent -----------------------------------------------------------
        Bundle user = getIntent().getExtras();
        String Seller_Email = user.getString("user");
//--------------------------------------------------------------------------------------------------

//        Using Shared Preference to store Last Date -----------------------------------------------
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String UploadDate = sharedPreferences.getString("Last Upload", "Not Uploaded");
        String DownloadDate = sharedPreferences.getString("Last Download", "Not Downloaded");

        //Upload Date
        uploadDate = findViewById(R.id.uploadDate);
        uploadDate.setText(UploadDate);

        // Download Date
        Download = findViewById(R.id.Download);
        Download.setText(DownloadDate);
//--------------------------------------------------------------------------------------------------

//      Upload Button ------------------------------------------------------------------------------
        upload = findViewById(R.id.uploadDatabtn);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lodingPB.setVisibility(View.VISIBLE);
                if (checkConnection()) {
                    //      Adding Seller detail in HashMap
                    Map<String, String> seller = new HashMap<>();

                    // getting seller info from SQLite
                    Cursor seller_cursor = local_db.GetUser(Seller_Email);

                    seller_cursor.moveToFirst();
                    if (seller_cursor.getCount() > 0) {
                        seller.put("Name", seller_cursor.getString(0));
                        seller.put("Enail", seller_cursor.getString(1));
                        seller.put("Password", seller_cursor.getString(2));
                        seller.put("GST", seller_cursor.getString(3));
                        seller.put("Contact", seller_cursor.getString(4));
                        seller.put("Address", seller_cursor.getString(5));
                    }

                    db.collection(seller_cursor.getString(4))
                            .document("Seller")
                            .set(seller)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    // Getting Customer table
                                    Map<String, Object> Customer_Info = new HashMap<>();

                                    Cursor Customer = local_db.cusInfo(Seller_Email);
                                    Customer.moveToFirst();
                                    do {
                                        if(Customer.getCount() > 0){
                                            if (Customer.getInt(6) == 0) {
                                                Customer_Info.put("Bill_ID", Customer.getString(0));
                                                Customer_Info.put("C_Name", Customer.getString(1));
                                                Customer_Info.put("C_Number", Customer.getString(2));
                                                Customer_Info.put("Date", Customer.getString(3));
                                                Customer_Info.put("Total", Customer.getString(4));
                                                Customer_Info.put("Seller", Customer.getString(5));
//                                        Customer_Info.put("backup", Customer.getString(6));

                                                db.collection(seller_cursor.getString(4))
                                                        .document("Business")
                                                        .collection("Customer_Info")
                                                        .document(Customer.getString(0))
                                                        .set(Customer_Info)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                int id = Integer.parseInt(String.valueOf(Customer_Info.get("Bill_ID")));
                                                                for (int i = 1; i <= id; i++) {
                                                                    String idd = String.valueOf(i);
                                                                    local_db.UpdateBackupcus(idd, 1);
                                                                }
                                                                lodingPB.setVisibility(View.GONE);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(Firestore_Backup.this, "Failed to add Customer", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }else{
                                            Toast.makeText(Firestore_Backup.this, "No customer Data Available", Toast.LENGTH_SHORT).show();
                                        }
                                    } while (Customer.moveToNext());

                                    // Adding Bill information
                                    Map<String, Object> Billes = new HashMap<>();

                                    Cursor bill = local_db.getAllCustomer(Seller_Email);

                                    bill.moveToFirst();
                                    do {
                                        if(bill.getCount() > 0){
                                            if (bill.getInt(10) == 0) {
                                                Billes.put("Index", bill.getString(0));
                                                Billes.put("P_Name", bill.getString(1));
                                                Billes.put("P_Price", bill.getString(2));
                                                Billes.put("P_Qty", bill.getString(3));
                                                Billes.put("Subtotal", bill.getString(4));
                                                Billes.put("Customer_Name", bill.getString(5));
                                                Billes.put("Customer_Number", bill.getString(6));
                                                Billes.put("Date", bill.getString(7));
                                                Billes.put("BillId", bill.getString(8));
                                                Billes.put("Seller", bill.getString(9));
//                                        Billes.put("backup", bill.getString(10));

                                                db.collection(seller_cursor.getString(4))
                                                        .document("Business")
                                                        .collection("Bill_Info")
                                                        .document(bill.getString(0))
                                                        .set(Billes)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sp.edit();
                                                                editor.putString("Last Upload", formattedDate);
                                                                editor.apply();
                                                                uploadDate.setText(formattedDate);
                                                                int id = Integer.parseInt(String.valueOf(Billes.get("BillId")));
                                                                for (int i = 1; i <= id; i++) {
                                                                    String idd = String.valueOf(i);
                                                                    local_db.UpdateBackup(idd, 1);
                                                                }
                                                                lodingPB.setVisibility(View.GONE);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(Firestore_Backup.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                continue;
                                            }
                                        }else{
                                            Toast.makeText(Firestore_Backup.this, "No Bille Available to Upload", Toast.LENGTH_SHORT).show();
                                        }
                                    } while (bill.moveToNext());
                                    lodingPB.setVisibility(View.GONE);
                                    Toast.makeText(Firestore_Backup.this, "Uploading Data...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    lodingPB.setVisibility(View.GONE);
                                    Toast.makeText(Firestore_Backup.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    lodingPB.setVisibility(View.GONE);
                    Toast.makeText(Firestore_Backup.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
//--------------------------------------------------------------------------------------------------

//        Download Button --------------------------------------------------------------------------
        Downloadbtn = findViewById(R.id.Downloadbtn);
        Downloadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Firestore_Backup.this);
                alert.setTitle("Download Data");
                alert.setMessage("Your non uploaded entry will be deleted, so make sure you have uploaded data before you download it ! ");
                alert.setPositiveButton("Confirm Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        lodingPB.setVisibility(View.VISIBLE);
                        if (checkConnection()) {
                            Cursor seller_cursor = local_db.GetUser(Seller_Email);

                            seller_cursor.moveToFirst();
                            boolean cleanTables = local_db.DeleteUserData(Seller_Email);
                            if (cleanTables) {
                                //                        Log.d("ENimesh","User is = "+seller_cursor.getString(4));
                                db.collection(seller_cursor.getString(4))
                                        .document("Business")
                                        .collection("Bill_Info")
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if (error != null) {
                                                    Log.d("ENimesh", "ERROR issss = " + error);
                                                    lodingPB.setVisibility(View.GONE);
                                                } else {
                                                    for (DocumentSnapshot dc : value) {
                                                        Map<String, Object> data = new HashMap<>(Objects.requireNonNull(dc.getData()));
                                                        int bID = Integer.parseInt(String.valueOf(data.get("BillId")));
                                                        int state = 1;
                                                        String cNmae = String.valueOf(data.get("Customer_Name"));
                                                        String cNum = String.valueOf(data.get("Customer_Number"));
                                                        String date = String.valueOf(data.get("Date"));
                                                        String Index = String.valueOf(data.get("Index"));
                                                        String P_Name = String.valueOf(data.get("P_Name"));
                                                        String P_Price = String.valueOf(data.get("P_Price"));
                                                        String P_Qty = String.valueOf(data.get("P_Qty"));
                                                        String Seller = String.valueOf(data.get("Seller"));
                                                        String Subtotal = String.valueOf(data.get("Subtotal"));

                                                        boolean ins = local_db.Insert_List(P_Name, P_Price, P_Qty, cNmae, cNum, date, bID, Seller, state);
                                                        if (ins) {
                                                            lodingPB.setVisibility(View.GONE);
                                                        } else {
                                                            lodingPB.setVisibility(View.GONE);
                                                            Toast.makeText(Firestore_Backup.this, "Failed to Download", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                }
                                            }
                                        });

                                lodingPB.setVisibility(View.VISIBLE);

//                        ENTETRING in to Customer table
                                db.collection(seller_cursor.getString(4))
                                        .document("Business")
                                        .collection("Customer_Info")
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if (error != null) {
                                                    Log.d("ENimesh", "ERROR issss = " + error);
                                                    lodingPB.setVisibility(View.GONE);
                                                } else {
                                                    for (DocumentSnapshot dc : value) {
                                                        Map<String, Object> data = new HashMap<>(Objects.requireNonNull(dc.getData()));
                                                        String bID = String.valueOf(data.get("Bill_ID"));
                                                        String cNmae = String.valueOf(data.get("C_Name"));
                                                        String cNum = String.valueOf(data.get("C_Number"));
                                                        String date = String.valueOf(data.get("Date"));
                                                        String Seller = String.valueOf(data.get("Seller"));
                                                        String Total = String.valueOf(data.get("Total"));

                                                        boolean ins = local_db.InsertCustomerCloud(bID, cNmae, cNum, date, Seller, 1,Total);
                                                        if (ins) {
                                                            SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sp.edit();
                                                            editor.putString("Last Download", formattedDate);
                                                            editor.apply();
                                                            Download.setText(formattedDate);

                                                            lodingPB.setVisibility(View.GONE);
                                                        } else {
                                                            lodingPB.setVisibility(View.GONE);
                                                            Toast.makeText(Firestore_Backup.this, "Failed to Download", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            lodingPB.setVisibility(View.GONE);
                            Toast.makeText(Firestore_Backup.this, "NO,Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(Firestore_Backup.this, "Download Canceled", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
        });
    }
//--------------------------------------------------------------------------------------------------

}