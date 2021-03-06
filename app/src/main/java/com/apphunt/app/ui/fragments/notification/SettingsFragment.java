package com.apphunt.app.ui.fragments.notification;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.apphunt.app.R;
import com.apphunt.app.constants.Constants;
import com.apphunt.app.constants.TrackingEvents;
import com.apphunt.app.services.InstallService;
import com.apphunt.app.ui.fragments.base.BaseFragment;
import com.apphunt.app.utils.FlurryWrapper;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.apphunt.app.utils.SoundsUtils;
import com.apphunt.app.utils.ui.ActionBarUtils;
import com.apphunt.app.utils.ui.NotificationsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingsFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getName();

    private View view;
    private AppCompatActivity activity;

    @InjectView(R.id.settings)
    RelativeLayout container;

    @InjectView(R.id.toggle_daily_notification)
    ToggleButton dailyNotifToggle;

    @InjectView(R.id.toggle_install_notifications)
    ToggleButton installNotifToggle;

    @InjectView(R.id.toggle_sounds)
    ToggleButton soundsAndVibrationToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarUtils.getInstance().hideActionBarShadow();
        FlurryWrapper.logEvent(TrackingEvents.UserViewedSettings);

        setFragmentTag(Constants.TAG_SETTINGS_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);

        initUI();

        return view;

    }

    private void initUI() {
        dailyNotifToggle.setChecked(SharedPreferencesHelper.getBooleanPreference(Constants.SETTING_NOTIFICATIONS_ENABLED, true));
        soundsAndVibrationToggle.setChecked(SharedPreferencesHelper.getBooleanPreference(Constants.IS_SOUNDS_ENABLED, true));
        installNotifToggle.setChecked(SharedPreferencesHelper.getBooleanPreference(Constants.IS_INSTALL_NOTIFICATION_ENABLED,
                true));
    }

    @OnClick(R.id.dismiss)
    public void onDissmissClick() {
        activity.getSupportFragmentManager().popBackStack();
    }

    @OnCheckedChanged(R.id.toggle_daily_notification)
    public void onDailyNotifStateChange(boolean isChecked) {
        if (isChecked) {
            FlurryWrapper.logEvent(TrackingEvents.UserEnabledDailyNotification);
            NotificationsUtils.setupDailyNotificationService(activity);
        } else {
            FlurryWrapper.logEvent(TrackingEvents.UserDisabledDailyNotification);
            NotificationsUtils.disableDailyNotificationsService(activity);
        }
    }

    @OnCheckedChanged(R.id.toggle_install_notifications)
    public void onInstallNotifStateChange(boolean isChecked) {
        if (!isChecked) {
            FlurryWrapper.logEvent(TrackingEvents.UserDisabledInstalledAppsNotification);
        } else {
            FlurryWrapper.logEvent(TrackingEvents.UserEnabledInstalledAppNotification);
        }
        SharedPreferencesHelper.setPreference(Constants.IS_INSTALL_NOTIFICATION_ENABLED, isChecked);
        InstallService.setupService(activity);
    }

    @OnCheckedChanged(R.id.toggle_sounds)
    public void onSoundsStateChange(boolean isChecked) {
        if (!isChecked) {
            FlurryWrapper.logEvent(TrackingEvents.UserDisabledSound);
        } else {
            FlurryWrapper.logEvent(TrackingEvents.UserEnabledSound);
        }
        SharedPreferencesHelper.setPreference(Constants.IS_SOUNDS_ENABLED, isChecked);
    }

    @OnClick(R.id.toggle_daily_notification)
    public void onDailyNotifToggleClick() {
        SoundsUtils.performHapticFeedback(dailyNotifToggle);
    }

    @OnClick(R.id.toggle_install_notifications)
    public void onInstallNotifToggleClick() {
        SoundsUtils.performHapticFeedback(installNotifToggle);
    }

    @OnClick(R.id.toggle_sounds)
    public void onSoundsBtnClick() {
        SoundsUtils.performHapticFeedback(soundsAndVibrationToggle);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            Animation enterAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_in);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation notificationEnterAnim = AnimationUtils.loadAnimation(activity,
                            R.anim.slide_in_top_notification);
                    notificationEnterAnim.setFillAfter(true);
                    container.startAnimation(notificationEnterAnim);
                }
            }, enterAnim.getDuration());
            return enterAnim;
        } else {
            Animation outAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_out);
            container.startAnimation(AnimationUtils.loadAnimation(activity,
                    R.anim.slide_out_top));

            return outAnim;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public int getTitle() {
        return R.string.title_settings;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ActionBarUtils.getInstance().showActionBarShadow();
    }
}
