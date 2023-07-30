package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OTP_Generator extends AppCompatActivity {

    private EditText otp;

    private Button verifyy;

    private ImageView menuclick;

    private View PlodingView;

    private String b = "Biller";

    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 60000; // 1 minute in milliseconds
    private static final long TIMER_INTERVAL = 1000; // 1 second in milliseconds

    //        finding textviews;
    TextView timerTV;
    TextView resendTV ;

    //      Getting Verification ID from INTENT --------------------------------------------------------
    String OTP ;
    String email ;
//--------------------------------------------------------------------------------------------------

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DBManager DB_local = new DBManager(this);

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_generator);

        //        finding textviews;
        timerTV = findViewById(R.id.timerOTP);
        resendTV = findViewById(R.id.resendButton);

//        STARTING timer for resend otp
        startTimer();

//        progressbar
        PlodingView = findViewById(R.id.Ploding);

//      Getting Verification ID from INTENT --------------------------------------------------------
        Bundle otpp = getIntent().getExtras();
        final String[] OTP = {otpp.getString("OTP")};
        String email = otpp.getString("Email");
//--------------------------------------------------------------------------------------------------

        OTP[0] = otpp.getString("OTP");

//      Getting Origin From Intent -----------------------------------------------------------------
        Bundle bundle = getIntent().getExtras();
        String origin = bundle.getString("Origin");
//--------------------------------------------------------------------------------------------------

//        WORKING WITH TOOLBAR Starts---------------------------------------------------------------
        //        Removing Suport bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

        //        FINDING menu
        menuclick = findViewById(R.id.Menu);

        //        Keeping MENUE Invisible
        menuclick.setVisibility(View.INVISIBLE);
//--------------------------------------------------------------------------------------------------

//      Verifying OTP ------------------------------------------------------------------------------
        verifyy = findViewById(R.id.Verify);
        final String[] finalOTP = {OTP[0]};
        verifyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp = findViewById(R.id.OTP);
                String otpInput = otp.getText().toString().trim();
                boolean OTP_V = OTPValidate(otpInput);
                if (OTP_V) {
                    if(finalOTP[0].equals(otpInput)){
                        Intent GoToResetPassword = new Intent(OTP_Generator.this, resetPassword.class);
                                        //                Fowarding number to intent reset password
                        Bundle bundle = getIntent().getExtras();
                        String Email = bundle.getString("Email");
                        GoToResetPassword.putExtra("Email", Email);
                        startActivity(GoToResetPassword);
                        finish();
                    }
                } else {
                    Toast.makeText(OTP_Generator.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
//--------------------------------------------------------------------------------------------------

        String finalEmail = email;
        resendTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlodingView .setVisibility(View.VISIBLE);
                String otp = getrandom();
                finalOTP[0] = otp;
                GenerateOtpWithEmail(finalEmail,otp);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private String getrandom() {
        Random rnd = new Random();
        int otp = rnd.nextInt(999999);
        return String.format("%06d", otp);
    }

    private void GenerateOtpWithEmail(String email,String otp) {
        // Replace "your_api_url" with the actual URL of the API endpoint you want to call
        String apiUrl = "https://solution-tech-nimesh.000webhostapp.com/OTP_Service/sendOTP.php";

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Execute the request and get the response
                    Response response = client.newCall(request).execute();

                    // Check if the request was successful (HTTP 2xx response codes)
                    if (response.isSuccessful()) {

                        String responseData = response.body().string();
                        Log.d("ENimesh","Response !! = " +responseData);
                        // Extract the JSON response part from the overall response data
                        String jsonResponseString = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        try {
                            // Parse the JSON response data
                            JSONObject jsonObject = new JSONObject(jsonResponseString);

                            // Extract the relevant information from the JSON object
                            String message = jsonObject.getString("status");

                            if(message.equals("false")){
                                OTP_Generator.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(OTP_Generator.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d("ENimesh","String = " + message);
                            }else{
                                OTP_Generator.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(OTP_Generator.this, "Resend successfully", Toast.LENGTH_SHORT).show();
                                        OTP = otp;
                                        timerTV.setVisibility(View.VISIBLE);
                                        startTimer();
                                        resendTV.setVisibility(View.GONE);
                                    }
                                });
                                PlodingView.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            OTP_Generator.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OTP_Generator.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                            // Handle JSON parsing exceptions
                        }
                        // Process the response data here (responseData contains the API response)
                    } else {
                        OTP_Generator.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OTP_Generator.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("ENimesh","Failed");
                        // Handle the error if the request was not successful
                        // For example, you can get the error message using response.message()
                    }
                } catch (Exception e) {
                    OTP_Generator.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OTP_Generator.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    Log.d("ENimesh","catched " + e.toString());
                    // Handle any exceptions that occurred during the request
                }
            }
        }).start();
    }
//--------------------------------------------------------------------------------------------------

    private void startTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
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

    //    OTP validation -------------------------------------------------------------------------------
    private boolean OTPValidate(String otpInput) {
        if (otpInput.length() < 6) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------



}