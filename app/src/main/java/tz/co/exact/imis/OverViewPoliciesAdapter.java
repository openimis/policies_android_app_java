package tz.co.exact.imis;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hiren on 10/12/2018.
 */

public class OverViewPoliciesAdapter extends RecyclerView.Adapter {
    private JSONArray policies;

    String InsuranceNumber = null;
    String isDone = null;
    String LastName = null;
    String OtherNames = null;
    String ProductCode = null;
    String ProductName = null;
    String UploadedDate = null;
    String RequestedDate = null;
    //Constructor
    Context _context;
    public OverViewPoliciesAdapter(Context rContext, JSONArray _policies){
        _context = rContext;
        policies = _policies;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_policy,parent,false);

        Reportmsg view = new Reportmsg(row);
        return view;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        try {
            JSONObject object = policies.getJSONObject(position);
            InsuranceNumber = object.getString("InsuranceNumber");
            LastName = object.getString("LastName");
            OtherNames = object.getString("OtherNames");
            ProductCode = object.getString("ProductCode");
            ProductName = object.getString("ProductName");
            isDone = object.getString("isDone");
            UploadedDate = object.getString("UploadedDate");
            RequestedDate = object.getString("ControlRequestDate");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((Reportmsg) holder).InsuranceNumber.setText(InsuranceNumber);
        ((Reportmsg) holder).isDone.setText(isDone);
        ((Reportmsg) holder).Names.setText(OtherNames + " " + LastName);
        ((Reportmsg) holder).ProductCode.setText(ProductCode);
        ((Reportmsg) holder).ProductName.setText(ProductName);
        ((Reportmsg) holder).UploadedDate.setText(UploadedDate);
        ((Reportmsg) holder).RequestedDate.setText(RequestedDate);

    }

    @Override
    public int getItemCount() {
        return policies.length();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

        public TextView InsuranceNumber;
        public TextView isDone;
        public TextView Names;
        public TextView ProductCode;
        public TextView ProductName;
        public TextView UploadedDate;
        public TextView RequestedDate;

        public Reportmsg(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String InsNo = InsuranceNumber.getText().toString();
                    //trackBox(No,qty,price,coins,tarehe);
                }
            });

            InsuranceNumber = (TextView) itemView.findViewById(R.id.InsuranceNumber);
            isDone = (TextView) itemView.findViewById(R.id.isDone);
            Names = (TextView) itemView.findViewById(R.id.Names);
            ProductCode = (TextView) itemView.findViewById(R.id.ProductCode);
            ProductName = (TextView) itemView.findViewById(R.id.ProductName);
            UploadedDate = (TextView) itemView.findViewById(R.id.UploadedDate);
            RequestedDate = (TextView) itemView.findViewById(R.id.RequestedDate);
        }
    }

/*    public void trackBox(String Number, String qty, String price, String coins, String tarehe){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(_context);
        View promptsView = li.inflate(R.layout.trackorder, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                _context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView OrderNo = (TextView) promptsView.findViewById(R.id.OrderNo);
        final TextView trackDate = (TextView) promptsView.findViewById(R.id.trackDate);
        final TextView ChapooValue = (TextView) promptsView.findViewById(R.id.ChapooValue);
        final TextView AmountValue = (TextView) promptsView.findViewById(R.id.AmountValue);
        final TextView CoinsValue = (TextView) promptsView.findViewById(R.id.CoinsValue);
        final TextView CurrentLocationName = (TextView) promptsView.findViewById(R.id.CurrentLocationName);
        final TextView TimeRemaining = (TextView) promptsView.findViewById(R.id.TimeRemaining);
        final TextView order_status = (TextView) promptsView.findViewById(R.id.order_status);


        OrderNo.setText(Number);
        ChapooValue.setText(qty);
        AmountValue.setText(price);
        CoinsValue.setText(coins);
        trackDate.setText(tarehe);

        //order_status.setTextColor(ContextCompat.getColor(_context, R.color.colorPrimary));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Go Back",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Abort",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }*/
}
