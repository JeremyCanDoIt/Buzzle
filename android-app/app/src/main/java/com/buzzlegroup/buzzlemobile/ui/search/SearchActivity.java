package com.buzzlegroup.buzzlemobile.ui.search;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.buzzlegroup.buzzlemobile.databinding.SearchBinding;
import com.buzzlegroup.buzzlemobile.ui.movielist.ListingActivity;

/**
 * Main page after logging in - perform full text search on the database here
 * @author Thomas Tran
 */
public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Bind to xml layout
        SearchBinding binding = SearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Make the search button send parameter to listing
        binding.submitsearch.setOnClickListener(view -> {
            //changes to listing page with data sent through Intent
            Intent intent = new Intent(SearchActivity.this, ListingActivity.class);
            intent.putExtra("search_query", binding.searchbox.getText().toString());
            startActivity(intent);
        });
    }
}