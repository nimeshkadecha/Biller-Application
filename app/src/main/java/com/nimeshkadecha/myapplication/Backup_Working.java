package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

public class Backup_Working extends AppCompatActivity {
    private TextView customerInfo, editInfo, backup, test;
    private ImageView menu, backBtn;
    private View navagationDrawer;

    private Button upload, download, show, saveBackup;

    private boolean entry;

    private int bidCHECKER = -1;
    private int generated_ID = 0;
    String id;

    DBManager DB = new DBManager(this);
    int billId;

    //    Verifying internet is ON
    boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo net = manager.getActiveNetworkInfo();

        if (net == null) {
            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_working);
//        Checking INTERNET
        boolean checkNetwork = checkConnection();

//        Finding everting
        show = findViewById(R.id.bShow);
        upload = findViewById(R.id.Upload);
        saveBackup = findViewById(R.id.saveBackup);
//        saveBackup.setVisibility(View.INVISIBLE);
        download = findViewById(R.id.Download);

//        Getting user from INTENT
        Bundle u = getIntent().getExtras();
        String user = u.getString("user");

        Bundle b = getIntent().getExtras();
        String sellertxt = b.getString("user");
//        Working with TOOLBAR STARTS --------------------------------------------------------------

//        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//        Hiding navigationgrawer
        navagationDrawer = findViewById(R.id.navigation);
        navagationDrawer.setVisibility(View.INVISIBLE);

//        FINDING menu
        menu = findViewById(R.id.Menu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.VISIBLE);
                download.setVisibility(View.INVISIBLE);
                upload.setVisibility(View.INVISIBLE);
                show.setVisibility(View.INVISIBLE);
                saveBackup.setVisibility(View.INVISIBLE);
            }
        });

//        FINDING Backbtn
        backBtn = findViewById(R.id.btnBack);
//        WORKING IN NAVAGATION DRAWER starts  -----------------------------------------------------

//        hiding navagation on back btn click------------
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navagationDrawer.setVisibility(View.INVISIBLE);
                download.setVisibility(View.VISIBLE);
                upload.setVisibility(View.VISIBLE);
                show.setVisibility(View.VISIBLE);
                saveBackup.setVisibility(View.VISIBLE);
            }
        });
//        Working with TOOLBAR Ends --------------------------------------------------------------

//        finding customer info btn and edit info btn
        customerInfo = findViewById(R.id.customerinfo);
        editInfo = findViewById(R.id.editInfo);

        customerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(home.this, "Customer Info Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Backup_Working.this, customer_Info.class);
                Bundle e = getIntent().getExtras();
                String email = e.getString("user");
                intent.putExtra("seller", email);
                startActivity(intent);
            }
        });

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(home.this, "Edit info btn CLICKED", Toast.LENGTH_SHORT).show();

//                This intent go to editinformation.class
                Intent intent = new Intent(Backup_Working.this, editInformation.class);
                Bundle e = getIntent().getExtras();
                String email = e.getString("user");
                intent.putExtra("Email", email);
                startActivity(intent);

            }
        });

//        Working on Backup ---------------------------------------------------------------
        backup = findViewById(R.id.backup);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Backup_Working.this, "You are Already in Backup!", Toast.LENGTH_SHORT).show();
            }
        });
//        WORKING IN NAVAGATION DRAWER Ends  -----------------------------------------------------

