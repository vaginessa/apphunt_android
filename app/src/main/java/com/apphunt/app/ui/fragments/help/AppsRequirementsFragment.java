package com.apphunt.app.ui.fragments.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apphunt.app.R;
import com.apphunt.app.constants.TrackingEvents;
import com.apphunt.app.ui.fragments.base.BaseFragment;
import com.apphunt.app.utils.FlurryWrapper;

import butterknife.ButterKnife;

/**
 * Created by nmp on 15-6-9.
 */
public class AppsRequirementsFragment extends BaseFragment {
    public static final String TAG = AppsRequirementsFragment.class.getSimpleName();
    public AppsRequirementsFragment() {
        setTitle(R.string.apps_requirements);
        FlurryWrapper.logEvent(TrackingEvents.UserViewedHelpAppsRequirements);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getTitle() {
        return R.string.apps_requirements;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_apps_requirements, container, false);
        ButterKnife.inject(this, view);

        return view;
    }
}
