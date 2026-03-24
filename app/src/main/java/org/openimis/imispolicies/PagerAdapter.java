package org.openimis.imispolicies;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;
    private String[] titles = new String[]{"Insurees", "Policies"};

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new InsureesFragment();
            case 1:
                return new PoliciesFragment();
            default:
                return new InsureesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public String getTitle(int position) {
        return titles[position];
    }
}
