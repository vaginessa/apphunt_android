package com.apphunt.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.apphunt.app.api.apphunt.client.ApiClient;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.ui.ClearSearchEvent;
import com.apphunt.app.event_bus.events.ui.HideFragmentEvent;
import com.apphunt.app.event_bus.events.ui.NetworkStatusChangeEvent;
import com.apphunt.app.event_bus.events.ui.ShowNotificationEvent;
import com.apphunt.app.event_bus.events.ui.auth.LoginEvent;
import com.apphunt.app.event_bus.events.ui.votes.AppVoteEvent;
import com.apphunt.app.smart_rate.SmartRate;
import com.apphunt.app.smart_rate.variables.RateDialogVariable;
import com.apphunt.app.ui.fragments.AppDetailsFragment;
import com.apphunt.app.ui.fragments.BaseFragment;
import com.apphunt.app.ui.fragments.SettingsFragment;
import com.apphunt.app.ui.fragments.SuggestFragment;
import com.apphunt.app.ui.fragments.TopAppsFragment;
import com.apphunt.app.ui.fragments.TrendingAppsFragment;
import com.apphunt.app.ui.fragments.navigation.NavigationDrawerCallbacks;
import com.apphunt.app.ui.fragments.navigation.NavigationDrawerFragment;
import com.apphunt.app.utils.ConnectivityUtils;
import com.apphunt.app.utils.Constants;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.apphunt.app.utils.TrackingEvents;
import com.apphunt.app.utils.ui.ActionBarUtils;
import com.apphunt.app.utils.ui.LoadersUtils;
import com.apphunt.app.utils.ui.NavUtils;
import com.apphunt.app.utils.ui.NotificationsUtils;
import com.facebook.widget.FacebookDialog;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import it.appspice.android.AppSpice;
import it.appspice.android.api.errors.AppSpiceError;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    public static final String TAG = MainActivity.class.getSimpleName();
    private NavigationDrawerFragment navigationDrawerFragment;
    private Toolbar toolbar;
    private boolean consumedBack;
    private boolean networkEventIsPosted;
    private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initDeepLinking();
        initNotifications();

        sendBroadcast(new Intent(Constants.ACTION_ENABLE_NOTIFICATIONS));
        SmartRate.init(this, Constants.APP_SPICE_APP_ID);
    }

    private void initUI() {
        initToolbarAndNavigationDrawer();

        ActionBarUtils.getInstance().init(this);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                ActionBarUtils.getInstance().configActionBar(MainActivity.this);
            }
        });

        showStartFragments(getIntent());
    }

    private void initToolbarAndNavigationDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    if (!navigationDrawerFragment.isDrawerOpen()) {
                        navigationDrawerFragment.openDrawer();
                    } else {
                        navigationDrawerFragment.closeDrawer();
                    }

                    return;
                }

                onBackPressed();
            }
        });
        navigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer));
        onNavigationDrawerItemSelected(Constants.TRENDING_APPS);
    }

    private void initNotifications() {
        String notificationType = getIntent().getStringExtra(Constants.KEY_NOTIFICATION_TYPE);
        if (!TextUtils.isEmpty(notificationType)) {
            Map<String, String> params = new HashMap<>();
            params.put("type", notificationType);
            FlurryAgent.logEvent(TrackingEvents.UserStartedAppFromNotification, params);
        }

        NotificationsUtils.updateNotificationIdIfNeeded(this);
    }

    private void initDeepLinking() {
        if (isStartedFromDeepLink()) {
            String action = getIntent().getAction();
            Uri data = getIntent().getData();

            Log.d("DeepLink Action", action);
            Log.d("DeepLink Data", data.toString());
        }
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(Constants.TAG_NOTIFICATION_FRAGMENT);
            if (ConnectivityUtils.isNetworkAvailable(context)) {
                if (fragment != null) {
                    getSupportFragmentManager().popBackStack(Constants.TAG_NOTIFICATION_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                if (!networkEventIsPosted && !firstTime) {
                    BusProvider.getInstance().post(new NetworkStatusChangeEvent(true));
                    firstTime = false;
                }

                networkEventIsPosted = true;
            } else {
                if (fragment == null) {
                    NotificationsUtils.showNotificationFragment(((ActionBarActivity) context), getString(R.string.notification_no_internet), true, false);
                }
            }
        }
    };

    private boolean isStartedFromDeepLink() {
        Intent intent = getIntent();
        return intent != null && intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showStartFragments(intent);
    }

    private boolean isStartedFromShareIntent(Intent intent) {
        return Intent.ACTION_SEND.equals(intent.getAction());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            menu.findItem(R.id.action_search).setVisible(false);
        } else {
            menu.findItem(R.id.action_search).setVisible(true);
        }

        if (getSupportFragmentManager().findFragmentByTag(Constants.TAG_APP_DETAILS_FRAGMENT) != null) {
            menu.findItem(R.id.action_share).setVisible(true);
        } else {
            menu.findItem(R.id.action_share).setVisible(false);
        }

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Map<String, String> params = new HashMap<>();
                params.put("query", s);
                FlurryAgent.logEvent(TrackingEvents.UserSearchedForApp, params);
                ApiClient.getClient(getApplicationContext()).searchApps(s, SharedPreferencesHelper.getStringPreference(Constants.KEY_USER_ID), 1, Constants.SEARCH_RESULT_COUNT,
                        Constants.PLATFORM);

                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (item.getItemId() == R.id.action_search) {
                    BusProvider.getInstance().post(new ClearSearchEvent());
                }
                return true;
            }
        });

        searchView.setIconifiedByDefault(true);
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                        FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
                    shareWithFacebook();
                    FlurryAgent.logEvent(TrackingEvents.UserSharedAppHuntWithFacebook);
                } else {
                    shareWithLocalApps();
                    FlurryAgent.logEvent(TrackingEvents.UserSharedAppHuntWithoutFacebook);
                }
                break;

            case android.R.id.home:
                AppDetailsFragment fragment = (AppDetailsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_APP_DETAILS_FRAGMENT);
                if (fragment != null && fragment.isVisible() && fragment.isCommentsBoxOpened()) {
                    fragment.showDetails();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
                break;
        }

        return true;
    }

    private void shareWithLocalApps() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.share_text)));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private void shareWithFacebook() {
        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                .setName("AppHunt")
                .setPicture("https://launchrock-assets.s3.amazonaws.com/logo-files/LWPRHM35_1421410706452.png?_=4")
                .setLink(Constants.GOOGLE_PLAY_APP_URL).build();
        shareDialog.present();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        BaseFragment fragment = null;
        String tag = null;
        boolean addToBackStack = false;

        switch (position) {
            case Constants.TRENDING_APPS:
                fragment = new TrendingAppsFragment();
                consumedBack = true;
                break;

            case Constants.TOP_APPS:
                fragment = new TopAppsFragment();
                consumedBack = false;
                break;

            case Constants.TOP_HUNTERS:
                break;

            case Constants.SUGGESTIONS:
                fragment = new SuggestFragment();
                fragment.setPreviousTitle(toolbar.getTitle().toString());

                tag = Constants.TAG_SUGGEST_FRAGMENT;
                addToBackStack = true;

                consumedBack = navigationDrawerFragment.getSelectedItemIndex() == Constants.TRENDING_APPS;
                break;

            case Constants.SETTINGS:
                fragment = new SettingsFragment();
                fragment.setPreviousTitle(toolbar.getTitle().toString());

                tag = Constants.TAG_SETTINGS_FRAGMENT;
                addToBackStack = true;

                consumedBack = navigationDrawerFragment.getSelectedItemIndex() == Constants.TRENDING_APPS;
                break;

            case Constants.ABOUT:
                break;
        }

        if (!addToBackStack) {
            navigationDrawerFragment.markSelectedPosition(position);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0 ||
                    !getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName().equals(tag)) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.abc_fade_in, R.anim.alpha_out)
                        .add(R.id.container, fragment, tag)
                        .addToBackStack(tag)
                        .commit();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(Constants.TAG_LOGIN_FRAGMENT);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        BusProvider.getInstance().register(this);
        AppSpice.onResume(this);
    }

    private void showStartFragments(Intent intent) {
        if (isStartedFromShareIntent(intent)) {
            NavUtils.getInstance(this).startSelectAppFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        AppSpice.onPause(this);

    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen()) {
            navigationDrawerFragment.closeDrawer();
            return;
        }

        if (NavUtils.getInstance(this).isOnBackBlocked()) {
            return;
        }

        AppDetailsFragment fragment = (AppDetailsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_APP_DETAILS_FRAGMENT);
        if (fragment != null && fragment.isVisible() && fragment.isCommentsBoxOpened()) {
            fragment.showDetails();
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !consumedBack) {
            onNavigationDrawerItemSelected(Constants.TRENDING_APPS);
            consumedBack = true;
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppSpice.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
        AppSpice.onStop(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void userVotedForAppEvent(AppVoteEvent event) {
        if (event.isVote()) {
            SmartRate.show(Constants.SMART_RATE_LOCATION_APP_VOTED);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onRateDialogVariableReady(RateDialogVariable rateDialogVariable) {
        SmartRate.setRateDialogVariable(rateDialogVariable);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onAppSpiceError(AppSpiceError error) {
        SmartRate.onError();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onUserLogin(LoginEvent event) {
        supportInvalidateOptionsMenu();
        NotificationsUtils.updateNotificationIdIfNeeded(this);

        NavUtils.getInstance(this).setOnBackBlocked(false);
        LoadersUtils.hideBottomLoader(this);

        AppDetailsFragment fragment = (AppDetailsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_APP_DETAILS_FRAGMENT);
        if (fragment != null && fragment.isVisible()) {
            fragment.loadData();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onHideFragmentEvent(HideFragmentEvent event) {
        getSupportFragmentManager().popBackStack(event.getTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void showNotificationFragment(ShowNotificationEvent event) {
        NotificationsUtils.showNotificationFragment(this, event.getMessage(), false, true);
    }
}