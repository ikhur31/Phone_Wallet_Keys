package com.example.averygrimes.phone_wallet_keys;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ThreeColum_ListAdapter extends ArrayAdapter<User> {

    private LayoutInflater mInflater;
    private ArrayList<User> users;
    private int mViewResourceId;

    public ThreeColum_ListAdapter(Context context, int textViewResourceId,ArrayList<User> users){
        super(context,textViewResourceId,users);
        this.users = users;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parents){
        convertView = mInflater.inflate(mViewResourceId, null);

        User user = users.get(position);

        if(user != null){
            TextView bName = (TextView) convertView.findViewById(R.id.tt1);
            TextView bTime = (TextView) convertView.findViewById(R.id.tt2);
            TextView bDate = (TextView) convertView.findViewById(R.id.tt3);
            TextView bStatus = (TextView) convertView.findViewById(R.id.tt4);

            if (bName != null){
                bName.setText((user.getbName()));
            }
            if (bTime != null){
                bTime.setText((user.getbTime()));
            }
            if (bDate != null){
                bDate.setText((user.getbDate()));
            }
            if (bStatus != null)
            {
                bStatus.setText((user.getbStatus()));
            }
        }
        return convertView;
    }
}