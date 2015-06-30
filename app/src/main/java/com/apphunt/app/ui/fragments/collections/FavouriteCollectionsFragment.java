package com.apphunt.app.ui.fragments.collections;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.client.ApiClient;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.api.collections.FavouriteCollectionEvent;
import com.apphunt.app.event_bus.events.api.collections.GetFavouriteCollectionsEvent;
import com.apphunt.app.event_bus.events.api.collections.UnfavouriteCollectionEvent;
import com.apphunt.app.ui.adapters.CollectionsAdapter;
import com.apphunt.app.ui.fragments.BaseFragment;
import com.apphunt.app.ui.listeners.EndlessScrollListener;
import com.apphunt.app.utils.Constants;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.apphunt.app.utils.ui.LoadersUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nmp on 15-6-26.
 */
public class FavouriteCollectionsFragment extends BaseFragment {
    public static final String TAG = FavouriteCollectionsFragment.class.getSimpleName();
    private CollectionsAdapter adapter;
    @InjectView(R.id.all_collections)
    ListView allCollections;
    private int currentPage = 0;
    private int totalCount = 0;

    @Override
    public int getTitle() {
        return R.string.title_favourite_collection;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getFavouriteCollections();

        View view  = inflater.inflate(R.layout.fragment_all_collections, container, false);
        ButterKnife.inject(this, view);

        allCollections.setOnScrollListener(new EndlessScrollListener(new EndlessScrollListener.OnEndReachedListener() {
            @Override
            public void onEndReached() {
                if(adapter.getCount() >= totalCount) {
                    return;
                }
                LoadersUtils.showBottomLoader(getActivity(),
                        SharedPreferencesHelper.getBooleanPreference(Constants.IS_SOUNDS_ENABLED));
                getFavouriteCollections();
            }
        }));
        return view;
    }

    private void getFavouriteCollections() {
        if(LoginProviderFactory.get(getActivity()).isUserLoggedIn()) {
            currentPage++;
            ApiClient.getClient(getActivity()).getFavouriteCollections(
                    LoginProviderFactory.get(getActivity()).getUser().getId(), currentPage, Constants.PAGE_SIZE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onCollectionFavourited(FavouriteCollectionEvent event) {
        Log.d(TAG + "onFavourited", event.getCollection().getId());
        adapter.addCollection(event.getCollection());
    }

    @Subscribe
    public void onCollectionUnfavourited(UnfavouriteCollectionEvent event) {
        adapter.removeCollection(event.getCollectionId());
    }

    @Subscribe
    public void onFavouriteCollectionReceived(GetFavouriteCollectionsEvent event) {
        LoadersUtils.hideBottomLoader(getActivity());
        if(adapter == null) {
            adapter = new CollectionsAdapter(getActivity(), event.getAppsCollection().getCollections());
            allCollections.setAdapter(adapter);
            totalCount = event.getAppsCollection().getTotalCount();
        } else {
            int currentSize = adapter.getCount();
            adapter.addAllCollections(event.getAppsCollection().getCollections());
            allCollections.smoothScrollToPosition(currentSize);
        }
    }
}
