package com.apphunt.app.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.client.ApiClient;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.api.apps.AppsSearchResultEvent;
import com.apphunt.app.event_bus.events.api.collections.CollectionsSearchResultEvent;
import com.apphunt.app.ui.adapters.SearchResultsAdapter;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * * Created by Seishin <atanas@naughtyspirit.co>
 * * on 8/8/15.
 * *
 * * NaughtySpirit 2015
 */
public class SearchFragment extends BaseFragment {
    public static final String TAG = SearchFragment.class.getSimpleName();

    private Activity activity;
    private View view;
    private String query;
    private LinearLayoutManager layoutManager;
    private SearchResultsAdapter resultsListAdapter;

    @InjectView(R.id.results_list)
    RecyclerView resultsList;

    @InjectView(R.id.loading)
    CircularProgressBar loader;

    @InjectView(R.id.no_results)
    RelativeLayout noResultsLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);

        initUI();

        return view;
    }

    private void initUI() {
        resultsList.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(getActivity());
        resultsList.setLayoutManager(layoutManager);
        resultsList.setHasFixedSize(true);

        resultsListAdapter = new SearchResultsAdapter(activity, resultsList, noResultsLayout);
        resultsList.setAdapter(resultsListAdapter);

        ApiClient.getClient(activity).getAppsByTags(query, 1, 10, LoginProviderFactory.get(activity).getUser().getId());
        ApiClient.getClient(activity).getCollectionsByTags(query, 1, 10, LoginProviderFactory.get(activity).getUser().getId());
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            loader.setVisibility(View.VISIBLE);
            return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top_notification);
        } else {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
        }
    }

    @Override
    public int getTitle() {
        return R.string.title_search;
    }

    public void setQuery(String q) {
        String[] tags = q.split(" ");
        String query = "?";

        for (String tag : tags) {
            query += "names[]=" + tag + "&";
        }

        this.query = query;
    }

    @Subscribe
    public void onAppsSearchResultsEvent(AppsSearchResultEvent event) {
        loader.progressiveStop();

        if (event.getResult().getTotalCount() > 0) {
            resultsListAdapter.notifyDataSetChanged();
        } else {
            noResultsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onCollectionsSearchResultsEvent(CollectionsSearchResultEvent event) {
        loader.progressiveStop();

        if (event.getResult().getTotalCount() > 0) {
            resultsListAdapter.notifyDataSetChanged();
        } else {
            noResultsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }
}
