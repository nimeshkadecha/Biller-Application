package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class backup_management extends AppCompatActivity {

    DBManager dbManager = new DBManager(this);
    public static final String SHARED_PREFS = "sharedPrefs";

    Button showPathAuto;
    Switch AutoUploadSwitch;

    private TextView uploadDate, Download, AutoUpdateLabel, AutoUpdateDate;

    //      Getting Current Date to put ----------------------------------------------------------------
    Date c = Calendar.getInstance().getTime();

    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    String formattedDate = df.format(c);
//--------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_management);

//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================

//        Using Shared Preference to store Last Date -----------------------------------------------
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String UploadDate = sharedPreferences.getString("Last Upload", "Not Uploaded");
        String DownloadDate = sharedPreferences.getString("Last Download", "Not Downloaded");
        String AutoUpload = sharedPreferences.getString("AutoUpload", "Not Uploaded");
//--------------------------------------------------------------------------------------------------

        //Upload Date
        uploadDate = findViewById(R.id.uploadDate);
        uploadDate.setText(UploadDate);

        // Download Date
        Download = findViewById(R.id.Download);
        Download.setText(DownloadDate);

        //butotn
        showPathAuto = findViewById(R.id.showPathAuto);
        showPathAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
                builder.setCancelable(true);
                builder.setTitle("Automatic Backup Location");
                builder.setMessage("If enabled then you can find backup at \n:/Android/data/com.nimeshkadecha.Biller/Auto Backup/Auto_Biller_Backup.db");
                builder.show();
            }
        });

        AutoUpdateLabel = findViewById(R.id.AutoUpdateLable);

        AutoUpdateDate = findViewById(R.id.autouploadDate);

        AutoUploadSwitch = findViewById(R.id.autoBackupSwitch);

        AutoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    boolean check = dbManager.AutoLocalBackup(backup_management.this);

                    if(check){
                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        String formattedDate = df.format(c);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("AutoUpload", formattedDate);
                        editor.apply();
                        AutoUpdateDate.setText(formattedDate);
                    }

                    AutoUpdateLabel.setVisibility(View.VISIBLE);
                    AutoUpdateDate.setVisibility(View.VISIBLE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("AutoBackup", "true");
                    editor.apply();
                } else {
                    AutoUpdateLabel.setVisibility(View.GONE);
                    AutoUpdateDate.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("AutoBackup", "false");
                    editor.apply();
                }
            }
        });

        String checkAutoBackup = sharedPreferences.getString("AutoBackup", "");
        if (checkAutoBackup.equals("true")) {

            AutoUploadSwitch.setChecked(true);
            AutoUpdateLabel.setVisibility(View.VISIBLE);
            AutoUpdateDate.setVisibility(View.VISIBLE);
            AutoUpdateDate.setText(AutoUpload);
        }

        Button DownloadBTN = findViewById(R.id.Downloadbtn);

        DownloadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String check = dbManager.downloadBackup(backup_management.this);
                if (!check.equals("false")) {
                    SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("Last Download", formattedDate);
                    editor.apply();
                    Download.setText(formattedDate);

                    Toast.makeText(backup_management.this, "Success", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
                    builder.setCancelable(true);
                    builder.setTitle("Bills");
                    builder.setMessage("You can locate the backup at \n \"" + check + "\" ");
                    builder.show();

                } else {
                    Toast.makeText(backup_management.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button uploadBTN = findViewById(R.id.LocaluploadDatabtn);
        uploadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBackupFile();
            }
        });
    }

    private void selectBackupFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream"); // Set the MIME type to all files
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                try {
                    File selectedFile = convertUriToFile(this, selectedFileUri); // Get the File from the file picker or any other source
                    String restoreSuccess = dbManager.UploadLocalBackup(this, selectedFile);
                    SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("Last Upload", formattedDate);
                    editor.putString("Login", "false");
                    editor.putString("bioLock", "false");
                    editor.putString("UserName", "");
                    editor.apply();

                    uploadDate.setText(formattedDate);

                    Toast.makeText(this, "Application Data is updated", Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder alert = new AlertDialog.Builder(backup_management.this);
                    alert.setTitle("Security Note:");
                    alert.setCancelable(false);
                    alert.setMessage("For security reasons, you have been logged out.\nKindly use your previous email and password to log back in.");
                    alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finishAffinity();

                            Intent logOUT = new Intent(backup_management.this, login_Screen.class);
                            startActivity(logOUT);
                        }
                    });
                    alert.show();

                    // Now you have the File object, and you can use it as needed
                    // For example, you can copy, move, or read the contents of the file
                } catch (IOException e) {
                    Log.d("ENimesh", "catch =" + e.toString());
                    e.printStackTrace();
                    // Handle the error here
                }
            } else {
                Toast.makeText(this, "Please select a valid .db file", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static File convertUriToFile(Context context, Uri uri) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            // Open an input stream from the Uri
            ContentResolver contentResolver = context.getContentResolver();
            input = contentResolver.openInputStream(uri);

            // Create a temporary file in the app's cache directory
            File outputFile = new File(context.getCacheDir(), "temp_file_biller");

            // Create a stream to write data to the output file
            output = new FileOutputStream(outputFile);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                // Write data to the output stream
                output.write(data, 0, count);
            }

            return outputFile; // Return the temporary file
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

}