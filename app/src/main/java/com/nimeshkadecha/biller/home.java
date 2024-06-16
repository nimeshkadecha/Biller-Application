package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class home extends AppCompatActivity {

	private static final String SHARED_PREFS = "sharedPrefs";
	private Button product;
	private final DBManager dbManager = new DBManager(this);
	private View navigationDrawerView;
	private EditText number, date;

	private MaterialAutoCompleteTextView name;

	AlertDialog.Builder alert;

	// network connection =============================================================================
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo() != null;
	}
	// ================================================================================================


	@SuppressLint({"MissingInflatedId", "Range"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		int[] billId_txt = new int[5]; // stores the BillId for customers

//        Adding seller email from INTENT  =========================================================
		Bundle bundle = getIntent().getExtras();
		String email = Objects.requireNonNull(bundle).getString("Email");
		String origin;
		origin = bundle.getString("Origin");
//  ================================================================================================

// Finding name and number texts ===================================================================

		// Working with name -----------------------------------------------------------------------------
		name = findViewById(R.id.customet_name_home);
		// adding names from database to auto complete textview
		name.setAdapter(new ArrayAdapter<>(home.this, android.R.layout.simple_list_item_1, dbManager.customersName_arr(email)));
		name.setOnClickListener(v -> {
			if (navigationDrawerView.getVisibility() == View.VISIBLE) {
				navigationDrawerView.setVisibility(View.INVISIBLE);
				product.setVisibility(View.VISIBLE);
			}
		});

		// adding customer number to input field from database if we have the customer
		name.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) navigationDrawerView.setVisibility(View.INVISIBLE);
			if (!hasFocus) set_customer_number(email);
		});
		// -----------------------------------------------------------------------------------------------

		// working with number ---------------------------------------------------------------------------
		number = findViewById(R.id.contact);
		number.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) navigationDrawerView.setVisibility(View.INVISIBLE);
		});
		number.setOnClickListener(v -> {
			if (navigationDrawerView.getVisibility() == View.VISIBLE) {
				navigationDrawerView.setVisibility(View.INVISIBLE);
				product.setVisibility(View.VISIBLE);
			}
			// inserting number from database if we have the customer
			if (number.getText().toString().isEmpty()) set_customer_number(email);
		});
		// -----------------------------------------------------------------------------------------------

//  ================================================================================================

//        Generating and formatting Date ============================================================
		date = findViewById(R.id.date);

		Date c = Calendar.getInstance().getTime();
		System.out.println("Current time => " + c);

		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String formattedDate = df.format(c);

		date.setText(formattedDate);
		date.setShowSoftInputOnFocus(false); // not opening keyboard for date input

		date.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				navigationDrawerView.setVisibility(View.INVISIBLE);
				show_date_time_picker();
			}
		});

		date.setOnClickListener(v -> show_date_time_picker());

		date.setOnLongClickListener(v -> {
			if (date.getText().toString().isEmpty()) date.setText(formattedDate);
			return true;
		});
//  ================================================================================================

//        Removing Support bar / top line containing name===========================================
		Objects.requireNonNull(getSupportActionBar()).hide();
//  ================================================================================================

//        Finding and hiding navigation drawer =====================================================
		navigationDrawerView = findViewById(R.id.navigation);
		navigationDrawerView.setVisibility(View.INVISIBLE);
//  ================================================================================================

