package com.apphunt.app.ui.fragments.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.apphunt.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nmp on 15-9-20.
 */
public class RightDrawerFragment extends Fragment {
    @InjectView(R.id.listView)
    ListView listView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_right_drawer, container, false);
        ButterKnife.inject(this, view);

        String[] items = new String[30];
        for(int i =0; i <  30; i++) {
            items[i] = "Test " + i;
        }

        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items));
        return view;
    }
}
