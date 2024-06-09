package com.nimeshkadecha.myapplication;

import static com.nimeshkadecha.myapplication.login_Screen.MANAGE_STORAGE_PERMISSION_CODE;
import static com.nimeshkadecha.myapplication.login_Screen.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class backup_management extends AppCompatActivity {


	public static final String SHARED_PREFS = "sharedPrefs";
	private static final int REQUEST_WRITE_STORAGE = 112;
	final String[] password = {"0000"};
	DBManager dbManager = new DBManager(this);

	private View PlodingView;
	private LinearLayout loadingBlur;


	@SuppressLint("UseSwitchCompatOrMaterialCode")

	//      Getting Current Date to put ----------------------------------------------------------------
	Date c = Calendar.getInstance().getTime();
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	String formattedDate = df.format(c);
	private TextView uploadDate, Download;

	// getting file from URL =======================================================================
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

			byte[] data = new byte[4096];
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_management);

//        Google ads code ==========================================================================
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);


		//        Finding progressbar
		PlodingView = findViewById(R.id.Ploding);
		PlodingView.setVisibility(View.INVISIBLE);
		loadingBlur = findViewById(R.id.LoadingBlur);
		loadingBlur.setVisibility(View.INVISIBLE);
//        Using Shared Preference to store Last Date ===============================================
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String UploadDate = sharedPreferences.getString("Last Upload", "Not Uploaded");
		String DownloadDate = sharedPreferences.getString("Last Download", "Not Downloaded");

		//Upload Date ==============================================================================
		uploadDate = findViewById(R.id.uploadDate);
		uploadDate.setText(UploadDate);

		// Download Date ===========================================================================
		Download = findViewById(R.id.Download);
		Download.setText(DownloadDate);

		// Download backup button ==================================================================
		Button DownloadBTN = findViewById(R.id.analyze_stock_btn);
		DownloadBTN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
			}
		});




		// upload backup button ====================================================================
		Button uploadBTN = findViewById(R.id.understand_customer_btn);
		uploadBTN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
				builder.setTitle("Enter Password of backup file");

				// Set up the input
				final EditText input = new EditText(backup_management.this);
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				builder.setView(input);

				// Set up the buttons
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						password[0] = input.getText().toString();
						if (!password[0].equals("") && password[0].length() > 7) {
							selectBackupFile();
						} else {
							Toast.makeText(backup_management.this, "Please enter password valid", Toast.LENGTH_SHORT).show();
						}
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				// Show the dialog
				builder.show();
			}
		});
	}


	private class BackupTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			PlodingView.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			String username = params[0];
			return dbManager.DownloadBackup(backup_management.this, username);
		}

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
				builder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.show();
			} else {
				Toast.makeText(backup_management.this, "Failed", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Permission checker ==========================================================================
	private boolean isPermissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
			return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
		}
	}

	// selecting .db file ==========================================================================
	private void selectBackupFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/octet-stream"); // Set the MIME type to all files
		startActivityForResult(intent, 101);
	}

	// getting url of file =========================================================================
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
//						SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//						SharedPreferences.Editor editor = sp.edit();
//						editor.putString("Last Upload", formattedDate);
//						editor.putString("Login", "false");
//						editor.putString("bioLock", "false");
//						editor.putString("UserName", "");
//						editor.apply();
//						uploadDate.setText(formattedDate);
//						AlertDialog.Builder alert = getBuilder();
//						alert.show();
						Toast.makeText(this, "Application Data is updated", Toast.LENGTH_SHORT).show();
					}
					// Now you have the File object, and you can use it as needed
					// For example, you can copy, move, or read the contents of the file
				} catch (IOException e) {
					e.printStackTrace();
					// Handle the error here
				}
			} else {
				Toast.makeText(this, "Please select a valid .db file", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@NonNull
	private AlertDialog.Builder getBuilder() {
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
		return alert;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_WRITE_STORAGE && grantResults.length > 0 && grantResults[0]
										!= PackageManager.PERMISSION_GRANTED)
				Toast.makeText(this, "Write External Storage permission denied", Toast.LENGTH_SHORT).show();
	}

//	@Override
//	protected void onStart() {
//		super.onStart();
//		//        Google ads code ==================================================================
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//	}
//
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		//        Google ads code ==================================================================
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		//        Google ads code ==================================================================
//		AdView mAdView;
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);
//	}

}