// Working with Navigation ====================================================================

		// Menu btn work ------------------------------------------------------------------------------
		ImageView menu = findViewById(R.id.Menu);

		menu.setOnClickListener(v -> {
			navigationDrawerView.setVisibility(View.VISIBLE);
			product.setVisibility(View.INVISIBLE);
			if (getCurrentFocus() != null) {
				InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			}
		});
		// -----------------------------------------------------------------------------------------------

		// GEMINI HOME -----------------------------------------------------------------------------------
		ImageView gemini_Home = findViewById(R.id.google_gemini_logo_btn);

		gemini_Home.setOnClickListener(v -> {
			if (checkConnection()) {
				Intent intent = new Intent(home.this, Gemini_Home.class);
				intent.putExtra("seller", email);
				startActivity(intent);
			} else {
				Toast.makeText(home.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
			}
		});
		// -----------------------------------------------------------------------------------------------

		// BackBtn in drawer -----------------------------------------------------------------------------
		ImageView backBtn = findViewById(R.id.btnBack);

		backBtn.setOnClickListener(v -> {
			navigationDrawerView.setVisibility(View.INVISIBLE);
			product.setVisibility(View.VISIBLE);
		});
		// -----------------------------------------------------------------------------------------------

		// Customer Info Button --------------------------------------------------------------------------
		Button customerInfo = findViewById(R.id.customerinfo);

		customerInfo.setOnClickListener(v -> {
			Intent intent = new Intent(home.this, bill_management.class);
			intent.putExtra("seller", email);
			startActivity(intent);
		});
		// -----------------------------------------------------------------------------------------------

		// Edit Info btn ---------------------------------------------------------------------------------
		Button editInfo = findViewById(R.id.editInfo);
		editInfo.setOnClickListener(v -> {
			Intent intent = new Intent(home.this, profile_editing.class);
			intent.putExtra("Email", email);
			startActivity(intent);

		});
		// -----------------------------------------------------------------------------------------------

		// Edit Info btn ---------------------------------------------------------------------------------
		Button stock = findViewById(R.id.stock);
		stock.setOnClickListener(v -> {
			Intent intent = new Intent(home.this, stock_control.class);
			intent.putExtra("Email", email);
			startActivity(intent);
		});
		// -----------------------------------------------------------------------------------------------

		// Backup Btn ------------------------------------------------------------------------------------
		Button backup = findViewById(R.id.backup);
		backup.setOnClickListener(v -> {
			Intent localBackup = new Intent(home.this, backup_management.class);
			startActivity(localBackup);
		});
		// -----------------------------------------------------------------------------------------------

		// Report button ---------------------------------------------------------------------------------
		Button report = findViewById(R.id.report);
		report.setOnClickListener(v -> {
			Intent goTOReport = new Intent(home.this, inventory_insights.class);
			goTOReport.putExtra("seller", email);
			startActivity(goTOReport);
		});
		// -----------------------------------------------------------------------------------------------

		// Log Out btn -----------------------------------------------------------------------------------
		Button logout = findViewById(R.id.logOutButton);

		logout.setOnClickListener(v -> {
			SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("Login", "false");
			editor.putString("UserName", "");
			editor.apply();

			Intent logOUT = new Intent(home.this, login_Screen.class);
			startActivity(logOUT);
			finish();
		});
		// -----------------------------------------------------------------------------------------------

		// Classing navigation drawer --------------------------------------------------------------------
		View homeLayout = findViewById(R.id.homeLayout);

		homeLayout.setOnClickListener(v -> {
			if (navigationDrawerView.getVisibility() == View.VISIBLE) {
				navigationDrawerView.setVisibility(View.INVISIBLE);
				product.setVisibility(View.VISIBLE);
			}
		});
		// -----------------------------------------------------------------------------------------------

		// Getting Bio-matrix unlock ---------------------------------------------------------------------
		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

		@SuppressLint("UseSwitchCompatOrMaterialCode") Switch bio_lock_switch = findViewById(R.id.bio_lock_switch);

		bio_lock_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			SharedPreferences.Editor editor = sp.edit();
			if (isChecked) {
				editor.putString("bioLock", "true");
			} else {
				editor.putString("bioLock", "false");
			}
			editor.apply();
		});

		// checking for fingerprint verification
		if (sp.getString("bioLock", "").equals("true")) {
			bio_lock_switch.setChecked(true);
		}
		// -----------------------------------------------------------------------------------------------
// WORKING IN NAVIGATION DRAWER Ends  ==============================================================

