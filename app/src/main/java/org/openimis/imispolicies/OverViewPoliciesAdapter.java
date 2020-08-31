package org.openimis.imispolicies;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openimis.imispolicies.R;

/**
 * Created by Hiren on 10/12/2018.
 */
//Please see newPolicies and query to check Insuaree numbers
public class OverViewPoliciesAdapter<VH extends TrackSelectionAdapter.ViewHolder> extends RecyclerView.Adapter {
    OverViewPolicies1 overViewPolicies = new OverViewPolicies1();
    private JSONArray policies;
    private JSONArray newPolicies;

    String PolicyId = null;
    String Id = null;
    String InsuranceNumber = null;
    String isDone = null;
    String PolicyValue = null;
    String LastName = null;
    String OtherNames = null;
    String ProductCode = null;
    String ProductName = null;
    String UploadedDate = null;
    String RequestedDate = null;

    private int focusedItem = 0;
    private int policyvalue = 0;


    public JSONArray paymentDetails = new JSONArray();
    public JSONObject paymentObject;



    //Constructor
    Context _context;
    public OverViewPoliciesAdapter(Context rContext, JSONArray _policies){
        _context = rContext;
        policies = _policies;

    }



    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_policy,parent,false);

        Reportmsg view = new Reportmsg(row);
        return view;
    }

    //Please see newPolicies and query to check Insuaree numbers
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();



                if (query.isEmpty()) {
                    newPolicies = policies;
                } else {
                    for(int i=0; i<=policies.length();i++){
                        try {
                            if (policies.getString(i).toLowerCase().contains(query.toLowerCase())) {
                                newPolicies.put(policies.getString(i));//Please see newPolicies and query to check Insuaree numbers
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = newPolicies.length();
                results.values = newPolicies;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                //policies = results.values;
                newPolicies = (JSONArray) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setSelected(focusedItem == position);
        newPolicies = policies;

        try {
            JSONObject object = newPolicies.getJSONObject(position);
            Id = object.getString("Id");
            PolicyId = object.getString("PolicyId");
            InsuranceNumber = object.getString("InsuranceNumber");
            LastName = object.getString("LastName");
            OtherNames = object.getString("OtherNames");
            ProductCode = object.getString("ProductCode");
            ProductName = object.getString("ProductName");
            isDone = object.getString("isDone");
            PolicyValue = object.getString("PolicyValue");
            UploadedDate = object.getString("UploadedDate");
            RequestedDate = object.getString("ControlRequestDate");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (UploadedDate.equals("") && !RequestedDate.equals("")) {
            LastName = "";
            OtherNames = "";
        }

        ((Reportmsg) holder).Id.setText(Id);
        ((Reportmsg) holder).PolicyId.setText(PolicyId);
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


    public int getCount(){
        return getItemCount();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

        public TextView Id;
        public TextView PolicyId;
        public TextView InsuranceNumber;
        public TextView isDone;
        public TextView Names;
        public TextView ProductCode;
        public TextView ProductName;
        public TextView UploadedDate;
        public TextView RequestedDate;
        public ImageView checkbox1;




        public Reportmsg(final View itemView) {
            super(itemView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Redraw the old selection and the new
                    if(overViewPolicies.num.size() == 0){
                        overViewPolicies.num.add(String.valueOf(getLayoutPosition()));
                        //itemView.setBackgroundColor(Color.GRAY);
                        checkbox1.setBackgroundResource(R.drawable.checked);

                        try {
                            paymentObject = new JSONObject();
                            paymentObject.put("Position", String.valueOf(getLayoutPosition()));
                            paymentObject.put("Id", String.valueOf(Id.getText()));
                            paymentObject.put("PolicyId", String.valueOf(PolicyId.getText()));
                            paymentObject.put("insurance_number", String.valueOf(InsuranceNumber.getText()));
                            paymentObject.put("insurance_product_code", String.valueOf(ProductCode.getText()));
                            paymentObject.put("uploaded_date", String.valueOf(UploadedDate.getText()));
                            if(String.valueOf(isDone.getText()).equals("N")){
                                paymentObject.put("renewal","0");
                            }else{
                                paymentObject.put("renewal","1");
                            }
                            paymentDetails.put(paymentObject);
                            overViewPolicies.paymentDetails = paymentDetails;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        overViewPolicies.PolicyValueToSend += Integer.parseInt(PolicyValue);

                    }else{
                        int ans = 0;
                        for (int i=0; i<overViewPolicies.num.size(); i++){
                            if(overViewPolicies.num.get(i).equals(String.valueOf(getLayoutPosition()))){
                                ans = 1;
                                overViewPolicies.num.remove(i);
                                //itemView.setBackgroundColor(Color.WHITE);
                                checkbox1.setBackgroundResource(R.drawable.unchecked);

                                JSONObject ob = null;
                                for(int j = 0;j < paymentDetails.length();j++){
                                    try {
                                        ob = paymentDetails.getJSONObject(j);
                                        String position = ob.getString("Position");
                                        if(position.equals(String.valueOf(getLayoutPosition()))){
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                paymentDetails.remove(j);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                overViewPolicies.paymentDetails = paymentDetails;
                                overViewPolicies.PolicyValueToSend -= Integer.parseInt(PolicyValue);

                                break;
                            }
                            ans = 0;
                        }
                        if(ans == 0){
                            overViewPolicies.num.add(String.valueOf(getLayoutPosition()));
                            //itemView.setBackgroundColor(Color.GRAY);
                            checkbox1.setBackgroundResource(R.drawable.checked);

                            try {
                                paymentObject = new JSONObject();
                                paymentObject.put("Id", String.valueOf(Id.getText()));
                                paymentObject.put("PolicyId", String.valueOf(PolicyId.getText()));
                                paymentObject.put("insurance_number", String.valueOf(InsuranceNumber.getText()));
                                paymentObject.put("insurance_product_code", String.valueOf(ProductCode.getText()));
                                paymentObject.put("uploaded_date", String.valueOf(UploadedDate.getText()));
                                if(String.valueOf(isDone.getText()).equals("N")){
                                    paymentObject.put("renewal","0");
                                }else{
                                    paymentObject.put("renewal","1");
                                }
                                paymentDetails.put(paymentObject);
                                overViewPolicies.paymentDetails = paymentDetails;

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            overViewPolicies.PolicyValueToSend += Integer.parseInt(PolicyValue);
                        }
                    }

                }
            });


            InsuranceNumber = (TextView) itemView.findViewById(R.id.InsuranceNumber);
            Id = (TextView) itemView.findViewById(R.id.Id);
            PolicyId = (TextView) itemView.findViewById(R.id.PolicyId);
            isDone = (TextView) itemView.findViewById(R.id.isDone);
            Names = (TextView) itemView.findViewById(R.id.Names);
            ProductCode = (TextView) itemView.findViewById(R.id.ProductCode);
            ProductName = (TextView) itemView.findViewById(R.id.ProductName);
            UploadedDate = (TextView) itemView.findViewById(R.id.UploadedDate);
            RequestedDate = (TextView) itemView.findViewById(R.id.RequestedDate);
            checkbox1 = (ImageView) itemView.findViewById(R.id.checkbox1);
        }
    }


}
