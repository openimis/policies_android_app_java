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

public class InsureesFragment extends Fragment {

    FloatingActionButton btnAddInsuree;
    private int familyId = 0;
    RecyclerView recyclerInsurees;
    private ClientAndroidInterface ca;

    public InsureesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.insurees_fragment, container, false);
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            boolean refresh = bundle.getBoolean("refresh_insurees");
            if (refresh) {
                LoadInsurees();
            }
        });
        btnAddInsuree = view.findViewById(R.id.btnNewInsuree);
        recyclerInsurees = view.findViewById(R.id.recyclerInsurees);
        ca = new ClientAndroidInterface(getActivity());
        familyId = getActivity().getIntent().getIntExtra("FamilyId", 0);
        recyclerInsurees.setLayoutManager(new LinearLayoutManager(getContext()));
        LoadInsurees();


        btnAddInsuree.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InsureeActivity.class);
            intent.putExtra("FamilyId", familyId);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadInsurees();
    }

    public void onRefresh(){
        LoadInsurees();
    }

    private void LoadInsurees() {
        try {
            String insurees = ca.getInsureesForFamily(familyId);
            JSONArray insureeArray = new JSONArray(insurees);
            InsureeAdapter adapter = new InsureeAdapter(getContext(),insureeArray, familyId);
            recyclerInsurees.setAdapter(adapter);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
