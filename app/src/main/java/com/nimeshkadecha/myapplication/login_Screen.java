package com.nimeshkadecha.myapplication;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class login_Screen extends AppCompatActivity {

	private int StoragePermisionCode = 112;

	final String[] passwordUpload = {"0000"};
	private static final int STORAGE_PERMISSION_CODE = 112;
	private static final int MANAGE_STORAGE_PERMISSION_CODE = 113;
	//    initlizing varablwe
	private EditText email, password;
	private Button permisions;

	private TextView Data_cloud;

	//    Creating object
	private DBManager DBM;

	private ImageView menuclick;


	//    Shared preference to store Login Information !
	public static final String SHARED_PREFS = "sharedPrefs";

	//    Verifying Wifi / internet is ON --------------------------------------------------------------
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo net = manager.getActiveNetworkInfo();

		if (net == null) {
			return false;
		} else {
			return true;
		}
	}
//--------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

//        Checking if user is already loged in or not ----------------------------------------------
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String checkLogin = sharedPreferences.getString("Login", "");
		String username = sharedPreferences.getString("UserName", "");
		if (Objects.equals(checkLogin, "true")) {

			Intent SucessfullyLogin = new Intent(this, home.class);
			SucessfullyLogin.putExtra("Email", username);
			SucessfullyLogin.putExtra("Origin", "Login");
			startActivity(SucessfullyLogin);

			finish();
		}
//--------------------------------------------------------------------------------------------------

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

//          menu Button ----------------------------------------------------------------------------
		menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
		menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//        assign variable --------------------------------------------------------------------------
		email = findViewById(R.id.email);
		password = findViewById(R.id.password);

//--------------------------------------------------------------------------------------------------

//        assigning DBM ----------------------------------------------------------------------------
		DBM = new DBManager(this);

//        Working with Permission ------------------------------------------------------------------

		permisions = findViewById(R.id.permisions);

		if (ContextCompat.checkSelfPermission(login_Screen.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			permisions.setVisibility(View.INVISIBLE);
		} else {
			ActivityCompat.requestPermissions(login_Screen.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
			permisions.setVisibility(View.VISIBLE);
			checkAndRequestPermissions();
		}

		final int[] times_clicked = {0};
		permisions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (times_clicked[0] == 0) {
					requestStoragePermissions();
					times_clicked[0]++;
				} else {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					Uri uri = Uri.fromParts("package", getPackageName(), null);
					intent.setData(uri);
					startActivity(intent);
				}

			}
		});


//        Fingerprint unlock -----------------------------------------------------------------------

		ImageView fingerprintUnlock = findViewById(R.id.fingerprint_unlock);

		//        checking for fingerprint verification
		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String bio_matrix_lock = sp.getString("bioLock", "");
		if (bio_matrix_lock.equals("true")) {
			fingerprintUnlock.setVisibility(View.VISIBLE);

		}

		fingerprintUnlock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean EV = EmailValidation(email.getText().toString());

				if (EV) {
					boolean verify = DBM.ValidateUser(email.getText().toString());
					if (verify) {
						Intent FingerprintVerification = new Intent(login_Screen.this, fingerprint_lock.class);

						FingerprintVerification.putExtra("Email", email.getText().toString());
						FingerprintVerification.putExtra("Origin", "Login");

						startActivity(FingerprintVerification);
					} else {
						Toast.makeText(login_Screen.this, "Email Don't Exists", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(login_Screen.this, "Enter Email for login", Toast.LENGTH_SHORT).show();
				}
			}
		});

//--------------------------------------------------------------------------------------------------

//        Login From Cloud button ------------------------------------------------------------------
		Data_cloud = findViewById(R.id.Data_cloud);
		Data_cloud.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(login_Screen.this);
				builder.setTitle("Enter Password");

				// Set up the input
				final EditText input = new EditText(login_Screen.this);
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				builder.setView(input);

				// Set up the buttons
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						passwordUpload[0] = input.getText().toString();
						if(!passwordUpload[0].isEmpty()){
							selectBackupFile();
						}else{
							Toast.makeText(login_Screen.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
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
//--------------------------------------------------------------------------------------------------
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
					Date c = Calendar.getInstance().getTime();
					SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
					String formattedDate = df.format(c);

					File selectedFile = convertUriToFile(this, selectedFileUri); // Get the File from the file picker or any other source
					boolean restoreSuccess = DBM.UploadLocalBackup(this, selectedFile, passwordUpload[0].toCharArray());
					if (!restoreSuccess) {
						Toast.makeText(this, "Password is incorrect", Toast.LENGTH_SHORT).show();
					} else {
						SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("Last Upload", formattedDate);
						editor.apply();

						Toast.makeText(this, "Application Data is updated", Toast.LENGTH_SHORT).show();
						// Now you have the File object, and you can use it as needed
						// For example, you can copy, move, or read the contents of the file
					}
				} catch (IOException e) {
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

	//    Working on requesting STORAGE permission -----------------------------------------------------
//	private void requestStoragePermissions() {
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//			try {
//				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//				intent.addCategory("android.intent.category.DEFAULT");
//				intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
//				startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
//			} catch (Exception e) {
//				Intent intent = new Intent();
//				intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//				startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
//			}
//		} else {
//			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//		}
////
////		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
////			new AlertDialog.Builder(this)
////											.setTitle("Permission is needed for")
////											.setMessage("Permission is needed for Creating Bill PDF")
////											.setPositiveButton("OK", new DialogInterface.OnClickListener() {
////												@Override
////												public void onClick(DialogInterface dialog, int which) {
////													ActivityCompat.requestPermissions(login_Screen.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
////												}
////											})
////											.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
////												@Override
////												public void onClick(DialogInterface dialog, int which) {
////													dialog.dismiss();
////												}
////											})
////											.create().show();
////		} else {
////			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, StoragePermisionCode);
////		}
//	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.d("ENimesh", "requestCode : " + requestCode + " permision code = " + StoragePermisionCode);

		if (requestCode == STORAGE_PERMISSION_CODE) {
			boolean allPermissionsGranted = true;
			for (int grantResult : grantResults) {
				if (grantResult != PackageManager.PERMISSION_GRANTED) {
					allPermissionsGranted = false;
					break;
				}
			}
			if (allPermissionsGranted) {
				Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permissions not Granted, Allow them to create PDF", Toast.LENGTH_SHORT).show();
			}
		}

//		if (requestCode == STORAGE_PERMISSION_CODE) {
//			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//				Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//				permisions.setVisibility(View.INVISIBLE);
//			} else {
//				Toast.makeText(this, "Permission not Granted, Allow it to create PDF", Toast.LENGTH_SHORT).show();
//			}
//		}

//		if (requestCode == StoragePermisionCode) {
//			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//				Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//				permisions.setVisibility(View.INVISIBLE);
//			} else {
//				Toast.makeText(this, "Permission not Granted, Allow it to create PDF", Toast.LENGTH_SHORT).show();
//			}
//		}

	}
//--------------------------------------------------------------------------------------------------

	//    Code for validating email starts--------------------------------------------------------------
	public boolean EmailValidation(String email) {
		String emailinput = email;
		if (!emailinput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailinput).matches()) {
			return true;
		} else {

			return false;
		}
	}
