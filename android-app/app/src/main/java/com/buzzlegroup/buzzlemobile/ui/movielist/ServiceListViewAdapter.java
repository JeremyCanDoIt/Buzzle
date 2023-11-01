package com.buzzlegroup.buzzlemobile.ui.movielist;

import com.buzzlegroup.buzzlemobile.R;
import com.buzzlegroup.buzzlemobile.data.model.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ServiceListViewAdapter extends ArrayAdapter<Service> {
    private final ArrayList<Service> services;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView sellername;
        TextView posteddate;
        TextView price;
        TextView status;
    }

    public ServiceListViewAdapter(Context context, ArrayList<Service> services) {
        super(context, R.layout.servicelist_row, services);
        this.services = services;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the service for this position
        final Service service = services.get(position);

        //Get the view (either by reusing or not)
        ViewHolder viewHolder;

        //If we're recycling -> get from tag
        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Not recycling -> inflate new view
        else {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.servicelist_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.sellername = convertView.findViewById(R.id.sellername);
            viewHolder.posteddate = convertView.findViewById(R.id.posteddate);
            viewHolder.price = convertView.findViewById(R.id.price);
            viewHolder.status = convertView.findViewById(R.id.status);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        }

        // Populate data
        viewHolder.title.setText(service.getTitle());
        viewHolder.sellername.setText(service.getSellerName());
        viewHolder.posteddate.setText(service.getDate());
        viewHolder.price.setText(service.getPrice());
        viewHolder.status.setText(service.getStatus());

        return convertView;
    }
}