package org.openimis.imispolicies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONSpinnerAdapter extends BaseAdapter {

    private Context context;
    private JSONArray data;
    private String text;

    public JSONSpinnerAdapter(Context context, JSONArray data, String text) {
        this.context = context;
        this.data = data;
        this.text = text;
    }

    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return data.getJSONObject(position);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView(context);
        textView.setPadding(20,20,20,20);
        textView.setTextSize(16);

        try {
            JSONObject obj = data.getJSONObject(position);
            textView.setText(obj.getString(text));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView(context);
        textView.setPadding(20,20,20,20);
        textView.setTextSize(16);

        try {
            JSONObject obj = data.getJSONObject(position);
            textView.setText(obj.getString(text));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return textView;
    }
}