//--------------------------------------------------------------------------------------------------

	//    Code for validation Password starts-----------------------------------------------------------
	public boolean passwordValidation(String password) {
		String passwordInput = password;
		if (!passwordInput.isEmpty() && passwordInput.length() > 6) {
			return true;
		} else {
			return false;
		}
	}
//--------------------------------------------------------------------------------------------------

	//    Going to register page -----------------------------------------------------------------------
	public void register(View view) {
		Intent register = new Intent(this, register.class);
		startActivity(register);
	}
//--------------------------------------------------------------------------------------------------

	//    Going to HOME Page if ID Password Is correct -------------------------------------------------
	public void login(View view) {
		Intent SucessfullyLogin = new Intent(this, home.class);
		boolean EV = EmailValidation(email.getText().toString());
		boolean EP = passwordValidation(password.getText().toString());
		if (EV && EP) {
			boolean verify;
			String emailTXT = email.getText().toString();
			String passwordTXT = password.getText().toString();
			verify = DBM.LoginUser(emailTXT, passwordTXT);
			if (verify) {
				SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("Login", "true");
				editor.putString("UserName", emailTXT);
				editor.apply();

				SucessfullyLogin.putExtra("Email", emailTXT);
				SucessfullyLogin.putExtra("Origin", "Login");
				startActivity(SucessfullyLogin);
				finish();
			} else {
				Toast.makeText(this, "wrong email OR password", Toast.LENGTH_SHORT).show();
			}
		} else if (!EV) {
			if (email.getText().length() == 0) {
				email.setError("Enter E-mail here");
				Toast.makeText(this, "Please Enter E-Mail", Toast.LENGTH_SHORT).show();
			} else {
				email.setError("Enter Valid E-mail");
				Toast.makeText(this, "Invalid E-Mail", Toast.LENGTH_SHORT).show();
			}
		} else if (!EP) {
			Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
		}
	}
//--------------------------------------------------------------------------------------------------

	//    ON Click Forgot Password ---------------------------------------------------------------------
	public void Forgot_Password(View view) {
		if (checkConnection()) {
			Intent forgotpassword = new Intent(this, forgot_password.class);

			forgotpassword.putExtra("Origin", "Forgot");
			startActivity(forgotpassword);
//            finish();
		} else {
			Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
		}

	}
//--------------------------------------------------------------------------------------------------

	private void checkAndRequestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (Environment.isExternalStorageManager()) {
				checkAndRequestLegacyPermissions();
			} else {
				Log.d("ENimesh", "permision granted by user");
//				permissions.setVisibility(View.VISIBLE);
			}
		} else {
			checkAndRequestLegacyPermissions();
		}
	}

	private void checkAndRequestLegacyPermissions() {
		List<String> permissionsNeeded = new ArrayList<>();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		}
		if (!permissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), STORAGE_PERMISSION_CODE);
//			permissions.setVisibility(View.VISIBLE);
			Log.d("ENimesh", "permision granted ");
		} else {
//			permissions.setVisibility(View.INVISIBLE);
			Log.d("ENimesh", "permision not granted");
		}
	}

	private void requestStoragePermissions() {
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
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
		}
	}

//	private void checkAndRequestPermissions() {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//			if (Environment.isExternalStorageManager()) {
//				Log.d("ENimesh","Permission is granted");
////				permisions.setVisibility(View.INVISIBLE);
//			} else {
//				Log.d("ENimesh","permission not granted");
////				permisions.setVisibility(View.VISIBLE);
//			}
//		} else {
//			List<String> permissionsNeeded = new ArrayList<>();
//			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//				permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//			}
//			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//				permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//			}
//			if (!permissionsNeeded.isEmpty()) {
//				ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), STORAGE_PERMISSION_CODE);
//				permisions.setVisibility(View.VISIBLE);
//			} else {
//				permisions.setVisibility(View.INVISIBLE);
//			}
//		}
//	}

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