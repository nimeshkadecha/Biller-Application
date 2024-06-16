package com.nimeshkadecha.biller;

import static com.nimeshkadecha.biller.login_Screen.MANAGE_STORAGE_PERMISSION_CODE;
import static com.nimeshkadecha.biller.login_Screen.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class backup_management extends AppCompatActivity {

	private static final String SHARED_PREFS = "sharedPrefs";
	private static final int REQUEST_WRITE_STORAGE = 112;

	private final String[] password = {"0000"};

	private final DBManager dbManager = new DBManager(this);

	private View PlodingView;
	private LinearLayout loadingBlur;

	// Getting Current Date to put ====================================================================
	Date c = Calendar.getInstance().getTime();
	private TextView Download,uploadDate;
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	String formattedDate = df.format(c);
// =================================================================================================

	// getting file from URL ==========================================================================
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
			output = Files.newOutputStream(outputFile.toPath());

			byte[] data = new byte[4096];
			int count;
			while (true) {
				assert input != null;
				if ((count = input.read(data)) == -1) break;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_management);

// working on view =================================================================================

		// Finding progressbar
		PlodingView = findViewById(R.id.Ploding);
		PlodingView.setVisibility(View.INVISIBLE);
		loadingBlur = findViewById(R.id.LoadingBlur);
		loadingBlur.setVisibility(View.INVISIBLE);

		// Using Shared Preference to store Last Date
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String UploadDate = sharedPreferences.getString("Last Upload", "Not Uploaded");
		String DownloadDate = sharedPreferences.getString("Last Download", "Not Downloaded");

		// Upload Date TextView
		uploadDate = findViewById(R.id.uploadDate);
		uploadDate.setText(UploadDate);

		// Download Date TextView
		Download = findViewById(R.id.Download);
		Download.setText(DownloadDate);
// =================================================================================================

// Download backup button ==========================================================================
		Button DownloadBTN = findViewById(R.id.analyze_stock_btn);
		DownloadBTN.setOnClickListener(view -> {
			PlodingView.setVisibility(View.VISIBLE);
			loadingBlur.setVisibility(View.VISIBLE);
			if (isPermissionGranted(backup_management.this)) {
				new BackupTask().execute(sharedPreferences.getString("UserName", ""));
			} else {
				PlodingView.setVisibility(View.INVISIBLE);
				loadingBlur.setVisibility(View.INVISIBLE);
				Toast.makeText(backup_management.this, "Please allow storage permission", Toast.LENGTH_SHORT).show();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					try {
						Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
						intent.addCategory("android.intent.category.DEFAULT");
						intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
						startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
					} catch (Exception e) {
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
						startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
					}
				} else {
					ActivityCompat.requestPermissions(backup_management.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
				}
			}
		});
// =================================================================================================

// upload backup button ============================================================================
		Button uploadBTN = findViewById(R.id.understand_customer_btn);
		uploadBTN.setOnClickListener(view -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
			builder.setTitle("Enter Password of backup file");
			builder.setMessage("NOTE: the current data will be lost and only the backup file data will be uploaded");

			// Set up the input
			final EditText input = new EditText(backup_management.this);
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			builder.setView(input);

			// Set up the buttons
			builder.setPositiveButton("OK", (dialog, which) -> {
				password[0] = input.getText().toString();
				if (!password[0].isEmpty() && password[0].length() > 7) {
					selectBackupFile();
				} else {
					Toast.makeText(backup_management.this, "Please enter password valid", Toast.LENGTH_SHORT).show();
				}
			});
			builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

			// Show the dialog
			builder.show();
		});
// =================================================================================================

	} // OnCreate Ends ================================================================================
// =================================================================================================

	// Permission checker =============================================================================
	private boolean isPermissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
			return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
		}
	}

	// selecting .db file =============================================================================
	private void selectBackupFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/octet-stream"); // Set the MIME type to all files
		startActivityForResult(intent, 101);
	}

	// getting url of file ============================================================================
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
			Uri selectedFileUri = data.getData();
			if (selectedFileUri != null) {
				try {
					File selectedFile = convertUriToFile(this, selectedFileUri); // Get the File from the file picker or any other source
					boolean restoreSuccess = dbManager.UploadLocalBackup(this, selectedFile, password[0].toCharArray());
					if (!restoreSuccess) {
						Toast.makeText(this, "Password is incorrect", Toast.LENGTH_SHORT).show();
					} else {
						SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("Last Upload", formattedDate);
						editor.apply();
						uploadDate.setText(formattedDate);

						Toast.makeText(this, "Application Data is updated", Toast.LENGTH_SHORT).show();
					}
					// Now you have the File object, and you can use it as needed
					// For example, you can copy, move, or read the contents of the file
				} catch (IOException e) {
					Toast.makeText(this, "File Error !", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			} else
				Toast.makeText(this, "Please select a valid .db file", Toast.LENGTH_SHORT).show();

		}
	}
// =================================================================================================

	// Permission Request =============================================================================
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_WRITE_STORAGE &&
										grantResults.length > 0 &&
										grantResults[0] != PackageManager.PERMISSION_GRANTED
		)
				Toast.makeText(this, "Write External Storage permission denied", Toast.LENGTH_SHORT).show();
	}
// =================================================================================================

	// Downloading backup =============================================================================
	@SuppressLint("StaticFieldLeak")
	private class BackupTask extends AsyncTask<String, Void, String> {
		// making loading view visible
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			PlodingView.setVisibility(View.VISIBLE);
		}

		// downloading backup
		@Override
		protected String doInBackground(String... params) {
			String username = params[0];
			return dbManager.DownloadBackup(backup_management.this, username);
		}

		// making loading view invisible
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			PlodingView.setVisibility(View.INVISIBLE);
			loadingBlur.setVisibility(View.INVISIBLE);

			if (!result.equals("false")) {
				SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("Last Download", formattedDate);
				editor.apply();
				Download.setText(formattedDate);

				Toast.makeText(backup_management.this, "Success", Toast.LENGTH_SHORT).show();
				AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
				builder.setCancelable(false);
				builder.setTitle("Bills");
				builder.setMessage(Html.fromHtml("<b>Notice:</b><br>Your current app password is used to encrypt this backup.<br>You must provide the same password to access it in the future.<br><br>Store securely; backup is deleted with app.<br><br>You can locate the backup at<br> \"" + result + "\" "));
				builder.setNeutralButton("ok", (dialog, which) -> {
				});
				builder.show();
			} else {
				Toast.makeText(backup_management.this, "Failed", Toast.LENGTH_SHORT).show();
			}
		}
	}
// =================================================================================================
}