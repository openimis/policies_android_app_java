package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;

public class PoliciesFragment extends Fragment {

    FloatingActionButton btnAddPolicy;
    private int familyId;
    RecyclerView recyclerPolicies;
    private ClientAndroidInterface ca;

    public PoliciesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.policies_fragment, container, false);
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            boolean refresh = bundle.getBoolean("refresh");
            if (refresh) {
                loadPolicies();
            }
        });
        familyId = FamilyInsurees.familyId;
        btnAddPolicy = view.findViewById(R.id.btnNewPolicy);
        recyclerPolicies = view.findViewById(R.id.recyclerPolicies);
        ca = new ClientAndroidInterface(getActivity());
        recyclerPolicies.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPolicies();

        btnAddPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PolicyActivity.class);
            intent.putExtra("FamilyId", familyId);
            intent.putExtra("RegionId", FamilyInsurees.regionId);
            intent.putExtra("DistrictId", FamilyInsurees.districtId);
            startActivity(intent);
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        loadPolicies();
    }

    private void loadPolicies(){
        try {
            String policies = ca.getFamilyPolicies(familyId);
            JSONArray policiesArray = new JSONArray(policies);
            PolicyAdapter adapter = new PolicyAdapter(getContext(),policiesArray, familyId);
            recyclerPolicies.setAdapter(adapter);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