//        Show Button -----------------------------------------------------------------------------
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = DB.cusInfo(sellertxt);
                if (res.getCount() == 0) {
                    Toast.makeText(Backup_Working.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
//                    DATE | name | number | Total |
                    buffer.append("Bill ID = " + res.getString(0) + "\n");
                    buffer.append("Customer Name = " + res.getString(1) + "\n");
                    buffer.append("Customer Number = " + res.getString(2) + "\n");
                    buffer.append("Date = " + res.getString(3) + "\n");
                    buffer.append("Total = " + res.getString(4) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(Backup_Working.this);
                builder.setCancelable(true);
                builder.setTitle("Bills");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
//        Show Button /-----------------------------------------------------------------------------

//        Upload Button --------------------------------------------------------------------------
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkNetwork = checkConnection();
                if (checkNetwork) {
                    DatabaseReference reference;
                    reference = FirebaseDatabase.getInstance().getReference();

//    user table = Email,snumber;
//    Customer table = ID,cNAme,cNumber,date,total,seller;
//    Display table = product,prict,qty,subtotal,billid,seller;
//    TREE in firebase
    /*
            -Biller
                -sellerNumber1
                    -billID1
                        -name
                        -Number
                        -date
                        -product
                        -price
                        -qty
                        -subtoTAL
                        -total
                        -sellerName
                    -billID2
                        -name
                        -Number
                        -date
                        -product
                        -price
                        -qty
                        -subtoTAL
                        -total
                        -sellerName
                -sellerNumber2
                    -billID
                        -name
                        -Number
                        -date
                        -product
                        -price
                        -qty
                        -subtoTAL
                        -total
                        -sellerName

    */
//                Uploading backups;
                    Cursor c1 = DB.GetUser(user); // sname snumber
                    Cursor c2 = DB.getAllCustomer(user);//  product,prict,qty,subtotal,billid,cNAme,cNumber,date,;
//                Cursor c3 = DB.cusInfo(user); // total subtotal = total;

                    reference.child("Biller");
                    c1.moveToFirst();
                    do {
                        String num = c1.getString(4);
                        String sname = c1.getString(0);
                        reference.child("Biller").child(num).child(sname);
                        c2.moveToFirst();
                        int total = 0;
                        do {
                            int backupIndex = c2.getColumnIndex("backup");
                            if (c2.getInt(backupIndex) == 0) {
                                String id = c2.getString(8);
                                String cName = c2.getString(5);
                                String cNumber = c2.getString(6);
                                String date = c2.getString(7);
                                String index = c2.getString(0);
                                String pName = c2.getString(1);
                                String pPrice = c2.getString(2);
                                String pQTY = c2.getString(3);
                                String subtotal = c2.getString(4);
                                total = Integer.parseInt(subtotal);
                                reference.child("Biller").child(num).child(sname).child(index).child("Bill ID").setValue(id);
                                reference.child("Biller").child(num).child(sname).child(index).child("Customer name").setValue(cName);
                                reference.child("Biller").child(num).child(sname).child(index).child("Customer number").setValue(cNumber);
                                reference.child("Biller").child(num).child(sname).child(index).child("date").setValue(date);
                                reference.child("Biller").child(num).child(sname).child(index).child("Product Name").setValue(pName);
                                reference.child("Biller").child(num).child(sname).child(index).child("Product Price").setValue(pPrice);
                                reference.child("Biller").child(num).child(sname).child(index).child("Product Quantity").setValue(pQTY);
                                reference.child("Biller").child(num).child(sname).child(index).child("SubTotal").setValue(subtotal);
                                DB.UpdateBackup(Integer.parseInt(id), 1);
                            }
                        } while (c2.moveToNext());
                    } while (c1.moveToNext());

                    Toast.makeText(Backup_Working.this, "Data Uploaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Backup_Working.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        Upload Button /--------------------------------------------------------------------------

//        Download Button ----------------------------------------------------------------------------
        test = findViewById(R.id.test);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean checkNetwork = checkConnection();
                if (checkNetwork) {
                    Boolean deleteData = DB.DeleteUserData(user);
                    billId = DB.getbillid();
                    if (deleteData) {
                        sleep s = new sleep();
//                        Toast.makeText(Backup_Working.this, "Data is Downloading", Toast.LENGTH_SHORT).show();
                        if (entry) {
                            saveBackup.setVisibility(View.VISIBLE);
                            Toast.makeText(Backup_Working.this, "Data Download Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Backup_Working.this, "Error While Downloading Data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Backup_Working.this, "Data can't be add", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Backup_Working.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        Download Button /----------------------------------------------------------------------------

//        Save button -------------------------------------------------------------------------
        saveBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean b = DB.autoInsertCustomer();
                if (b) {
                    Toast.makeText(Backup_Working.this, "Done", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Backup_Working.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        Save button -------------------------------------------------------------------------
    }

    //    Downloading DATA
    class sleep extends Thread {

        sleep() {

            Bundle u = getIntent().getExtras();
            String user = u.getString("user");

            String sname;
            String sellerNumber;
            Cursor c = DB.GetUser(user);
            c.moveToFirst();
            sname = c.getString(0);
            sellerNumber = c.getString(4);

            DatabaseReference reference;
            reference = FirebaseDatabase.getInstance().getReference();

//            reference.child("Biller").child(sellerNumber).child(sname)

            reference.child("Biller").child(sellerNumber).child(sname).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    try {
                        if (snapshot.exists()) {
                            String key = snapshot.getKey();
//                            Thread.sleep(1000);
                            reference.child("Biller").child(sellerNumber).child(sname).child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {

                                    try {
                                        Map map = (Map) snapshot1.getValue();
                                        // TODO : Add Seller email;
                                        String billID = String.valueOf(map.get("Bill ID"));
                                        String cname = String.valueOf(map.get("Customer name"));
                                        String number = String.valueOf(map.get("Customer number"));
                                        String pname = String.valueOf(map.get("Product Name"));
                                        String pnumber = String.valueOf(map.get("Product Price"));
                                        String pqty = String.valueOf(map.get("Product Quantity"));
                                        String subtotal = String.valueOf(map.get("SubTotal"));
                                        String date = String.valueOf(map.get("date"));

//                                    DownloaData(user,sname,sellerNumber,cname,number,pname,pnumber,pqty,subtotal,date);

                                        if (bidCHECKER != Integer.parseInt(billID)) {
                                            if (bidCHECKER != -1) {
//                                                Boolean customerInsert = DB.InsertCustomer(String.valueOf(generated_ID), cname, number, date, user, 1);
                                            }

//                                            billId = DB.getbillid();
                                            billId += 1;
                                            id = String.valueOf(billId);
                                            generated_ID = billId;
                                            bidCHECKER = Integer.parseInt(billID);
                                        } else {
                                            billId = generated_ID;
                                            id = String.valueOf(billId);
                                        }

                                        Boolean displayInser = DB.Insert_List(pname, pnumber, pqty, cname, number, date, billId, user, 1);
//                                        Boolean customerInsert = DB.InsertCustomer(id, cname, number, date, user, 1);
                                        DB.UpdateBackup(Integer.parseInt(id), 1);
                                        if (displayInser) {
                                            entry = true;
                                        } else {
                                            entry = false;
                                        }
//                                        Thread.sleep(2000);
                                    } catch (Exception e) {
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Backup_Working.this, "Fail To Download!", Toast.LENGTH_SHORT).show();
                }
            });

            try {
                Thread.sleep(100);
            } catch (Exception w) {
            }
        }
    }
}