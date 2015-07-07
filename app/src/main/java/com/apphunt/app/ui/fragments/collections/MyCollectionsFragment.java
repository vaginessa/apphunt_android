package com.apphunt.app.ui.fragments.collections;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.client.ApiClient;
import com.apphunt.app.api.apphunt.models.apps.App;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.constants.StatusCode;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.api.collections.CreateCollectionEvent;
import com.apphunt.app.event_bus.events.api.collections.GetMyCollectionsEvent;
import com.apphunt.app.event_bus.events.api.collections.UpdateCollectionEvent;
import com.apphunt.app.event_bus.events.ui.auth.LoginEvent;
import com.apphunt.app.event_bus.events.ui.auth.LogoutEvent;
import com.apphunt.app.ui.adapters.SelectCollectionAdapter;
import com.apphunt.app.ui.fragments.BaseFragment;
import com.apphunt.app.ui.interfaces.OnItemClickListener;
import com.apphunt.app.utils.ui.ActionBarUtils;
import com.apphunt.app.utils.ui.NavUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * * Created by Seishin <atanas@naughtyspirit.co>
 * * on 6/26/15.
 * *
 * * NaughtySpirit 2015
 */
public class MyCollectionsFragment extends BaseFragment implements OnItemClickListener {

    private AppCompatActivity activity;
    private View view;
    private App app;

    private String appId;
    private SelectCollectionAdapter selectCollectionAdapter;

    @InjectView(R.id.collections_list)
    RecyclerView collectionsList;

    @InjectView(R.id.vs_no_collection)
    ViewStub vsNoCollection;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_collections, container, false);

        initUI();

        return view;
    }

    private void initUI() {
        ButterKnife.inject(this, view);
        ActionBarUtils.getInstance().setTitle(R.string.title_my_collections);

        getCollections();

        collectionsList.setItemAnimator(new DefaultItemAnimator());
        collectionsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        collectionsList.setHasFixedSize(true);
    }

    private void getCollections() {
        if(LoginProviderFactory.get(getActivity()).isUserLoggedIn()) {
            ApiClient.getClient(activity).getMyCollections(LoginProviderFactory.get(activity).getUser().getId(), 1, 10);
            vsNoCollection.setVisibility(View.GONE);
        } else {
            vsNoCollection.setVisibility(View.VISIBLE);
        }
    }

    public void setSelectedApp(App selectedApp) {
        this.app = selectedApp;
    }

    @Subscribe
    public void onMyCollectionsReceive(GetMyCollectionsEvent event) {
        if (event.getAppsCollection().getTotalCount() > 0) {
            vsNoCollection.setVisibility(View.GONE);

            selectCollectionAdapter = new SelectCollectionAdapter(activity, event.getAppsCollection().getCollections());
            selectCollectionAdapter.setOnItemClickListener(this);
            collectionsList.setAdapter(selectCollectionAdapter);
        } else {
            vsNoCollection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (AppCompatActivity) activity;
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
        ActionBarUtils.getInstance().showActionBarShadow();
    }

    @Override
    public int getTitle() {
        return R.string.title_my_collections;
    }

    @Override
    public void onClick(View view, int position) {
        if (app != null) {
            ApiClient.getClient(getActivity()).updateCollection(selectCollectionAdapter.getCollectionId(position),
                    new String[] {app.getId()});
        } else {
            NavUtils.getInstance(activity).presentViewCollectionFragment(selectCollectionAdapter.getCollection(position));
        }
    }

    @Subscribe
    public void onUpdateCollection(UpdateCollectionEvent event) {
        if (event.getStatusCode() == StatusCode.SUCCESS.getCode()) {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void onCollectionCreateSuccess(CreateCollectionEvent event) {
        getCollections();
    }

    @Subscribe
    public void onLoginSuccess(LoginEvent event) {
        getCollections();
    }

    @Subscribe
    public void onLogoutSuccess(LogoutEvent event) {
        collectionsList.removeAllViews();
        collectionsList.setAdapter(null);
        vsNoCollection.setVisibility(View.VISIBLE);
    }
}
