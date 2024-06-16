package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class reset_password extends AppCompatActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	private EditText password, confirmPassword;
	private DBManager DBM;

	@SuppressLint("MissingInflatedId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_password);

//        finding
		password = findViewById(R.id.password);
		confirmPassword = findViewById(R.id.ConfirmPassword);
		DBM = new DBManager(this);

//        WORKING WITH TOOLBAR =====================================================================
		//        Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
		//        Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//==================================================================================================

//        Confirm Button ===========================================================================
		Button confirm = findViewById(R.id.confirm);
		confirm.setOnClickListener(v -> {
			// Calling Function
			Confirm();
		});
//==================================================================================================

	} // End of onCreate

	//    validating Password length >= 6 =============================================================
	private boolean PasswordValidation(EditText password, EditText confirmPassword) {
		String passwordInput = password.getText().toString().trim();
		String confirmPasswordInput = confirmPassword.getText().toString().trim();
		return passwordInput.length() == confirmPasswordInput.length() && passwordInput.length() >= 6;
	}
//==================================================================================================

	//    RESETTING PASSWORD ==========================================================================
	public void Confirm() {
		boolean VP = PasswordValidation(password, confirmPassword);
		if (VP) {
			if (password.getText().toString().equals(confirmPassword.getText().toString())) {
				Bundle bundle = getIntent().getExtras();
				assert bundle != null;
				String Email = bundle.getString("Email");
				boolean check;
				check = DBM.ResetPassword(Email, confirmPassword.getText().toString().trim());

				if (check) {
					// Login in user After Reset
					SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("Login", "true");
					editor.putString("UserName", Email);
					editor.apply();

					Intent SucessfullyLogin = new Intent(reset_password.this, home.class);
					SucessfullyLogin.putExtra("Email", Email);
					SucessfullyLogin.putExtra("Origin", "Login");
					startActivity(SucessfullyLogin);
					finish();
					Toast.makeText(reset_password.this, "Password Updated", Toast.LENGTH_SHORT).show();

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
//==================================================================================================

}