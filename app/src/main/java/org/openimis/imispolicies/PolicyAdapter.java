package org.openimis.imispolicies;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

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
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PolicyAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
