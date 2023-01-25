package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class editInformation extends AppCompatActivity {

    private ImageView menuclick;
    private View conformationDialog;

    private DBManager DBM;
    private Button show, update, delete;
    private EditText name, password, gst, contact, address;
    private TextView header;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);
//        Creating object DBM for database activity
        DBM = new DBManager(this);

//      Displaying name and filling edittext of that user data -------------------------------------
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("Email");
        String number = "";

        header = findViewById(R.id.Header);
        name = findViewById(R.id.rName);
        password = findViewById(R.id.password);
        gst = findViewById(R.id.gst);
        contact = findViewById(R.id.contactNumber);
        address = findViewById(R.id.address);

        Cursor getuserinfo = DBM.GetUser(email);
        if (getuserinfo.getCount() > 0) {
            getuserinfo.moveToFirst();
            do {
                name.setText(getuserinfo.getString(0));
                password.setText(getuserinfo.getString(2));
                gst.setText(getuserinfo.getString(3));
                contact.setText(email);
                address.setText(getuserinfo.getString(5));
                number = getuserinfo.getString(4);
                header.setText("Update data of { +91 " + getuserinfo.getString(4) + " }");
            } while (getuserinfo.moveToNext());
        }

//  ------------------------------------------------------------------------------------------------

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

//          menu Button ----------------------------------------------------------------------------
        menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        SHOW Btn Works starts --------------------------------------------------------------------
        show = findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = DBM.GetUser(email);
                if (res.getCount() == 0) {
                    Toast.makeText(editInformation.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("E-mail: " + res.getString(0) + "\n");
                    buffer.append("Name: " + res.getString(1) + "\n");
                    buffer.append("Password: " + res.getString(2) + "\n");
                    buffer.append("gst: " + res.getString(3) + "\n");
                    buffer.append("Contact: " + res.getString(4) + "\n");
                    buffer.append("Address: " + res.getString(5) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(editInformation.this);
                builder.setCancelable(true);
                builder.setTitle("Users");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
//--------------------------------------------------------------------------------------------------

//       UPDATE btn Code Starts --------------------------------------------------------------------
        update = findViewById(R.id.update);
        String finalNumber = number;
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()) {

                    String nameTXT = name.getText().toString();
//                String emailTXT = name.getText().toString();
                    String passwordTXT = password.getText().toString();
                    String gstTXT = gst.getText().toString();
                    String EmailTXT = contact.getText().toString();
                    String addressTXT = address.getText().toString();

//                VALIDATING password
                    MainActivity ma;
                    ma = new MainActivity();
                    boolean passwordCheck;
                    passwordCheck = ma.passwordValidation(passwordTXT);
                    int PasswordChecking = 0;
                    if (passwordCheck) {
                        PasswordChecking = 1;
                    } else {
                        PasswordChecking = 0;
                        password = findViewById(R.id.password);
                        passwordTXT = name.getText().toString();
                    }

                    if (nameTXT.isEmpty() || passwordTXT.isEmpty() || gstTXT.isEmpty() || EmailTXT.isEmpty() || addressTXT.isEmpty()) {
                        Toast.makeText(editInformation.this, "Fill Up complete Form", Toast.LENGTH_SHORT).show();
                    } else {
                        if (PasswordChecking == 1) {
                            boolean check;
                            check = DBM.UpdateUser(nameTXT, email, passwordTXT, gstTXT, finalNumber, addressTXT);
                            if (check) {

//                            TODO :UPDATE
                                Map<String, String> Update = new HashMap<>();

                                Update.put("Name", nameTXT);
                                Update.put("Enail", email);
                                Update.put("Password", passwordTXT);
                                Update.put("GST", gstTXT);
                                Update.put("Contact", finalNumber);
                                Update.put("Address", addressTXT);

                                db.collection(finalNumber)
                                        .document("Seller")
                                        .set(Update)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(editInformation.this, "Data Updated In cloud", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(editInformation.this, "Failed to Updated In cloud", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                Toast.makeText(editInformation.this, "Data Updated Locally", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(editInformation.this, "Data Not Updated", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            Toast.makeText(editInformation.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(editInformation.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
//--------------------------------------------------------------------------------------------------

//        DELETE BUTTON ----------------------------------------------------------------------------
        delete = findViewById(R.id.Delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(editInformation.this);
                alert.setTitle("Delete user");
                alert.setMessage("Confirm deleting Account permanently ");
                alert.setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean check;
                        check = DBM.DeleteUser(email);
                        if (check) {
//                        Deleting all users data

//                        Going back to login after deleting user
                            Intent intent = new Intent(editInformation.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(editInformation.this, "User DELETED ...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(editInformation.this, "Unable to delete user at this Time", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(editInformation.this, "Deletion Cancel", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
        });
    }
    //--------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        finish();
    }
}