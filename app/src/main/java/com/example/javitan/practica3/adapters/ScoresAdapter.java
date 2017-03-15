package com.example.javitan.practica3.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.javitan.practica3.R;
import com.example.javitan.practica3.pojo.HighScoreList;

/**
 * Created by Javitan on 19/02/2017.
 */

public class ScoresAdapter extends ArrayAdapter {

    private HighScoreList data;
    private Context context;
    private int layout;

    private class ViewHolder {
        TextView tvScoreName;
        TextView tvScoreValue;
    }

    public ScoresAdapter(Context context, int resource, HighScoreList objects) {
        super(context, resource, objects.getScores());
        this.data = objects;
        this.context = context;
        this.layout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
            result = inflater.inflate(this.layout, null);
            holder = new ViewHolder();
            holder.tvScoreName = (TextView) result.findViewById(R.id.tvName);
            holder.tvScoreValue = (TextView) result.findViewById(R.id.tvScore);
            result.setTag(holder);
        }
        holder = (ViewHolder) result.getTag();
        holder.tvScoreName.setText(data.getScores().get(position).getName());
        holder.tvScoreValue.setText(String.valueOf(data.getScores().get(position).getScoring()));
        return result;
    }
}
