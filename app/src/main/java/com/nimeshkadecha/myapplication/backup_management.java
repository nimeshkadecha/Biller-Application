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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
	Button showPathAuto;
	@SuppressLint("UseSwitchCompatOrMaterialCode")
	Switch AutoUploadSwitch;
	//      Getting Current Date to put ----------------------------------------------------------------
	Date c = Calendar.getInstance().getTime();
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	String formattedDate = df.format(c);
	private TextView uploadDate, Download, AutoUpdateLabel, AutoUpdateDate;

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


//        Using Shared Preference to store Last Date ===============================================
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String UploadDate = sharedPreferences.getString("Last Upload", "Not Uploaded");
		String DownloadDate = sharedPreferences.getString("Last Download", "Not Downloaded");
		String AutoUpload = sharedPreferences.getString("AutoUpload", "Not Uploaded");


		//Upload Date ==============================================================================
		uploadDate = findViewById(R.id.uploadDate);
		uploadDate.setText(UploadDate);

		// Download Date ===========================================================================
		Download = findViewById(R.id.Download);
		Download.setText(DownloadDate);

		//button ===================================================================================
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

		// AutoDownload switch =====================================================================
		AutoUpdateLabel = findViewById(R.id.AutoUpdateLable);

		AutoUpdateDate = findViewById(R.id.autouploadDate);

		AutoUploadSwitch = findViewById(R.id.autoBackupSwitch);

		boolean hasPermission = (ContextCompat.checkSelfPermission(getApplicationContext(),
		                                                           Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

		if (!hasPermission) {
			Log.d("Permission", "Requesting Write External Storage permission");
			ActivityCompat.requestPermissions(backup_management.this,
			                                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
			                                  REQUEST_WRITE_STORAGE);
		} else {
			Log.d("Permission", "Write External Storage permission already granted");
		}

		AutoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (isChecked) {
					boolean check = dbManager.AutoLocalBackup(backup_management.this);

					if (check) {
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

		// Download backup button ==================================================================
		Button DownloadBTN = findViewById(R.id.Downloadbtn);
		DownloadBTN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (isPermissionGranted(backup_management.this)) {

					String check = dbManager.DownloadBackup(backup_management.this, sharedPreferences.getString("UserName", ""));
					if (!check.equals("false")) {
						SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("Last Download", formattedDate);
						editor.apply();
						Download.setText(formattedDate);

						Toast.makeText(backup_management.this, "Success", Toast.LENGTH_SHORT).show();
						AlertDialog.Builder builder = new AlertDialog.Builder(backup_management.this);
						builder.setCancelable(false);
						builder.setTitle("Bills");
						builder.setMessage("Notice:\nYour current app password is used to encrypt this backup.\nYou must provide the same password to access it in the future.\n\nStore securely; backup is deleted with app.\n\nYou can locate the backup at\n \"" + check + "\" ");
						builder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						builder.show();
					}
				} else {
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
		Button uploadBTN = findViewById(R.id.LocaluploadDatabtn);
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

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_WRITE_STORAGE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d("Permission", "Write External Storage permission granted");
			} else {
				Log.d("Permission", "Write External Storage permission denied");
			}
		}
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