package com.nimeshkadecha.biller;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
	private ImageView fingerprintUnlock,billerLogo;

	private String checkLogin, username, bio_matrix_lock;

	private View PlodingView;
	private LinearLayout loadingBlur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		//          menu Button ----------------------------------------------------------------------------
		ImageView menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
		menuclick.setVisibility(View.INVISIBLE);

//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

		// loding animation
		// Finding progressbar
		PlodingView = findViewById(R.id.Ploding_ls);
		loadingBlur = findViewById(R.id.LoadingBlur_ls);
		PlodingView.setVisibility(View.INVISIBLE);
		loadingBlur.setVisibility(View.INVISIBLE);

		// Initialize Lottie animation view and show it
		lottieAnimationView = findViewById(R.id.lottie_animation_login);
		lottieAnimationView_GEMINI = findViewById(R.id.lottie_animation_login_gemini);
		loginForm = findViewById(R.id.loginForm);
		billerLogo = findViewById(R.id.billerImage);

// Execute async task to perform initialization in the background
		// In your activity's onCreate() or appropriate method
		lottieAnimationView.setVisibility(View.VISIBLE);
		lottieAnimationView_GEMINI.setVisibility(View.VISIBLE);
		findViewById(R.id.poweredBy).setVisibility(View.VISIBLE);
		new InitTask().execute();
//--------------------------------------------------------------------------------------------------

//		Demo Login button

		InsertDemoDataTask task = new InsertDemoDataTask(() -> {
			Intent SucessfullyLogin = new Intent(login_Screen.this, home.class);
			boolean verify;
			String emailTXT = "contact@fastbites.com";
			String passwordTXT = "1234567890";
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
				Toast.makeText(login_Screen.this, "wrong email OR password", Toast.LENGTH_SHORT).show();
			}
		});

		Button d_login = findViewById(R.id.demoStart);
		d_login.setOnClickListener(v -> {
			PlodingView.setVisibility(View.VISIBLE);
			loadingBlur.setVisibility(View.VISIBLE);
			task.execute();
		});
//==================================================================================================
	}
//==================================================================================================

	//    Convert Uri to File =========================================================================
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
	//=================================================================================================

	//    Verifying Wifi / internet is ON =============================================================
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo() != null;
	}
// =================================================================================================

	//    Select Backup file ==========================================================================
	private void selectBackupFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/octet-stream"); // Set the MIME type to all files
		startActivityForResult(intent, 101);
	}
// =================================================================================================

	//    Select Backup file ==========================================================================
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
	// ================================================================================================

	//    Code for validating email ===================================================================
	public boolean EmailValidation(String email) {
		return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
// =================================================================================================

	//    Code for validation Password ================================================================
	public boolean passwordValidation(String password) {
		return !password.isEmpty() && password.length() > 6;
	}
//==================================================================================================

	//    Going to register page ======================================================================
	public void registerBtn(View view) {
		startActivity(new Intent(this, register.class));
		finish();
	}
//==================================================================================================

	//    Going to HOME Page if ID Password Is correct ================================================
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
		}
	}
//==================================================================================================

	//    ON Click Forgot Password ====================================================================
	public void Forgot_Password(View view) {
		if (checkConnection()) {
			Intent forgotpassword = new Intent(this, forgot_password.class);
			forgotpassword.putExtra("Origin", "Forgot");
			startActivity(forgotpassword);
		} else {
			Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
		}
	}

//==================================================================================================

	//    Background Initialization ===================================================================
	@SuppressLint("StaticFieldLeak")
	private class InitTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			// Background tasks (non-UI work only)
			DBM = new DBManager(login_Screen.this);
			sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
			checkLogin = sharedPreferences.getString("Login", "");
			username = sharedPreferences.getString("UserName", "");
			bio_matrix_lock = sharedPreferences.getString("bioLock", "");
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			// UI thread work - initialize views after background tasks complete
			email = findViewById(R.id.email);
			password = findViewById(R.id.password);
			local_upload = findViewById(R.id.local_upload);
			fingerprintUnlock = findViewById(R.id.fingerprint_unlock);
			permisions = findViewById(R.id.permisions);

			// Hide animations immediately
			lottieAnimationView.setVisibility(View.GONE);
			lottieAnimationView_GEMINI.setVisibility(View.GONE);
			findViewById(R.id.poweredBy).setVisibility(View.GONE);

			// Check login status immediately
			if (Objects.equals(checkLogin, "true")) {
				Intent SuccessfullyLogin = new Intent(login_Screen.this, home.class);
				SuccessfullyLogin.putExtra("Email", username);
				SuccessfullyLogin.putExtra("Origin", "Login");
				startActivity(SuccessfullyLogin);
				finish();
			} else {
				// Show login UI
				loginForm.setVisibility(View.VISIBLE);
				permisions.setVisibility(View.INVISIBLE);
				billerLogo.setVisibility(View.GONE);

				if (bio_matrix_lock.equals("true")) {
					fingerprintUnlock.setVisibility(View.VISIBLE);
				}

				setupButtonListeners();
			}
		}

		private void setupButtonListeners() {
			local_upload.setOnClickListener(v -> showPasswordDialog());
			fingerprintUnlock.setOnClickListener(v -> handleFingerprintLogin());
		}

		private void showPasswordDialog() {
			AlertDialog.Builder builder = new AlertDialog.Builder(login_Screen.this);
			builder.setTitle("Enter Password");
			final EditText input = new EditText(login_Screen.this);
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			builder.setView(input);

			builder.setPositiveButton("OK", (dialog, which) -> {
				String password = input.getText().toString();
				if (!password.isEmpty()) {
					selectBackupFile();
				} else {
					Toast.makeText(login_Screen.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
				}
			});
			builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
			builder.show();
		}

		private void handleFingerprintLogin() {
			if (EmailValidation(email.getText().toString())) {
				new Thread(() -> { // Move database operation to background
					final boolean verify = DBM.ValidateUser(email.getText().toString());
					runOnUiThread(() -> {
						if (verify) {
							Intent FingerprintVerification = new Intent(login_Screen.this, fingerprint_lock.class);
							FingerprintVerification.putExtra("Email", email.getText().toString());
							FingerprintVerification.putExtra("Origin", "Login");
							startActivity(FingerprintVerification);
						} else {
							Toast.makeText(login_Screen.this, "Email Doesn't Exist", Toast.LENGTH_SHORT).show();
						}
					});
				}).start();
			} else {
				Toast.makeText(login_Screen.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
			}
		}
	}
// =================================================================================================

//	Insert Demo data in background ==================================================================

	public interface OnDataInsertedListener {
		void onDataInserted();
	}

	public class InsertDemoDataTask extends AsyncTask<Void, Void, Boolean> {

		private OnDataInsertedListener listener;

		// Constructor to accept listener
		public InsertDemoDataTask(OnDataInsertedListener listener) {
			this.listener = listener;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			// Assuming DBM.insertDemoData() returns a boolean indicating success
			return DBM.insertDemoData();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			PlodingView.setVisibility(View.INVISIBLE);
			loadingBlur.setVisibility(View.INVISIBLE);
			System.out.println(result );
			System.out.println( listener);
			if (result && listener != null) {
				listener.onDataInserted();  // Notify listener if insertion was successful
			}else{
				Toast.makeText(login_Screen.this, "Error while logging in please clear Application data to insert demo data ", Toast.LENGTH_SHORT).show();
			}
		}
	}
//	=================================================================================================

}