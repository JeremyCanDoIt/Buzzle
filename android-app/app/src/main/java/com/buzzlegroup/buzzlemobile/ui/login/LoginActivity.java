package com.buzzlegroup.buzzlemobile.ui.login;

import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.buzzlegroup.buzzlemobile.Constants;
import com.buzzlegroup.buzzlemobile.ui.search.SearchActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.buzzlegroup.buzzlemobile.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.buzzlegroup.buzzlemobile.data.NetworkManager;
import com.buzzlegroup.buzzlemobile.databinding.ActivityLoginBinding;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Page for logging into the Buzzle app. Uses Recaptcha to verify humanity.
 * @author Thomas Tran
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username;
    private EditText password;
    private TextView message;

    //Re-captcha stuff
    private final String tag = LoginActivity.class.getSimpleName();
    private final String SITE_KEY = "6LccNEMmAAAAAIBQDmh8lxojavhMoKBsmGK0vgT7";

    private String recaptchaResponse = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        //Set up the captcha
        Button buttonVerifyCaptcha = findViewById(R.id.captcha_button);
        buttonVerifyCaptcha.setOnClickListener(this);


        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                Constants.baseURL + "/api/login",
                response -> {

                    try {
                        final JSONObject json = new JSONObject(response);

                        //Check for success
                        if (json.getBoolean("success")) {
                            //Complete and destroy login activity once successful
                            Log.d("login.success", response);
                            finish();

                            // Redirect to the search page
                            final Intent searchPage = new Intent(LoginActivity.this, SearchActivity.class);
                            startActivity(searchPage);
                        }

                        //Show returned error message if login failed
                        else {
                            final String errorMessage = json.getString("message");
                            if (!errorMessage.isEmpty()) {
                                //display error message
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    catch (JSONException e) {
                        Log.d("login.error", "Response JSON parse failed");
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());

                if (recaptchaResponse != null) {
                    params.put("g-recaptcha-response", recaptchaResponse);
                }

                params.put("platform", "android"); //hardcoded platform var to check against correct secret key
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }

    //User clicks on the ReCaptcha
    //Base code is from Android Recaptcha Documentation: https://developer.android.com/training/safetynet/recaptcha.html#java
    @Override
    public void onClick(View view) {
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        final String result = response.getTokenResult();

                        //Successful captcha use. Assuming this is the g-recaptcha-response,
                        //set instance var to send in API call
                        if (result != null && !result.isEmpty()) {
                            recaptchaResponse = result;
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.d(tag, "Error message: " +
                                    CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                        } else {
                            Log.d(tag, "Unknown type of error: " + e.getMessage());
                        }
                    }
                });

    }
}