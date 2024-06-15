package com.nimeshkadecha.biller;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class login_Screen extends AppCompatActivity {
	//    Shared preference to store Login Information !
	static final int STORAGE_PERMISSION_CODE = 112;
	static final int MANAGE_STORAGE_PERMISSION_CODE = 113;
	final String[] passwordUpload = {"0000"};

	public static final String SHARED_PREFS = "sharedPrefs";

	private LottieAnimationView lottieAnimationView , lottieAnimationView_GEMINI;
	private ConstraintLayout loginForm;

	//    initlizing varablwe
	private EditText email, password;
	SharedPreferences sharedPreferences;

	private Button permisions;
	private TextView local_upload;
	//    Creating object
	private DBManager DBM;
	private ImageView menuclick, fingerprintUnlock;

	private String checkLogin, username, bio_matrix_lock;

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
//--------------------------------------------------------------------------------------------------

	//    Verifying Wifi / internet is ON --------------------------------------------------------------
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo net = manager.getActiveNetworkInfo();

		return net != null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		//          menu Button ----------------------------------------------------------------------------
		menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
		menuclick.setVisibility(View.INVISIBLE);

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

		// Initialize Lottie animation view and show it
		lottieAnimationView = findViewById(R.id.lottie_animation_login);
		lottieAnimationView_GEMINI = findViewById(R.id.lottie_animation_login_gemini);
		loginForm = findViewById(R.id.loginForm);

// Execute async task to perform initialization in the background
		new InitTask().execute();
//--------------------------------------------------------------------------------------------------
	}

	private class InitTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			// Perform background initialization tasks
			email = findViewById(R.id.email);
			password = findViewById(R.id.password);
			DBM = new DBManager(login_Screen.this);
			local_upload = findViewById(R.id.local_upload);
			fingerprintUnlock = findViewById(R.id.fingerprint_unlock);
			//        Working with Permission ------------------------------------------------------------------
			permisions = findViewById(R.id.permisions);
			email = findViewById(R.id.email);
			password = findViewById(R.id.password);


			// Check if user is already logged in
			sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
			checkLogin = sharedPreferences.getString("Login", "");
			username = sharedPreferences.getString("UserName", "");
			bio_matrix_lock = sharedPreferences.getString("bioLock", "");

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			// Check login status after animation
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					lottieAnimationView.setVisibility(View.GONE);
					lottieAnimationView_GEMINI.setVisibility(View.GONE);
					findViewById(R.id.poweredBy).setVisibility(View.GONE);
					if (Objects.equals(checkLogin, "true")) {
						Intent SuccessfullyLogin = new Intent(login_Screen.this, home.class);
						SuccessfullyLogin.putExtra("Email", username);
						SuccessfullyLogin.putExtra("Origin", "Login");
						startActivity(SuccessfullyLogin);
						finish();
					} else {
						loginForm.setVisibility(View.VISIBLE);
						permisions.setVisibility(View.INVISIBLE);

						if (bio_matrix_lock.equals("true")) {
							fingerprintUnlock.setVisibility(View.VISIBLE);
						}
						//        Login From Local button ------------------------------------------------------------------
						local_upload.setOnClickListener(new View.OnClickListener() {
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
										if (!passwordUpload[0].isEmpty()) {
											selectBackupFile();
										} else {
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
						//        Fingerprint unlock -----------------------------------------------------------------------
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

					}
				}
			}, 3000); // Delay for 3 seconds while showing the animation
		}
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

//--------------------------------------------------------------------------------------------------

	//    Code for validating email starts--------------------------------------------------------------
	public boolean EmailValidation(String email) {
		String emailinput = email;
		return !emailinput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailinput).matches();
	}
//--------------------------------------------------------------------------------------------------

	//    Code for validation Password starts-----------------------------------------------------------
	public boolean passwordValidation(String password) {
		String passwordInput = password;
		return !passwordInput.isEmpty() && passwordInput.length() > 6;
	}
//--------------------------------------------------------------------------------------------------

	//    Going to register page -----------------------------------------------------------------------
	public void register(View view) {
		Intent register = new Intent(this, register.class);
		startActivity(register);
		finish();
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

}