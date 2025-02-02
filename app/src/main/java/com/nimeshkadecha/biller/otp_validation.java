package com.nimeshkadecha.biller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class otp_validation extends AppCompatActivity {

	private static final long TIMER_DURATION = 60000; // 1 minute in milliseconds
	private static final long TIMER_INTERVAL = 1000; // 1 second in milliseconds

	private Animation alpha;

	//        finding textviews;
	TextView timerTV;
	TextView resendTV;

	//      Getting Verification ID from INTENT
	String OTP;
	private EditText otp;
	private ImageView PlodingView;
	private final String b = "Biller";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.otp_validation);

//        WORKING WITH TOOLBAR =====================================================================
		//        Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();

		//        Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);
//==================================================================================================

		//        finding textviews;
		timerTV = findViewById(R.id.timerOTP);
		resendTV = findViewById(R.id.resendButton);

//        STARTING timer for resend otp
		startTimer();

//        progressbar
		PlodingView = findViewById(R.id.Ploding);
		alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);


//      Getting Verification ID from INTENT ========================================================
		Bundle otpp = getIntent().getExtras();
		assert otpp != null;
		final String[] OTP = {otpp.getString("OTP")};
		String email = otpp.getString("Email");
//==================================================================================================

		OTP[0] = otpp.getString("OTP");

//      Getting Origin From Intent =================================================================
		Bundle bundle = getIntent().getExtras();
//==================================================================================================

//      Verifying OTP ==============================================================================
		Button verifyy = findViewById(R.id.Verify);
		final String[] finalOTP = {OTP[0]};
		verifyy.setOnClickListener(v -> {
			otp = findViewById(R.id.OTP);
			String otpInput = otp.getText().toString().trim();
			boolean OTP_V = OTPValidate(otpInput);
			if (OTP_V) {
				if (finalOTP[0].equals(otpInput)) {
					Intent GoToResetPassword = new Intent(otp_validation.this, reset_password.class);
					//                Fowarding number to intent reset password
					Bundle bundle1 = getIntent().getExtras();
					String Email = bundle1.getString("Email");
					GoToResetPassword.putExtra("Email", Email);
					startActivity(GoToResetPassword);
					finish();
				}
			} else {
				Toast.makeText(otp_validation.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
			}
		});
//==================================================================================================

		//        Resend OTP =============================================================================
		resendTV.setOnClickListener(view -> {
			if (checkConnection()) {
				PlodingView.setVisibility(View.VISIBLE);
				PlodingView.startAnimation(alpha);
				String otp = getrandom();
				finalOTP[0] = otp;
				GenerateOtpWithEmail(email, otp);
			} else {
				Toast.makeText(otp_validation.this, "No internet", Toast.LENGTH_SHORT).show();
			}
		});
		// ===============================================================================================

	} // End of OnCreate

	//    Verifying internet is ON ====================================================================
	boolean checkConnection() {
		ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo() != null;
	}
// =================================================================================================

	//    Generating OTP ==============================================================================
	@SuppressLint("DefaultLocale")
	private String getrandom() {
		Random rnd = new Random();
		int otp = rnd.nextInt(999999);
		return String.format("%06d", otp);
	}
//==================================================================================================	

	//    Sending OTP to Email ========================================================================
	private void GenerateOtpWithEmail(String email, String otp) {
		// Replace "your_api_url" with the actual URL of the API endpoint you want to call
		String apiUrl = "https://solution-nimesh.000webhostapp.com/otp.php";

		// Create a JSON object with the four parameters
		JSONObject jsonData = new JSONObject();
		try {
			jsonData.put("To", email);
			jsonData.put("OTP_Code", otp);
			jsonData.put("Company_name", "BillerApp");
			jsonData.put("Company_email", "nimeshkadecha4560@gmail.com");
		} catch (JSONException e) {
			e.printStackTrace();
			return; // JSON creation failed, exit the method
		}

		// Define the MediaType for JSON data
		okhttp3.MediaType JSON = MediaType.parse("application/json; charset=utf-8");

		// Create an OkHttpClient
		OkHttpClient client = new OkHttpClient();

		// Create the request body using the JSON data
		RequestBody requestBody = RequestBody.create(jsonData.toString(), JSON);

		// Create the POST request
		Request request = new Request.Builder()
										.url(apiUrl)
										.post(requestBody)
										.build();

		// Execute the request in a background thread (AsyncTask, ThreadPool, etc.)
		// For simplicity, we use a separate thread using Thread class here
		new Thread(() -> {
			try {
				// Execute the request and get the response
				Response response = client.newCall(request).execute();

				// Check if the request was successful (HTTP 2xx response codes)
				if (response.isSuccessful()) {

					assert response.body() != null;
					String responseData = response.body().string();
					// Extract the JSON response part from the overall response data
					String jsonResponseString = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
					try {
						// Parse the JSON response data
						JSONObject jsonObject = new JSONObject(jsonResponseString);

						// Extract the relevant information from the JSON object
						String message = jsonObject.getString("status");

						if (message.equals("false")) {
							otp_validation.this.runOnUiThread(() -> Toast.makeText(otp_validation.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
						} else {
							otp_validation.this.runOnUiThread(() -> {
								Toast.makeText(otp_validation.this, "Resend successfully", Toast.LENGTH_SHORT).show();
								OTP = otp;
								timerTV.setVisibility(View.VISIBLE);
								startTimer();
								resendTV.setVisibility(View.GONE);
							});
							PlodingView.setVisibility(View.GONE);
							PlodingView.clearAnimation();
						}

					} catch (JSONException e) {
						otp_validation.this.runOnUiThread(() -> Toast.makeText(otp_validation.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
						e.printStackTrace();
						// Handle JSON parsing exceptions
					}
					// Process the response data here (responseData contains the API response)
				} else {
					otp_validation.this.runOnUiThread(() -> Toast.makeText(otp_validation.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
					// Handle the error if the request was not successful
					// For example, you can get the error message using response.message()
				}
			} catch (Exception e) {
				otp_validation.this.runOnUiThread(() -> Toast.makeText(otp_validation.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
				e.printStackTrace();
			}
		}).start();
	}
//==================================================================================================

	//    Timer for resend OTP ========================================================================
	private void startTimer() {
		// Calculate the remaining minutes and seconds
		// Update the timer TextView with the remaining time
		// Enable the resend button and reset the timer TextView
		CountDownTimer countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
			@SuppressLint("DefaultLocale")
			@Override
			public void onTick(long millisUntilFinished) {
				// Calculate the remaining minutes and seconds
				long minutes = millisUntilFinished / 60000;
				long seconds = (millisUntilFinished % 60000) / 1000;

				// Update the timer TextView with the remaining time
				timerTV.setText(String.format("%d:%02d", minutes, seconds));
			}

			@Override
			public void onFinish() {
				// Enable the resend button and reset the timer TextView
				resendTV.setVisibility(View.VISIBLE);
				timerTV.setVisibility(View.GONE);
			}
		};

		// Start the timer
		countDownTimer.start();
	}

	//    OTP validation ==============================================================================
	private boolean OTPValidate(String otpInput) {
		return otpInput.length() >= 6;
	}
//==================================================================================================

}