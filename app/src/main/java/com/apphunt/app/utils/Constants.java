package com.apphunt.app.utils;


public class Constants {
    public static final String PACKAGE_NAME = "com.apphunt.app";

    public static final String KEY_USER_ID = "user_id";
//    public static final String KEY_NAME = "name";
//    public static final String KEY_PROFILE_PICTURE = "profile_picture";
    public static final String KEY_EMAIL = "profile_email";
    public static final String KEY_DATA = "data";
    public static final String KEY_NOTIFICATION = "notification";
    public static final String KEY_SHOW_SETTINGS = "show_settings";
    public static final String KEY_SHOW_RATING = "show_rating";
    public static final String KEY_INVITE_SHARE = "invite_count_left";
    public static final String KEY_LOGIN_PROVIDER = "login_provider";
    public static final String KEY_DAILY_REMINDER_NOTIFICATION = "daily_reminder_notification";
    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_APP_NAME = "app_name";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final String KEY_PROFILE_IMAGE = "profile_image";

    public static final String PLATFORM = "Android";

    // Settings
    public static final String IS_DAILY_NOTIFICATION_ENABLED = "isDisplayNotificationServiceEnabled";
    public static final String IS_SOUNDS_ENABLED = "isSoundsEnabled";
    public static final String WAS_SPLASH_SHOWN = "wasSplashShown";

    // Fragment TAGs
    public static final String TAG_LOGIN_FRAGMENT = "login_fragment";
    public static final String TAG_SELECT_APP_FRAGMENT = "add_app_fragment";
    public static final String TAG_SAVE_APP_FRAGMENT = "add_save_fragment";
    public static final String TAG_SETTINGS_FRAGMENT = "settings_fragment";
    public static final String TAG_NOTIFICATION_FRAGMENT = "notification_fragment";
    public static final String TAG_INVITE_FRAGMENT = "invite_fragment";
    public static final String TAG_SUGGEST_FRAGMENT = "suggest_fragment";
    public static final String TAG_APP_DETAILS_FRAGMENT = "app_details_fragment";
    
    // RequestCodes
    public static final int REQUEST_NETWORK_SETTINGS = 3;

    // Actions
    public static final String ACTION_ENABLE_NOTIFICATIONS = "com.apphunt.app.action.ENABLE_NOTIFICATIONS";

    // Smart Rate
    public static final String SMART_RATE_LOCATION_APP_SAVED = "SaveApp#appSaved";
    public static final String SMART_RATE_LOCATION_APP_VOTED = "TrendingApps#appVoted";
    public static final String APP_SPICE_APP_ID = "54d4a61f6275f00300b032d9";

    // Sharing
    public static final String GOOGLE_PLAY_APP_URL = "https://play.google.com/store/apps/details?id=com.apphunt.app";
    public static final String BIT_LY_GOOGLE_PLAY_URL = "http://bit.ly/1umy2AV";
    public static final String LAUNCHROCK_ICON = "https://launchrock-assets.s3.amazonaws.com/logo-files/LWPRHM35_1421410706452.png?_=4";

    // Invites
    public static final int INVITE_SHARES_COUNT = 3;
    public static final int REQUEST_ACCOUNT_EMAIL = 5;
    public static final int USER_SKIP_INVITE_PERCENTAGE = 60;


    public enum ItemType {
        SEPARATOR(0), ITEM(1), MORE_APPS(2), 
        COMMENT(3), SUBCOMMENT(4);

        private int value;

        ItemType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
