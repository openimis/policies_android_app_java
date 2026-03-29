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

public class InsureeAdapter extends RecyclerView.Adapter<InsureeAdapter.ViewHolder> {

    private Context context;
    private JSONArray insurees;
    private int familyId;
    private ProgressDialog progressDialog;

    public InsureeAdapter(Context context, JSONArray insurees, int familyId) {
        this.context = context;
        this.insurees = insurees;
        this.familyId = familyId;
    }


    @NonNull
    @Override
    public InsureeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_insuree, parent, false);

        return new InsureeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsureeAdapter.ViewHolder holder, int position) {
        try {
            JSONObject insuree = insurees.getJSONObject(position);
            holder.name.setText(insuree.getString("InsureeName"));
            holder.chfid.setText(insuree.getString("CHFID"));
            holder.dob.setText(insuree.getString("DOB"));

            if(insuree.getString("isHead").equals("1")){
                holder.insureeCard.setStrokeColor(context.getResources().getColor(R.color.colorAccent));
            }
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
        return insurees.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,chfid,dob;
        ImageView btnContextMenu;
        MaterialCardView insureeCard;

        public ViewHolder(View itemView) {
            super(itemView);
            chfid = itemView.findViewById(R.id.CHFID);
            name = itemView.findViewById(R.id.InsureeName);
            dob = itemView.findViewById(R.id.item_dob);
            btnContextMenu = itemView.findViewById(R.id.btnContextMenuInsuree);
            insureeCard = itemView.findViewById(R.id.family_insurees_card);
        }
    }

    private void showContextMenu(View anchorView, int position) {
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.insuree_menu, popup.getMenu());
        try {
            JSONObject insuree = insurees.getJSONObject(position);
            int insureeId = insuree.getInt("InsureeId");
            int isOffline = insuree.getInt("isOffline");
            if(insuree.getString("isHead").equals("1")){
                popup.getMenu().findItem(R.id.insuree_menu_delete).setVisible(false);
            } else {
                popup.getMenu().findItem(R.id.insuree_menu_delete).setVisible(true);
            }
            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.insuree_menu_edit){
                    Intent intent = new Intent(context, InsureeActivity.class);
                    intent.putExtra("InsureeId", insureeId);
                    intent.putExtra("FamilyId", familyId);
                    context.startActivity(intent);
                } else if(item.getItemId() == R.id.insuree_menu_delete){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.Confirm)
                            .setMessage(R.string.DeleteInsuree)
                            .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showLoadingDialog();
                                    ClientAndroidInterface ca = new ClientAndroidInterface((Activity) context);
                                    int deleteSuccess = 0;
                                    if(isOffline == 0 || isOffline == 2){
                                        showLoadingDialog();
                                        deleteSuccess = ca.DeleteOnlineData(insureeId, "I");
                                    } else {
                                        deleteSuccess = ca.DeleteInsuree(insureeId);
                                    }
                                    if (deleteSuccess == 1) {
                                        dialogInterface.dismiss();
                                        progressDialog.dismiss();
                                        FragmentActivity activity = (FragmentActivity) context;
                                        FragmentManager fm = activity.getSupportFragmentManager();
                                        Bundle result = new Bundle();
                                        result.putBoolean("refresh_insurees", true);
                                        fm.setFragmentResult("requestKey", result);
                                        ca.ShowDialog(context.getResources().getString(R.string.InsureeDeleted));
                                    } else if(deleteSuccess == 2){
                                        dialogInterface.dismiss();
                                        progressDialog.dismiss();
                                        ca.ShowDialog(context.getResources().getString(R.string.IsHeadDelete));
                                    } else if(deleteSuccess == -1){
                                        dialogInterface.dismiss();
                                        progressDialog.dismiss();
                                        ca.ShowDialog(context.getResources().getString(R.string.LoginToDeleteOnlineData));
                                    } else {
                                        dialogInterface.dismiss();
                                        ca.ShowDialog(context.getResources().getString(R.string.InsureeNotDeleted));
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
