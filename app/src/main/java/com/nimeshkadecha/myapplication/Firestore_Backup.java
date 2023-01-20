package com.nimeshkadecha.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Firestore_Backup extends AppCompatActivity {

    FirebaseFirestore db;

    DBManager local_db = new DBManager(this);

    private Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_backup);

        Bundle user = getIntent().getExtras();
        String Seller_Email = user.getString("user");


        db = FirebaseFirestore.getInstance();

        upload = findViewById(R.id.uploadDatabtn);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                db.collection(Seller_Email)
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
                                    Customer_Info.put("Bill_ID", Customer.getString(0));
                                    Customer_Info.put("C_Name", Customer.getString(1));
                                    Customer_Info.put("C_Number", Customer.getString(2));
                                    Customer_Info.put("Date", Customer.getString(3));
                                    Customer_Info.put("Total", Customer.getString(4));
                                    Customer_Info.put("Seller", Customer.getString(5));
                                    Customer_Info.put("backup", Customer.getString(6));

                                    db.collection(Seller_Email)
                                            .document("Business")
                                            .collection("Customer_Info")
                                            .document(Customer.getString(0))
                                            .set(Customer_Info)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(Firestore_Backup.this, "Customer info ADDED", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Firestore_Backup.this, "Failed to add Customer", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } while (Customer.moveToNext());

                                // Adding Bill information
                                Map<String, Object> Billes = new HashMap<>();

                                Cursor bill = local_db.getAllCustomer(Seller_Email);

                                bill.moveToFirst();
                                do {
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
                                    Billes.put("backup", bill.getString(10));

                                    db.collection(Seller_Email)
                                            .document("Business")
                                            .collection("Bill_Info")
                                            .document(bill.getString(0))
                                            .set(Billes)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
//                                            Log.d("ENimesh","Added");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Firestore_Backup.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } while (bill.moveToNext());

                                Toast.makeText(Firestore_Backup.this, "User ADDED", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Firestore_Backup.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

//  INsert to FIrestore

        // adding display table detail in hash map


//        Map<String ,Object> data = new HashMap<>();
//
//        data.put("Name","Testing");
//        data.put("Name1","Testing1");
//        data.put("Name2","Testing2");
//
//        db.collection("user")
//                .document("BillID")
//                .collection("Bill123")
//                .add(data)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Toast.makeText(Firestore_Backup.this, "Party", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("ENimesh","ERROR IS =" + e);
//                        Toast.makeText(Firestore_Backup.this, "ERROR", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }
}