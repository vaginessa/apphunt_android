package com.apphunt.app;

import android.app.Application;
import android.text.TextUtils;

import com.apphunt.app.utils.Constants;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Naughty Spirit <hi@naughtyspirit.co>
 * on 3/6/15.
 */
public class AppHuntApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String userId = SharedPreferencesHelper.getStringPreference(this, Constants.KEY_USER_ID);
        if (!TextUtils.isEmpty(userId)) {
            FlurryAgent.setUserId(userId);
        }

        if (BuildConfig.DEBUG) {
            FlurryAgent.init(this, Constants.FLURRY_DEBUG_API_KEY);
        } else {
            FlurryAgent.init(this, Constants.FLURRY_API_KEY);
        }

        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY,
                        Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
    }
}
