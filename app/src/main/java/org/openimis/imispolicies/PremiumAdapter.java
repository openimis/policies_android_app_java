package org.openimis.imispolicies;

import android.content.Context;
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

public class PremiumAdapter extends RecyclerView.Adapter<PremiumAdapter.ViewHolder> {

    private Context context;
    private JSONArray premiums;

    public PremiumAdapter(Context context, JSONArray premiums) {
        this.context = context;
        this.premiums = premiums;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_premium, parent, false);

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
        holder.btnContextMenu.setOnClickListener(v ->{
            showContextMenu(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return premiums.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date,payMode,amount,receipt;
        ImageView btnContextMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tvPremiumDate);
            payMode = itemView.findViewById(R.id.tvPremiumPayMode);
            amount = itemView.findViewById(R.id.tvPremiumAmount);
            receipt = itemView.findViewById(R.id.tvPremiumReceiptNo);
            btnContextMenu = itemView.findViewById(R.id.btnContextMenuPremium);
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

                } else if(item.getItemId() == R.id.premium_menu_delete){

                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return false;
        });

        popup.show();

    }
}
