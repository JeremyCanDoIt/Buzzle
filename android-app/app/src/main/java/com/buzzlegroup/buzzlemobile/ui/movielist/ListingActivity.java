package com.buzzlegroup.buzzlemobile.ui.movielist;

import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.buzzlegroup.buzzlemobile.Constants;
import com.buzzlegroup.buzzlemobile.data.NetworkManager;
import com.buzzlegroup.buzzlemobile.data.model.Service;
import com.buzzlegroup.buzzlemobile.databinding.ActivityServicelistBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Page for service listing
 * @author Thomas Tran
 */
public class ListingActivity extends AppCompatActivity {

    private int page = 1;
    private int maxPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create binding to xml page
        ActivityServicelistBinding binding = ActivityServicelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hook up the page nav buttons
        binding.prev.setOnClickListener(view -> ChangePage(binding, -1));
        binding.next.setOnClickListener(view -> ChangePage(binding, 1));

        PopulateList(binding);
    }

    /**
     * Performs an API call to the backend and
     * populates the service listing with results.
     */
    private void PopulateList(final ActivityServicelistBinding binding) {
        //get the parameter from the search page
        final Bundle extras = getIntent().getExtras();
        final String fullTextSearchQuery = (extras != null) ? extras.getString("search_query") : null;

        //send the API call to the backend to get results
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest request = new StringRequest(
                //NOTE: WE ARE USING A POST REQUEST HERE INSTEAD OF GET.
                //THE BACKEND REDIRECTS POST REQS (FOR api/services) TO GET ANYWAY,
                //AND USING GET REQUEST HERE BREAKS PARAMETER PASSING (WOULD NEED TO BE BAKED INTO URL)
                Request.Method.POST,

                Constants.baseURL + "/api/services",
                response -> {
                    try {
                        final JSONObject json = new JSONObject(response);

                        //Update the max page limit
                        this.maxPage = json.getInt("max-pages");

                        //Make the list display work
                        final ArrayList<Service> services = convertServiceValues(json.getJSONArray("values"));
                        ServiceListViewAdapter adapter = new ServiceListViewAdapter(this, services);
                        ListView listView = binding.list;
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            final Service service = services.get(position);

                            //Passes ID to single service page and changes page
                            final Intent intent = new Intent(ListingActivity.this, SingleService.class);
                            intent.putExtra("id", service.getId());
                            startActivity(intent);
                        });
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

                params.put("page", String.valueOf(page));
                if (fullTextSearchQuery != null) {
                    params.put("full-text", fullTextSearchQuery);
                }

                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Refreshes the listing with new content when the page is changed
     * @param binding binding
     * @param delta how much to change page by
     */
    private void ChangePage(final ActivityServicelistBinding binding, int delta) {
        //Page bound check
        final int newPage = this.page + delta;
        if (newPage <= 0 || (maxPage != -1 && newPage > maxPage)) return;

        //Set new page
        this.page = newPage;

        //Clear the list view
        final ListView listView = binding.list;
        listView.setAdapter(null);

        //Re-populate the list
        PopulateList(binding);
    }


    /**
     * Converts a JSON array of Services to an ArrayList of Services
     * @param array the JSON array object
     * @return the array list of service objects
     * @throws JSONException if JSON is malformed
     */
    private ArrayList<Service> convertServiceValues(final JSONArray array) throws JSONException {
        final ArrayList<Service> ret = new ArrayList<>();

        final int length = array.length();
        for (int i = 0; i < length; ++i) {
            final JSONObject entry = array.getJSONObject(i);
            ret.add(
                    new Service(
                        entry.getString("service_id"),
                        entry.getString("seller_id"),
                        entry.getString("service_title"),
                        entry.getString("seller_name"),
                        entry.getString("service_date"),
                        entry.getString("service_price"),
                        entry.getString("service_status")
                    )
            );
        }

        return ret;
    }
}