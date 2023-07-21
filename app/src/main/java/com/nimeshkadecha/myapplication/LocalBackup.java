package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class LocalBackup extends AppCompatActivity {

    DBManager dbManager = new DBManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_backup);

        Button DownloadBTN = findViewById(R.id.Downloadbtn);

        DownloadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               String check = dbManager.downloadBackup(LocalBackup.this);
               if(!check.equals("false")){
                   Toast.makeText(LocalBackup.this, "Success", Toast.LENGTH_SHORT).show();
                   AlertDialog.Builder builder = new AlertDialog.Builder(LocalBackup.this);
                   builder.setCancelable(true);
                   builder.setTitle("Bills");
                   builder.setMessage("You can locate the backup at \n \""+check+"\" ");
                   builder.show();
               }else{
                   Toast.makeText(LocalBackup.this, "Failed", Toast.LENGTH_SHORT).show();
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
        intent.setType("*/*"); // Set the MIME type to all files
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                boolean success = restoreBackupFromUri(LocalBackup.this,fileUri);

                if (success) {
                    // Backup data restored successfully
                    Toast.makeText(this, "Backup data restored successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Error restoring backup data
                    Toast.makeText(this, "Error restoring backup data", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static boolean restoreBackupFromUri(Context context, Uri backupFileUri) {
        try {
            // Get the path of the backup file
            String backupFilePath = backupFileUri.getPath();

            // Open the database and close it to create an empty database file
            SQLiteDatabase targetDatabase = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
            targetDatabase.close();

            // Get the database directory path
            File dbDir = context.getDatabasePath("biller").getParentFile();

            // Copy the backup file to the database directory with the correct name
            File backupFile = new File(backupFilePath);
            File targetFile = new File(dbDir, "biller");
            FileChannel src = new FileInputStream(backupFile).getChannel();
            FileChannel dst = new FileOutputStream(targetFile).getChannel();
            dst.transferFrom(src, 0, src.size());

            src.close();
            dst.close();

            return true;
        } catch (SQLiteException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}