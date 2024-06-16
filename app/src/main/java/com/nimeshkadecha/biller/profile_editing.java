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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class profile_editing extends AppCompatActivity {
	public static final String SHARED_PREFS = "sharedPrefs";
	private DBManager DBM;
	private EditText name, password, gst, contact, address;

	@SuppressLint({"MissingInflatedId", "SetTextI18n", "ResourceType"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_editing);

		//    WORKING WITH TOOLBAR =======================================================================
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
//          Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//  ================================================================================================

//        Creating object DBM for database activity
		DBM = new DBManager(this);

//      Displaying name and filling edittext of that user data =====================================
		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		String email = bundle.getString("Email");
		String number = "";

		TextView header = findViewById(R.id.Header);
		header.setText("Update data of E-Mail " + email);
		name = findViewById(R.id.rName);
		password = findViewById(R.id.password);
		gst = findViewById(R.id.gst);
		contact = findViewById(R.id.contactNumber);
		address = findViewById(R.id.address);

		Cursor getuserinfo = DBM.GetUser(email);
		if (getuserinfo.getCount() > 0) {
			getuserinfo.moveToFirst();
			do {
				name.setText(getuserinfo.getString(getuserinfo.getColumnIndex("name")));
				password.setText(getuserinfo.getString(getuserinfo.getColumnIndex("password")));
				if (getuserinfo.getString(getuserinfo.getColumnIndex("gst")).equals("-1")) {
					gst.setText("no");
				} else {
					gst.setText(getuserinfo.getString(getuserinfo.getColumnIndex("gst")));
				}
				contact.setText(getuserinfo.getString(getuserinfo.getColumnIndex("contact")));
				address.setText(getuserinfo.getString(getuserinfo.getColumnIndex("address")));
			} while (getuserinfo.moveToNext());
		}

//  ================================================================================================

//        SHOW Btn Works starts ====================================================================
		Button show = findViewById(R.id.show);
		show.setVisibility(View.GONE);
		show.setOnClickListener(v -> {
			Cursor res = DBM.GetUser(email);
			if (res.getCount() == 0) {
				Toast.makeText(profile_editing.this, "No Entry Exist", Toast.LENGTH_SHORT).show();
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

			AlertDialog.Builder builder = new AlertDialog.Builder(profile_editing.this);
			builder.setCancelable(true);
			builder.setTitle("Users");
			builder.setMessage(buffer.toString());
			builder.show();
		});
//  ================================================================================================

//       UPDATE btn Code Starts ====================================================================
		Button update = findViewById(R.id.update);
		update.setOnClickListener(v -> {
			String nameTXT = name.getText().toString();
			String passwordTXT = password.getText().toString();
			String gstTXT = gst.getText().toString().toLowerCase().trim();
			if (!gstTXT.isEmpty() && !gstTXT.equals("no")) {
				gstTXT = gst.getText().toString();
			} else {
				gst.setText("no");
				gstTXT = "-1";
			}
			String contactNumberTXT = contact.getText().toString();
			String addressTXT = address.getText().toString();

//                VALIDATING password
			login_Screen ma;
			ma = new login_Screen();
			boolean passwordCheck;
			passwordCheck = ma.passwordValidation(passwordTXT);
			int PasswordChecking = 0;
			if (passwordCheck) {
				PasswordChecking = 1;
			} else {
				password = findViewById(R.id.password);
				passwordTXT = name.getText().toString();
			}
			if (nameTXT.isEmpty() || passwordTXT.isEmpty() || gstTXT.isEmpty() || contactNumberTXT.isEmpty() || addressTXT.isEmpty()) {
				Toast.makeText(profile_editing.this, "Fill up complete Form", Toast.LENGTH_SHORT).show();
			} else {
				if (PasswordChecking == 1) {
					boolean check;
					check = DBM.UpdateUser(nameTXT, email, passwordTXT, gstTXT, contactNumberTXT, addressTXT);
					if (check) {
						Toast.makeText(profile_editing.this, "Data Updated", Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(profile_editing.this, "Invalid Password", Toast.LENGTH_SHORT).show();
				}
			}
		});
//  ================================================================================================

//        DELETE BUTTON ============================================================================
		Button delete = findViewById(R.id.Delete);
		delete.setOnClickListener(v -> {
			AlertDialog.Builder alert = new AlertDialog.Builder(profile_editing.this);
			alert.setTitle("Delete user");
			alert.setMessage("Confirm deleting Account permanently ");
			alert.setPositiveButton("Confirm Delete", (dialogInterface, i) -> {
				boolean check;
				check = DBM.DeleteUser(email);
				if (check) {
//                        Deleting all users data
//                        Going back to login after deleting user
					Intent intent = new Intent(profile_editing.this, login_Screen.class);
					startActivity(intent);

					SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("Login", "false");
					editor.putString("UserName", "");
					editor.apply();
					finish();

					Toast.makeText(profile_editing.this, "User DELETED ...", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(profile_editing.this, "Unable to delete user at this Time", Toast.LENGTH_SHORT).show();
				}
			});
			alert.setNegativeButton("Cancel", (dialogInterface, i) -> {
				Toast.makeText(profile_editing.this, "Deletion Cancel", Toast.LENGTH_SHORT).show();
				dialogInterface.dismiss();
			});
			alert.show();
		});
	}
//  ================================================================================================
}