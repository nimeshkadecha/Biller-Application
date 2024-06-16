package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class register extends AppCompatActivity {
	private EditText name, email, password, gst, contact, address;
	public static final String SHARED_PREFS = "sharedPrefs";
	private final login_Screen MA = new login_Screen();
	private DBManager DBM;
	private ImageView menuclick;

	private TextInputLayout gstLayout;

	@SuppressLint("MissingInflatedId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

//        WORKING WITH TOOLBAR =====================================================================
		//        Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
		//        Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//==================================================================================================

//        Assigning object it's value for SQLite Assess ============================================
		DBM = new DBManager(this);
//==================================================================================================

//        Finding editText and Buttons  ============================================================
		name = findViewById(R.id.rName);
		email = findViewById(R.id.email);
		password = findViewById(R.id.password);
		gst = findViewById(R.id.gst);
		gstLayout = findViewById(R.id.textInputLayoutGST_Number);
// Making GST button invisible so that user can togel switch
		gst.setVisibility(View.GONE);
		gstLayout.setVisibility(View.GONE);
		gst.setText("-1");
		contact = findViewById(R.id.contactNumber);
		address = findViewById(R.id.address);
		@SuppressLint("UseSwitchCompatOrMaterialCode") Switch gstSwitch = findViewById(R.id.switch1GST);
		gstSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
			if (isChecked) {
				gst.setText("");
				gst.setVisibility(View.VISIBLE);
				gstLayout.setVisibility(View.VISIBLE);
			} else {
				gst.setVisibility(View.GONE);
				gstLayout.setVisibility(View.GONE);
				gst.setText("0");
			}
		});

		Button show = findViewById(R.id.show);
		show.setVisibility(View.INVISIBLE);
//==================================================================================================

//        Display all information of users but it's hidden it is for testing purposes ==============
		show.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("Range")
			@Override
			public void onClick(View v) {
				Cursor res = DBM.getData();
				if (res.getCount() == 0) {
					Toast.makeText(register.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
					return;
				}
				StringBuilder buffer = new StringBuilder();
				while (res.moveToNext()) {
					buffer.append("E-mail: ").append(res.getString(res.getColumnIndex("email"))).append("\n");
					buffer.append("Name: ").append(res.getString(res.getColumnIndex("name"))).append("\n");
					buffer.append("Password: ").append(res.getString(res.getColumnIndex("password"))).append("\n");
					buffer.append("gst: ").append(res.getString(res.getColumnIndex("gst"))).append("\n");
					buffer.append("Contact: ").append(res.getString(res.getColumnIndex("contact"))).append("\n");
					buffer.append("Address: ").append(res.getString(res.getColumnIndex("address"))).append("\n\n");
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
				builder.setCancelable(true);
				builder.setTitle("Users");
				builder.setMessage(buffer.toString());
				builder.show();
			}
		});
//==================================================================================================
	}

	//    Register button =============================================================================
	public void registerBtn_reg(View view) {
		Intent login = new Intent(this, login_Screen.class);

		String nameTXT = name.getText().toString();
		String emailTXT = email.getText().toString();
		String passwordTXT = password.getText().toString();
		String gstTXT = gst.getText().toString();
		String contactTXT = contact.getText().toString();
		String addressTXT = address.getText().toString();

		if (emailTXT.isEmpty() || passwordTXT.isEmpty() || nameTXT.isEmpty() || gstTXT.isEmpty() || contactTXT.isEmpty() || addressTXT.isEmpty()) {
			if (emailTXT.isEmpty() && passwordTXT.isEmpty() && nameTXT.isEmpty() && gstTXT.isEmpty() && contactTXT.isEmpty() && addressTXT.isEmpty()) {
				Toast.makeText(this, "Fill-up FORM", Toast.LENGTH_SHORT).show();
			} else if (emailTXT.isEmpty()) {
				Toast.makeText(this, "Fill up E-mail", Toast.LENGTH_SHORT).show();
			} else if (passwordTXT.isEmpty()) {
				Toast.makeText(this, "Fill up Password", Toast.LENGTH_SHORT).show();
			} else if (nameTXT.isEmpty()) {
				Toast.makeText(this, "Fill up name", Toast.LENGTH_SHORT).show();
			} else if (gstTXT.isEmpty()) {
				Toast.makeText(this, "Fill up GST", Toast.LENGTH_SHORT).show();
			} else if (contactTXT.isEmpty()) {
				Toast.makeText(this, "Fill up Contact", Toast.LENGTH_SHORT).show();
			} else if (addressTXT.isEmpty()) {
				Toast.makeText(this, "Fill up address", Toast.LENGTH_SHORT).show();
			}
		} else {
			boolean validEmail = MA.EmailValidation(emailTXT);
			boolean validPassword = MA.passwordValidation(passwordTXT);

			if (contactTXT.length() != 10) {
				Toast.makeText(register.this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
			} else {
				boolean CheckOperation;

				if (validEmail && validPassword) {
					CheckOperation = DBM.RegisterUser(nameTXT, emailTXT, passwordTXT, gstTXT, contactTXT, addressTXT);
					if (CheckOperation) {
						Intent SuccessfullyLogin = new Intent(this, home.class);
						Toast.makeText(this, "User Register Successfully", Toast.LENGTH_SHORT).show();
						SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("Login", "true");
						editor.putString("UserName", emailTXT);
						editor.apply();

						SuccessfullyLogin.putExtra("Email", emailTXT);
						SuccessfullyLogin.putExtra("Origin", "Login");
						startActivity(SuccessfullyLogin);
						finish();
					} else {
						Toast.makeText(this, "Fail to Register User", Toast.LENGTH_SHORT).show();
					}
				} else if (!validEmail) {
					Toast.makeText(this, "Invalid E-Mail", Toast.LENGTH_SHORT).show();
				} else if (!validPassword) {
					Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
//==================================================================================================
}