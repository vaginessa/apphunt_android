package com.apphunt.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.clients.rest.ApiClient;
import com.apphunt.app.api.apphunt.models.apps.SaveApp;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.constants.Constants;
import com.apphunt.app.constants.TrackingEvents;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.api.tags.TagsSuggestionApiEvent;
import com.apphunt.app.event_bus.events.ui.AppSubmittedEvent;
import com.apphunt.app.event_bus.events.ui.HideFragmentEvent;
import com.apphunt.app.event_bus.events.ui.LoginSkippedEvent;
import com.apphunt.app.event_bus.events.ui.ShowNotificationEvent;
import com.apphunt.app.event_bus.events.ui.auth.LoginEvent;
import com.apphunt.app.ui.fragments.base.BaseFragment;
import com.apphunt.app.ui.fragments.help.AppsRequirementsFragment;
import com.apphunt.app.ui.views.widgets.TagGroup;
import com.apphunt.app.utils.FlurryWrapper;
import com.apphunt.app.utils.LoginUtils;
import com.apphunt.app.utils.ui.ActionBarUtils;
import com.apptentive.android.sdk.Apptentive;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SaveAppFragment extends BaseFragment implements OnClickListener {

    private static final String TAG = SaveAppFragment.class.getName();

    private View view;

    private ApplicationInfo data;
    private AppCompatActivity activity;
    private AutoCompleteTextView tagView;

    @InjectView(R.id.container)
    RelativeLayout container;

    @InjectView(R.id.title)
    TextView title;

    @InjectView(R.id.app_icon)
    ImageView icon;

    @InjectView(R.id.save)
    Button saveButton;

    @InjectView(R.id.description)
    EditText desc;

    @InjectView(R.id.tag_group)
    TagGroup tagGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        data = getArguments().getParcelable(Constants.KEY_DATA);
        Map<String, String> params = new HashMap<>();
        params.put("appPackage", data.packageName);
        FlurryWrapper.logEvent(TrackingEvents.UserViewedSaveApp, params);
        setHasOptionsMenu(true);

        setFragmentTag(Constants.TAG_SAVE_APP_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_save_app, container, false);
        ButterKnife.inject(this, view);
        initUI();

        return view;
    }

    private void initUI() {
        container.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.vertical_flip));
        container.setOnClickListener(this);

        title.setText(data.loadLabel(activity.getPackageManager()));
        icon.setImageDrawable(data.loadIcon(activity.getPackageManager()));

        tagGroup.setOnTagTextEntryListener(new TagGroup.OnTagTextEntryListener() {
            @Override
            public void onTextEntry(AutoCompleteTextView view, String text) {
                tagView = view;
                if (!TextUtils.isEmpty(text)) ApiClient.getClient(activity).getTagsSuggestion(text);
            }
        });
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_out_right);
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_search2);
        if(item != null)  {
            item.setVisible(false);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.container:
                closeKeyboard(desc);
                break;
        }
    }

    @OnClick(R.id.rules)
    public void openRules() {
        AppsRequirementsFragment fragment = new AppsRequirementsFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment, AppsRequirementsFragment.TAG)
                .addToBackStack(AppsRequirementsFragment.TAG)
                .commit();
    }

    @OnClick(R.id.save)
    public void saveApp() {
        if(!LoginProviderFactory.get(getActivity()).isUserLoggedIn()) {
            showLoginFragment();
        } else {
            saveApp(saveButton, LoginProviderFactory.get(activity).getUser().getId());
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    private void showLoginFragment() {
        Random random = new Random();
        int currPercent = random.nextInt(100) + 1;
        if(currPercent <= Constants.USER_SKIP_LOGIN_PERCENTAGE) {
            FlurryWrapper.logEvent(TrackingEvents.AppShowedSkippableLogin);
            LoginUtils.showLoginFragment(true, R.string.login_info_save);
        } else {
            FlurryWrapper.logEvent(TrackingEvents.AppShowedRegularLogin);
            LoginUtils.showLoginFragment(false, R.string.login_info_save);
        }
    }

    private void saveApp(final View v, String userId) {
        if (desc.getText() != null && desc.getText().length() > 0 && desc.getText().length() <= 50) {
            desc.setHint(R.string.hint_short_description);
            desc.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
            desc.setError("Min 50 chars");
            vibrate();
        } else if (desc.getText() == null || desc.getText() != null && desc.getText().length() == 0) {
            desc.setHint(R.string.hint_please_enter_description);
            desc.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
            vibrate();
        } else {
            v.setEnabled(false);
            SaveApp app = new SaveApp();
            app.setDescription(desc.getText().toString());
            app.setPackageName(data.packageName);
            app.setPlatform(Constants.PLATFORM);
            app.setUserId(userId);

            if (tagGroup.getTags().length > 0) {
                app.setTags(tagGroup.getTags());
            }

            ApiClient.getClient(getActivity()).saveApp(app);

            FlurryWrapper.logEvent(TrackingEvents.UserAddedApp);
            BusProvider.getInstance().post(new HideFragmentEvent(Constants.TAG_SAVE_APP_FRAGMENT));
            BusProvider.getInstance().post(new ShowNotificationEvent(getString(R.string.saved_successfully), false, false));
            BusProvider.getInstance().post(new AppSubmittedEvent(data.packageName));
            Apptentive.engage(activity, "user.added.app");
        }
    }

    private void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if(imm != null && v != null && v.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (AppCompatActivity) activity;
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ActionBarUtils.getInstance().hideActionBarShadow();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        closeKeyboard(desc);
        BusProvider.getInstance().unregister(this);
        ActionBarUtils.getInstance().hideActionBarShadow();
    }

    @Override
    public int getTitle() {
        return R.string.title_save_app;
    }

    @Subscribe
    public void onLoginSkipped(LoginSkippedEvent event) {
        FlurryWrapper.logEvent(TrackingEvents.UserSkippedLoginWhenAddApp);
        saveApp(saveButton, Constants.APPHUNT_ADMIN_USER_ID);
    }

    @Subscribe
    public void onUserCreated(LoginEvent event) {
        saveApp(saveButton, event.getUser().getId());
    }

    @Subscribe
    public void onTagsSuggestionReceive(TagsSuggestionApiEvent event) {
        tagView.setAdapter(new ArrayAdapter<>(activity, R.layout.layout_tags_suggestion, event.getTags().getTags()));
    }
}