// Enter Product BTN ===============================================================================

		// if the we come from add_product activity then we have to get the data from there
		// adding data to current field ------------------------------------------------------------------
		if (origin != null && origin.equalsIgnoreCase("addItem")) {
			String cName_txt, cNumber_txt, date_text;

			Bundle bundle1 = getIntent().getExtras();
			cName_txt = bundle1.getString("cName");
			cNumber_txt = bundle1.getString("cNumber");
			date_text = bundle1.getString("date");

			billId_txt[0] = bundle1.getInt("billId");

			assert cName_txt != null;
			if (!cName_txt.isEmpty()) {
				name.setText(cName_txt);
				number.setText(cNumber_txt);
				date.setText(date_text);
			}
		} else {
			billId_txt[0] = 0;
		}

		final int[] finalBillId_txt = billId_txt;
		// -----------------------------------------------------------------------------------------------

		// If not then Transferring customer data to next activity in Enter Product button click ---------
		product = findViewById(R.id.products);

		product.setOnClickListener(v -> {
			Intent intent = new Intent(home.this, add_product.class);

			String name_txt, number_txt, date_txt;
			name_txt = name.getText().toString();
			number_txt = number.getText().toString();
			date_txt = date.getText().toString();

			// Validation user Inputs ----------------------------------------------------------------------
			if (name_txt.isEmpty() || number_txt.isEmpty() || date_txt.isEmpty()) {
				if (name_txt.isEmpty() && number_txt.isEmpty() && date_txt.isEmpty()) {
					name.setError("Enter Name Here");
					number.setError("Enter Number Here");
					date.setError("Enter Date Here");
					Toast.makeText(home.this, "Fill up above detail", Toast.LENGTH_SHORT).show();
				} else if (name_txt.isEmpty()) {
					name.setError("Enter Name Here");
					Toast.makeText(home.this, "Enter Customer Name", Toast.LENGTH_SHORT).show();
				} else if (number_txt.length() != 10) {
					set_customer_number(email);
					number_txt = number.getText().toString();
					if (number_txt.length() != 10) {
						number.setError("Enter valid Number Here");
						Toast.makeText(home.this, "Enter Customer Number", Toast.LENGTH_SHORT).show();
					}
				} else {
					date.setError("Enter Date Here");
					Toast.makeText(home.this, "Enter Date", Toast.LENGTH_SHORT).show();
				}
			}
			// ---------------------------------------------------------------------------------------------

			// performing operation ------------------------------------------------------------------------
			else {
				date_txt = date.getText().toString();
				if (finalBillId_txt[0] == 0) finalBillId_txt[0] = dbManager.GetBillId();

				intent.putExtra("cName", name_txt);
				intent.putExtra("cNumber", number_txt);
				intent.putExtra("date", date_txt);
				intent.putExtra("billId", finalBillId_txt[0]);
				intent.putExtra("seller", email);
				intent.putExtra("origin", "home");
				new NameValidatorAsyncTask(name_txt, number_txt, email, home.this, intent).execute();
				}
			// ---------------------------------------------------------------------------------------------
		});
		// -----------------------------------------------------------------------------------------------
//  ================================================================================================

	} // end of OnCreate ==============================================================================

	@SuppressLint("Range")
	public void set_customer_number(String email) {
		Cursor numberC = dbManager.ParticularCustomerInformation(email, name.getText().toString());
		numberC.moveToFirst();
		if (numberC.getCount() > 0) {
			number.setText(numberC.getString(numberC.getColumnIndex("customerNumber")));
			Toast.makeText(home.this, "found customer", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(home.this, "New Customer", Toast.LENGTH_SHORT).show();
		}
	}
	//=================================================================================================

	//  Alert dialog box for Exiting Application ======================================================
	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		if (String.valueOf(navigationDrawerView.getVisibility()).equals("0")) {
			navigationDrawerView.setVisibility(View.INVISIBLE);
			product.setVisibility(View.VISIBLE);
		} else {
			alert = new AlertDialog.Builder(home.this);
			alert.setTitle("Exit App");
			alert.setMessage("Confirm Exit");
			alert.setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity());
			alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

			alert.show();
		}
	}

	// Showing Date picker and hiding input filed =====================================================
	private void show_date_time_picker() {

		InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);

		final Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(home.this, new DatePickerDialog.OnDateSetListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
				date.setText(" ");
				date.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
			}
		}, year, month, day);
		datePickerDialog.show();
	}
//  ================================================================================================

	// confirming that the number match to the name if don't then update the name =====================
	@SuppressLint("StaticFieldLeak")
	private class NameValidatorAsyncTask extends AsyncTask<Void, Void, Void> {

		private final String name;
		private final String number;
		private final String email;
		private final Context context;
		private final Intent intent;

		public NameValidatorAsyncTask(String name, String number, String email, Context context, Intent intent) {
			this.name = name;
			this.number = number;
			this.email = email;
			this.context = context;
			this.intent = intent;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			// Create a Handler to handle the result on the main thread
			Handler handler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(Message msg) {
					boolean result = (boolean) msg.obj;
					if (result) {
						context.startActivity(intent);
					} else {
						Toast.makeText(context, "Failed to update! try again", Toast.LENGTH_SHORT).show();
					}
				}
			};

			// Perform validation in the background thread
			dbManager.validateNameAndNumberConnection(name, number, email, context, handler);
			return null;
		}
	}
//  ================================================================================================

}