package org.openimis.imispolicies;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotEnrolledPoliciesOverviewAdapter extends RecyclerView.Adapter<NotEnrolledPoliciesOverviewAdapter.Reportmsg> {
    private JSONArray policies;
    private JSONArray newPolicies;

    private List<Reportmsg> items = new ArrayList<>();

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

    public NotEnrolledPoliciesOverviewAdapter(Context rContext, JSONArray _policies) {
        _context = rContext;
        policies = _policies;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
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

    public boolean isAllChecked() {
        boolean isAllChecked = true;
        int size = items.size();
        for (int i = 0; i < size; i++) {
            Reportmsg item = items.get(i);
            if (!item.checked) {
                isAllChecked = false;
                break;
            }
        }

        return isAllChecked;
    }


    public void selectAll() {
        boolean isAllChecked = isAllChecked();
        if (isAllChecked) { // deselect all
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).checked) {
                    items.get(i).itemView.callOnClick();
                }
            }
        } else { // select all
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).checked) {
                    items.get(i).itemView.callOnClick();
                }
            }
        }

    }

    @NonNull
    @Override
    public Reportmsg onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_policy, parent, false);

        Reportmsg view = new Reportmsg(row);
        items.add(view);
        return view;
    }

    @Override
    public void onBindViewHolder(@NonNull Reportmsg holder, int position) {
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

        holder.Id.setText(Id);
        holder.PolicyId.setText(PolicyId);
        holder.InsuranceNumber.setText(InsuranceNumber);
        holder.isDone.setText(isDone);
        holder.Names.setText(OtherNames + " " + LastName);
        holder.ProductCode.setText(ProductCode);
        holder.ProductName.setText(ProductName);
        holder.UploadedDate.setText(UploadedDate);
        holder.RequestedDate.setText(RequestedDate);
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
                    for (int i = 0; i <= policies.length(); i++) {
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
    public int getItemCount() {
        return policies.length();
    }


    public int getCount() {
        return getItemCount();
    }

    public class Reportmsg extends RecyclerView.ViewHolder {

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
        public boolean checked = false;

        private View itemView;

        public Reportmsg(final View itemView) {
            super(itemView);
            this.itemView = itemView;

            itemView.setOnClickListener(view -> {

                // Redraw the old selection and the new
                if (NotEnrolledPoliciesOverview.num.size() == 0) {
                    NotEnrolledPoliciesOverview.num.add(String.valueOf(getLayoutPosition()));
                    //itemView.setBackgroundColor(Color.GRAY);
                    checkbox1.setBackgroundResource(R.drawable.checked);
                    checked = true;

                    try {
                        paymentObject = new JSONObject();
                        paymentObject.put("Position", String.valueOf(getLayoutPosition()));
                        paymentObject.put("Id", String.valueOf(Id.getText()));
                        paymentObject.put("PolicyId", String.valueOf(PolicyId.getText()));
                        paymentObject.put("insurance_number", String.valueOf(InsuranceNumber.getText()));
                        paymentObject.put("insurance_product_code", String.valueOf(ProductCode.getText()));
                        paymentObject.put("uploaded_date", String.valueOf(UploadedDate.getText()));
                        if (String.valueOf(isDone.getText()).equals("N")) {
                            paymentObject.put("renewal", "0");
                        } else {
                            paymentObject.put("renewal", "1");
                        }
                        paymentObject.put("amount", String.valueOf(PolicyValue));
                        paymentDetails.put(paymentObject);
                        NotEnrolledPoliciesOverview.paymentDetails = paymentDetails;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    NotEnrolledPoliciesOverview.PolicyValueToSend += Integer.parseInt(PolicyValue);

                } else {
                    int ans = 0;
                    for (int i = 0; i < NotEnrolledPoliciesOverview.num.size(); i++) {
                        if (NotEnrolledPoliciesOverview.num.get(i).equals(String.valueOf(getLayoutPosition()))) {
                            ans = 1;
                            NotEnrolledPoliciesOverview.num.remove(i);
                            //itemView.setBackgroundColor(Color.WHITE);
                            checkbox1.setBackgroundResource(R.drawable.unchecked);
                            checked = false;

                            JSONObject ob = null;
                            for (int j = 0; j < paymentDetails.length(); j++) {
                                try {
                                    ob = paymentDetails.getJSONObject(j);
                                    String position = ob.getString("Position");
                                    if (position.equals(String.valueOf(getLayoutPosition()))) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            paymentDetails.remove(j);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            NotEnrolledPoliciesOverview.paymentDetails = paymentDetails;
                            NotEnrolledPoliciesOverview.PolicyValueToSend -= Integer.parseInt(PolicyValue);

                            break;
                        }
                        ans = 0;
                    }
                    if (ans == 0) {
                        NotEnrolledPoliciesOverview.num.add(String.valueOf(getLayoutPosition()));
                        //itemView.setBackgroundColor(Color.GRAY);
                        checkbox1.setBackgroundResource(R.drawable.checked);
                        checked = true;

                        try {
                            paymentObject = new JSONObject();
                            paymentObject.put("Id", String.valueOf(Id.getText()));
                            paymentObject.put("PolicyId", String.valueOf(PolicyId.getText()));
                            paymentObject.put("insurance_number", String.valueOf(InsuranceNumber.getText()));
                            paymentObject.put("insurance_product_code", String.valueOf(ProductCode.getText()));
                            paymentObject.put("uploaded_date", String.valueOf(UploadedDate.getText()));
                            if (String.valueOf(isDone.getText()).equals("N")) {
                                paymentObject.put("renewal", "0");
                            } else {
                                paymentObject.put("renewal", "1");
                            }
                            paymentObject.put("amount", String.valueOf(PolicyValue));
                            paymentDetails.put(paymentObject);
                            NotEnrolledPoliciesOverview.paymentDetails = paymentDetails;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        NotEnrolledPoliciesOverview.PolicyValueToSend += Integer.parseInt(PolicyValue);
                    }
                }

                ((NotEnrolledPoliciesOverview) _context).updateSelectAllButton(isAllChecked());
            });


            InsuranceNumber = itemView.findViewById(R.id.InsuranceNumber);
            Id = itemView.findViewById(R.id.Id);
            PolicyId = itemView.findViewById(R.id.PolicyId);
            isDone = itemView.findViewById(R.id.isDone);
            Names = itemView.findViewById(R.id.Names);
            ProductCode = itemView.findViewById(R.id.ProductCode);
            ProductName = itemView.findViewById(R.id.ProductName);
            UploadedDate = itemView.findViewById(R.id.UploadedDate);
            RequestedDate = itemView.findViewById(R.id.RequestedDate);
            checkbox1 = itemView.findViewById(R.id.checkbox1);
        }
    }
}
