package org.openimis.imispolicies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.AndroidUtils;

public class PremiumAdapter extends RecyclerView.Adapter<PremiumAdapter.ViewHolder> {

    private final Context context;
    private final JSONArray premiums;
    private final int policyId;
    private final int familyId;
    private final int regionId;
    private final int districtId;


    public PremiumAdapter(Context context, JSONArray premiums, int policyId, int familyId, int regionId, int districtId) {
        this.context = context;
        this.premiums = premiums;
        this.policyId = policyId;
        this.familyId = familyId;
        this.regionId = regionId;
        this.districtId = districtId;
    }

    @NonNull
    @Override
    public PremiumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_premium, parent, false);

        Log.e("premium", premiums.toString());

        return new PremiumAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumAdapter.ViewHolder holder, int position) {
        try{
            JSONObject premium = premiums.getJSONObject(position);
            holder.date.setText(premium.getString("PayDate"));
            holder.payMode.setText(premium.getString("PayType"));
            holder.amount.setText(premium.getString("Amount"));
            holder.receipt.setText(premium.getString("Receipt"));
        } catch (Exception e){
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener((v)-> showContextMenu(v,position));
    }

    @Override
    public int getItemCount() {
        return premiums.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date,payMode,amount,receipt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tvPremiumDate);
            payMode = itemView.findViewById(R.id.tvPremiumPayMode);
            amount = itemView.findViewById(R.id.tvPremiumAmount);
            receipt = itemView.findViewById(R.id.tvPremiumReceiptNo);
        }
    }

    private void showContextMenu(View anchorView, int position){
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.premium_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            try{
                JSONObject premium = premiums.getJSONObject(position);
                int premiumId = premium.getInt("PremiumId");
                String isOffline = premium.getString("isOffline");
                if(item.getItemId() == R.id.premium_menu_edit){
                    Intent intent = new Intent(context.getApplicationContext(), PremiumActivity.class);
                    intent.putExtra("PremiumId", premiumId);
                    intent.putExtra("PolicyId", policyId);
                    intent.putExtra("FamilyId", familyId);
                    intent.putExtra("RegionId", regionId);
                    intent.putExtra("DistrictId", districtId);
                    context.startActivity(intent);
                } else if(item.getItemId() == R.id.premium_menu_delete){

                    ClientAndroidInterface ca = new ClientAndroidInterface((Activity) context);
                    AndroidUtils.showConfirmDialog(context, R.string.ConfirmDeletePremium, (dialog, i)->{
                        int deletedSuccess = -1;
                        if (isOffline.equals("0") || isOffline.equals("2")) {
                            deletedSuccess = ca.DeleteOnlineData(premiumId, "PR");
                        } else {
                            deletedSuccess = ca.DeletePremium(premiumId, policyId);
                        }
                        if (deletedSuccess == 1) {
                            ((Activity) context).recreate();
                            AndroidUtils.showDialog(context, R.string.PremiumDeleted);
                        }
                        else if (deletedSuccess == -1) {
                            ca.ShowDialog(context.getResources().getString(R.string.LoginToDeleteOnlineData));
                        }
                    });

                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return false;
        });
        popup.show();
    }
}
