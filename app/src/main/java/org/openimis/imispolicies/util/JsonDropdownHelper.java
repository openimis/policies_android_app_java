package org.openimis.imispolicies.util;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonDropdownHelper {

    public interface OnJsonItemSelectedListener {
        void onItemSelected(JSONObject selectedItem, int position);
    }

    public static void bindDropdown(Context context,
                                    MaterialAutoCompleteTextView dropdown,
                                    JSONArray jsonArray,
                                    String displayField,
                                    String defaultText,
                                    OnJsonItemSelectedListener listener) {

        List<String> displayList = new ArrayList<>();
        SparseArray<JSONObject> itemMap = new SparseArray<>();

        try {
            // Ajouter le texte par défaut
            int startPosition = 0;
            if (defaultText != null && !defaultText.isEmpty()) {
                displayList.add(defaultText);
                itemMap.put(0, null);
                startPosition = 1;
            }

            // Ajouter les données JSON
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String displayValue = item.getString(displayField);
                displayList.add(displayValue);
                itemMap.put(startPosition + i, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                displayList
        );
        dropdown.setAdapter(adapter);

        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    JSONObject selectedItem = itemMap.get(position);
                    listener.onItemSelected(selectedItem, position);
                }
            }
        });
    }

    /**
     * Méthode utilitaire pour pré-sélectionner une valeur après le bind
     */
    public static void selectValue(Context context,
                                   MaterialAutoCompleteTextView dropdown,
                                   JSONArray jsonArray,
                                   String displayField,
                                   String valueField,
                                   String savedValue) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String itemValue = item.getString(valueField);

                if (itemValue.equals(savedValue)) {
                    String displayValue = item.getString(displayField);
                    dropdown.setText(displayValue, false);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void selectValueFromObject(MaterialAutoCompleteTextView dropdown,
                                             JSONArray jsonArray,
                                             String displayField,
                                             String valueField,
                                             JSONObject selectedObject) {

        if (selectedObject == null) return;

        try {
            String selectedValue = selectedObject.getString(valueField);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                if (item.getString(valueField).equals(selectedValue)) {
                    dropdown.setText(item.getString(displayField), false);
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
