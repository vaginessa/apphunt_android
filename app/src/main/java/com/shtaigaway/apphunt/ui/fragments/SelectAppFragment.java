package com.shtaigaway.apphunt.ui.fragments;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;

import com.shtaigaway.apphunt.R;
import com.shtaigaway.apphunt.api.AppHuntApiClient;
import com.shtaigaway.apphunt.api.Callback;
import com.shtaigaway.apphunt.api.models.Packages;
import com.shtaigaway.apphunt.smart_rate.SmartRate;
import com.shtaigaway.apphunt.ui.adapters.UserAppsAdapter;
import com.shtaigaway.apphunt.ui.interfaces.OnAppSelectedListener;
import com.shtaigaway.apphunt.utils.InstalledPackagesUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class SelectAppFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = SelectAppFragment.class.getName();

    private View view;
    private GridView gridView;
    private UserAppsAdapter userAppsAdapter;

    private OnAppSelectedListener callback;

    private List<ApplicationInfo> data;
    private ActionBarActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_select_app);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_app, container, false);

        initUI();

        return view;
    }

    private void initUI() {
        gridView = (GridView) view.findViewById(R.id.gv_apps_list);
        gridView.setOnItemClickListener(this);

        Packages packages = new Packages();
        data = InstalledPackagesUtils.installedPackages(activity.getPackageManager());
        for (ApplicationInfo info : data) {
            packages.getPackages().add(info.packageName);
        }

        AppHuntApiClient.getClient().filterApps(packages, new Callback<Packages>() {
            @Override
            public void success(Packages packages, Response response) {

                if (response.getStatus() == 200 && packages != null) {
                    List<ApplicationInfo> tempData = new ArrayList<>();

                    for (ApplicationInfo info : data) {
                        for (String packageName : packages.getAvailablePackages()) {
                            if (info.packageName.equals(packageName)) {
                                tempData.add(info);
                            }
                        }
                    }

                    userAppsAdapter = new UserAppsAdapter(activity, tempData);
                    gridView.setAdapter(userAppsAdapter);
                }
            }
        });
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_out_top);
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (ActionBarActivity) activity;

        try {
            callback = (OnAppSelectedListener) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        callback.onAppSelected(userAppsAdapter.getItem(position));
    }
}
