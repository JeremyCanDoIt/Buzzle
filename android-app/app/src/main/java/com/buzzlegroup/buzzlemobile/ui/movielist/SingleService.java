package com.buzzlegroup.buzzlemobile.ui.movielist;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.buzzlegroup.buzzlemobile.Constants;
import com.buzzlegroup.buzzlemobile.data.NetworkManager;
import com.buzzlegroup.buzzlemobile.databinding.ActivitySingleServiceBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Page for displaying a single service
 * @author Thomas Tran
 */
public class SingleService extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bind to xml layout
        ActivitySingleServiceBinding binding = ActivitySingleServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get the id parameter from extras
        final Bundle extras = getIntent().getExtras();
        final String id = extras.getString("id");

        //id invalid - page cannot continue. exit
        if (id == null || id.isEmpty()) {
            finish();
            return;
        }

        //make an API call to get data
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest request = new StringRequest(
                //NOTE: WE ARE USING A POST REQUEST HERE INSTEAD OF GET.
                //THE BACKEND REDIRECTS POST REQS (FOR api/single-service) TO GET ANYWAY,
                //AND USING GET REQUEST HERE BREAKS PARAMETER PASSING (WOULD NEED TO BE BAKED INTO URL)
                Request.Method.POST,

                Constants.baseURL + "/api/single-service",
                response -> {
                    try {
                        final JSONObject json = new JSONArray(response).getJSONObject(0);

                        binding.title.setText(preventNull(json.getString("service_title")));
                        binding.price.setText(preventNull(json.getString("service_price")));
                        binding.paymenttype.setText(preventNull(json.getString("service_payment_type")));
                        binding.posteddate.setText(preventNull(json.getString("service_posted_date")));
                        binding.status.setText(preventNull(json.getString("service_status")));
                        binding.sellername.setText(preventNull(json.getString("seller_name")));
                        binding.sellerusername.setText(preventNull(json.getString("seller_username")));
                        binding.description.setText(preventNull(json.getString("service_description")));
                        binding.sellerrating.setText(preventNull(json.getString("seller_avg_rating")));
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
                params.put("id", id);

                return params;
            }
        };
        queue.add(request);
    }

    private String preventNull(String str) {
        return (str != null ? str : "");
    }
}
