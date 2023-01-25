package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class resetPassword extends AppCompatActivity {

    private EditText password, confirmPassword;

    private DBManager DBM;
    private ImageView menuclick;

    private Button confirm;
    public static final String SHARED_PREFS = "sharedPrefs";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
//        finding
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.ConfirmPassword);
        DBM = new DBManager(this);

//        WORKING WITH TOOLBAR Starts---------------------------------------------------------------
    //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

    //        FINDING menu
        menuclick = findViewById(R.id.Menu);

    //        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        Confirm Button ---------------------------------------------------------------------------
        confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling Function
                Confirm();
            }
        });
//--------------------------------------------------------------------------------------------------

    }

//    validating Password length >= 6 --------------------------------------------------------------
    private boolean PasswordValidation(EditText password, EditText confirmPassword) {
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();
        if (passwordInput.length() != confirmPasswordInput.length() || passwordInput.length()<6) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------

//    RESETTING PASSWORD ---------------------------------------------------------------------------
    public void Confirm() {
        boolean VP = PasswordValidation(password, confirmPassword);
        if (VP) {
            if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                Bundle bundle = getIntent().getExtras();
                String number = bundle.getString("number");
                boolean check;
                check = DBM.resetPassword(number, confirmPassword.getText().toString().trim());

                if (check) {
                    // After updating Locally Updating it to Cloud
                    db.collection(number)
                            .document("Seller")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Map<String, Object> Reset = new HashMap<>();

                                    DocumentSnapshot ds = task.getResult();
                                    Reset.put("Name", String.valueOf(ds.get("Name")));
                                    Reset.put("Enail", String.valueOf(ds.get("Enail")));
                                    Reset.put("Password", confirmPassword.getText().toString());
                                    Reset.put("GST", String.valueOf(ds.get("GST")));
                                    Reset.put("Contact", String.valueOf(ds.get("Contact")));
                                    Reset.put("Address", String.valueOf(ds.get("Address")));

                                    db.collection(number)
                                            .document("Seller")
                                            .set(Reset)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        String emailTXT = String.valueOf(Reset.get("Enail"));

                                                        // Login in user After Reset
                                                        SharedPreferences sp = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sp.edit();
                                                        editor.putString("Login","true");
                                                        editor.putString("UserName",emailTXT);
                                                        editor.apply();

                                                        Intent SucessfullyLogin = new Intent(resetPassword.this, home.class);
                                                        SucessfullyLogin.putExtra("Email", emailTXT);
                                                        SucessfullyLogin.putExtra("Origin", "Login");
                                                        startActivity(SucessfullyLogin);
                                                        finish();
                                                        Toast.makeText(resetPassword.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(resetPassword.this, "Failed to Update Password", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });}
                            });

                } else {
                    Toast.makeText(this, "Password NOT reset Successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password Don't Match", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        }
    }
//--------------------------------------------------------------------------------------------------
}