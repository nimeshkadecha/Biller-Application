package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class forgot_password extends AppCompatActivity {

	private EditText Email;

	private ImageView PlodingView;

	private Animation alpha;

	@SuppressLint({"MissingInflatedId", "SetTextI18n"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password);

//        WORKING WITH TOOLBAR Starts ==============================================================
		//        Removing Support bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

		// hiding menu button
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//        WORKING WITH TOOLBAR Ends-----------------------------------------------------------------

//        Finding  ---------------------------------------------------------------------------------
		Email = findViewById(R.id.userEmailInput_fp);
//--------------------------------------------------------------------------------------------------

//        Finding progressbar
		PlodingView = findViewById(R.id.Ploding);
		alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);

	}

	//    Code for validating email ===================================================================
	public boolean EmailValidation(String email) {
		return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
//==================================================================================================

	//    Verifying internet is ON ====================================================================
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		return manager.getActiveNetworkInfo() != null;
	}
//==================================================================================================

	//   "Get OTP" button On click ====================================================================
	public void GetOTP(View view) {
		if (checkConnection()) {
			if (EmailValidation(Email.getText().toString().trim())) {
				findViewById(R.id.button).setEnabled(false);
				GenerateOtpWithEmail(Email.getText().toString().trim());
				PlodingView.setVisibility(View.VISIBLE);
				PlodingView.startAnimation(alpha);

			} else {
				findViewById(R.id.button).setEnabled(true);
				Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
			}
		} else {
			findViewById(R.id.button).setEnabled(true);
			Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
		}
	}

	// =================================================================================================
	// generate OTP ===================================================================================
	@SuppressLint("DefaultLocale")
	private String getrandom() {
		Random rnd = new Random();
		int otp = rnd.nextInt(999999);
		return String.format("%06d", otp);
	}
	// ================================================================================================

	// Hitting API to get OTP =========================================================================
	private void GenerateOtpWithEmail(String email) {
//		Since we know no one will use the app i am generating a tempraroly solution to give feel like OTP

							Intent GETOTP = new Intent(forgot_password.this, otp_validation.class);
							GETOTP.putExtra("Email", email);
							GETOTP.putExtra("OTP", getOTP());
							startActivity(GETOTP);
							PlodingView.setVisibility(View.GONE);
							PlodingView.clearAnimation();


//		This is real working code if we get to host our phpMailer then we can use that code


		// Replace "your_api_url" with the actual URL of the API endpoint you want to call
//		String apiUrl = "https://solution-nimesh.000webhostapp.com/otp.php";
//
//		String otp = getrandom();
//		// Create a JSON object with the four parameters
//		JSONObject jsonData = new JSONObject();
//		try {
//			jsonData.put("To", email);
//			jsonData.put("OTP_Code", otp);
//			jsonData.put("Company_name", "BillerApp");
//			jsonData.put("Company_email", "nimeshkadecha4560@gmail.com");
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return; // JSON creation failed, exit the method
//		}
//
//		// Define the MediaType for JSON data
//		okhttp3.MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//		// Create an OkHttpClient
//		OkHttpClient client = new OkHttpClient();
//
//		// Create the request body using the JSON data
//		RequestBody requestBody = RequestBody.create(jsonData.toString(), JSON);
//
//		// Create the POST request
//		Request request = new Request.Builder()
//										.url(apiUrl)
//										.post(requestBody)
//										.build();

		// Execute the request in a background thread (AsyncTask, ThreadPool, etc.)
		// For simplicity, we use a separate thread using Thread class here
//		new Thread(() -> {
//			try {
//				// Execute the request and get the response
//				Response response = client.newCall(request).execute();
//
//				// Check if the request was successful (HTTP 2xx response codes)
//				if (response.isSuccessful()) {
//
//					assert response.body() != null;
//					String responseData = response.body().string();
//					// Extract the JSON response part from the overall response data
//					String jsonResponseString = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
//					try {
//						// Parse the JSON response data
//						JSONObject jsonObject = new JSONObject(jsonResponseString);
//
//						// Extract the relevant information from the JSON object
//						String message = jsonObject.getString("status");
//
//						if (message.equals("false")) {
//							Log.d("ENimesh", "String = " + message);
//						} else {
//							Intent GETOTP = new Intent(forgot_password.this, otp_validation.class);
//							GETOTP.putExtra("Email", email);
//							GETOTP.putExtra("OTP", otp);
//							startActivity(GETOTP);
//							PlodingView.setVisibility(View.GONE);
//							PlodingView.clearAnimation();
//							forgot_password.this.runOnUiThread(this::finish);
//						}
//
//					} catch (JSONException e) {
//						e.printStackTrace();
//						// Handle JSON parsing exceptions
//					}
//					// Process the response data here (responseData contains the API response)
//				} else {
//					PlodingView.setVisibility(View.GONE);
//					PlodingView.clearAnimation();
////					Toast.makeText(this, "Failed to hit API", Toast.LENGTH_SHORT).show();
//					Log.d("ENimesh", "Failed");
//					forgot_password.this.runOnUiThread(this::finish);
//					// Handle the error if the request was not successful
//					// For example, you can get the error message using response.message()
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				PlodingView.setVisibility(View.GONE);
//				PlodingView.clearAnimation();
////				Toast.makeText(this, "Failed to hit API", Toast.LENGTH_SHORT).show();
//				Log.d("ENimesh", "catched " + e);
//				forgot_password.this.runOnUiThread(this::finish);
//				// Handle any exceptions that occurred during the request
//			}
//		}).start();
	}

	private String getOTP() {
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
		return sdf.format(new Date());
	}
//==================================================================================================

}