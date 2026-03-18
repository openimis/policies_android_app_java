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

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsureeAdapter extends RecyclerView.Adapter<InsureeAdapter.ViewHolder> {

    private Context context;
    private JSONArray insurees;
    private int familyId;

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

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.insuree_menu_edit){
                try {
                    JSONObject insuree = insurees.getJSONObject(position);
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
