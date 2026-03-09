package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.ViewHolder> {

    private Context context;
    private JSONArray families;

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

        String familyId;
        try {
            JSONObject family = families.getJSONObject(position);
            familyId = family.getString("FamilyId");
            holder.name.setText(family.getString("InsureeName"));
            holder.chfid.setText(family.getString("InsuranceNumber"));
            holder.region.setText(family.getString("Region"));
            holder.district.setText(family.getString("District"));
            holder.village.setText(family.getString("Village"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        holder.itemView.setOnLongClickListener(v -> {

            PopupMenu menu = new PopupMenu(context, holder.itemView);
            menu.inflate(R.menu.family_menu);

            menu.setOnMenuItemClickListener(item -> {

                if(item.getItemId() == R.id.family_menu_edit){
                    Intent intent = new Intent(context, FamilyActivity.class);
                    intent.putExtra("familyId", familyId);
                    context.startActivity(intent);

                } else if(item.getItemId() == R.id.family_menu_delete){

                    new AlertDialog.Builder(context)
                            .setTitle("Delete Family")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Ok", (d,w)->{
                                families.remove(position);
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }

                return true;
            });

            menu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return families.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,chfid,region, district, village;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtFamilyInsureeName);
            chfid = itemView.findViewById(R.id.txtFamilyInsuranceNumber);
            region = itemView.findViewById(R.id.txtRegion);
            district = itemView.findViewById(R.id.txtDistrict);
            village = itemView.findViewById(R.id.txtVillage);
        }
    }
}