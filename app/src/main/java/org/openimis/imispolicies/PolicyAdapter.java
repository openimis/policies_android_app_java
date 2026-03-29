package org.openimis.imispolicies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.ViewHolder> {

    private Context context;
    private JSONArray policies;
    private int familyId;
    private ProgressDialog progressDialog;

    public PolicyAdapter(Context context, JSONArray policies, int familyId) {
        this.context = context;
        this.policies = policies;
        this.familyId = familyId;
    }

    @NonNull
    @Override
    public PolicyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_policy, parent, false);

        return new PolicyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PolicyAdapter.ViewHolder holder, int position) {
        try {
            JSONObject insuree = policies.getJSONObject(position);
            holder.productCode.setText(insuree.getString("ProductCode"));
            holder.productName.setText(insuree.getString("ProductName"));
            holder.startDate.setText(insuree.getString("StartDate"));
            holder.expiryDate.setText(insuree.getString("ExpiryDate"));
            holder.value.setText(insuree.getString("PolicyValue"));
            holder.policyStatus.setText(insuree.getString("PolicyStatus"));
            holder.effectiveDate.setText(insuree.getString("EffectiveDate"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        holder.btnContextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContextMenu(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return policies.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView productCode,productName,policyStatus, expiryDate, startDate, value, effectiveDate;
        ImageView btnContextMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            productCode = itemView.findViewById(R.id.policyProductCode);
            productName = itemView.findViewById(R.id.policyProductName);
            policyStatus = itemView.findViewById(R.id.policyStatus);
            expiryDate = itemView.findViewById(R.id.policyExpiryDate);
            effectiveDate = itemView.findViewById(R.id.policyEffectiveDate);
            startDate = itemView.findViewById(R.id.policyStartDate);
            value = itemView.findViewById(R.id.policyValue);
            btnContextMenu = itemView.findViewById(R.id.btnContextMenuPolicy);
        }
    }

    private void showContextMenu(View anchorView, int position) {
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.policy_menu, popup.getMenu());
        try {
            JSONObject policy = policies.getJSONObject(position);
            int policyId = policy.getInt("PolicyId");
            int isOffline = policy.getInt("isOffline");
            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.policy_menu_edit){
                    Intent intent = new Intent(context, PolicyActivity.class);
                    intent.putExtra("PolicyId", policyId);
                    intent.putExtra("FamilyId", familyId);
                    intent.putExtra("RegionId", FamilyInsurees.regionId);
                    intent.putExtra("DistrictId", FamilyInsurees.districtId);
                    context.startActivity(intent);
                } else if(item.getItemId() == R.id.policy_menu_payment){
//                    Intent intent = new Intent(context, PolicyPremiumActivity.class);
//                    intent.putExtra("PolicyId", policyId);
//                    intent.putExtra("FamilyId", familyId);
//                    context.startActivity(intent);
                } else if(item.getItemId() == R.id.policy_menu_delete){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.Confirm)
                            .setMessage(R.string.DeletePolicyPremium)
                            .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showLoadingDialog();
                                    ClientAndroidInterface ca = new ClientAndroidInterface((Activity) context);
                                    int deleteSuccess = -1;
                                    if(isOffline == 0 || isOffline == 2){
                                        showLoadingDialog();
                                        deleteSuccess = ca.DeleteOnlineData(policyId, "PO");
                                    } else {
                                        deleteSuccess = ca.DeletePolicy(policyId);
                                    }
                                    if (deleteSuccess == 1) {
                                        FragmentActivity activity = (FragmentActivity) context;
                                        FragmentManager fm = activity.getSupportFragmentManager();
                                        dialogInterface.dismiss();
                                        progressDialog.dismiss();
                                        Bundle result = new Bundle();
                                        result.putBoolean("refresh", true);
                                        fm.setFragmentResult("requestKey", result);
                                        ca.ShowDialog(context.getResources().getString(R.string.PolicyDeleted));
                                    } else if(deleteSuccess == -1){
                                        dialogInterface.dismiss();
                                        progressDialog.dismiss();
                                        ca.ShowDialog(context.getResources().getString(R.string.LoginToDeleteOnlineData));
                                    }
                                }
                            })
                            .setNegativeButton(R.string.No, null)
                            .show();
                }
                return false;
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        popup.show();
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.Pleasewait));
        progressDialog.setTitle(context.getResources().getString(R.string.Delete));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}
