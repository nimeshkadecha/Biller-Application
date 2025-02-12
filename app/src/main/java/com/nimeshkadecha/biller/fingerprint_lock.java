package com.nimeshkadecha.biller;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class fingerprint_lock extends AppCompatActivity {

	private static final String SHARED_PREFS = "sharedPrefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprint_lock);

		Button back = findViewById(R.id.back);

		BiometricPrompt.PromptInfo promptInfo;

		BiometricManager biometricManager = BiometricManager.from(this);
		switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
			case BiometricManager.BIOMETRIC_SUCCESS:
			case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
			case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
			case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
			case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
			case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
			case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
				break;
		}

		Executor executor = ContextCompat.getMainExecutor(this);
		BiometricPrompt biometricPrompt = new BiometricPrompt(fingerprint_lock.this,
		                                                      executor, new BiometricPrompt.AuthenticationCallback() {
			@Override
			public void onAuthenticationError(int errorCode,
			                                  @NonNull CharSequence errString) {

				super.onAuthenticationError(errorCode, errString);

			}

			@Override
			public void onAuthenticationSucceeded(
											@NonNull BiometricPrompt.AuthenticationResult result) {
				super.onAuthenticationSucceeded(result);
				Toast.makeText(getApplicationContext(),
				               "Login succeeded!", Toast.LENGTH_SHORT).show();

				Bundle bundle = getIntent().getExtras();
				assert bundle != null;
				String email = bundle.getString("Email");

				SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("Login", "true");
				editor.putString("UserName", email);
				editor.apply();

				Intent bio_logedIN = new Intent(fingerprint_lock.this, home.class);

				bio_logedIN.putExtra("Email", email);
				bio_logedIN.putExtra("Origin", "Login");

				startActivity(bio_logedIN);
				finish();
			}

			@Override
			public void onAuthenticationFailed() {
				super.onAuthenticationFailed();
				Toast.makeText(getApplicationContext(), "Authentication failed",
				               Toast.LENGTH_SHORT)
												.show();

				back.setVisibility(View.VISIBLE);
			}
		});

		promptInfo = new BiometricPrompt.PromptInfo.Builder()
										.setTitle("Biometric login for Biller")
										.setSubtitle("Log in using your biometric credential")
										.setNegativeButtonText("Go Back")
										.build();

		biometricPrompt.authenticate(promptInfo);

		back.setOnClickListener(v -> {
			startActivity(new Intent(fingerprint_lock.this, login_Screen.class));
			finish();
		});
	}
}