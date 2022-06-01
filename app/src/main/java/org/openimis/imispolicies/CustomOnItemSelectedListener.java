package org.openimis.imispolicies;

/**
 * Created by Hiren on 15/03/2019.
 */

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class CustomOnItemSelectedListener implements OnItemSelectedListener {
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        OverViewPolicies.PayType = parent.getItemAtPosition(pos).toString();
        SearchOverViewControlNumber.PayType = parent.getItemAtPosition(pos).toString();
/*        Toast.makeText(parent.getContext(),
                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
