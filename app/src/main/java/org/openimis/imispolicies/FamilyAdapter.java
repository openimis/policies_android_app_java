package org.openimis.imispolicies;

import static androidx.core.app.ActivityCompat.recreate;
import static org.openimis.imispolicies.util.AndroidUtils.showDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;

public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.ViewHolder> {

    private Context context;
    private JSONArray families;
    private ProgressDialog progressDialog;

    public FamilyAdapter(Context context, JSONArray families) {
        this.context = context;
        this.families = families;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_family, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        try {
            JSONObject family = families.getJSONObject(position);
            holder.name.setText(family.getString("InsureeName"));
            holder.chfid.setText(family.getString("CHFID"));
            holder.region.setText(family.getString("RegionName"));
            holder.district.setText(family.getString("DistrictName"));
            holder.village.setText(family.getString("VillageName"));
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
        return families.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,chfid,region, district, village;
        ImageView btnContextMenu;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtFamilyInsureeName);
            chfid = itemView.findViewById(R.id.txtFamilyInsuranceNumber);
            region = itemView.findViewById(R.id.txtRegion);
            district = itemView.findViewById(R.id.txtDistrict);
            village = itemView.findViewById(R.id.txtVillage);
            btnContextMenu = itemView.findViewById(R.id.btnContextMenuFamily);
        }
    }

    private void showContextMenu(View anchorView, int position) {
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.family_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            try {
                JSONObject family = families.getJSONObject(position);
                int familyId = family.getInt("FamilyId");
                String isOffline = family.getString("isOffline");
                if(item.getItemId() == R.id.family_menu_edit){
                    Intent intent = new Intent(context, FamilyInsurees.class);
                    intent.putExtra("FamilyId", familyId);
                    context.startActivity(intent);
                } else if(item.getItemId() == R.id.family_menu_delete){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.Confirm)
                            .setMessage(R.string.DeleteFamily)
                            .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showLoadingDialog();
                                    ClientAndroidInterface ca = new ClientAndroidInterface((Activity) context);
                                    if(isOffline.equals("0") || isOffline.equals("2") || isOffline.equals("false")){
                                        int result = ca.DeleteOnlineDataF(familyId);
                                        if (result == 1) {
                                            showDialog(context, context.getResources().getString(R.string.FamilyDeleted));
                                            dialogInterface.dismiss();
                                            recreate((Activity) context);
                                        }
                                    } else {
                                        int deleteSuccess = ca.DeleteFamily(familyId);
                                        if (deleteSuccess == 1) {
                                            showDialog(context, context.getResources().getString(R.string.FamilyDeleted));
                                            dialogInterface.dismiss();
                                            recreate((Activity) context);
                                        } else if(deleteSuccess == -1){
                                            showDialog(context, context.getResources().getString(R.string.LoginToDeleteOnlineData));
                                        } else if(deleteSuccess == 3){
                                            int result = ca.DeleteOnlineDataF(familyId);
                                            if (result == 1) {
                                                showDialog(context, context.getResources().getString(R.string.FamilyDeleted));
                                                dialogInterface.dismiss();
                                                recreate((Activity) context);
                                            }
                                        }
                                    }
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.No, null)
                            .show();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return false;
        });

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