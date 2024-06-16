package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.CountTokensResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Gemini_Home extends AppCompatActivity {

	String[] shorting = {"Gemini Pro"};

	public static final String SHARED_PREFS = "sharedPrefs";

	String email;

	DBManager dbManager = new DBManager(this);

	private ConstraintLayout register_layout, interface_layout;

	private TextView custom_tc, product_tc, business_tc, used_tc;

	public static JSONObject customer_JO, stock_JO, business_JO;

	private GenerativeModelFutures model;

	int customer_token_count = 0, stock_token_count = 0, business_token_count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gemini_home);

		//      WORKING WITH TOOLBAR =====================================================================
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
//          Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
// =================================================================================================

		register_layout = findViewById(R.id.Gemini_setup_layout);
		register_layout.setVisibility(View.GONE);
		interface_layout = findViewById(R.id.Gemini_layout);
		interface_layout.setVisibility(View.GONE);

		//        Adding Spinner (Dropdown menu) =========================================================
		Spinner spinner = findViewById(R.id.spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(Gemini_Home.this, android.R.layout.simple_spinner_item, shorting);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
// =================================================================================================

		//        Getting Bundle =========================================================================
		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		email = bundle.getString("seller");

		// getting token count from shared preferences ===================================================
		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		sp.getString("tokenCount", "0");

		//        Checking if user is already registered or not ==========================================
		if (dbManager.getApiKey(email).isEmpty()) {
			DisplayUi("register");
		} else {
			DisplayUi("Interface");
		}


		/* modelName */
		// Access your API key as a Build Configuration variable (see "Set up your API key" above)
		/* apiKey */
		GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash",
// Access your API key as a Build Configuration variable (see "Set up your API key" above)
										/* apiKey */ dbManager.getApiKey(email));
		model = GenerativeModelFutures.from(gm);

		// setup GEMINI ==================================================================================
		EditText api_key_et = findViewById(R.id.gemini_api_key);
		findViewById(R.id.submit_api_key).setOnClickListener(v -> {
			if (api_key_et.getText().toString().isEmpty()) {
				api_key_et.setError("Enter API Key");
			} else {
				if (dbManager.insertApiKey(api_key_et.getText().toString().trim(), email)) {
					Toast.makeText(Gemini_Home.this, "Submitted", Toast.LENGTH_SHORT).show();
					DisplayUi("Interface");
				} else {
					Toast.makeText(Gemini_Home.this, "Something went wrong", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// interface ! =================================================================================
		if (interface_layout.getVisibility() == View.VISIBLE) {
			product_tc = findViewById(R.id.stockTokenCount);
			custom_tc = findViewById(R.id.customerTokenCount);
			used_tc = findViewById(R.id.totalUsedToken);
			business_tc = findViewById(R.id.businessTokenCount);
			used_tc.setText(sp.getString("tokenCount", "0"));
		}

		//        Click Listeners =========================================================================
		findViewById(R.id.business_insights_btn).setOnClickListener(v -> {
			if (checkConnection()) {
				Intent intent = new Intent(Gemini_Home.this, Gemini_chat.class);
				intent.putExtra("seller", email);
				intent.putExtra("type", "business");
				sp.edit().putString("tokenCount", String.valueOf(Integer.parseInt(sp.getString("tokenCount", "0")) + business_token_count)).apply();
				used_tc.setText(sp.getString("tokenCount", "0"));

				startActivity(intent);
			} else {
				Toast.makeText(Gemini_Home.this, "No internet", Toast.LENGTH_SHORT).show();
			}
		});

		//        Click Listeners =========================================================================
		findViewById(R.id.analyze_stock_btn).setOnClickListener(v -> {
			if (checkConnection()) {
				Intent intent = new Intent(Gemini_Home.this, Gemini_chat.class);
				intent.putExtra("seller", email);
				intent.putExtra("type", "stock");
				sp.edit().putString("tokenCount", String.valueOf(Integer.parseInt(sp.getString("tokenCount", "0")) + stock_token_count)).apply();
				used_tc.setText(sp.getString("tokenCount", "0"));
				startActivity(intent);
			} else {
				Toast.makeText(Gemini_Home.this, "No internet", Toast.LENGTH_SHORT).show();
			}
		});

		//        Click Listeners =========================================================================
		findViewById(R.id.understand_customer_btn).setOnClickListener(v -> {
			if (checkConnection()) {

				Intent intent = new Intent(Gemini_Home.this, Gemini_chat.class);
				intent.putExtra("seller", email);
				intent.putExtra("type", "customer");
				sp.edit().putString("tokenCount", String.valueOf(Integer.parseInt(sp.getString("tokenCount", "0")) + customer_token_count)).apply();
				used_tc.setText(sp.getString("tokenCount", "0"));
				startActivity(intent);
			} else {
				Toast.makeText(Gemini_Home.this, "No Internet", Toast.LENGTH_SHORT).show();
			}
		});

		//        Click Listeners =========================================================================
		findViewById(R.id.continue_btn).setOnClickListener(v -> {
			if (checkConnection()) {

				Intent intent = new Intent(Gemini_Home.this, Gemini_chat.class);
				intent.putExtra("seller", email);
				intent.putExtra("type", "continue");
				startActivity(intent);
			} else {
				Toast.makeText(Gemini_Home.this, "No internet", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// Checking Internet Connection ===================================================================
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		return manager.getActiveNetworkInfo() != null;
	}
// =================================================================================================

	// Displaying Ui's ================================================================================
	private void DisplayUi(String type) {
		int sellerId = dbManager.get_userId(email);
		switch (type) {
			case "register":
				register_layout.setVisibility(View.VISIBLE);
				interface_layout.setVisibility(View.GONE);
				break;
			case "Interface":
				register_layout.setVisibility(View.GONE);
				interface_layout.setVisibility(View.VISIBLE);

				if(checkConnection()){

				new FetchDataAsyncTask_BusinessInsights(this).execute(sellerId);
				new FetchDataAsyncTask_AnalyzeStoke().execute(sellerId);
				new FetchDataAsyncTask_CustomerData().execute(sellerId);

				}else{
					Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}
// =================================================================================================


	// AsyncTask to fetch and combine data from multiple tables =======================================
	@SuppressLint("StaticFieldLeak")
	private class FetchDataAsyncTask_CustomerData extends AsyncTask<Integer, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Integer... params) {
			int sellerId = params[0];

			JSONObject combinedData = new JSONObject();
			try {
				combinedData.put("userData", dbManager.getTableData("users", sellerId));
				combinedData.put("salesData", dbManager.getTableData("display", sellerId));
				combinedData.put("customerData", dbManager.getTableData("customers", sellerId));
				combinedData.put("productData", dbManager.getTableData("products", sellerId));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return combinedData;
		}


		@Override
		protected void onPostExecute(JSONObject result) {
			// Handle the fetched data here
			customer_JO = result;
			runOnUiThread(() -> {

				Content.Builder userContentBuilder3 = new Content.Builder();
				userContentBuilder3.setRole("user");
				userContentBuilder3.addText(result.toString());
				Content userContent3 = userContentBuilder3.build();

				// token count
				ListenableFuture<CountTokensResponse> countTokensResponse = model.countTokens(userContent3);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					Futures.addCallback(countTokensResponse, new FutureCallback<CountTokensResponse>() {
						@Override
						public void onSuccess(CountTokensResponse result1) {
							custom_tc.setText(String.valueOf(result1.getTotalTokens()));
							customer_token_count = result1.getTotalTokens();
						}

						@Override
						public void onFailure(@NonNull Throwable t) {
							t.printStackTrace();
						}
					}, getMainExecutor());
				}
			});
		}
	}


	// AsyncTask to fetch and combine data from multiple tables =======================================
	@SuppressLint("StaticFieldLeak")
	private class FetchDataAsyncTask_AnalyzeStoke extends AsyncTask<Integer, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Integer... params) {
			int sellerId = params[0];

			JSONObject combinedData = new JSONObject();
			try {
				combinedData.put("userData", dbManager.getTableData("users", sellerId));
				combinedData.put("salesData", dbManager.getTableData("display", sellerId));
				combinedData.put("productData", dbManager.getTableData("products", sellerId));
				combinedData.put("stockData", dbManager.getTableData("stock", sellerId));
				combinedData.put("stockQuantityData", dbManager.getTableData("stockQuantity", sellerId));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return combinedData;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// Handle the fetched data here
			stock_JO = result;
			runOnUiThread(() -> {

				Content.Builder userContentBuilder3 = new Content.Builder();
				userContentBuilder3.setRole("user");
				userContentBuilder3.addText(result.toString());
				Content userContent3 = userContentBuilder3.build();

				// token count
				ListenableFuture<CountTokensResponse> countTokensResponse = model.countTokens(userContent3);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					Futures.addCallback(countTokensResponse, new FutureCallback<CountTokensResponse>() {
						@Override
						public void onSuccess(CountTokensResponse result1) {
							int totalTokens = result1.getTotalTokens();
							product_tc.setText(String.valueOf(totalTokens));
							stock_token_count = totalTokens;
//								return null;
						}

						@Override
						public void onFailure(@NonNull Throwable t) {
							t.printStackTrace();
						}
					}, getMainExecutor());
				}
			});
		}
	}

	// AsyncTask to fetch and combine data from multiple tables =======================================
	@SuppressLint("StaticFieldLeak")
	private class FetchDataAsyncTask_BusinessInsights extends AsyncTask<Integer, Void, JSONObject> {

		FetchDataAsyncTask_BusinessInsights(Context context) {
			Context context1 = context.getApplicationContext(); // Use application context
		}

		@Override
		protected JSONObject doInBackground(Integer... params) {
			int sellerId = params[0];
			JSONObject combinedData = new JSONObject();
			try {
				combinedData.put("userData", dbManager.getTableData("users", sellerId));
				combinedData.put("salesData", dbManager.getTableData("display", sellerId));
				combinedData.put("productData", dbManager.getTableData("products", sellerId));
				combinedData.put("customerData", dbManager.getTableData("customers", sellerId));
				combinedData.put("stockData", dbManager.getTableData("stock", sellerId));
				combinedData.put("stockQuantityData", dbManager.getTableData("stockQuantity", sellerId));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return combinedData;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			business_JO = result;
			runOnUiThread(() -> {
				Content.Builder userContentBuilder3 = new Content.Builder();
				userContentBuilder3.setRole("user");
				userContentBuilder3.addText(result.toString());
				Content userContent3 = userContentBuilder3.build();

				ListenableFuture<CountTokensResponse> countTokensResponse = model.countTokens(userContent3);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					Futures.addCallback(countTokensResponse, new FutureCallback<CountTokensResponse>() {
						@Override
						public void onSuccess(CountTokensResponse result1) {
							int totalTokens = result1.getTotalTokens();
							business_tc.setText(String.valueOf(totalTokens));
							business_token_count = totalTokens;
						}

						@Override
						public void onFailure(@NonNull Throwable t) {
							t.printStackTrace();
						}
					}, getMainExecutor());
				}
			});
		}
	}


}