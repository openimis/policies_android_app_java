package org.openimis.imispolicies;

import android.app.AlertDialog;
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
import org.json.JSONException;
import org.json.JSONObject;

public class InsureeAdapter extends RecyclerView.Adapter<InsureeAdapter.ViewHolder> {

    private Context context;
    private JSONArray insurees;

    public InsureeAdapter(Context context, JSONArray insurees) {
        this.context = context;
        this.insurees = insurees;
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
            JSONObject family = insurees.getJSONObject(position);
            holder.name.setText(family.getString("InsureeName"));
            holder.chfid.setText(family.getString("CHFID"));
            holder.dob.setText(family.getString("DOB"));
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

        public ViewHolder(View itemView) {
            super(itemView);
            chfid = itemView.findViewById(R.id.CHFID);
            name = itemView.findViewById(R.id.InsureeName);
            dob = itemView.findViewById(R.id.item_dob);
            btnContextMenu = itemView.findViewById(R.id.btnContextMenuInsuree);
        }
    }

    private void showContextMenu(View anchorView, int position) {
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.insuree_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.insuree_menu_edit){
                try {
                    JSONObject insuree = insurees.getJSONObject(position);
                    int familyId = insuree.getInt("FamilyId");
                    int insureeId = insuree.getInt("InsureeId");
                    Intent intent = new Intent(context, InsureeActivity.class);
                    intent.putExtra("InsureeId", insureeId);
                    intent.putExtra("FamilyId", familyId);
                    context.startActivity(intent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            } else if(item.getItemId() == R.id.family_menu_delete){

                new AlertDialog.Builder(context)
                        .setTitle(R.string.Confirm)
                        .setMessage(R.string.DeleteFamily)
                        .setPositiveButton(R.string.Yes, (d,w)->{
//                            families.remove(position);
//                            notifyDataSetChanged();
                        })
                        .setNegativeButton(R.string.No, null)
                        .show();
            }
            return false;
        });

        popup.show();
    }
}
