package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class forgot_password extends AppCompatActivity {

    private EditText Email;

    private ImageView menuclick;

    private View PlodingView;

    private TextView heading;

//    Verifying internet is ON =====================================================================
    boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo net = manager.getActiveNetworkInfo();

        if (net == null) {
            return false;
        } else {
            return true;
        }
    }
//--------------------------------------------------------------------------------------------------

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


//        WORKING WITH TOOLBAR Starts ==============================================================
    //        Removing Support bar / top line containing name
        Objects.requireNonNull(getSupportActionBar()).hide();

    //        FINDING menu
        menuclick = findViewById(R.id.Menu);

    //        Keeping menu Invisible
        menuclick.setVisibility(View.INVISIBLE);
//        WORKING WITH TOOLBAR Ends-----------------------------------------------------------------

//        Finding  ---------------------------------------------------------------------------------
        Email = findViewById(R.id.contactnumber);
        heading = findViewById(R.id.textView5);

        // Getting Data From Intent to find where it come from
        // if KEY "origin" has value CLoud then it come from Login CLoud Button and it change function according to it
        Bundle bundle = getIntent().getExtras();
        String origin = bundle.getString("Origin");
        if (origin != null && origin.equalsIgnoreCase("Cloud")) {
            heading.setText("Cloud Login");
        }
//--------------------------------------------------------------------------------------------------

//        Finding progressbar
        PlodingView = findViewById(R.id.Ploding);


    }
    //    Code for validating email starts--------------------------------------------------------------
    public boolean EmailValidation(String email) {
        String emailinput = email;
        if (!emailinput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailinput).matches()) {
            return true;
        } else {

            return false;
        }
    }
//--------------------------------------------------------------------------------------------------

//   "Get OTP" button On click -----------------------------------------------------------------------
    public void GetOTP(View view) {
        if(checkConnection()){
            boolean NV = EmailValidation(Email.getText().toString().trim());
            if (NV) {
                //        finding button
                Button getOTPButton = findViewById(R.id.button);

                getOTPButton.setEnabled(false);
                GenerateOtpWithEmail(Email.getText().toString().trim());
                PlodingView.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("DefaultLocale")
    private String getrandom() {
        Random rnd = new Random();
        int otp = rnd.nextInt(999999);
        return String.format("%06d", otp);
    }

    private void GenerateOtpWithEmail(String email) {
        // Replace "your_api_url" with the actual URL of the API endpoint you want to call
        String apiUrl = "https://solution-tech-nimesh.000webhostapp.com/OTP_Service/sendOTP.php";

        String otp = getrandom();
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
                        // Extract the JSON response part from the overall response data
                        String jsonResponseString = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        try {
                            // Parse the JSON response data
                            JSONObject jsonObject = new JSONObject(jsonResponseString);

                            // Extract the relevant information from the JSON object
                            String message = jsonObject.getString("status");

                            if(message.equals("false")){
                                Log.d("ENimesh","String = " + message);
                            }else{
//                                Log.d("ENimesh","String = " + message);
                                Intent GETOTP = new Intent(forgot_password.this, otp_validation.class);
                                GETOTP.putExtra("Email", email);
                                GETOTP.putExtra("OTP", otp);
                                startActivity(GETOTP);
                                PlodingView.setVisibility(View.GONE);
                                forgot_password.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing exceptions
                        }
                        // Process the response data here (responseData contains the API response)
                    } else {
                        Log.d("ENimesh","Failed");
                        // Handle the error if the request was not successful
                        // For example, you can get the error message using response.message()
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ENimesh","catched " + e.toString());
                    // Handle any exceptions that occurred during the request
                }
            }
        }).start();
    }
//--------------------------------------------------------------------------------------------------


    @Override
    protected void onStart() {
        super.onStart();
        //        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Google ads code --------------------------------------------------------------------------
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//  ================================================================================================
    }

}