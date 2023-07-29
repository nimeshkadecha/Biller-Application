package com.nimeshkadecha.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                if (!check.equals("false")) {
                    Toast.makeText(LocalBackup.this, "Success", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(LocalBackup.this);
                    builder.setCancelable(true);
                    builder.setTitle("Bills");
                    builder.setMessage("You can locate the backup at \n \"" + check + "\" ");
                    builder.show();
                } else {
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
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();

            if (selectedFileUri != null) {

                Log.d("ENimesh", "Selected file URL " + selectedFileUri);

                try {
                    File selectedFile = convertUriToFile(this,selectedFileUri); // Get the File from the file picker or any other source
                    String restoreSuccess = dbManager.UploadLocalBackup(this, selectedFile);
                    Log.d("ENimesh", "result !!! restore ? = " + restoreSuccess);
                    // Now you have the File object, and you can use it as needed
                    // For example, you can copy, move, or read the contents of the file
                } catch (IOException e) {
                    Log.d("ENimesh","catch ="+e.toString());
                    e.printStackTrace();
                    // Handle the error here
                }

            } else {
                Toast.makeText(this, "Null ?1", Toast.LENGTH_SHORT).show();
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
            Log.d("ENimesh","current dirr:"+ context.getCacheDir());
            Log.d("ENimesh","input:"+ input);
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


    // Helper method to get File from Uri
//    private File getFileFromUri(Uri uri) {
//        String filePath = null;
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] projection = {MediaStore.MediaColumns.DATA};
//            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//                    filePath = cursor.getString(columnIndex);
//                }
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            filePath = uri.getPath();
//        }
//
//        if (filePath != null) {
//            return new File(filePath);
//        }
//
//        return null;
//    }
}